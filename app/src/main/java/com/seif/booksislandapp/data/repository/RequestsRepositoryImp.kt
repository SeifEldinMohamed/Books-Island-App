package com.seif.booksislandapp.data.repository

import android.net.ConnectivityManager
import com.google.firebase.firestore.FieldValue
import com.google.firebase.firestore.FirebaseFirestore
import com.seif.booksislandapp.R
import com.seif.booksislandapp.data.mapper.toMyReceivedRequest
import com.seif.booksislandapp.data.mapper.toMyRequest
import com.seif.booksislandapp.data.mapper.toRequestDto
import com.seif.booksislandapp.data.remote.dto.UserDto
import com.seif.booksislandapp.data.remote.dto.request.RequestDto
import com.seif.booksislandapp.domain.model.adv.AdType
import com.seif.booksislandapp.domain.model.request.MyReceivedRequest
import com.seif.booksislandapp.domain.model.request.MySentRequest
import com.seif.booksislandapp.domain.repository.RequestsRepository
import com.seif.booksislandapp.utils.Constants
import com.seif.booksislandapp.utils.Constants.Companion.AUCTION_ADVERTISEMENT_FIRESTORE_COLLECTION
import com.seif.booksislandapp.utils.Constants.Companion.DONATE_ADVERTISEMENT_FIRESTORE_COLLECTION
import com.seif.booksislandapp.utils.Constants.Companion.EXCHANGE_ADVERTISEMENT_FIRESTORE_COLLECTION
import com.seif.booksislandapp.utils.Constants.Companion.NUMBER_OF_COMPLETED_AUCTION_ADS_FIELD
import com.seif.booksislandapp.utils.Constants.Companion.NUMBER_OF_COMPLETED_DONATE_ADS_FIELD
import com.seif.booksislandapp.utils.Constants.Companion.NUMBER_OF_COMPLETED_EXCHANGE_ADS_FIELD
import com.seif.booksislandapp.utils.Constants.Companion.NUMBER_OF_COMPLETED_SELL_ADS_FIELD
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

    /** Sent **/
    override suspend fun sendRequest(mySentRequest: MySentRequest): Resource<String, String> {
        if (!connectivityManager.checkInternetConnection()) // remove this check if we want get cached data
            return Resource.Error(resourceProvider.string(R.string.no_internet_connection))
        else {
            return try {
                withTimeout(Constants.TIMEOUT_UPLOAD) {
                    val doc = firestore.collection(REQUESTS_FIIRESTORE_COLLECTION).document()
                    mySentRequest.id = doc.id
                    Timber.d("sendRequest: before mapper")
                    doc.set(mySentRequest.toRequestDto()).await()
                    Timber.d("sendRequest: after mapper")

                    val collectionName: String = getCollectionNameBaseOnAdType(mySentRequest.adType)

                    firestore.collection(collectionName).document(mySentRequest.advertisementId)
                        .update("confirmationMessageSent", true)
                        .await()

                    firestore.collection(collectionName).document(mySentRequest.advertisementId)
                        .update("confirmationRequestId", mySentRequest.id)
                        .await()

                    Resource.Success(mySentRequest.id)
                }
            } catch (e: Exception) {
                Resource.Error(e.message.toString())
            }
        }
    }

    override suspend fun fetchSentRequests(currentUserId: String) = callbackFlow {
        if (!connectivityManager.checkInternetConnection()) // remove this check if we want get cached data
            trySend(Resource.Error(resourceProvider.string(R.string.no_internet_connection)))
        else {
            try {
                withTimeout(Constants.TIMEOUT) {
                    firestore.collection(REQUESTS_FIIRESTORE_COLLECTION)
                        .addSnapshotListener { requestsQuerySnapShot, error ->
                            if (error != null) {
                                trySend(Resource.Error(error.message.toString()))
                            }
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
                                    .addOnSuccessListener { usersQuerySnapShot ->
                                        if (usersQuerySnapShot != null) {
                                            val requests = arrayListOf<MySentRequest>()
                                            for (snapShot in usersQuerySnapShot) {
                                                val userDto = snapShot.toObject(UserDto::class.java)
                                                for (i in 0 until requestsDto.size) {
                                                    if (userDto.id == requestsDto[i].receiverId) // get users that I sent requests to them
                                                        requests.add(
                                                            requestsDto[i].toMyRequest(
                                                                userDto
                                                            )
                                                        )
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
        }
        awaitClose { }
    }

    override suspend fun cancelSentRequest(
        requestId: String,
        adType: AdType,
        advertisementId: String
    ): Resource<String, String> {
        if (!connectivityManager.checkInternetConnection()) // remove this check if we want get cached data
            return Resource.Error(resourceProvider.string(R.string.no_internet_connection))

        firestore.collection(REQUESTS_FIIRESTORE_COLLECTION)
            .document(requestId)
            .delete()
            .await()

        updateIsConfirmationRequestSent(adType, advertisementId, false)

        return Resource.Success("Request Cancelled")
    }

    override suspend fun deleteRequest(requestId: String): Resource<String, String> {
        if (!connectivityManager.checkInternetConnection()) // remove this check if we want get cached data
            return Resource.Error(resourceProvider.string(R.string.no_internet_connection))

        firestore.collection(REQUESTS_FIIRESTORE_COLLECTION)
            .document(requestId)
            .delete()
            .await()
        return Resource.Success("Deleted Successfully")
    }

    /** Received **/

    override suspend fun fetchReceivedRequests(currentUserId: String) = callbackFlow {
        if (!connectivityManager.checkInternetConnection()) // remove this check if we want get cached data
            trySend(Resource.Error(resourceProvider.string(R.string.no_internet_connection)))
        else {
            try {
                withTimeout(Constants.TIMEOUT) {
                    firestore.collection(REQUESTS_FIIRESTORE_COLLECTION)
                        .addSnapshotListener { requestsQuerySnapShot, error ->
                            if (error != null) {
                                trySend(Resource.Error(error.message.toString()))
                            }
                            val requestsDto = arrayListOf<RequestDto>()
                            if (requestsQuerySnapShot != null) {
                                for (snapShot in requestsQuerySnapShot) {
                                    val request = snapShot.toObject(RequestDto::class.java)
                                    if (request.receiverId == currentUserId && request.status == "Pending")
                                        requestsDto.add(request)
                                }
                                // fetch sender user
                                firestore.collection(USER_FIRESTORE_COLLECTION)
                                    .get()
                                    .addOnSuccessListener { usersQuerySnapShot ->
                                        if (usersQuerySnapShot != null) {
                                            val requests = arrayListOf<MyReceivedRequest>()
                                            for (snapShot in usersQuerySnapShot) {
                                                val userDto = snapShot.toObject(UserDto::class.java)
                                                for (i in 0 until requestsDto.size) {
                                                    if (userDto.id == requestsDto[i].senderId) // get users that sent to me the request
                                                        requests.add(
                                                            requestsDto[i].toMyReceivedRequest(
                                                                userDto
                                                            )
                                                        )
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
        }
        awaitClose { }
    }

    override suspend fun acceptConfirmationRequest(
        requestId: String,
        sellerId: String,
        adType: AdType,
        acceptStatus: String,
        advertisementId: String
    ): Resource<String, String> {
        if (!connectivityManager.checkInternetConnection()) // remove this check if we want get cached data
            return Resource.Error(resourceProvider.string(R.string.no_internet_connection))

        firestore.collection(REQUESTS_FIIRESTORE_COLLECTION)
            .document(requestId)
            .update("status", acceptStatus)
            .await()

        firestore.collection(getCollectionNameBaseOnAdType(adType)).document(advertisementId)
            .update("confirmationRequestId", "", "status", "Closed")
            .await()

        // increase totalNumberOfCompletedDealIn(AdType) in the seller profile
        firestore.collection(USER_FIRESTORE_COLLECTION)
            .document(sellerId)
            .update(getCounterFieldNameBaseOnAdType(adType), FieldValue.increment(1))
            .await()
        return Resource.Success("Confirmation Accepted")
    }

    override suspend fun rejectConfirmationRequest(
        requestId: String,
        advertisementId: String,
        adType: AdType,
        rejectStatus: String
    ): Resource<String, String> {
        if (!connectivityManager.checkInternetConnection()) // remove this check if we want get cached data
            return Resource.Error(resourceProvider.string(R.string.no_internet_connection))

        firestore.collection(REQUESTS_FIIRESTORE_COLLECTION)
            .document(requestId)
            .update("status", rejectStatus)
            .await()

        // update confirmationMessageSent field of that ad to false so seller have opportunity to send another request
        updateIsConfirmationRequestSent(adType, advertisementId, false)

        //  sendConfirmationResultNotification(username,true, sellerId)

        return Resource.Success("Confirmation Rejected")
    }

    private suspend fun updateIsConfirmationRequestSent(
        adType: AdType,
        advertisementId: String,
        isConfirmationSent: Boolean
    ) {
        Timber.d("updateIsConfirmationRequestSent: in function")

        val advertisementDocumentReference =
            firestore.collection(getCollectionNameBaseOnAdType(adType))
                .document(advertisementId)
        val doc = advertisementDocumentReference.get().await()
        if (doc.exists()) { // check if ad is still exists because the seller may delete ad after sending the confirmation request
            Timber.d("updateIsConfirmationRequestSent: update confirmationMessageSent to $isConfirmationSent")
            advertisementDocumentReference.update("confirmationMessageSent", isConfirmationSent)
                .await()
        }
    }

    private fun getCollectionNameBaseOnAdType(adType: AdType): String {
        return when (adType) {
            AdType.Buying -> SELL_ADVERTISEMENT_FIRESTORE_COLLECTION
            AdType.Donation -> DONATE_ADVERTISEMENT_FIRESTORE_COLLECTION
            AdType.Exchange -> EXCHANGE_ADVERTISEMENT_FIRESTORE_COLLECTION
            AdType.Auction -> AUCTION_ADVERTISEMENT_FIRESTORE_COLLECTION
        }
    }

    private fun getCounterFieldNameBaseOnAdType(adType: AdType): String {
        return when (adType) {
            AdType.Buying -> NUMBER_OF_COMPLETED_SELL_ADS_FIELD
            AdType.Donation -> NUMBER_OF_COMPLETED_DONATE_ADS_FIELD
            AdType.Exchange -> NUMBER_OF_COMPLETED_EXCHANGE_ADS_FIELD
            AdType.Auction -> NUMBER_OF_COMPLETED_AUCTION_ADS_FIELD
        }
    }
}