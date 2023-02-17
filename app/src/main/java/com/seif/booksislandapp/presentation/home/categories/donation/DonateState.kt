package com.seif.booksislandapp.presentation.home.categories.donation

import com.seif.booksislandapp.domain.model.adv.donation.DonateAdvertisement
sealed class DonateState {
    object Init : DonateState()
    data class IsLoading(val isLoading: Boolean) : DonateState()
    data class ShowError(val message: String) : DonateState()
    data class FetchAllDonateAdvertisementSuccessfully(
        val donateAds: ArrayList<DonateAdvertisement>
    ) : DonateState()
    data class SearchDonateAdvertisementSuccessfully(
        val searchedDonateAds: ArrayList<DonateAdvertisement>
    ) : DonateState()
    data class NoInternetConnection(val message: String) : DonateState()
}
