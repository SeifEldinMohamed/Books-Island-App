package com.seif.booksislandapp.presentation.intro.authentication.login.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.seif.booksislandapp.R
import com.seif.booksislandapp.domain.usecase.usecase.auth.LoginUseCase
import com.seif.booksislandapp.utils.Resource
import com.seif.booksislandapp.utils.ResourceProvider
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import javax.inject.Inject

@HiltViewModel
class LoginViewModel @Inject constructor(
    private val loginUseCase: LoginUseCase,
    private val resourceProvider: ResourceProvider
) : ViewModel() {
    private var _loginState = MutableStateFlow<LoginState>(LoginState.Init)
    val loginState = _loginState.asStateFlow()
    fun login(email: String, password: String) {
        setLoading(true)
        viewModelScope.launch(Dispatchers.IO) {
            loginUseCase.invoke(email, password).let {
                when (it) {
                    is Resource.Error -> {
                        withContext(Dispatchers.Main) {
                            setLoading(false)
                            showError(it.message)
                        }
                    }
                    is Resource.Success -> {
                        withContext(Dispatchers.Main) {
                            setLoading(false)
                            _loginState.value = LoginState.LoginSuccessfully(it.data)
                        }
                    }
                }
            }
        }
    }

    private fun showError(message: String) {
        when (message) {
            resourceProvider.string(R.string.no_internet_connection) -> {
                _loginState.value = LoginState.NoInternetConnection(message)
            }
            else -> {
                _loginState.value = LoginState.ShowError(message)
            }
        }
    }

    private fun setLoading(status: Boolean) {
        when (status) {
            true -> { _loginState.value = LoginState.IsLoading(true) }
            false -> { _loginState.value = LoginState.IsLoading(false) }
        }
    }
}