package com.seif.booksislandapp.domain.usecase.usecase.request.sent

import com.seif.booksislandapp.data.repository.RequestsRepositoryImp
import com.seif.booksislandapp.utils.Resource
import javax.inject.Inject

class DeleteSentRequestUseCase @Inject constructor(
    private val requestRepositoryImp: RequestsRepositoryImp
) {
    suspend operator fun invoke(
        requestId: String
    ): Resource<String, String> {
        return requestRepositoryImp.deleteRequest(requestId)
    }
}