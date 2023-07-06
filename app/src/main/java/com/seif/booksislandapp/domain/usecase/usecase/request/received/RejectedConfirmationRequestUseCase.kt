package com.seif.booksislandapp.domain.usecase.usecase.request.received

import com.seif.booksislandapp.data.repository.RequestsRepositoryImp
import com.seif.booksislandapp.domain.model.request.MyReceivedRequest
import com.seif.booksislandapp.utils.Resource
import javax.inject.Inject

class RejectedConfirmationRequestUseCase @Inject constructor(
    private val requestRepositoryImp: RequestsRepositoryImp
) {
    suspend operator fun invoke(
        myReceivedRequest: MyReceivedRequest,
        rejectStatus: String
    ): Resource<String, String> {
        return requestRepositoryImp.rejectConfirmationRequest(
            myReceivedRequest, rejectStatus
        )
    }
}