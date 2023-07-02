package com.seif.booksislandapp.presentation.admin.user_details

sealed class SuspendState {
    object Init : SuspendState()
    data class ShowError(val message: String) : SuspendState()
    data class UpdatedSuccessfully(val suspendState: Boolean) : SuspendState()
    data class NoInternetConnection(val message: String) : SuspendState()
}
