package com.seif.booksislandapp.presentation.home.home

import androidx.lifecycle.ViewModel
import com.seif.booksislandapp.domain.usecase.usecase.auth.GetFromSharedPreference
import dagger.hilt.android.lifecycle.HiltViewModel

import javax.inject.Inject

@HiltViewModel
class HomeViewModel @Inject constructor(
    private val getFromSharedPreference: GetFromSharedPreference
) : ViewModel() {
    fun <T> readFromSP(key: String, clazz: Class<T>): T {
        return getFromSharedPreference(key = key, clazz = clazz)
    }
}