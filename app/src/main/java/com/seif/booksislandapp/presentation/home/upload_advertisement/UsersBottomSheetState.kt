package com.seif.booksislandapp.presentation.home.upload_advertisement

import com.seif.booksislandapp.domain.model.User

sealed class UsersBottomSheetState {
    object Init : UsersBottomSheetState()
    data class IsLoading(val isLoading: Boolean) : UsersBottomSheetState()
    data class ShowError(val message: String) : UsersBottomSheetState()
    data class FetchUsersIChatWithSuccessfully(val usersIChatWith: List<User>) :
        UsersBottomSheetState()

    data class NoInternetConnection(val message: String) : UsersBottomSheetState()
}
