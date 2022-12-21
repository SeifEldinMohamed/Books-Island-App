package com.seif.booksislandapp.domain.usecase.usecase

import com.seif.booksislandapp.data.repository.AuthRepositoryImp
import com.seif.booksislandapp.utils.Resource
import com.seif.booksislandapp.utils.isValidEmailInput
import javax.inject.Inject

class ForgetPasswordUseCase @Inject constructor(
    private val authRepositoryImp: AuthRepositoryImp
) {
    suspend operator fun invoke(email: String): Resource<String, String> {
        return when (val result = isValidEmailInput(email)) {
            is Resource.Error -> result
            is Resource.Success -> authRepositoryImp.forgetPassword(email)
        }
    }
}