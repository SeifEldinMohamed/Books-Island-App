package com.seif.booksislandapp.domain.usecase.usecase

import com.seif.booksislandapp.data.repository.AuthRepositoryImp
import com.seif.booksislandapp.utils.Resource
import com.seif.booksislandapp.utils.isValidEmailAndPassword
import javax.inject.Inject

class LoginUseCase @Inject constructor(
    private var authRepositoryImp: AuthRepositoryImp
) {
    suspend operator fun invoke(email: String, password: String): Resource<String, String> {
        return when (val result = isValidEmailAndPassword(email, password)) {
            is Resource.Error -> Resource.Error(result.message)
            is Resource.Success -> authRepositoryImp.login(email, password)
        }
    }
}