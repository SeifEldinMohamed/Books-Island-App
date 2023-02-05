package com.seif.booksislandapp.presentation.intro.authentication.register.viewmodel

import com.seif.booksislandapp.domain.model.auth.District
import com.seif.booksislandapp.domain.model.auth.Governorate

sealed class RegisterState {
    object Init : RegisterState()
    data class IsLoading(val isLoading: Boolean) : RegisterState()
    data class ShowError(val message: String) : RegisterState()
    data class RegisteredSuccessfully(val message: String) : RegisterState()
    data class GetGovernoratesSuccessfully(val governorates: List<Governorate>) : RegisterState()
    data class GetDistrictsSuccessfully(val districts: List<District>) : RegisterState()
    data class NoInternetConnection(val message: String) : RegisterState()
}