package com.seif.booksislandapp.domain.usecase.usecase.advertisement.exchange

import com.seif.booksislandapp.domain.model.adv.exchange.ExchangeAdvertisement
import com.seif.booksislandapp.domain.repository.ExchangeAdvertisementRepository
import com.seif.booksislandapp.utils.Resource
import javax.inject.Inject

class GetAllExchangeAdvertisementUseCase @Inject constructor(
    private val exchangeAdvertisementRepository: ExchangeAdvertisementRepository
) {
    suspend operator fun invoke(): Resource<ArrayList<ExchangeAdvertisement>, String> {
        return exchangeAdvertisementRepository.getAllExchangeAdvertisement()
    }
}