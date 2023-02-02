package com.seif.booksislandapp.domain.usecase.usecase.auth

import com.seif.booksislandapp.domain.repository.AuthRepository
import javax.inject.Inject

class GetFromSharedPreference @Inject constructor(
    private val authRepository: AuthRepository
) {
    operator fun <T> invoke(key: String, clazz: Class<T>): T {
        return authRepository.getFromSharedPreference(key, clazz)
    }
}