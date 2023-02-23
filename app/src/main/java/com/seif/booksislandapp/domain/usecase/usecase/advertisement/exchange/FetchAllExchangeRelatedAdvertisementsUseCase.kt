package com.seif.booksislandapp.domain.usecase.usecase.advertisement.exchange

import com.seif.booksislandapp.data.repository.ExchangeAdvertisementRepositoryImp
import com.seif.booksislandapp.domain.model.adv.exchange.ExchangeAdvertisement
import com.seif.booksislandapp.utils.Resource
import javax.inject.Inject

class FetchAllExchangeRelatedAdvertisementsUseCase @Inject constructor(
    private val exchangeAdvertisementRepositoryImp: ExchangeAdvertisementRepositoryImp
) {
    suspend operator fun invoke(
        adId: String,
        category: String
    ): Resource<List<ExchangeAdvertisement>, String> {
        return if (category.isNotEmpty()) {
            exchangeAdvertisementRepositoryImp.fetchRelatedExchangeAdvertisement(adId, category)
        } else {
            Resource.Error("category is empty!")
        }
    }
}