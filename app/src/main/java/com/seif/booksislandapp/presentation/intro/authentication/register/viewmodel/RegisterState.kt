package com.seif.booksislandapp.presentation.intro.authentication.register.viewmodel

sealed class RegisterState {
    object Init : RegisterState()
    data class IsLoading(val isLoading: Boolean) : RegisterState()
    data class ShowError(val message: String) : RegisterState()
    data class RegisteredSuccessfully(val message: String) : RegisterState()
    data class NoInternetConnection(val message: String) : RegisterState()
}