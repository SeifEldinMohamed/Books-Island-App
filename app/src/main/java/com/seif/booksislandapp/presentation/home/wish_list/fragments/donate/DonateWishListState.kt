package com.seif.booksislandapp.presentation.home.wish_list.fragments.donate

import com.seif.booksislandapp.domain.model.adv.donation.DonateAdvertisement

sealed class DonateWishListState {
    object Init : DonateWishListState()
    data class IsLoading(val isLoading: Boolean) : DonateWishListState()
    data class ShowError(val message: String) : DonateWishListState()
    data class FetchAllWishDonateItemsSuccessfully(val donateAds: ArrayList<DonateAdvertisement>) : DonateWishListState()
    data class NoInternetConnection(val message: String) : DonateWishListState()
}
