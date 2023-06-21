package com.seif.booksislandapp.presentation.admin.report_details

import com.seif.booksislandapp.domain.model.User

sealed class GetUserByIdState {
    object Init : GetUserByIdState()
    data class IsLoading(val isLoading: Boolean) : GetUserByIdState()
    data class ShowError(val message: String) : GetUserByIdState()
    data class GetUserByIdSuccessfully(val user: User) : GetUserByIdState()
    data class NoInternetConnection(val message: String) : GetUserByIdState()
}
