package com.seif.booksislandapp.domain.usecase.usecase.wish_list

import com.seif.booksislandapp.data.repository.AdvertisementRepositoryImp
import com.seif.booksislandapp.domain.model.adv.donation.DonateAdvertisement
import com.seif.booksislandapp.utils.Resource
import javax.inject.Inject

class GetAllDonateWishListUseCase @Inject constructor(
    private val advertisementRepositoryImp: AdvertisementRepositoryImp
) {
    suspend operator fun invoke(donateIdList: List<String>): Resource<ArrayList<DonateAdvertisement>, String> {
        return advertisementRepositoryImp.fetchDonateWishListAds(donateIdList)
    }
}