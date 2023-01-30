package com.seif.booksislandapp.presentation.intro.authentication.login.viewmodel
sealed class LoginState {
    object Init : LoginState()
    data class IsLoading(val isLoading: Boolean) : LoginState()
    data class ShowError(val message: String) : LoginState()
    data class LoginSuccessfully(val message: String) : LoginState()
    data class NoInternetConnection(val message: String) : LoginState()
}
