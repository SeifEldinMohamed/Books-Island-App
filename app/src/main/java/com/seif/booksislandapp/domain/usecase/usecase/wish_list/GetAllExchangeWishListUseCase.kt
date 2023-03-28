package com.seif.booksislandapp.domain.usecase.usecase.wish_list

import com.seif.booksislandapp.data.repository.ExchangeAdvertisementRepositoryImp
import com.seif.booksislandapp.domain.model.adv.exchange.ExchangeAdvertisement
import com.seif.booksislandapp.utils.Resource
import javax.inject.Inject

class GetAllExchangeWishListUseCase @Inject constructor(
    private val exchangeAdvertisementRepositoryImp: ExchangeAdvertisementRepositoryImp
) {
    suspend operator fun invoke(exchangeIdList: List<String>): Resource<ArrayList<ExchangeAdvertisement>, String> {
        return exchangeAdvertisementRepositoryImp.fetchExchangeWishListAds(exchangeIdList)
    }
}