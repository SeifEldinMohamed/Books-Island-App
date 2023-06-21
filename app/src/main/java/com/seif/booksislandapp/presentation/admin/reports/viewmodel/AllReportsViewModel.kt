package com.seif.booksislandapp.presentation.admin.reports.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.seif.booksislandapp.R
import com.seif.booksislandapp.domain.usecase.usecase.admin.GetAllReportsUseCase
import com.seif.booksislandapp.presentation.admin.reports.AllReportsState
import com.seif.booksislandapp.utils.Resource
import com.seif.booksislandapp.utils.ResourceProvider
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import javax.inject.Inject

@HiltViewModel
class AllReportsViewModel @Inject constructor(
    private val resourceProvider: ResourceProvider,
    private val getAllReportsUseCase: GetAllReportsUseCase
) : ViewModel() {
    private var _reportsState = MutableStateFlow<AllReportsState>(AllReportsState.Init)
    val reportsState = _reportsState.asStateFlow()

    fun getAllReports() {
        setLoading(true)
        viewModelScope.launch(Dispatchers.IO) {
            getAllReportsUseCase().collect {
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
}