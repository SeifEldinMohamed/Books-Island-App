package com.seif.booksislandapp.presentation.admin.reports

import com.seif.booksislandapp.domain.model.Report

sealed class AllReportsState {
    object Init : AllReportsState()
    data class IsLoading(val isLoading: Boolean) : AllReportsState()
    data class ShowError(val message: String) : AllReportsState()
    data class GetAllReportsSuccessfully(val reports: ArrayList<Report>) : AllReportsState()
    data class NoInternetConnection(val message: String) : AllReportsState()
}
