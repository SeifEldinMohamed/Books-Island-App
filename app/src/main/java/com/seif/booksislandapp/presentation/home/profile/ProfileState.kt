package com.seif.booksislandapp.presentation.home.profile

import com.seif.booksislandapp.domain.model.User
import com.seif.booksislandapp.domain.model.auth.District
import com.seif.booksislandapp.domain.model.auth.Governorate

sealed class ProfileState {
    object Init : ProfileState()
    data class IsLoading(val isLoading: Boolean) : ProfileState()
    data class ShowError(val message: String) : ProfileState()
    data class GetGovernoratesSuccessfully(val governorates: List<Governorate>) : ProfileState()
    data class GetDistrictsSuccessfully(val districts: List<District>) : ProfileState()
    data class UpdateUserProfileSuccessfully(val user: User) : ProfileState()
    data class GetUserByIdSuccessfully(val user: User) : ProfileState()
    data class LogoutSuccessfully(val message: String) : ProfileState()
    data class NoInternetConnection(val message: String) : ProfileState()
}
