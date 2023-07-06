package com.seif.booksislandapp.domain.usecase.usecase.advertisement.donate

import com.seif.booksislandapp.domain.model.adv.donation.DonateAdvertisement
import com.seif.booksislandapp.domain.repository.AdvertisementRepository
import com.seif.booksislandapp.utils.Resource
import javax.inject.Inject

class GetAllDonateAdvertisementUseCase @Inject constructor(
    private val advertisementRepository: AdvertisementRepository
) {
    suspend operator fun invoke(): Resource<ArrayList<DonateAdvertisement>, String> {
        return advertisementRepository.getAllDonateAds()
    }
}