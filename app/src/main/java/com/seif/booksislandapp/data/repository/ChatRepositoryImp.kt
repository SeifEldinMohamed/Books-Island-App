package com.seif.booksislandapp.data.repository

import android.net.ConnectivityManager
import android.net.Uri
import com.google.firebase.firestore.CollectionReference
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.SetOptions
import com.google.firebase.storage.StorageReference
import com.seif.booksislandapp.R
import com.seif.booksislandapp.data.mapper.toMessage
import com.seif.booksislandapp.data.mapper.toMessageDto
import com.seif.booksislandapp.data.mapper.toUser
import com.seif.booksislandapp.data.mapper.toUserDto
import com.seif.booksislandapp.data.remote.dto.MyChatDto
import com.seif.booksislandapp.data.remote.dto.UserDto
import com.seif.booksislandapp.data.remote.dto.chat.MessageDto
import com.seif.booksislandapp.domain.model.User
import com.seif.booksislandapp.domain.model.chat.Message
import com.seif.booksislandapp.domain.repository.ChatRepository
import com.seif.booksislandapp.utils.Constants
import com.seif.booksislandapp.utils.Constants.Companion.MESSAGES_FIIRESTORE_COLLECTION
import com.seif.booksislandapp.utils.Resource
import com.seif.booksislandapp.utils.ResourceProvider
import com.seif.booksislandapp.utils.checkInternetConnection
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.tasks.await
import kotlinx.coroutines.withContext
import kotlinx.coroutines.withTimeout
import timber.log.Timber
import javax.inject.Inject

class ChatRepositoryImp @Inject constructor(
    private val firestore: FirebaseFirestore,
    private val storageReference: StorageReference,
    private val resourceProvider: ResourceProvider,
    private val connectivityManager: ConnectivityManager
) : ChatRepository {
    override suspend fun sendMessage(message: Message): Resource<Message, String> {
        if (!connectivityManager.checkInternetConnection())
            return Resource.Error(resourceProvider.string(R.string.no_internet_connection))

        return try {
            withTimeout(Constants.TIMEOUT_UPLOAD) {

                updateMyChats(message) // / delaying the sending action!!

                val documentNameReciever = "${message.receiverId}_${message.senderId}"
                val documentNameSender = "${message.senderId}_${message.receiverId}"
                val msgCollection = firestore.collection(Constants.CHATS_FIIRESTORE_COLLECTION)
                val documentSnapshot2 = msgCollection.document(documentNameReciever).get().await()

                if (documentSnapshot2.exists()) {
                    Timber.v("Case 2 document already exits")
                    uploadMessage(message, msgCollection, documentNameReciever)
                } else {
                    // no previous chat history(senderId_receiverId & receiverId_senderId both don't exist)
                    // so we create document senderId_receiverId then messages array then add messageMap to messages
                    // this node exists send your message
                    // add ids of chat members
                    Timber.v("Case 3 no previous chat history(senderId_receiverId & receiverId_senderId both don't exist)")
                    uploadMessage(message, msgCollection, documentNameSender)
                }
            }
        } catch (e: Exception) {
            Resource.Error(e.message.toString())
        }
    }

    private suspend fun uploadMessage(
        message: Message,
        msgCollection: CollectionReference,
        documentName: String
    ): Resource<Message, String> {
        val doc = msgCollection.document(documentName)
            .collection(MESSAGES_FIIRESTORE_COLLECTION)
            .document()
        message.id = doc.id
        return if (message.imageUrl != null) {
            when (
                val result =
                    uploadMessageImaage(message.senderId, message.imageUrl!!)
            ) {
                is Resource.Error -> Resource.Error(result.message)
                is Resource.Success -> {
                    message.imageUrl = result.data
                    doc.set(
                        message.toMessageDto(),
                        SetOptions.merge()
                    ).await()
                    Resource.Success(message)
                }
            }
        } else {
            doc.set(
                message.toMessageDto(),
                SetOptions.merge()
            ).await()
            Resource.Success(message)
        }
    }

    private suspend fun updateMyChats(message: Message) {
        val usersCollection = firestore.collection(Constants.USER_FIRESTORE_COLLECTION)
        val myChatsQuerySnapshot =
            usersCollection.document(message.senderId)
                .collection(Constants.MY_CHATS_FIIRESTORE_COLLECTION).get().await()

        val myChats: ArrayList<MyChatDto> = arrayListOf()
        for (document in myChatsQuerySnapshot) {
            try {
                val myChat = document.toObject(MyChatDto::class.java)
                myChats.add(myChat)
            } catch (e: Exception) {
                Timber.d("updateMyChats: Error = ${e.message}")
            }
        }
        Timber.d("updateMyChats: my chats = $myChats")
        if (myChats.isEmpty()) {
            Timber.d("case1: all chats is empty (first time to chat with someone) ")
            when (val result = getUserById(message.receiverId)) {
                is Resource.Error -> Resource.Error(result.message)
                is Resource.Success -> {
                    val receiverUser = result.data
                    usersCollection.document(message.senderId)
                        .collection(Constants.MY_CHATS_FIIRESTORE_COLLECTION)
                        .document(message.receiverId)
                        .set(
                            MyChatDto(
                                senderId = message.senderId,
                                userIChatWith = receiverUser.toUserDto(),
                                lastMessage = message.text.toString(),
                                lastMessageDate = message.date
                            )
                        ).await()
                }
            }
        } else {
            Timber.d("updateMyChats: $myChats")
            myChats.forEach { myChat ->
                if (myChat.userIChatWith!!.id != message.receiverId) {
                    Timber.d("case2: first time to chat with this user")
                    when (val result = getUserById(message.receiverId)) {
                        is Resource.Error -> Resource.Error(result.message)
                        is Resource.Success -> {
                            val receiverUser = result.data
                            usersCollection.document(message.senderId)
                                .collection(Constants.MY_CHATS_FIIRESTORE_COLLECTION)
                                .document(message.receiverId)
                                .set(
                                    MyChatDto(
                                        senderId = message.senderId,
                                        userIChatWith = receiverUser.toUserDto(),
                                        lastMessage = message.text.toString(),
                                        lastMessageDate = message.date
                                    )
                                ).await()
                        }
                    }
                } else {
                    Timber.d("case3: not first time to chat with him ")
                    when (val result = getUserById(message.receiverId)) {
                        is Resource.Error -> Resource.Error(result.message)
                        is Resource.Success -> {
                            val receiverUser = result.data
                            usersCollection.document(message.senderId)
                                .collection(Constants.MY_CHATS_FIIRESTORE_COLLECTION)
                                .document(message.receiverId)
                                .set( // use update instead
                                    MyChatDto(
                                        senderId = message.senderId,
                                        userIChatWith = receiverUser.toUserDto(),
                                        lastMessage = message.text.toString(),
                                        lastMessageDate = message.date
                                    )
                                ).await()
                            Timber.d(
                                "updateMyChats: ${
                                MyChatDto(
                                    senderId = message.senderId,
                                    userIChatWith = receiverUser.toUserDto(),
                                    lastMessage = message.text.toString(),
                                    lastMessageDate = message.date
                                )
                                }"
                            )
                        }
                    }
                }
            }
        }
    }

    override fun getMessages(
        senderId: String,
        receiverId: String
    ): Flow<Resource<List<Message>, String>> = callbackFlow {
        val messageSubCollection = firestore.collectionGroup(MESSAGES_FIIRESTORE_COLLECTION)
        messageSubCollection.whereArrayContains("chatUsers", senderId)
            .orderBy("date")
            .addSnapshotListener { snapShots, error ->
                Timber.v("isFromCache ${snapShots?.metadata?.isFromCache}")
                if (error != null) {
                    Timber.d("getMessages: ${error.message}")
                    trySend(Resource.Error(error.message.toString()))
                    return@addSnapshotListener
                }
                if (snapShots != null) {
                    Timber.d("sender: $senderId")
                    Timber.d("receiver: $receiverId")
                    val messageList = arrayListOf<MessageDto>()
                    for (snapShot in snapShots) {
                        val message = snapShot.toObject(MessageDto::class.java)
                        Timber.d("message =  $message")
                        if ((message.senderId == senderId && message.receiverId == receiverId) || (message.senderId == receiverId && message.receiverId == senderId)) // first check for messages i send and the second is for messages i recieved from that user
                            messageList.add(message)
                    }
                    Timber.d("messages: $messageList")
                    trySend(Resource.Success(messageList.map { it.toMessage() }))
                }
            }

        awaitClose { }
    }

    private suspend fun getUserById(id: String): Resource<User, String> {
        if (!connectivityManager.checkInternetConnection())
            return Resource.Error(resourceProvider.string(R.string.no_internet_connection))
        return try {
            withTimeout(Constants.TIMEOUT) {

                delay(500) // to show loading progress
                val querySnapshot =
                    firestore.collection(Constants.USER_FIRESTORE_COLLECTION).document(id)
                        .get()
                        .await()
                val user = querySnapshot.toObject(UserDto::class.java)
                Resource.Success(
                    data = user!!.toUser()
                )
            }
        } catch (e: Exception) {
            Timber.d("getUserById: ${e.message}")
            Resource.Error(e.message.toString())
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