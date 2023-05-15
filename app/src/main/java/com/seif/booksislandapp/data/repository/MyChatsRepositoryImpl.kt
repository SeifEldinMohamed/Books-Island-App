package com.seif.booksislandapp.data.repository

import com.google.firebase.firestore.FirebaseFirestore
import com.seif.booksislandapp.data.mapper.toMyChat
import com.seif.booksislandapp.data.remote.dto.MyChatDto
import com.seif.booksislandapp.data.remote.dto.UserDto
import com.seif.booksislandapp.data.remote.dto.chat.MessageDto
import com.seif.booksislandapp.domain.repository.MyChatsRepository
import com.seif.booksislandapp.utils.Constants.Companion.CHATS_FIIRESTORE_COLLECTION
import com.seif.booksislandapp.utils.Constants.Companion.CHAT_LIST_FIIRESTORE_COLLECTION
import com.seif.booksislandapp.utils.Constants.Companion.USER_FIRESTORE_COLLECTION
import com.seif.booksislandapp.utils.Resource
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.callbackFlow
import timber.log.Timber

// Edit logic for getting mychats but still we have isssue in getting last message in realtime
class MyChatsRepositoryImpl(
    private val firestore: FirebaseFirestore
) : MyChatsRepository {

    override suspend fun getMyChats(userId: String) = callbackFlow {
        var messages: ArrayList<MessageDto>
        val lastMessages = arrayListOf<MyChatDto>()

        firestore.collectionGroup(CHAT_LIST_FIIRESTORE_COLLECTION)
            .addSnapshotListener { snapShots, _ ->
                if (snapShots != null) {
                    var chatList = arrayListOf<String>()
                    for (snapShot in snapShots) {
                        val myChatList = snapShot.get("ids") // id of the user i chat with
                        Timber.d("getMyChats: snapshot id = ${snapShot.id}")
                        if (snapShot.id == userId) {
                            // chatList.addAll(myChatList.values)
                            Timber.d("getMyChats: $myChatList")
                            chatList = myChatList as ArrayList<String>
                            break
                        }
                    }
                    Timber.d("getMyChats: $chatList")
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
                                // last messsages
                                usersIChatWith.forEach { user ->
                                    firestore.collection(CHATS_FIIRESTORE_COLLECTION)
                                        .addSnapshotListener { snapShots3, _ ->
                                            messages = arrayListOf<MessageDto>()

                                            Timber.d("getMyChats: detect change in chats")
                                            if (snapShots3 != null) {
                                                for (snapShot in snapShots3) {
                                                    val message =
                                                        snapShot.toObject(MessageDto::class.java)
                                                    if (message.senderId == user.id && message.receiverId == userId ||
                                                        message.senderId == userId && message.receiverId == user.id
                                                    ) {
                                                        messages.add(message)
                                                    }
                                                }

                                                val lastMessage =
                                                    messages.sortedBy { it.date }.last()
                                                Timber.d(
                                                    "getMyChats: lastMessage = $lastMessage"
                                                )
                                                var flag = true
                                                for (i in 0 until lastMessages.size) {
                                                    if (lastMessages[i].userIChatWith!!.id == user.id) {
                                                        // update values
                                                        lastMessages[i].lastMessage =
                                                            lastMessage.text
                                                        lastMessages[i].lastMessageDate =
                                                            lastMessage.date
                                                        lastMessages[i].userIChatWith = user
                                                        flag = false
                                                        break
                                                    }
                                                }
                                                if (flag) {
                                                    lastMessages.add(
                                                        MyChatDto(
                                                            userIChatWith = user,
                                                            lastMessage = lastMessage.text,
                                                            lastMessageDate = lastMessage.date
                                                        )
                                                    )
                                                }

                                                if (usersIChatWith.last() == user) {
                                                    trySend(
                                                        Resource.Success(
                                                            lastMessages.map { it.toMyChat() }
                                                                .sortedByDescending { it.lastMessageDate } //  sort myChats according to the newest user i chated with
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

        awaitClose { }
    }

//    override suspend fun getMyChats(userId: String) = callbackFlow {
//        firestore.collectionGroup(MY_CHATS_FIIRESTORE_COLLECTION)
//            .orderBy("lastMessageDate", Query.Direction.DESCENDING)
//            .addSnapshotListener { snapShots, error ->
//                if (error != null) {
//                    Timber.d("getMyChats: ${error.message}")
//                    trySend(Resource.Error(error.message.toString()))
//                    return@addSnapshotListener
//                }
//                if (snapShots != null) {
//                    val myChats = arrayListOf<MyChatDto>()
//                    for (snapShot in snapShots) {
//                        val myChatDto = snapShot.toObject(MyChatDto::class.java)
//                        myChats.add(myChatDto)
//                    }
//                    // Timber.d("mychats: ${myChats.filter { it.senderId == userId }}")
//                    //  Timber.d("mychats: ${myChats.filter { it.userIChatWith!!.id == userId }}")
//                    // Timber.d("mychats: ${myChats.filter { it.senderId == userId && it.userIChatWith!!.id == userId }}")
// //                        val newMyChats = arrayListOf<MyChatDto>()
// //                        for (i in 0 until myChats.size - 1) {
// //                            if (myChats[i].senderId == myChats[i + 1].userIChatWith!!.id && myChats[i].userIChatWith!!.id == myChats[i + 1].senderId) {
// //                                if (myChats[i].lastMessageDate!! > myChats[i + 1].lastMessageDate!!)
// //                                    newMyChats.add(myChats[i])
// //                            }
// //                        }
//                    Timber.d("getMyChats: $myChats")
//
//                    trySend(
//                        Resource.Success(
//                            myChats.filter { it.userIChatWith!!.id != userId }
//                                .filter { it.senderId == userId }
//                                .map { it.toMyChat() }
//                        )
//                    )
//                }
//            }
//        awaitClose { }
//    }
}