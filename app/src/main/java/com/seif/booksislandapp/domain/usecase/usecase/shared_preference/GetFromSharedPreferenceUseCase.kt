package com.seif.booksislandapp.domain.usecase.usecase.shared_preference

import com.seif.booksislandapp.domain.repository.AuthRepository
import javax.inject.Inject

class GetFromSharedPreferenceUseCase @Inject constructor(
    private val authRepository: AuthRepository
) {
    operator fun <T> invoke(key: String, clazz: Class<T>): T {
        return authRepository.getFromSharedPreference(key, clazz)
    }
}