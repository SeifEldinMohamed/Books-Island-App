package com.seif.booksislandapp.domain.usecase.usecase.my_ads.donate

import com.seif.booksislandapp.data.repository.AdvertisementRepositoryImp
import com.seif.booksislandapp.domain.model.adv.donation.DonateAdvertisement
import com.seif.booksislandapp.utils.Resource
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject

class GetMyDonateAdsUseCase @Inject constructor(
    private val advertisementRepositoryImp: AdvertisementRepositoryImp
) {
    suspend operator fun invoke(userId: String): Flow<Resource<ArrayList<DonateAdvertisement>, String>> {
        return advertisementRepositoryImp.fetchMyDonateAds(userId)
    }
}