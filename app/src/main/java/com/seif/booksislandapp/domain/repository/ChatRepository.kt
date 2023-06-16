package com.seif.booksislandapp.domain.repository

import com.seif.booksislandapp.domain.model.chat.Message
import com.seif.booksislandapp.utils.Resource
import kotlinx.coroutines.flow.Flow

interface ChatRepository {
    suspend fun sendMessage(message: Message): Resource<Message, String>
    fun getMessages(
        senderId: String,
        receiverId: String,
    ): Flow<Resource<List<Message>, String>>

    suspend fun updateIsSeen(
        senderId: String,
        receiverId: String,
        messages: List<Message>
    )
}