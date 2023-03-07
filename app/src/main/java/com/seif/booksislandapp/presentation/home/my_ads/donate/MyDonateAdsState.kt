package com.seif.booksislandapp.presentation.home.my_ads.donate

import com.seif.booksislandapp.domain.model.adv.donation.DonateAdvertisement

sealed class MyDonateAdsState {
    object Init : MyDonateAdsState()
    data class IsLoading(val isLoading: Boolean) : MyDonateAdsState()
    data class ShowError(val errorMessage: String) : MyDonateAdsState()
    data class FetchAllMyDonateAdsSuccessfully(val donateAds: ArrayList<DonateAdvertisement>) :
        MyDonateAdsState()

    data class NoInternetConnection(val message: String) : MyDonateAdsState()
}