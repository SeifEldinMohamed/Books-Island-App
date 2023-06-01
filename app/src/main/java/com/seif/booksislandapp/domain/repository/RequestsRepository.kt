package com.seif.booksislandapp.domain.repository

import com.seif.booksislandapp.domain.model.adv.AdType
import com.seif.booksislandapp.domain.model.request.MyReceivedRequest
import com.seif.booksislandapp.domain.model.request.MySentRequest
import com.seif.booksislandapp.utils.Resource
import kotlinx.coroutines.flow.Flow

interface RequestsRepository {
    suspend fun sendRequest(mySentRequest: MySentRequest): Resource<String, String>
    suspend fun fetchSentRequests(currentUserId: String): Flow<Resource<List<MySentRequest>, String>>
    suspend fun fetchReceivedRequests(currentUserId: String): Flow<Resource<List<MyReceivedRequest>, String>>
    suspend fun acceptConfirmationRequest(
        requestId: String,
        sellerId: String,
        adType: AdType,
        acceptStatus: String,
        advertisementId: String
    ): Resource<String, String>

    suspend fun rejectConfirmationRequest(
        requestId: String,
        advertisementId: String,
        adType: AdType,
        rejectStatus: String
    ): Resource<String, String>

    suspend fun cancelSentRequest(
        requestId: String,
        adType: AdType,
        advertisementId: String
    ): Resource<String, String>

    suspend fun deleteRequest(requestId: String): Resource<String, String>
}