package com.seif.booksislandapp.presentation.home.ad_details.exchange

import com.seif.booksislandapp.domain.model.User
import com.seif.booksislandapp.domain.model.adv.exchange.ExchangeAdvertisement

sealed class ExchangeDetailsState {
    object Init : ExchangeDetailsState()
    data class IsLoading(val isLoading: Boolean) : ExchangeDetailsState()
    data class ShowError(val message: String) : ExchangeDetailsState()
    data class GetUserByIdSuccessfully(val user: User) : ExchangeDetailsState()
    data class GetCurrentUserByIdSuccessfully(val user: User) : ExchangeDetailsState()
    data class FetchRelatedExchangeAdvertisementSuccessfully(
        val relatedAds: List<ExchangeAdvertisement>
    ) : ExchangeDetailsState()
    data class NoInternetConnection(val message: String) : ExchangeDetailsState()
    data class AddedToFavorite(val message: String) : ExchangeDetailsState()
}
