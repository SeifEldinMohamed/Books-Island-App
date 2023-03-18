package com.seif.booksislandapp.presentation.home.wish_list.fragments.buy

import com.seif.booksislandapp.domain.model.adv.sell.SellAdvertisement

sealed class BuyWishListState {
    object Init : BuyWishListState()
    data class IsLoading(val isLoading: Boolean) : BuyWishListState()
    data class ShowError(val message: String) : BuyWishListState()
    data class FetchAllWishBuyItemsSuccessfully(val sellAds: ArrayList<SellAdvertisement>) : BuyWishListState()
    data class NoInternetConnection(val message: String) : BuyWishListState()
}
