package com.seif.booksislandapp.data.repository

import android.net.ConnectivityManager
import com.google.firebase.firestore.FirebaseFirestore
import com.seif.booksislandapp.R
import com.seif.booksislandapp.data.mapper.toUser
import com.seif.booksislandapp.data.remote.dto.UserDto
import com.seif.booksislandapp.data.remote.dto.chat.MessageDto
import com.seif.booksislandapp.domain.model.User
import com.seif.booksislandapp.domain.model.chat.MyChat
import com.seif.booksislandapp.domain.repository.MyChatsRepository
import com.seif.booksislandapp.utils.Constants
import com.seif.booksislandapp.utils.Constants.Companion.CHATS_FIIRESTORE_COLLECTION
import com.seif.booksislandapp.utils.Constants.Companion.MESSAGES_FIIRESTORE_COLLECTION
import com.seif.booksislandapp.utils.Resource
import com.seif.booksislandapp.utils.ResourceProvider
import com.seif.booksislandapp.utils.checkInternetConnection
import kotlinx.coroutines.delay
import kotlinx.coroutines.tasks.await
import kotlinx.coroutines.withTimeout

class MyChatsRepositoryImpl(
    private val firestore: FirebaseFirestore,
    private val resourceProvider: ResourceProvider,
    private val connectivityManager: ConnectivityManager
) : MyChatsRepository {
    override suspend fun getMyChats(userId: String): Resource<List<MyChat>, String> {
        val snapshot = firestore.collection(CHATS_FIIRESTORE_COLLECTION)
            .document(userId)
            .collection(MESSAGES_FIIRESTORE_COLLECTION)
            .get()
            .await()
        val messages: ArrayList<MessageDto> = arrayListOf()
        val usersIdIChatWith: ArrayList<String> = arrayListOf()
        val myChats: ArrayList<MyChat> = arrayListOf()
        for (document in snapshot) {
            val message = document.toObject(MessageDto::class.java)
            messages.add(message)
            if (!usersIdIChatWith.contains(message.receiverId)) {
                usersIdIChatWith.add(message.receiverId)
            }
        }
        usersIdIChatWith.forEach { id ->
            when (val result = getUserById(id)) {
                is Resource.Error -> return Resource.Error(result.message)
                is Resource.Success -> {
                    val receiverUser = result.data
                    val userMessages = messages.filter { it.receiverId == id }
                        .sortedBy { it.date }.last()
                    myChats.add(
                        MyChat(
                            userIChatWith = receiverUser,
                            lastMessage = userMessages.text.toString(),
                            lastMessageDate = userMessages.date
                        )
                    )
                }
            }
        }
        return Resource.Success(myChats)
    }

//    override fun getMyChats(userId: String): Flow<Resource<List<MyChats>, String>> = callbackFlow {
//        val myChats = mutableListOf<MyChats>()
//        val chatsRef = firestore.collection(Constants.CHATS_FIIRESTORE_COLLECTION)
//            .document(userId)
//            .collection(Constants.MESSAGES_FIIRESTORE_COLLECTION)
//
//        val chatsListener = chatsRef.orderBy("date", Query.Direction.DESCENDING)
//            .addSnapshotListener { snapshot, error ->
//                if (error != null) {
//                    trySend(Resource.Error(error.message.toString()))
//                    return@addSnapshotListener
//                }
//
//                if (snapshot != null) {
//                    myChats.clear()
//                    for (document in snapshot.documents) {
//                        val receiverId = document.id
//                        val messagesRef = chatsRef.document(receiverId)
//                            .collection(Constants.MESSAGES_FIIRESTORE_COLLECTION)
//                            .orderBy("date", Query.Direction.DESCENDING)
//                            .limit(1)
//
//                        messagesRef.addSnapshotListener { messagesSnapshot, messagesError ->
//                            if (messagesError != null) {
//                                trySend(Resource.Error(messagesError.message.toString()))
//                                return@addSnapshotListener
//                            }
//
//                            if (messagesSnapshot != null) {
//                                for (messagesDocument in messagesSnapshot.documents) {
//                                    val lastMessage = messagesDocument.toObject(MessageDto::class.java)?.toMessage()?.text
//                                    val lastMessageDate = messagesDocument.toObject(MessageDto::class.java)?.toMessage()?.date
//                                    val userRef = firestore.collection(Constants.USER_FIRESTORE_COLLECTION)
//                                        .document(receiverId)
//                                    userRef.get().addOnCompleteListener { userTask ->
//                                        if (userTask.isSuccessful && userTask.result != null) {
//                                            val userDto = userTask.result!!.toObject(UserDto::class.java)
//                                            if (userDto != null) {
//                                                myChats.add(
//                                                    MyChats(
//                                                        receiverId,
//                                                        userDto.avatarImage,
//                                                        userDto.username,
//                                                        lastMessage ?: "",
//                                                        lastMessageDate ?: Date()
//                                                    )
//                                                )
//                                                trySend(Resource.Success(myChats))
//                                            }
//                                        } else {
//                                            trySend(Resource.Error(userTask.exception?.message.toString()))
//                                        }
//                                    }
//                                }
//                            }
//                        }
//                    }
//                }
//            }
//
//        awaitClose {
//            chatsListener.remove()
//        }
//    }

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
            Resource.Error(e.message.toString())
        }
    }
}