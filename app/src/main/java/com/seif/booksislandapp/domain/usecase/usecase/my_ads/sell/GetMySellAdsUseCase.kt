package com.seif.booksislandapp.domain.usecase.usecase.my_ads.sell

import com.seif.booksislandapp.data.repository.AdvertisementRepositoryImp
import com.seif.booksislandapp.domain.model.adv.sell.SellAdvertisement
import com.seif.booksislandapp.utils.Resource
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject

class GetMySellAdsUseCase @Inject constructor(
    private val advertisementRepositoryImp: AdvertisementRepositoryImp
) {
    suspend operator fun invoke(userId: String): Flow<Resource<ArrayList<SellAdvertisement>, String>> {
        return advertisementRepositoryImp.fetchMySellAds(userId)
    }
}