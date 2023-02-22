package com.seif.booksislandapp.domain.usecase.usecase.advertisement.exchange

import com.seif.booksislandapp.data.repository.ExchangeAdvertisementRepositoryImp
import com.seif.booksislandapp.domain.model.adv.exchange.ExchangeAdvertisement
import com.seif.booksislandapp.utils.Resource
import javax.inject.Inject

class GetAllExchangeAdvertisementUseCase @Inject constructor(
    private val exchangeAdvertisementRepositoryImp: ExchangeAdvertisementRepositoryImp
) {
    suspend operator fun invoke(): Resource<ArrayList<ExchangeAdvertisement>, String> {
        return exchangeAdvertisementRepositoryImp.getAllExchangeAdvertisement()
    }
}