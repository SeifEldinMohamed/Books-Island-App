package com.seif.booksislandapp.presentation.home.categories.buy

import com.seif.booksislandapp.domain.model.adv.SellAdvertisement

sealed class BuyState {
    object Init : BuyState()
    data class IsLoading(val isLoading: Boolean) : BuyState()
    data class ShowError(val message: String) : BuyState()
    data class FetchAllSellAdvertisementSuccessfully(val sellAds: ArrayList<SellAdvertisement>) : BuyState()
    data class SearchSellAdvertisementSuccessfully(
        val searchedSellAds: ArrayList<SellAdvertisement>
    ) : BuyState()
    data class NoInternetConnection(val message: String) : BuyState()
}
