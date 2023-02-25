package com.seif.booksislandapp.presentation.home.home

import androidx.lifecycle.ViewModel
import com.seif.booksislandapp.domain.usecase.usecase.shared_preference.GetFromSharedPreferenceUseCase
import dagger.hilt.android.lifecycle.HiltViewModel

import javax.inject.Inject

@HiltViewModel
class HomeViewModel @Inject constructor(
    private val getFromSharedPreferenceUseCase: GetFromSharedPreferenceUseCase
) : ViewModel() {
    fun <T> readFromSP(key: String, clazz: Class<T>): T {
        return getFromSharedPreferenceUseCase(key = key, clazz = clazz)
    }
}