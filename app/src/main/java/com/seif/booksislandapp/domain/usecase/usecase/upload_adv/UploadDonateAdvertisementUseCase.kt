package com.seif.booksislandapp.domain.usecase.usecase.upload_adv

import com.seif.booksislandapp.domain.model.adv.donation.DonateAdvertisement
import com.seif.booksislandapp.domain.repository.AdvertisementRepository
import com.seif.booksislandapp.utils.Resource
import com.seif.booksislandapp.utils.checkDonateAdvertisementUpload
import javax.inject.Inject

class UploadDonateAdvertisementUseCase @Inject constructor(
    private val advertisementRepository: AdvertisementRepository
) {
    suspend operator fun invoke(donateAdvertisement: DonateAdvertisement): Resource<String, String> {
        return when (val result = donateAdvertisement.checkDonateAdvertisementUpload()) {
            is Resource.Error -> Resource.Error(result.message)
            is Resource.Success -> advertisementRepository.uploadDonateAdv(donateAdvertisement)
        }
    }
}