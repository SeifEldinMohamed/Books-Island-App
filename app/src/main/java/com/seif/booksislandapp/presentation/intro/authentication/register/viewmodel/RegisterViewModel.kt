package com.seif.booksislandapp.presentation.intro.authentication.register.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.seif.booksislandapp.R
import com.seif.booksislandapp.domain.model.User
import com.seif.booksislandapp.domain.usecase.usecase.RegisterUseCase
import com.seif.booksislandapp.utils.Resource
import com.seif.booksislandapp.utils.ResourceProvider
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import javax.inject.Inject

@HiltViewModel
class RegisterViewModel @Inject constructor(
    private val registerUseCase: RegisterUseCase,
    private val resourceProvider: ResourceProvider
) : ViewModel() {
    private var _registerState = MutableStateFlow<RegisterState>(RegisterState.Init)
    val registerState = _registerState.asStateFlow()
    fun register(user: User) {
        viewModelScope.launch(Dispatchers.IO) {
            registerUseCase.invoke(user).let {
                setLoading(true)
                delay(1000)
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
                            _registerState.value = RegisterState.RegisteredSuccessfully(it.data)
                        }
                    }
                }
            }
        }
    }

    private fun setLoading(status: Boolean) {
        when (status) {
            true -> { _registerState.value = RegisterState.IsLoading(true) }
            false -> { _registerState.value = RegisterState.IsLoading(false) }
        }
    }

    private fun showError(message: String) {
        setLoading(false)
        when (message) {
            resourceProvider.string(R.string.no_internet_connection) -> {
                _registerState.value = RegisterState.NoInternetConnection(message)
            }
            else -> {
                _registerState.value = RegisterState.ShowError(message)
            }
        }
    }
}