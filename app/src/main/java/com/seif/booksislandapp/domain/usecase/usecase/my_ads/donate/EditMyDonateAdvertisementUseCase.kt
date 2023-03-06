package com.seif.booksislandapp.domain.usecase.usecase.my_ads.donate

import com.seif.booksislandapp.data.repository.AdvertisementRepositoryImp
import com.seif.booksislandapp.domain.model.adv.donation.DonateAdvertisement
import com.seif.booksislandapp.utils.Resource
import com.seif.booksislandapp.utils.checkDonateAdvertisementUpload
import javax.inject.Inject

class EditMyDonateAdvertisementUseCase @Inject constructor(
    private val advertisementRepositoryImp: AdvertisementRepositoryImp
) {
    suspend operator fun invoke(donateAdvertisement: DonateAdvertisement): Resource<String, String> {
        return when (val result = donateAdvertisement.checkDonateAdvertisementUpload()) {
            is Resource.Error -> Resource.Error(result.message)
            is Resource.Success -> advertisementRepositoryImp.editMyDonateAdv(donateAdvertisement)
        }
    }
}