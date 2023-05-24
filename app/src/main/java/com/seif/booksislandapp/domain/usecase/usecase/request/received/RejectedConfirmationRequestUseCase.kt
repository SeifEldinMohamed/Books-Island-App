package com.seif.booksislandapp.domain.usecase.usecase.request.received

import com.seif.booksislandapp.data.repository.RequestsRepositoryImp
import com.seif.booksislandapp.utils.Resource
import javax.inject.Inject

class RejectedConfirmationRequestUseCase @Inject constructor(
    private val requestRepositoryImp: RequestsRepositoryImp
) {
    suspend operator fun invoke(
        requestId: String,
        advertisementId: String,
        adType: String,
        rejectStatus: String
    ): Resource<String, String> {
        return requestRepositoryImp.rejectConfirmationRequest(
            requestId, advertisementId, adType, rejectStatus
        )
    }
}