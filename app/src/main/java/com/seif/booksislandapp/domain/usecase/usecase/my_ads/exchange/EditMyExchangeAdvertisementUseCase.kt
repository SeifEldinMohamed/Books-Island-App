package com.seif.booksislandapp.domain.usecase.usecase.my_ads.exchange

import com.seif.booksislandapp.data.repository.ExchangeAdvertisementRepositoryImp
import com.seif.booksislandapp.domain.model.adv.exchange.ExchangeAdvertisement
import com.seif.booksislandapp.utils.Resource
import com.seif.booksislandapp.utils.checkExchangeAdvertisementUpload
import javax.inject.Inject

class EditMyExchangeAdvertisementUseCase @Inject constructor(
    private val exchangeAdvertisementRepositoryImp: ExchangeAdvertisementRepositoryImp
) {
    suspend operator fun invoke(exchangeAdvertisement: ExchangeAdvertisement): Resource<String, String> {
        return when (val result = exchangeAdvertisement.checkExchangeAdvertisementUpload()) {
            is Resource.Error -> Resource.Error(result.message)
            is Resource.Success -> exchangeAdvertisementRepositoryImp.editMyExchangeAdv(
                exchangeAdvertisement
            )
        }
    }
}