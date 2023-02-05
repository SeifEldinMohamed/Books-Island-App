package com.seif.booksislandapp.presentation.intro.authentication.forget_password.viewmodel

sealed class ForgetPasswordState {
    object Init : ForgetPasswordState()
    data class IsLoading(val isLoading: Boolean) : ForgetPasswordState()
    data class ShowError(val message: String) : ForgetPasswordState()
    data class ResetSuccessfully(val message: String) : ForgetPasswordState()
    data class NoInternetConnection(val message: String) : ForgetPasswordState()
}