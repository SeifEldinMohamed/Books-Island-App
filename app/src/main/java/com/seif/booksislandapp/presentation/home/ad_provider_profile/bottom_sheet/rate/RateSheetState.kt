package com.seif.booksislandapp.presentation.home.ad_provider_profile.bottom_sheet.rate

sealed class RateSheetState {
    object Init : RateSheetState()
    data class IsLoading(val isLoading: Boolean) : RateSheetState()
    data class ShowError(val message: String) : RateSheetState()
    data class RateUserSuccessfully(val rates: Pair<String, String>) : RateSheetState()
    data class NoInternetConnection(val message: String) : RateSheetState()
}
