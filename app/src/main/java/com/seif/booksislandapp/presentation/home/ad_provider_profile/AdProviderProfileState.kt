package com.seif.booksislandapp.presentation.home.ad_provider_profile

import com.seif.booksislandapp.domain.model.User

sealed class AdProviderProfileState {
    object Init : AdProviderProfileState()
    data class IsLoading(val isLoading: Boolean) : AdProviderProfileState()
    data class ShowError(val message: String) : AdProviderProfileState()
    data class BlockUserSuccessfully(val message: String) : AdProviderProfileState()
    data class FetchAdProviderUserSuccessfully(val user: User) : AdProviderProfileState()
    data class FetchCurrentUserSuccessfully(val user: User) : AdProviderProfileState()
    data class NoInternetConnection(val message: String) : AdProviderProfileState()
}