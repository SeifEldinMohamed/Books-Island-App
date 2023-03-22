package com.seif.booksislandapp.domain.usecase.usecase.wish_list

import com.seif.booksislandapp.data.repository.AdvertisementRepositoryImp
import com.seif.booksislandapp.domain.model.adv.sell.SellAdvertisement
import com.seif.booksislandapp.utils.Resource
import javax.inject.Inject

class GetAllBuyWishListUseCase @Inject constructor(
    private val advertisementRepositoryImp: AdvertisementRepositoryImp
) {
    suspend operator fun invoke(buyIdList: List<String>): Resource<ArrayList<SellAdvertisement>, String> {
        return advertisementRepositoryImp.fetchBuyWishListAds(buyIdList)
    }
}