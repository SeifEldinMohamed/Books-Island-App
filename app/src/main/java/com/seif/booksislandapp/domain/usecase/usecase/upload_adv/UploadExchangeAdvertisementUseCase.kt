package com.seif.booksislandapp.domain.usecase.usecase.upload_adv

import com.seif.booksislandapp.domain.model.adv.exchange.ExchangeAdvertisement
import com.seif.booksislandapp.domain.repository.AdvertisementRepository
import com.seif.booksislandapp.utils.Resource
import com.seif.booksislandapp.utils.checkExchangeAdvertisementUpload
import javax.inject.Inject

class UploadExchangeAdvertisementUseCase @Inject constructor(
    private val advertisementRepository: AdvertisementRepository
) {
    suspend operator fun invoke(exchangeAdvertisement: ExchangeAdvertisement): Resource<String, String> {
        return when (val result = exchangeAdvertisement.checkExchangeAdvertisementUpload()) {
            is Resource.Error -> Resource.Error(result.message)
            is Resource.Success -> advertisementRepository.uploadExchangeAdv(exchangeAdvertisement)
        }
    }
}