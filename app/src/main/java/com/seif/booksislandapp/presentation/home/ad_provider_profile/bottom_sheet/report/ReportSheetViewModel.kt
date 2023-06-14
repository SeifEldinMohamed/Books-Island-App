package com.seif.booksislandapp.presentation.home.ad_provider_profile.bottom_sheet.report

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.seif.booksislandapp.R
import com.seif.booksislandapp.domain.model.Report
import com.seif.booksislandapp.domain.usecase.usecase.user.ReportUserUseCase
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
class ReportSheetViewModel @Inject constructor(
    private val reportUserUseCase: ReportUserUseCase,
    private val resourceProvider: ResourceProvider,
) : ViewModel() {

    private var _reportSheetState =
        MutableStateFlow<ReportSheetState>(ReportSheetState.Init)
    val reportSheetState = _reportSheetState.asStateFlow()

    private val mutableReportSent = MutableLiveData<String>()
    val reportSent: LiveData<String> get() = mutableReportSent

    fun reportUser(report: Report) {
        setLoading(true)
        viewModelScope.launch(Dispatchers.IO) {
            reportUserUseCase(report).let {
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
                            _reportSheetState.value =
                                ReportSheetState.ReportUserSuccessfully(it.data)
                        }
                    }
                }
            }
        }
    }

    private fun setLoading(status: Boolean) {
        when (status) {
            true -> {
                _reportSheetState.value = ReportSheetState.IsLoading(true)
            }

            false -> {
                _reportSheetState.value = ReportSheetState.IsLoading(false)
            }
        }
    }

    private fun showError(message: String) {
        when (message) {
            resourceProvider.string(R.string.no_internet_connection) -> {
                _reportSheetState.value = ReportSheetState.NoInternetConnection(message)
            }

            else -> {
                _reportSheetState.value = ReportSheetState.ShowError(message)
            }
        }
    }

    fun updateReportState(message: String) {
        mutableReportSent.value = message
    }
}