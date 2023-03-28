package com.seif.booksislandapp.presentation.home.wish_list.fragments.auction

import com.seif.booksislandapp.domain.model.adv.auction.AuctionAdvertisement

sealed class AuctionWishListState {
    object Init : AuctionWishListState()
    data class IsLoading(val isLoading: Boolean) : AuctionWishListState()
    data class ShowError(val message: String) : AuctionWishListState()
    data class FetchAllWishAuctionItemsSuccessfully(val auctionAds: ArrayList<AuctionAdvertisement>) : AuctionWishListState()
    data class NoInternetConnection(val message: String) : AuctionWishListState()
}
