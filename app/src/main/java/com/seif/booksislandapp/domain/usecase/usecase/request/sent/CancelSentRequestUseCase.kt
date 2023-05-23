package com.seif.booksislandapp.domain.usecase.usecase.request.sent

import com.seif.booksislandapp.data.repository.RequestsRepositoryImp
import com.seif.booksislandapp.utils.Resource
import javax.inject.Inject

class CancelSentRequestUseCase @Inject constructor(
    private val requestRepositoryImp: RequestsRepositoryImp
) {
    suspend operator fun invoke(
        requestId: String,
        adType: String,
        advertisementId: String
    ): Resource<String, String> {
        return requestRepositoryImp.cancelSentRequest(requestId, adType, advertisementId)
    }
}