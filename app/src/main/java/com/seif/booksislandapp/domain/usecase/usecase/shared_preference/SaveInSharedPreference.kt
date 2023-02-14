package com.seif.booksislandapp.domain.usecase.usecase.shared_preference

import com.seif.booksislandapp.domain.repository.AuthRepository
import javax.inject.Inject

class SaveInSharedPreference @Inject constructor(
    private val authRepository: AuthRepository
) {
    operator fun <T> invoke(key: String, data: T) {
        authRepository.saveInSharedPreference(key, data)
    }
}