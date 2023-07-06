package com.seif.booksislandapp.data.repository

import android.net.ConnectivityManager
import com.google.firebase.firestore.FieldValue
import com.google.firebase.firestore.FirebaseFirestore
import com.seif.booksislandapp.R
import com.seif.booksislandapp.data.mapper.toMyReceivedRequest
import com.seif.booksislandapp.data.mapper.toMyRequest
import com.seif.booksislandapp.data.mapper.toRequestDto
import com.seif.booksislandapp.data.remote.FCMApiService
import com.seif.booksislandapp.data.remote.dto.UserDto
import com.seif.booksislandapp.data.remote.dto.notification.FCMMessageDto
import com.seif.booksislandapp.data.remote.dto.notification.NotificationDto
import com.seif.booksislandapp.data.remote.dto.request.RequestDto
import com.seif.booksislandapp.domain.model.adv.AdType
import com.seif.booksislandapp.domain.model.adv.AdvStatus
import com.seif.booksislandapp.domain.model.adv.auction.AuctionStatus
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
    private val connectivityManager: ConnectivityManager,
    private val fcmApiService: FCMApiService
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

                    sendNotificationWhenSendRequest(mySentRequest)

                    Resource.Success(mySentRequest.id)
                }
            } catch (e: Exception) {
                Resource.Error(e.message.toString())
            }
        }
    }

    private suspend fun sendNotificationWhenSendRequest(mySentRequest: MySentRequest) {
        val receiverTokenDocumentSnapshot =
            firestore.collection(Constants.TOKENS_FIIRESTORE_COLLECTION)
                .document(mySentRequest.receiverId)
                .get()
                .await()
        if (receiverTokenDocumentSnapshot.exists()) {
            val token = receiverTokenDocumentSnapshot.get("token").toString()

            val fcmMessageDto = FCMMessageDto(
                title = resourceProvider.string(R.string.request_notification_title),
                body = "${mySentRequest.username} ${resourceProvider.string(R.string.request_send_notification)} for your ${mySentRequest.bookTitle} ${mySentRequest.adType} Advertisement",
                senderId = mySentRequest.senderId,
                receiverId = mySentRequest.receiverId,
                image = "null"
            )
            val notificationDto = NotificationDto(
                fcmMessageDto = fcmMessageDto,
                token = token
            )
            Timber.d("sendRequestNotification: = $notificationDto")
            fcmApiService.sendNotification(notificationDto = notificationDto)
        }
    }

    private suspend fun sendNotificationWhenAcceptOrRejectRequest(
        myReceivedRequest: MyReceivedRequest,
        message: String,
        title: String
    ) {
        val receiverTokenDocumentSnapshot =
            firestore.collection(Constants.TOKENS_FIIRESTORE_COLLECTION)
                .document(myReceivedRequest.senderId)
                .get()
                .await()
        if (receiverTokenDocumentSnapshot.exists()) {
            val token = receiverTokenDocumentSnapshot.get("token").toString()

            val fcmMessageDto = FCMMessageDto(
                title = title,
                body = "${myReceivedRequest.username} $message",
                senderId = myReceivedRequest.receiverId,
                receiverId = myReceivedRequest.senderId,
                image = "null"
            )
            val notificationDto = NotificationDto(
                fcmMessageDto = fcmMessageDto,
                token = token
            )
            Timber.d("sendRequestNotification: = $notificationDto")
            fcmApiService.sendNotification(notificationDto = notificationDto)
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
        myReceivedRequest: MyReceivedRequest,
        acceptStatus: String,
    ): Resource<String, String> {
        if (!connectivityManager.checkInternetConnection()) // remove this check if we want get cached data
            return Resource.Error(resourceProvider.string(R.string.no_internet_connection))

        firestore.collection(REQUESTS_FIIRESTORE_COLLECTION)
            .document(myReceivedRequest.id)
            .update("status", acceptStatus)
            .await()
        val statusField = if (myReceivedRequest.adType == AdType.Auction) {
            "auctionStatus"
        } else {
            "status"
        }

        val statusFieldValue = if (myReceivedRequest.adType == AdType.Auction) {
            AuctionStatus.CLOSED.toString()
        } else {
            AdvStatus.Closed
        }
        firestore.collection(getCollectionNameBaseOnAdType(myReceivedRequest.adType))
            .document(myReceivedRequest.advertisementId)
            .update("confirmationRequestId", "", statusField, statusFieldValue)
            .await()

        // increase totalNumberOfCompletedDealIn(AdType) in the seller profile
        firestore.collection(USER_FIRESTORE_COLLECTION)
            .document(myReceivedRequest.senderId)
            .update(
                getCounterFieldNameBaseOnAdType(myReceivedRequest.adType),
                FieldValue.increment(1)
            )
            .await()

        val message =
            "accepted your confirmation request for ${myReceivedRequest.bookTitle} ${myReceivedRequest.adType} Advertisement"
        val title = resourceProvider.string(R.string.accept_request_notification_title)
        sendNotificationWhenAcceptOrRejectRequest(myReceivedRequest, message, title)
        return Resource.Success("Confirmation Accepted")
    }

    override suspend fun rejectConfirmationRequest(
        myReceivedRequest: MyReceivedRequest,
        rejectStatus: String
    ): Resource<String, String> {
        if (!connectivityManager.checkInternetConnection()) // remove this check if we want get cached data
            return Resource.Error(resourceProvider.string(R.string.no_internet_connection))

        firestore.collection(REQUESTS_FIIRESTORE_COLLECTION)
            .document(myReceivedRequest.id)
            .update("status", rejectStatus)
            .await()

        // update confirmationMessageSent field of that ad to false so seller have opportunity to send another request
        updateIsConfirmationRequestSent(
            myReceivedRequest.adType,
            myReceivedRequest.advertisementId,
            false
        )

        //  sendConfirmationResultNotification(username,true, sellerId)
        val message =
            "rejected your confirmation request for ${myReceivedRequest.bookTitle} ${myReceivedRequest.adType} Advertisement"
        val title = resourceProvider.string(R.string.reject_request_notification_title)
        sendNotificationWhenAcceptOrRejectRequest(myReceivedRequest, message, title)
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