package com.seif.booksislandapp.domain.repository

import com.seif.booksislandapp.domain.model.chat.MyChat
import com.seif.booksislandapp.utils.Resource
import kotlinx.coroutines.flow.Flow

interface MyChatsRepository {
    suspend fun getMyChats(userId: String): Flow<Resource<List<MyChat>, String>>
}