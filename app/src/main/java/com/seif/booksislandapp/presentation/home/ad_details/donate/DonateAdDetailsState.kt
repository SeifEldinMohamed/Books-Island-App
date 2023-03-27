package com.seif.booksislandapp.presentation.home.ad_details.donate

import com.seif.booksislandapp.domain.model.User
import com.seif.booksislandapp.domain.model.adv.donation.DonateAdvertisement

sealed class DonateAdDetailsState {
    object Int : DonateAdDetailsState()
    data class IsLoading(val isLoading: Boolean) : DonateAdDetailsState()
    data class ShowError(val message: String) : DonateAdDetailsState()
    data class GetCurrentUserByIdSuccessfully(val user: User) : DonateAdDetailsState()
    data class GetUserByIdSuccessfully(val user: User) : DonateAdDetailsState()
    data class FetchRelatedDonateAdvertisementSuccessfully(
        val relatedAds: List<DonateAdvertisement>
    ) :
        DonateAdDetailsState()
    data class AddedToFavorite(val message: String) : DonateAdDetailsState()
    data class NoInternetConnection(val message: String) : DonateAdDetailsState()
}
