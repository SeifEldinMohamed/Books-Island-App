package com.seif.booksislandapp.presentation.home.categories.auction

import com.seif.booksislandapp.domain.model.adv.auction.AuctionAdvertisement

sealed class AuctionState {
    object Init : AuctionState()
    data class IsLoading(val isLoading: Boolean) : AuctionState()
    data class ShowError(val message: String) : AuctionState()
    data class FetchAllAuctionsAdsSuccessfully(val auctionAds: ArrayList<AuctionAdvertisement>) : AuctionState()
    data class SearchAuctionsAdsSuccessfully(
        val searchedAuctionsAds: ArrayList<AuctionAdvertisement>
    ) : AuctionState()
    data class NoInternetConnection(val message: String) : AuctionState()
}
