package com.seif.booksislandapp.presentation.home.my_ads.exchange

import com.seif.booksislandapp.domain.model.adv.exchange.ExchangeAdvertisement

sealed class MyExchangeAdsState {
    object Init : MyExchangeAdsState()
    data class IsLoading(val isLoading: Boolean) : MyExchangeAdsState()
    data class ShowError(val errorMessage: String) : MyExchangeAdsState()
    data class FetchAllMyExchangeAdsSuccessfully(val exchangeAds: ArrayList<ExchangeAdvertisement>) :
        MyExchangeAdsState()

    data class NoInternetConnection(val message: String) : MyExchangeAdsState()
}
