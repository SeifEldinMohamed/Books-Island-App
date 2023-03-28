package com.seif.booksislandapp.presentation.home.wish_list.fragments.exchange

import com.seif.booksislandapp.domain.model.adv.exchange.ExchangeAdvertisement

sealed class ExchangeWishListState {
    object Init : ExchangeWishListState()
    data class IsLoading(val isLoading: Boolean) : ExchangeWishListState()
    data class ShowError(val message: String) : ExchangeWishListState()
    data class FetchAllWishExchangeItemsSuccessfully(
        val exchangeAds: ArrayList<ExchangeAdvertisement>
    ) : ExchangeWishListState()
    data class NoInternetConnection(val message: String) : ExchangeWishListState()
}
