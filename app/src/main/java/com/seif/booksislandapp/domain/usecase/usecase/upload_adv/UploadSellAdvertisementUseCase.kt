package com.seif.booksislandapp.domain.usecase.usecase.upload_adv

import com.seif.booksislandapp.domain.model.Advertisement
import com.seif.booksislandapp.domain.repository.AdvertisementRepository
import javax.inject.Inject

class UploadSellAdvertisementUseCase @Inject constructor(
    private val advertisementRepository: AdvertisementRepository
) {
    suspend operator fun invoke(sellAdvertisement: Advertisement){

    }
}