package com.seif.booksislandapp.presentation.admin.user_details.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.seif.booksislandapp.R
import com.seif.booksislandapp.domain.usecase.usecase.admin.GetAllReportsOnUserUseCase
import com.seif.booksislandapp.domain.usecase.usecase.admin.UpdateSuspendSateUseCase
import com.seif.booksislandapp.presentation.admin.reports.AllReportsState
import com.seif.booksislandapp.presentation.admin.user_details.SuspendState
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
class UserDetailsViewModel @Inject constructor(
    private val getAllReportsOnUser: GetAllReportsOnUserUseCase,
    private val updateSuspendSateUseCase: UpdateSuspendSateUseCase,
    private val resourceProvider: ResourceProvider,

) : ViewModel() {
    private var _reportsState = MutableStateFlow<AllReportsState>(AllReportsState.Init)
    val reportsState = _reportsState.asStateFlow()

    private var _suspendState = MutableStateFlow<SuspendState>(SuspendState.Init)
    val suspendState = _suspendState.asStateFlow()

    fun getAllReports(userId: String) {
        setLoading(true)
        viewModelScope.launch(Dispatchers.IO) {
            getAllReportsOnUser(userId).let {
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
                        _reportsState.value = AllReportsState.GetAllReportsSuccessfully(it.data)
                    }
                }
            }
        }
    }

    private fun setLoading(status: Boolean) {
        when (status) {
            true -> {
                _reportsState.value = AllReportsState.IsLoading(true)
            }
            false -> {
                _reportsState.value = AllReportsState.IsLoading(false)
            }
        }
    }

    private fun showError(message: String) {
        when (message) {
            resourceProvider.string(R.string.no_internet_connection) -> {
                _reportsState.value = AllReportsState.NoInternetConnection(message)
            }
            else -> {
                _reportsState.value = AllReportsState.ShowError(message)
            }
        }
    }
    private fun showErrorOfSuspendState(message: String) {
        when (message) {
            resourceProvider.string(R.string.no_internet_connection) -> {
                _suspendState.value = SuspendState.NoInternetConnection(message)
            }
            else -> {
                _suspendState.value = SuspendState.ShowError(message)
            }
        }
    }
    fun handleSuspendState(isSuspended: Boolean, userId: String) {
        viewModelScope.launch(Dispatchers.IO) {
            updateSuspendSateUseCase(isSuspended, userId).let {
                when (it) {
                    is Resource.Error -> showErrorOfSuspendState(it.message)
                    is Resource.Success -> _suspendState.value = SuspendState.UpdatedSuccessfully(it.data)
                }
            }
        }
    }
}