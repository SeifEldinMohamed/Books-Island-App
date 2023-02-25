package com.seif.booksislandapp.presentation.home.ad_details.auction.sheet

import com.seif.booksislandapp.domain.model.adv.auction.AuctionAdvertisement

sealed class AuctionSheetState {
    object Init : AuctionSheetState()
    data class IsLoading(val isLoading: Boolean) : AuctionSheetState()
    data class ShowError(val message: String) : AuctionSheetState()
    data class FetchAuctionAdByIdSuccessfully(val auctionAd: AuctionAdvertisement) : AuctionSheetState()
    data class AddBidderSuccessfully(val message: String) : AuctionSheetState()
    data class NoInternetConnection(val message: String) : AuctionSheetState()
}