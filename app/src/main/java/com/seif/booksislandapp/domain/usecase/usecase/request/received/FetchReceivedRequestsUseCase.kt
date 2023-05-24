package com.seif.booksislandapp.domain.usecase.usecase.request.received

import com.seif.booksislandapp.data.repository.RequestsRepositoryImp
import com.seif.booksislandapp.domain.model.request.MyReceivedRequest
import com.seif.booksislandapp.utils.Resource
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject

class FetchReceivedRequestsUseCase @Inject constructor(
    private val requestRepositoryImp: RequestsRepositoryImp
) {
    suspend operator fun invoke(currentUserId: String): Flow<Resource<List<MyReceivedRequest>, String>> {
        return requestRepositoryImp.fetchReceivedRequests(currentUserId)
    }
}