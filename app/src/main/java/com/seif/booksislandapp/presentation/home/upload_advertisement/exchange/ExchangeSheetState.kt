package com.seif.booksislandapp.presentation.home.upload_advertisement.exchange

import com.seif.booksislandapp.domain.model.book.BooksToExchange

sealed class ExchangeSheetState {
    object Init : ExchangeSheetState()
    data class IsLoading(val isLoading: Boolean) : ExchangeSheetState()
    data class ShowError(val message: String) : ExchangeSheetState()
    data class ValidBookToExchangeData(val booksToExchange: BooksToExchange) : ExchangeSheetState()
}
