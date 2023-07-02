package com.seif.booksislandapp.presentation.home.bidders_history

import com.seif.booksislandapp.domain.model.adv.auction.AuctionAdvertisement

sealed class BiddersHistoryState {
    object Init : BiddersHistoryState()
    data class IsLoading(val isLoading: Boolean) : BiddersHistoryState()
    data class ShowError(val message: String) : BiddersHistoryState()
    data class FetchAuctionAdByIdSuccessfully(val auctionAd: AuctionAdvertisement) :
        BiddersHistoryState()

    data class NoInternetConnection(val message: String) : BiddersHistoryState()
}