package com.seif.booksislandapp.domain.repository

import com.seif.booksislandapp.domain.model.request.MyRequest
import com.seif.booksislandapp.utils.Resource
import kotlinx.coroutines.flow.Flow

interface RequestsRepository {
    suspend fun fetchSentRequests(currentUserId: String): Flow<Resource<List<MyRequest>, String>>
    suspend fun fetchReceivedRequests(currentUserId: String): Flow<Resource<List<MyRequest>, String>>
    suspend fun sendRequest(myRequest: MyRequest): Resource<String, String>
}