package com.seif.booksislandapp.domain.usecase.usecase.advertisement.sell

import com.seif.booksislandapp.data.repository.AdvertisementRepositoryImp
import com.seif.booksislandapp.domain.model.adv.SellAdvertisement
import com.seif.booksislandapp.utils.Resource
import javax.inject.Inject

class FetchRelatedSellAdsUseCase @Inject constructor(
    private val advertisementRepositoryImp: AdvertisementRepositoryImp
) {
    suspend operator fun invoke(
        adId: String,
        category: String
    ): Resource<List<SellAdvertisement>, String> {
        return if (category.isNotEmpty()) {
            advertisementRepositoryImp.fetchRelatedSellAdvertisement(adId, category)
        } else {
            Resource.Error("category is empty!")
        }
    }
}