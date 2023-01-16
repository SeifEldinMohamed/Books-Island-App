package com.seif.booksislandapp.domain.usecase.usecase.auth

import com.seif.booksislandapp.data.repository.AuthRepositoryImp
import com.seif.booksislandapp.utils.Resource
import javax.inject.Inject

class LogoutUseCase @Inject constructor(
    private val authRepositoryImp: AuthRepositoryImp
) {
    suspend operator fun invoke(): Resource<String, String> {
        return authRepositoryImp.logout()
    }
}