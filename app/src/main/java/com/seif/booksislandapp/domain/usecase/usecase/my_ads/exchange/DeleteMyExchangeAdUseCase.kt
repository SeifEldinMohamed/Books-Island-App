package com.seif.booksislandapp.domain.usecase.usecase.my_ads.exchange

import com.seif.booksislandapp.data.repository.ExchangeAdvertisementRepositoryImp
import com.seif.booksislandapp.utils.Resource
import javax.inject.Inject

class DeleteMyExchangeAdUseCase @Inject constructor(
    private val exchangeAdvertisementRepositoryImp: ExchangeAdvertisementRepositoryImp
) {
    suspend operator fun invoke(myExchangeAdId: String): Resource<String, String> {
        return exchangeAdvertisementRepositoryImp.deleteMyExchangeAdv(myExchangeAdId)
    }
}