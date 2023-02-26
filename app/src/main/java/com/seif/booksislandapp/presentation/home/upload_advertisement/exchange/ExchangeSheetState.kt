package com.seif.booksislandapp.presentation.home.upload_advertisement.exchange

sealed class ExchangeSheetState {
    object Init : ExchangeSheetState()
    data class IsLoading(val isLoading: Boolean) : ExchangeSheetState()
    data class ShowError(val message: String) : ExchangeSheetState()
}
