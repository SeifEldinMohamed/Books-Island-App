package com.seif.booksislandapp.domain.repository

import com.seif.booksislandapp.domain.model.chat.MyChat
import com.seif.booksislandapp.utils.Resource

interface MyChatsRepository {
    suspend fun getMyChats(userId: String): Resource<List<MyChat>, String>
}