package com.seif.booksislandapp.domain.usecase.usecase.my_ads.donate

import com.seif.booksislandapp.data.repository.AdvertisementRepositoryImp
import com.seif.booksislandapp.utils.Resource
import javax.inject.Inject

class DeleteMyDonateAdUseCase @Inject constructor(
    private val advertisementRepositoryImp: AdvertisementRepositoryImp
) {
    suspend operator fun invoke(myDonateAdId: String): Resource<String, String> {
        return advertisementRepositoryImp.deleteMyDonateAdv(myDonateAdId)
    }
}