package com.seif.booksislandapp.presentation.intro.splash

import androidx.lifecycle.ViewModel
import com.seif.booksislandapp.domain.usecase.usecase.shared_preference.GetFromSharedPreferenceUseCase
import com.seif.booksislandapp.domain.usecase.usecase.shared_preference.SaveInSharedPreferenceUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject

@HiltViewModel
class SplashViewModel @Inject constructor(
    private val saveInSharedPreferenceUseCase: SaveInSharedPreferenceUseCase,
    private val getFromSharedPreferenceUseCase: GetFromSharedPreferenceUseCase
) : ViewModel() {

    fun <T> saveInSP(key: String, data: T) {
        saveInSharedPreferenceUseCase(key, data)
    }

    fun <T> getFromSP(key: String, clazz: Class<T>): T {
        return getFromSharedPreferenceUseCase(key, clazz)
    }
}