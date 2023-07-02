package com.seif.booksislandapp.presentation.admin.reports.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.seif.booksislandapp.R
import com.seif.booksislandapp.domain.usecase.usecase.admin.GetAllReportsUseCase
import com.seif.booksislandapp.domain.usecase.usecase.admin.SetReviewedUseCase
import com.seif.booksislandapp.presentation.admin.reports.AllReportsState
import com.seif.booksislandapp.presentation.admin.reports.ReviewedState
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
    private val getAllReportsUseCase: GetAllReportsUseCase,
    private val setReviewedUseCase: SetReviewedUseCase
) : ViewModel() {
    private var _reportsState = MutableStateFlow<AllReportsState>(AllReportsState.Init)
    val reportsState = _reportsState.asStateFlow()

    private var _reportReviewState = MutableStateFlow<ReviewedState>(ReviewedState.Init)
    val reportReviewState = _reportReviewState.asStateFlow()

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

    private fun showErrorReviewReport(message: String) {
        when (message) {
            resourceProvider.string(R.string.no_internet_connection) -> {
                _reportReviewState.value = ReviewedState.NoInternetConnection(message)
            }
            else -> {
                _reportReviewState.value = ReviewedState.ShowError(message)
            }
        }
    }

    fun setReviewed(reportId: String) {
        viewModelScope.launch(Dispatchers.IO) {
            setReviewedUseCase(reportId).let {
                when (it) {
                    is Resource.Error -> showErrorReviewReport(it.message)
                    is Resource.Success ->
                        _reportReviewState.value =
                            ReviewedState.UpdatedSuccessfully(it.data)
                }
            }
        }
    }
}