package com.seif.booksislandapp.domain.usecase.usecase.request.sent

import com.seif.booksislandapp.data.repository.RequestsRepositoryImp
import com.seif.booksislandapp.domain.model.request.MySentRequest
import com.seif.booksislandapp.utils.Resource
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject

class FetchMySentRequestsUseCase @Inject constructor(
    private val requestRepositoryImp: RequestsRepositoryImp
) {
    suspend operator fun invoke(currentUserId: String): Flow<Resource<List<MySentRequest>, String>> {
        return requestRepositoryImp.fetchSentRequests(currentUserId)
    }
}