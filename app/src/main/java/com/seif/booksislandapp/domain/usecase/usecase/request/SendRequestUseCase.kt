package com.seif.booksislandapp.domain.usecase.usecase.request

import com.seif.booksislandapp.data.repository.RequestsRepositoryImp
import com.seif.booksislandapp.domain.model.request.MyRequest
import com.seif.booksislandapp.utils.Resource
import javax.inject.Inject

class SendRequestUseCase @Inject constructor(
    private val requestRepositoryImp: RequestsRepositoryImp
) {
    suspend operator fun invoke(myRequest: MyRequest): Resource<String, String> {
        return requestRepositoryImp.sendRequest(myRequest)
    }
}