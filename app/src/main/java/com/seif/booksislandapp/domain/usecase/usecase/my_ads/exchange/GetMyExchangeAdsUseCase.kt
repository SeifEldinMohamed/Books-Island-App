package com.seif.booksislandapp.domain.usecase.usecase.my_ads.exchange

import com.seif.booksislandapp.data.repository.ExchangeAdvertisementRepositoryImp
import com.seif.booksislandapp.domain.model.adv.exchange.ExchangeAdvertisement
import com.seif.booksislandapp.utils.Resource
import javax.inject.Inject

class GetMyExchangeAdsUseCase @Inject constructor(
    private val exchangeAdvertisementRepositoryImp: ExchangeAdvertisementRepositoryImp
) {
    suspend operator fun invoke(userId: String): Resource<ArrayList<ExchangeAdvertisement>, String> {
        return exchangeAdvertisementRepositoryImp.fetchMyExchangeAds(userId)
    }
}