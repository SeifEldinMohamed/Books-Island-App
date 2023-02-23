package com.seif.booksislandapp.domain.usecase.usecase.advertisement.donate

import com.seif.booksislandapp.data.repository.AdvertisementRepositoryImp
import com.seif.booksislandapp.domain.model.adv.donation.DonateAdvertisement
import com.seif.booksislandapp.utils.Resource
import javax.inject.Inject

class FetchAllDonateRelatedAdvertisementsUseCase @Inject constructor(
    private val advertisementRepositoryImp: AdvertisementRepositoryImp
) {

    suspend operator fun invoke(
        adId: String,
        category: String
    ): Resource<List<DonateAdvertisement>, String> {
        return if (category.isNotEmpty()) {
            advertisementRepositoryImp.fetchRelatedDonateAdvertisement(adId, category)
        } else {
            Resource.Error("category is empty!")
        }
    }
}