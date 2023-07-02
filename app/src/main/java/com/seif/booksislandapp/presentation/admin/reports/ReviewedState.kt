package com.seif.booksislandapp.presentation.admin.reports

sealed class ReviewedState {
    object Init : ReviewedState()
    data class ShowError(val message: String) : ReviewedState()
    data class UpdatedSuccessfully(val reviewed: String) : ReviewedState()
    data class NoInternetConnection(val message: String) : ReviewedState()
}
