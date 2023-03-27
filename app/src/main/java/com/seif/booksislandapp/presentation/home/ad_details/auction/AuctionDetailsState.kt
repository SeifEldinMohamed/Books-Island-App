package com.seif.booksislandapp.presentation.home.ad_details.auction

import com.seif.booksislandapp.domain.model.User
import com.seif.booksislandapp.domain.model.adv.auction.AuctionAdvertisement

sealed class AuctionDetailsState {
    object Init : AuctionDetailsState()
    data class IsLoading(val isLoading: Boolean) : AuctionDetailsState()
    data class ShowError(val message: String) : AuctionDetailsState()
    data class GetCurrentUserByIdSuccessfully(val user: User) : AuctionDetailsState()
    data class GetUserByIdSuccessfully(val user: User) : AuctionDetailsState()
    data class FetchRelatedAuctionAdvertisementSuccessfully(
        val relatedAds: List<AuctionAdvertisement>
    ) : AuctionDetailsState()
    data class NoInternetConnection(val message: String) : AuctionDetailsState()
    data class AddedToFavorite(val message: String) : AuctionDetailsState()
}
