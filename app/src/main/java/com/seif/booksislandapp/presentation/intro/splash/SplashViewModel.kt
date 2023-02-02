package com.seif.booksislandapp.presentation.intro.splash

import androidx.lifecycle.ViewModel
import com.seif.booksislandapp.domain.usecase.usecase.auth.GetFromSharedPreference
import com.seif.booksislandapp.domain.usecase.usecase.auth.SaveInSharedPreference
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject

@HiltViewModel
class SplashViewModel @Inject constructor(
    private val saveInSharedPreference: SaveInSharedPreference,
    private val getFromSharedPreference: GetFromSharedPreference
) : ViewModel() {

    fun <T> saveInSP(key: String, data: T) {
        saveInSharedPreference(key, data)
    }

    fun <T> getFromSP(key: String, clazz: Class<T>): T {
        return getFromSharedPreference(key, clazz)
    }

}