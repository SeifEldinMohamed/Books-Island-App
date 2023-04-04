package com.seif.booksislandapp.data.repository

import android.net.ConnectivityManager
import android.net.Uri
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.storage.StorageReference
import com.seif.booksislandapp.R
import com.seif.booksislandapp.data.mapper.toMessage
import com.seif.booksislandapp.data.mapper.toMessageDto
import com.seif.booksislandapp.data.remote.dto.chat.MessageDto
import com.seif.booksislandapp.domain.model.chat.Message
import com.seif.booksislandapp.domain.repository.ChatRepository
import com.seif.booksislandapp.utils.Constants
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
import javax.inject.Inject

class ChatRepositoryImp @Inject constructor(
    private val firestore: FirebaseFirestore,
    private val storageReference: StorageReference,
    private val resourceProvider: ResourceProvider,
    private val userRepositoryImp: UserRepositoryImp,
    private val advertisementRepositoryImp: AdvertisementRepositoryImp,
    private val connectivityManager: ConnectivityManager
) : ChatRepository {
    override suspend fun sendMessage(message: Message): Resource<Message, String> {
        if (!connectivityManager.checkInternetConnection())
            return Resource.Error(resourceProvider.string(R.string.no_internet_connection))

        return try {
            withTimeout(Constants.TIMEOUT_UPLOAD) {
                val doc = firestore.collection(Constants.USER_FIRESTORE_COLLECTION)
                    .document(message.senderId)
                    .collection(Constants.CHATS_FIIRESTORE_COLLECTION)
                    .document(message.receiverId)
                    .collection(Constants.MESSAGES_FIIRESTORE_COLLECTION)
                    .document()
                if (message.imageUrl != null) {
                    when (val result = uploadMessageImaage(message.senderId, message.imageUrl!!)) {
                        is Resource.Error -> Resource.Error(result.message)
                        is Resource.Success -> {
                            message.imageUrl = result.data
                            message.id = doc.id
                            doc.set(message.toMessageDto())
                                .await()
                            advertisementRepositoryImp.getUserById(message.senderId)//////////
                            // userRepositoryImp.updateUserProfile()
                            Resource.Success(message)
                        }
                    }
                } else {
                    message.id = doc.id
                    doc.set(message.toMessageDto())
                        .await()
                    Resource.Success(message)
                }
            }
        } catch (e: Exception) {
            Resource.Error(e.message.toString())
        }
    }

    override suspend fun getMessages(
        senderId: String,
        receiverId: String
    ): Flow<Resource<List<Message>, String>> = callbackFlow {
        val senderMessages = mutableListOf<MessageDto>()
        val receiverMessages = mutableListOf<MessageDto>()

        val senderRef = firestore.collection(Constants.USER_FIRESTORE_COLLECTION)
            .document(senderId)
            .collection(Constants.CHATS_FIIRESTORE_COLLECTION)
            .document(receiverId)
            .collection(Constants.MESSAGES_FIIRESTORE_COLLECTION)
            .orderBy("date")

        val receiverRef = firestore.collection(Constants.USER_FIRESTORE_COLLECTION)
            .document(receiverId)
            .collection(Constants.CHATS_FIIRESTORE_COLLECTION)
            .document(senderId)
            .collection(Constants.MESSAGES_FIIRESTORE_COLLECTION)
            .orderBy("date")

        val senderListener = senderRef.addSnapshotListener { snapshot, error ->
            if (error != null) {
                trySend(Resource.Error(error.message.toString()))
                return@addSnapshotListener
            }

            if (snapshot != null) {
                senderMessages.clear()
                senderMessages.addAll(snapshot.documents.mapNotNull { it.toObject(MessageDto::class.java) })
                val allMessages = (senderMessages + receiverMessages).sortedBy { it.date }
                trySend(Resource.Success(allMessages.map { it.toMessage() }))
            }
        }

        val receiverListener = receiverRef.addSnapshotListener { snapshot, error ->
            if (error != null) {
                trySend(Resource.Error(error.message.toString()))
                return@addSnapshotListener
            }

            if (snapshot != null) {
                receiverMessages.clear()
                receiverMessages.addAll(snapshot.documents.mapNotNull { it.toObject(MessageDto::class.java) })
                val allMessages = (senderMessages + receiverMessages).sortedBy { it.date }
                trySend(Resource.Success(allMessages.map { it.toMessage() }))
            }
        }

        awaitClose {
            senderListener.remove()
            receiverListener.remove()
        }
    }

    private suspend fun uploadMessageImaage(
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