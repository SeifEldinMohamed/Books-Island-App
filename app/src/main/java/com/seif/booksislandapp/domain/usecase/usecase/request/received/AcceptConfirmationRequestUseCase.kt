package com.seif.booksislandapp.domain.usecase.usecase.request.received

import com.seif.booksislandapp.data.repository.RequestsRepositoryImp
import com.seif.booksislandapp.domain.model.adv.AdType
import com.seif.booksislandapp.utils.Resource
import javax.inject.Inject

class AcceptConfirmationRequestUseCase @Inject constructor(
    private val requestRepositoryImp: RequestsRepositoryImp
) {
    suspend operator fun invoke(
        requestId: String,
        sellerId: String,
        adType: AdType,
        acceptStatus: String,
        advertisementId: String
    ): Resource<String, String> {
        return requestRepositoryImp.acceptConfirmationRequest(
            requestId,
            sellerId,
            adType,
            acceptStatus,
            advertisementId
        )
    }
}