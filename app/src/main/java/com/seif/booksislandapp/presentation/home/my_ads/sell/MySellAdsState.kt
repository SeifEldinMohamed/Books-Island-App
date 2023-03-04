package com.seif.booksislandapp.presentation.home.my_ads.sell

import com.seif.booksislandapp.domain.model.adv.sell.SellAdvertisement

sealed class MySellAdsState {
    object Init : MySellAdsState()
    data class IsLoading(val isLoading: Boolean) : MySellAdsState()
    data class ShowError(val errorMessage: String) : MySellAdsState()
    data class FetchAllMySellAdsSuccessfully(val sellAds: ArrayList<SellAdvertisement>) :
        MySellAdsState()

    data class NoInternetConnection(val message: String) : MySellAdsState()
}
