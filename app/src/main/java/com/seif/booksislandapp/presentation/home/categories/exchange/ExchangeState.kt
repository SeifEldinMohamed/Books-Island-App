package com.seif.booksislandapp.presentation.home.categories.exchange

import com.seif.booksislandapp.domain.model.adv.exchange.ExchangeAdvertisement

sealed class ExchangeState {
    object Init : ExchangeState()
    data class IsLoading(val isLoading: Boolean) : ExchangeState()
    data class ShowError(val error: String) : ExchangeState()
    data class FetchAllExchangeAdsSuccessfully(val exchangeAds: ArrayList<ExchangeAdvertisement>) : ExchangeState()
    data class SearchExchangeAdsSuccessfully(
        val searchExchangeAds: ArrayList<ExchangeAdvertisement>
    ) : ExchangeState()
    data class NoInternetConnection(val message: String) : ExchangeState()
}
