package com.seif.booksislandapp.domain.usecase.usecase.my_ads.sell

import com.seif.booksislandapp.data.repository.AdvertisementRepositoryImp
import com.seif.booksislandapp.domain.model.adv.sell.SellAdvertisement
import com.seif.booksislandapp.utils.Resource
import com.seif.booksislandapp.utils.checkSellAdvertisementUpload
import javax.inject.Inject

class EditMySellAdvertisementUseCase @Inject constructor(
    private val advertisementRepositoryImp: AdvertisementRepositoryImp
) {
    suspend operator fun invoke(sellAdvertisement: SellAdvertisement): Resource<String, String> {
        return when (val result = sellAdvertisement.checkSellAdvertisementUpload()) {
            is Resource.Error -> Resource.Error(result.message)
            is Resource.Success -> advertisementRepositoryImp.editMySellAdv(sellAdvertisement)
        }
    }
}