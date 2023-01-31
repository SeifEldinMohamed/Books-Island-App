package com.seif.booksislandapp.domain.usecase.usecase.upload_adv

import com.seif.booksislandapp.domain.model.adv.SellAdvertisement
import com.seif.booksislandapp.domain.repository.AdvertisementRepository
import com.seif.booksislandapp.utils.Resource
import com.seif.booksislandapp.utils.checkSellAdvertisementUpload
import javax.inject.Inject

class UploadSellAdvertisementUseCase @Inject constructor(
    private val advertisementRepository: AdvertisementRepository
) {
    suspend operator fun invoke(sellAdvertisement: SellAdvertisement): Resource<String, String> {
        return when (val result = sellAdvertisement.checkSellAdvertisementUpload()) {
            is Resource.Error -> Resource.Error(result.message)
            is Resource.Success -> advertisementRepository.uploadSellAdv(sellAdvertisement)
        }
    }
}