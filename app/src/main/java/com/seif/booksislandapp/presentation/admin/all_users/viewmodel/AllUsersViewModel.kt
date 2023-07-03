package com.seif.booksislandapp.presentation.admin.all_users.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.seif.booksislandapp.R
import com.seif.booksislandapp.domain.usecase.usecase.admin.GetAllUsersUseCase
import com.seif.booksislandapp.presentation.admin.all_users.GetAllUsersState
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
class AllUsersViewModel @Inject constructor(
    private val resourceProvider: ResourceProvider,
    private val getAllUsersUseCase: GetAllUsersUseCase
) : ViewModel() {
    private var _usersState = MutableStateFlow<GetAllUsersState>(GetAllUsersState.Init)
    val usersState = _usersState.asStateFlow()

    fun getAllUsers() {
        setLoading(true)
        viewModelScope.launch(Dispatchers.IO) {
            getAllUsersUseCase().collect {
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
                        }
                        _usersState.value = GetAllUsersState.GetAllUsersSuccessfully(it.data)
                    }
                }
            }
        }
    }

    private fun setLoading(status: Boolean) {
        when (status) {
            true -> {
                _usersState.value = GetAllUsersState.IsLoading(true)
            }
            false -> {
                _usersState.value = GetAllUsersState.IsLoading(false)
            }
        }
    }

    private fun showError(message: String) {
        when (message) {
            resourceProvider.string(R.string.no_internet_connection) -> {
                _usersState.value = GetAllUsersState.NoInternetConnection(message)
            }
            else -> {
                _usersState.value = GetAllUsersState.ShowError(message)
            }
        }
    }
}