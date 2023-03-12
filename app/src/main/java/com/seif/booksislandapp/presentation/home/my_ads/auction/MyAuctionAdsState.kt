package com.seif.booksislandapp.presentation.home.my_ads.auction

import com.seif.booksislandapp.domain.model.adv.auction.AuctionAdvertisement

sealed class MyAuctionAdsState {
    object Init : MyAuctionAdsState()
    data class IsLoading(val isLoading: Boolean) : MyAuctionAdsState()
    data class ShowError(val errorMessage: String) : MyAuctionAdsState()
    data class FetchAllMyAuctionAdsSuccessfully(val auctionAds: ArrayList<AuctionAdvertisement>) :
        MyAuctionAdsState()

    data class NoInternetConnection(val message: String) : MyAuctionAdsState()
}
