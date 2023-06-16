package com.seif.booksislandapp.presentation.home.ad_provider_profile.bottom_sheet.report

sealed class ReportSheetState {
    object Init : ReportSheetState()
    data class IsLoading(val isLoading: Boolean) : ReportSheetState()
    data class ShowError(val message: String) : ReportSheetState()
    data class ReportUserSuccessfully(val message: String) : ReportSheetState()
    data class NoInternetConnection(val message: String) : ReportSheetState()
}
