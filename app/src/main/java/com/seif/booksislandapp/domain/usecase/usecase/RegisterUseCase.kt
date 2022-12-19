package com.seif.booksislandapp.domain.usecase.usecase

import com.seif.booksislandapp.domain.model.User
import com.seif.booksislandapp.domain.repository.AuthRepository
import com.seif.booksislandapp.utils.Resource
import com.seif.booksislandapp.utils.isValidUser
import javax.inject.Inject

class RegisterUseCase @Inject constructor(
    private val authRepository: AuthRepository
) {
    suspend operator fun invoke(user: User): Resource<String, String> {
        return when (val result = user.isValidUser()) {
            is Resource.Error -> result
            is Resource.Success -> authRepository.register(user)
        }
    }
}