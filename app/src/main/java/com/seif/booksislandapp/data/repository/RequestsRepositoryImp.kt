package com.seif.booksislandapp.data.repository

import android.net.ConnectivityManager
import com.google.firebase.firestore.FirebaseFirestore
import com.seif.booksislandapp.R
import com.seif.booksislandapp.data.mapper.toMyRequest
import com.seif.booksislandapp.data.mapper.toRequestDto
import com.seif.booksislandapp.data.remote.dto.UserDto
import com.seif.booksislandapp.data.remote.dto.request.RequestDto
import com.seif.booksislandapp.domain.model.request.MyRequest
import com.seif.booksislandapp.domain.repository.RequestsRepository
import com.seif.booksislandapp.utils.Constants
import com.seif.booksislandapp.utils.Constants.Companion.AUCTION_ADVERTISEMENT_FIRESTORE_COLLECTION
import com.seif.booksislandapp.utils.Constants.Companion.DONATE_ADVERTISEMENT_FIRESTORE_COLLECTION
import com.seif.booksislandapp.utils.Constants.Companion.EXCHANGE_ADVERTISEMENT_FIRESTORE_COLLECTION
import com.seif.booksislandapp.utils.Constants.Companion.REQUESTS_FIIRESTORE_COLLECTION
import com.seif.booksislandapp.utils.Constants.Companion.SELL_ADVERTISEMENT_FIRESTORE_COLLECTION
import com.seif.booksislandapp.utils.Constants.Companion.USER_FIRESTORE_COLLECTION
import com.seif.booksislandapp.utils.Resource
import com.seif.booksislandapp.utils.ResourceProvider
import com.seif.booksislandapp.utils.checkInternetConnection
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.tasks.await
import kotlinx.coroutines.withTimeout
import timber.log.Timber
import javax.inject.Inject

class RequestsRepositoryImp @Inject constructor(
    private val firestore: FirebaseFirestore,
    private val resourceProvider: ResourceProvider,
    private val connectivityManager: ConnectivityManager
) : RequestsRepository {

    override suspend fun sendRequest(myRequest: MyRequest): Resource<String, String> {
        if (!connectivityManager.checkInternetConnection()) // remove this check if we want get cached data
            return Resource.Error(resourceProvider.string(R.string.no_internet_connection))

        return withTimeout(Constants.TIMEOUT_UPLOAD) {
            try {
                val doc = firestore.collection(REQUESTS_FIIRESTORE_COLLECTION).document()
                myRequest.id = doc.id
                Timber.d("sendRequest: before mapper")
                doc.set(myRequest.toRequestDto()).await()
                Timber.d("sendRequest: after mapper")
                val collectionName: String = when (myRequest.adType) {
                    "Buying" -> SELL_ADVERTISEMENT_FIRESTORE_COLLECTION
                    "Donation" -> DONATE_ADVERTISEMENT_FIRESTORE_COLLECTION
                    "Exchange" -> EXCHANGE_ADVERTISEMENT_FIRESTORE_COLLECTION
                    "Auction" -> AUCTION_ADVERTISEMENT_FIRESTORE_COLLECTION
                    else -> ""
                }
                firestore.collection(collectionName).document(myRequest.advertisementId)
                    .update("confirmationMessageSent", true).await()

                Resource.Success("Confirmation Request Sent Successfully")
            } catch (e: Exception) {
                Resource.Error(e.message.toString())
            }
        }
    }

    override suspend fun fetchSentRequests(currentUserId: String) = callbackFlow {
        if (!connectivityManager.checkInternetConnection()) // remove this check if we want get cached data
            trySend(Resource.Error(resourceProvider.string(R.string.no_internet_connection)))
        try {
            withTimeout(Constants.TIMEOUT) {
                firestore.collection(REQUESTS_FIIRESTORE_COLLECTION)
                    .addSnapshotListener { requestsQuerySnapShot, error ->
                        val requestsDto = arrayListOf<RequestDto>()
                        if (requestsQuerySnapShot != null) {
                            for (snapShot in requestsQuerySnapShot) {
                                val request = snapShot.toObject(RequestDto::class.java)
                                if (request.senderId == currentUserId) // requests sent by current user
                                    requestsDto.add(request)
                            }
                            // fetch sender user
                            firestore.collection(USER_FIRESTORE_COLLECTION)
                                .get()
                                .addOnSuccessListener {
                                    if (it != null) {
                                        val requests = arrayListOf<MyRequest>()
                                        for (snapShot in it) {
                                            val userDto = snapShot.toObject(UserDto::class.java)
                                            for (i in 0 until requestsDto.size) {
                                                if (userDto.id == requestsDto[i].receiverId) // get users that i sent requests to them
                                                    requests.add(requestsDto[i].toMyRequest(userDto))
                                            }
                                        }
                                        trySend(Resource.Success(requests.sortedByDescending { it.date }))
                                    }
                                }
                                .addOnFailureListener {
                                    trySend(Resource.Error(it.message.toString()))
                                }
                        }
                    }
            }
        } catch (e: Exception) {
            Resource.Error(message = e.message.toString())
        }
        awaitClose { }
    }

    override suspend fun fetchReceivedRequests(currentUserId: String) = callbackFlow {
        if (!connectivityManager.checkInternetConnection()) // remove this check if we want get cached data
            trySend(Resource.Error(resourceProvider.string(R.string.no_internet_connection)))
        try {
            withTimeout(Constants.TIMEOUT) {
                firestore.collection(REQUESTS_FIIRESTORE_COLLECTION)
                    .document(currentUserId)
                    .collection(currentUserId)
                    .addSnapshotListener { requestsQuerySnapShot, error ->
                        val requestsDto = arrayListOf<RequestDto>()
                        if (requestsQuerySnapShot != null) {
                            for (snapShot in requestsQuerySnapShot) {
                                val request = snapShot.toObject(RequestDto::class.java)
                                if (request.receiverId == currentUserId)
                                    requestsDto.add(request)
                            }
                            // fetch sender user
                            firestore.collection(USER_FIRESTORE_COLLECTION)
                                .get()
                                .addOnSuccessListener {
                                    if (it != null) {
                                        val requests = arrayListOf<MyRequest>()
                                        for (snapShot in it) {
                                            val userDto = snapShot.toObject(UserDto::class.java)
                                            for (i in 0 until requestsDto.size) {
                                                if (userDto.id == requestsDto[i].senderId) // get users that sent to me the request
                                                    requests.add(requestsDto[i].toMyRequest(userDto))
                                            }
                                        }
                                        trySend(Resource.Success(requests.sortedByDescending { it.date }))
                                    }
                                }
                                .addOnFailureListener {
                                    trySend(Resource.Error(it.message.toString()))
                                }
                        }
                    }
            }
        } catch (e: Exception) {
            Resource.Error(message = e.message.toString())
        }
        awaitClose { }
    }
}