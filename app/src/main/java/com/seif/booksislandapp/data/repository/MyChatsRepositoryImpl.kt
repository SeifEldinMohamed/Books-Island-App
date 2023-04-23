package com.seif.booksislandapp.data.repository

import android.net.ConnectivityManager
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.Query
import com.seif.booksislandapp.R
import com.seif.booksislandapp.data.mapper.toMyChat
import com.seif.booksislandapp.data.mapper.toUser
import com.seif.booksislandapp.data.remote.dto.MyChatDto
import com.seif.booksislandapp.data.remote.dto.UserDto
import com.seif.booksislandapp.domain.model.User
import com.seif.booksislandapp.domain.repository.MyChatsRepository
import com.seif.booksislandapp.utils.Constants
import com.seif.booksislandapp.utils.Constants.Companion.MY_CHATS_FIIRESTORE_COLLECTION
import com.seif.booksislandapp.utils.Resource
import com.seif.booksislandapp.utils.ResourceProvider
import com.seif.booksislandapp.utils.checkInternetConnection
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.tasks.await
import kotlinx.coroutines.withTimeout
import timber.log.Timber

// Edit logic for getting mychats but still we have isssue in getting last message in realtime
class MyChatsRepositoryImpl(
    private val firestore: FirebaseFirestore,
    private val resourceProvider: ResourceProvider,
    private val connectivityManager: ConnectivityManager
) : MyChatsRepository {
    override suspend fun getMyChats(userId: String) = callbackFlow {
        firestore.collectionGroup(MY_CHATS_FIIRESTORE_COLLECTION)
            .orderBy("lastMessageDate", Query.Direction.DESCENDING)
            .addSnapshotListener { snapShots, error ->
                if (error != null) {
                    Timber.d("getMyChats: ${error.message}")
                    trySend(Resource.Error(error.message.toString()))
                    return@addSnapshotListener
                }
                if (snapShots != null) {
                    val myChats = arrayListOf<MyChatDto>()
                    for (snapShot in snapShots) {
                        val myChatDto = snapShot.toObject(MyChatDto::class.java)
                        myChats.add(myChatDto)
                    }
                    // Timber.d("mychats: ${myChats.filter { it.senderId == userId }}")
                    //  Timber.d("mychats: ${myChats.filter { it.userIChatWith!!.id == userId }}")
                    // Timber.d("mychats: ${myChats.filter { it.senderId == userId && it.userIChatWith!!.id == userId }}")
//                        val newMyChats = arrayListOf<MyChatDto>()
//                        for (i in 0 until myChats.size - 1) {
//                            if (myChats[i].senderId == myChats[i + 1].userIChatWith!!.id && myChats[i].userIChatWith!!.id == myChats[i + 1].senderId) {
//                                if (myChats[i].lastMessageDate!! > myChats[i + 1].lastMessageDate!!)
//                                    newMyChats.add(myChats[i])
//                            }
//                        }
                    Timber.d("getMyChats: $myChats")

                    trySend(
                        Resource.Success(
                            myChats.filter { it.userIChatWith!!.id != userId }
                                .filter { it.senderId == userId }
                                .map { it.toMyChat() }
                        )
                    )
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
            Resource.Error(e.message.toString())
        }
    }
}