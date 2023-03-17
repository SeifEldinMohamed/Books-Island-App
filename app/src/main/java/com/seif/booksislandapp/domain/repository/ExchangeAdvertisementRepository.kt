package com.seif.booksislandapp.domain.repository
import com.seif.booksislandapp.domain.model.adv.exchange.ExchangeAdvertisement
import com.seif.booksislandapp.utils.Resource

interface ExchangeAdvertisementRepository {
    suspend fun getAllExchangeAdvertisement(): Resource<ArrayList<ExchangeAdvertisement>, String>
    suspend fun searchExchangeAdv(searchQuery: String): Resource<ArrayList<ExchangeAdvertisement>, String>
    suspend fun fetchRelatedExchangeAdvertisement(
        adId: String,
        category: String
    ): Resource<ArrayList<ExchangeAdvertisement>, String>

    suspend fun fetchMyExchangeAds(userId: String): Resource<ArrayList<ExchangeAdvertisement>, String>
    suspend fun deleteMyExchangeAdv(myExchangeAdId: String): Resource<String, String>
    suspend fun editMyExchangeAdv(exchangeAdvertisement: ExchangeAdvertisement): Resource<String, String>
}
