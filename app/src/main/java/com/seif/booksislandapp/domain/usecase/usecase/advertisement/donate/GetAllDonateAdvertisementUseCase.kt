package com.seif.booksislandapp.domain.usecase.usecase.advertisement.donate

import com.seif.booksislandapp.data.repository.AdvertisementRepositoryImp
import com.seif.booksislandapp.domain.model.adv.DonateAdvertisement
import com.seif.booksislandapp.utils.Resource
import javax.inject.Inject

class GetAllDonateAdvertisementUseCase @Inject constructor(
    private val advertisementRepositoryImp: AdvertisementRepositoryImp
) {
    suspend operator fun invoke(): Resource<ArrayList<DonateAdvertisement>, String> {
        return advertisementRepositoryImp.getAllDonateAds()
    }
}