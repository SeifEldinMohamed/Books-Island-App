package com.seif.booksislandapp.data.repository

import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.net.ConnectivityManager
import android.net.Uri
import com.google.firebase.firestore.DocumentReference
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.SetOptions
import com.google.firebase.storage.StorageReference
import com.seif.booksislandapp.R
import com.seif.booksislandapp.data.mapper.toMessage
import com.seif.booksislandapp.data.mapper.toMessageDto
import com.seif.booksislandapp.data.remote.FCMApiService
import com.seif.booksislandapp.data.remote.dto.UserDto
import com.seif.booksislandapp.data.remote.dto.chat.MessageDto
import com.seif.booksislandapp.data.remote.dto.notification.FCMMessageDto
import com.seif.booksislandapp.data.remote.dto.notification.NotificationDto
import com.seif.booksislandapp.domain.model.chat.Message
import com.seif.booksislandapp.domain.repository.ChatRepository
import com.seif.booksislandapp.utils.Constants
import com.seif.booksislandapp.utils.Constants.Companion.CHATS_FIIRESTORE_COLLECTION
import com.seif.booksislandapp.utils.Constants.Companion.USER_FIRESTORE_COLLECTION
import com.seif.booksislandapp.utils.Resource
import com.seif.booksislandapp.utils.ResourceProvider
import com.seif.booksislandapp.utils.checkInternetConnection
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.tasks.await
import kotlinx.coroutines.withContext
import kotlinx.coroutines.withTimeout
import timber.log.Timber
import java.net.URL
import javax.inject.Inject

class ChatRepositoryImp @Inject constructor(
    private val firestore: FirebaseFirestore,
    private val storageReference: StorageReference,
    private val resourceProvider: ResourceProvider,
    private val connectivityManager: ConnectivityManager,
    private val fcmApiService: FCMApiService
) : ChatRepository {
    override suspend fun sendMessage(message: Message): Resource<Message, String> {
        if (!connectivityManager.checkInternetConnection())
            return Resource.Error(resourceProvider.string(R.string.no_internet_connection))

        return withTimeout(Constants.TIMEOUT_UPLOAD) {
            try {
                uploadMessage(message)
            } catch (e: Exception) {
                Resource.Error(e.message.toString())
            }
        }
    }

    private suspend fun uploadMessage(
        message: Message
    ): Resource<Message, String> {
        val chatsDocumentReference = firestore.collection(CHATS_FIIRESTORE_COLLECTION).document()
        message.id = chatsDocumentReference.id
        return if (message.imageUrl != null) {
            uploadImageMessage(message, chatsDocumentReference)
        } else { // text message
            uploadTextMessage(message, chatsDocumentReference)
        }
    }

    private suspend fun uploadTextMessage(
        message: Message,
        chatsDocumentReference: DocumentReference
    ): Resource<Message, String> {
        chatsDocumentReference.set(
            message.toMessageDto(),
            SetOptions.merge()
        ).await()
        Timber.d("uploadMessage: message uploaded successfully")
        // add receiver to sender chat list
        val senderChatListDocumentReference =
            firestore.collection(Constants.CHAT_LIST_FIIRESTORE_COLLECTION)
                .document(message.senderId)
        val documentSnapshot = senderChatListDocumentReference.get().await()

        if (documentSnapshot.exists()) {
            var ids = documentSnapshot.get("ids") as? ArrayList<String>
            if (ids != null) {
                // Array already exists, add the new id if it's not there
                if (!ids.contains(message.receiverId))
                    ids.add(message.receiverId)
            } else {
                // Array does not exist, create a new array with the first id
                ids = arrayListOf(message.receiverId)
            }

            // Update the document with the modified or new array
            firestore.collection(Constants.CHAT_LIST_FIIRESTORE_COLLECTION)
                .document(message.senderId).set(hashMapOf("ids" to ids)).await()
        } else { // first time to chat with someone

            firestore.collection(Constants.CHAT_LIST_FIIRESTORE_COLLECTION)
                .document(message.senderId).set(hashMapOf("ids" to message.receiverId))
                .await()
        }

        // add sender to receiver chat list
        val receiverChatListDocumentReference =
            firestore.collection(Constants.CHAT_LIST_FIIRESTORE_COLLECTION)
                .document(message.receiverId)
        val documentSnapshot2 = receiverChatListDocumentReference.get().await()
        if (documentSnapshot2.exists()) {
            var ids = documentSnapshot2.get("ids") as? ArrayList<String>
            if (ids != null) {
                // Array already exists, add the new id if it's not there
                if (!ids.contains(message.senderId))
                    ids.add(message.senderId)
            } else {
                // Array does not exist, create a new array with the first id
                val newItem = message.senderId
                val newArray = arrayListOf(newItem)
                ids = newArray
            }
            // Update the document with the modified or new array
            firestore.collection(Constants.CHAT_LIST_FIIRESTORE_COLLECTION)
                .document(message.receiverId).set(hashMapOf("ids" to ids)).await()
        } else { // first time to chat with someone
            firestore.collection(Constants.CHAT_LIST_FIIRESTORE_COLLECTION)
                .document(message.receiverId).set(hashMapOf("ids" to message.senderId))
                .await()
        }

        val senderUserDocumentSnapshot =
            firestore.collection(USER_FIRESTORE_COLLECTION).document(message.senderId).get()
                .await()
        if (senderUserDocumentSnapshot.exists()) {
            val userDto = senderUserDocumentSnapshot.toObject(UserDto::class.java)
            Timber.d("uploadMessage: user exits $userDto")
            // send notification
            sendNotification(userDto!!.username, message)
        }

        return Resource.Success(message)
    }

    private suspend fun uploadImageMessage(
        message: Message,
        chatsDocumentReference: DocumentReference
    ): Resource<Message, String> {
        return when (
            val result =
                uploadImageToStorage(message.senderId, message.imageUrl!!)
        ) {
            is Resource.Error -> Resource.Error(result.message)
            is Resource.Success -> {
                message.imageUrl = result.data
                chatsDocumentReference.set(
                    message.toMessageDto(),
                    SetOptions.merge()
                ).await()

                val senderUserDocumentSnapshot =
                    firestore.collection(USER_FIRESTORE_COLLECTION).document(message.senderId)
                        .get()
                        .await()
                if (senderUserDocumentSnapshot.exists()) {
                    val user = senderUserDocumentSnapshot.toObject(UserDto::class.java)
                    Timber.d("uploadMessage: user exits $user")
                    // send notification
                    sendNotification(user!!.username, message)
                }

                Resource.Success(message)
            }
        }
    }

    private suspend fun sendNotification(username: String, message: Message) {
        val receiverTokenDocumentSnapshot =
            firestore.collection(Constants.TOKENS_FIIRESTORE_COLLECTION)
                .document(message.receiverId)
                .get()
                .await()
        if (receiverTokenDocumentSnapshot.exists()) {
            val token = receiverTokenDocumentSnapshot.get("token").toString()
            Timber.d("sendNotification: exits and it's token =  $token")

            val fcmMessageDto = FCMMessageDto(
                title = "New Message",
                body = "$username: ${message.text}",
                senderId = message.senderId,
                receiverId = message.receiverId,
                image = message.imageUrl.toString()
            )
            val notificationDto = NotificationDto(
                fcmMessageDto = fcmMessageDto,
                token = token
            )
            Timber.d("sendNotification: notificationDto= $notificationDto")
            fcmApiService.sendNotification(notificationDto = notificationDto)
        }
    }

    private fun convertImageUrlToBitmap(image: String): Bitmap {
        val url = URL(image)
        return BitmapFactory.decodeStream(url.openConnection().getInputStream())
    }

    override fun getMessages(
        senderId: String,
        receiverId: String
    ): Flow<Resource<List<Message>, String>> = callbackFlow {
        val chatsCollectionReference = firestore.collection(CHATS_FIIRESTORE_COLLECTION)
        chatsCollectionReference.orderBy("date")
            .addSnapshotListener { snapShots, error ->
                Timber.v("isFromCache ${snapShots?.metadata?.isFromCache}")
                if (error != null) {
                    Timber.d("getMessages: ${error.message}")
                    trySend(Resource.Error(error.message.toString()))
                    return@addSnapshotListener
                }
                if (snapShots != null) {
                    val messageList = arrayListOf<MessageDto>()
                    for (snapShot in snapShots) {
                        val messageDto = snapShot.toObject(MessageDto::class.java)
                        if (
                            (messageDto.senderId == senderId && messageDto.receiverId == receiverId) ||
                            (messageDto.senderId == receiverId && messageDto.receiverId == senderId)
                        ) // first check for messages i send and the second is for messages i recieved from that user
                            messageList.add(messageDto)
                    }
                    Timber.d("messages List =  $messageList")
                    trySend(Resource.Success(messageList.map { it.toMessage() }))
                }
            }

        awaitClose { }
    }

    private suspend fun uploadImageToStorage(
        ownerId: String,
        imageUri: Uri
    ): Resource<Uri, String> {
        return try {
            withTimeout(Constants.TIMEOUT_UPLOAD) {
                val uri: Uri = withContext(Dispatchers.IO) {
                    storageReference.child(
                        "$ownerId/${imageUri.lastPathSegment ?: System.currentTimeMillis()}"
                    )
                        .putFile(imageUri)
                        .await()
                        .storage
                        .downloadUrl
                        .await()
                }
                Resource.Success(uri)
            }
        } catch (e: Exception) {
            Resource.Error(e.message.toString())
        }
    }
}

/**
msgCollection.document(documentNameSender)
.set(
mapOf(
"chat_members" to FieldValue.arrayUnion(
message.senderId,
message.receiverId
)
),
SetOptions.merge()
).await()
 **/
// Timber.d("chat member update successfully")