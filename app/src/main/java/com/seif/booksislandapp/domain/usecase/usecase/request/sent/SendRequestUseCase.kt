package com.seif.booksislandapp.domain.usecase.usecase.request.sent

import com.seif.booksislandapp.data.repository.RequestsRepositoryImp
import com.seif.booksislandapp.domain.model.request.MySentRequest
import com.seif.booksislandapp.utils.Resource
import javax.inject.Inject

class SendRequestUseCase @Inject constructor(
    private val requestRepositoryImp: RequestsRepositoryImp
) {
    suspend operator fun invoke(mySentRequest: MySentRequest): Resource<String, String> {
        return requestRepositoryImp.sendRequest(mySentRequest)
    }
}