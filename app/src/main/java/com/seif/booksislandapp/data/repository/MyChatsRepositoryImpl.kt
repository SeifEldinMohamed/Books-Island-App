package com.seif.booksislandapp.data.repository

import com.google.firebase.firestore.FirebaseFirestore
import com.seif.booksislandapp.data.mapper.toMyChat
import com.seif.booksislandapp.data.remote.dto.MyChatDto
import com.seif.booksislandapp.data.remote.dto.UserDto
import com.seif.booksislandapp.data.remote.dto.chat.MessageDto
import com.seif.booksislandapp.domain.repository.MyChatsRepository
import com.seif.booksislandapp.utils.Constants
import com.seif.booksislandapp.utils.Constants.Companion.CHATS_FIIRESTORE_COLLECTION
import com.seif.booksislandapp.utils.Constants.Companion.CHAT_LIST_FIIRESTORE_COLLECTION
import com.seif.booksislandapp.utils.Constants.Companion.USER_FIRESTORE_COLLECTION
import com.seif.booksislandapp.utils.Resource
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.withTimeout
import timber.log.Timber

class MyChatsRepositoryImpl(
    private val firestore: FirebaseFirestore
) : MyChatsRepository {

    override suspend fun getMyChats(userId: String) = callbackFlow {
        try {
            withTimeout(Constants.TIMEOUT) {
                var messages: ArrayList<MessageDto>
                val myChats = arrayListOf<MyChatDto>()

                // listen for changes in ChatList Collection (to update myChats when I chat with new User in realtime)
                firestore.collectionGroup(CHAT_LIST_FIIRESTORE_COLLECTION)
                    .addSnapshotListener { snapShots, _ ->
                        if (snapShots != null) {
                            var chatList = arrayListOf<String>()
                            for (snapShot in snapShots) {
                                val myChatList = snapShot.get("ids") // id of the user i chat with
                                if (snapShot.id == userId) {
                                    chatList = myChatList as ArrayList<String>
                                    break
                                }
                            }
                            Timber.d("getMyChats: $chatList")
                            // listen for changes in Users Collection (to update myChats when user I chat with update his data (change his username or avatar image) so i get the new changes in realtime)
                            firestore.collection(USER_FIRESTORE_COLLECTION)
                                .addSnapshotListener { snapShots2, _ ->
                                    if (snapShots2 != null) {
                                        val usersIChatWith = arrayListOf<UserDto>()
                                        for (snapShot in snapShots2) {
                                            val userDto = snapShot.toObject(UserDto::class.java)
                                            chatList.forEach { id ->
                                                if (userDto.id == id)
                                                    usersIChatWith.add(userDto)
                                            }
                                        }
                                        Timber.d("getMyChats: usersIChatWith = $usersIChatWith")

                                        // algorithm for getting last messages
                                        usersIChatWith.forEach { user ->
                                            // listen for changes in Chats Collection to update myChats when new message added to get lastMessage in realtime
                                            firestore.collection(CHATS_FIIRESTORE_COLLECTION)
                                                .addSnapshotListener { snapShots3, _ ->
                                                    messages = arrayListOf()

                                                    if (snapShots3 != null) {
                                                        for (snapShot in snapShots3) {
                                                            val message =
                                                                snapShot.toObject(MessageDto::class.java)
                                                            // condition for getting the messages of that specific user
                                                            if (message.senderId == user.id && message.receiverId == userId ||
                                                                message.senderId == userId && message.receiverId == user.id
                                                            ) {
                                                                messages.add(message)
                                                            }
                                                        }

                                                        val lastMessage =
                                                            messages.sortedBy { it.date }.last()
                                                        Timber.d("getMyChats: lastMessage = $lastMessage")

                                                        var flag = true
                                                        for (i in 0 until myChats.size) {
                                                            if (myChats[i].userIChatWith!!.id == user.id) { // this user exists in myChats so update his values

                                                                myChats[i].lastMessage =
                                                                    lastMessage.text
                                                                myChats[i].lastMessageDate =
                                                                    lastMessage.date
                                                                myChats[i].userIChatWith = user
                                                                flag = false
                                                                break
                                                            }
                                                        }
                                                        if (flag) { // not exits in myChats list so add him for first time
                                                            myChats.add(
                                                                MyChatDto(
                                                                    userIChatWith = user,
                                                                    lastMessage = lastMessage.text,
                                                                    lastMessageDate = lastMessage.date
                                                                )
                                                            )
                                                        }
                                                        // send myChats when we reach the last user in usersIChatWith
                                                        if (usersIChatWith.last() == user) {
                                                            trySend(
                                                                Resource.Success(
                                                                    myChats.map { it.toMyChat() }
                                                                        .sortedByDescending { it.lastMessageDate } //  sort myChats according to the newest user I Chat with ( to make him in the top)
                                                                )
                                                            )
                                                        }
                                                    }
                                                }
                                        }
                                    }
                                }
                        }
                    }
            }
        } catch (e: Exception) {
            trySend(Resource.Error(e.message.toString()))
        }
        awaitClose { }
    }
}