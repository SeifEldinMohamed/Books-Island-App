package com.seif.booksislandapp.domain.usecase.usecase.my_ads.sell

import com.seif.booksislandapp.data.repository.AdvertisementRepositoryImp
import com.seif.booksislandapp.utils.Resource
import javax.inject.Inject

class DeleteMySellAdUseCase @Inject constructor(
    private val advertisementRepositoryImp: AdvertisementRepositoryImp
) {
    suspend operator fun invoke(mySellAdId: String): Resource<String, String> {
        return advertisementRepositoryImp.deleteMySellAdv(mySellAdId)
    }
}