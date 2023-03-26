package com.seif.booksislandapp.presentation.home.ad_details.sell

import com.seif.booksislandapp.domain.model.User
import com.seif.booksislandapp.domain.model.adv.sell.SellAdvertisement

sealed class SellDetailsState {
    object Init : SellDetailsState()
    data class IsLoading(val isLoading: Boolean) : SellDetailsState()
    data class ShowError(val message: String) : SellDetailsState()
    data class GetUserByIdSuccessfully(val user: User) : SellDetailsState()
    data class GetCurrentUserByIdSuccessfully(val user: User) : SellDetailsState()
    data class FetchRelatedSellAdvertisementSuccessfully(val relatedAds: List<SellAdvertisement>) : SellDetailsState()
    data class NoInternetConnection(val message: String) : SellDetailsState()
    data class AddedToFavorite(val message: String) : SellDetailsState()
}
