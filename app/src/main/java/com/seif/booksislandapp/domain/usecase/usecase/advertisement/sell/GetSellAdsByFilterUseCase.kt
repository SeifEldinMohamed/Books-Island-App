package com.seif.booksislandapp.domain.usecase.usecase.advertisement.sell

import com.seif.booksislandapp.data.repository.AdvertisementRepositoryImp
import com.seif.booksislandapp.domain.model.adv.sell.SellAdvertisement
import com.seif.booksislandapp.utils.Resource
import javax.inject.Inject

class GetSellAdsByFilterUseCase @Inject constructor(
    private val advertisementRepositoryImp: AdvertisementRepositoryImp
) {
    suspend operator fun invoke(
        category: String?,
        governorate: String?,
        district: String?,
        condition: String?
    ): Resource<ArrayList<SellAdvertisement>, String> {
        return advertisementRepositoryImp.getSellAdsByFilter(category, governorate, district, condition)
    }
}