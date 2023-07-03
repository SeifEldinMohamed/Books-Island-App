package com.seif.booksislandapp.presentation.admin.all_users

import com.seif.booksislandapp.domain.model.User

sealed class GetAllUsersState {
    object Init : GetAllUsersState()
    data class IsLoading(val isLoading: Boolean) : GetAllUsersState()
    data class ShowError(val message: String) : GetAllUsersState()
    data class GetAllUsersSuccessfully(val users: ArrayList<User>) : GetAllUsersState()
    data class NoInternetConnection(val message: String) : GetAllUsersState()
}