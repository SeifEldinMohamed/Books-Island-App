package com.seif.booksislandapp.domain.repository

import com.seif.booksislandapp.domain.model.User
import com.seif.booksislandapp.domain.model.adv.donation.DonateAdvertisement
import com.seif.booksislandapp.domain.model.adv.exchange.ExchangeAdvertisement
import com.seif.booksislandapp.domain.model.adv.sell.SellAdvertisement
import com.seif.booksislandapp.utils.Resource

interface AdvertisementRepository {
    suspend fun getAllSellAds(): Resource<ArrayList<SellAdvertisement>, String>
    suspend fun getAllDonateAds(): Resource<ArrayList<DonateAdvertisement>, String>
    suspend fun uploadSellAdv(sellAdvertisement: SellAdvertisement): Resource<String, String>
    suspend fun searchSellAdv(searchQuery: String): Resource<ArrayList<SellAdvertisement>, String>
    suspend fun getUserById(id: String): Resource<User, String>
    // donate
    suspend fun fetchRelatedSellAdvertisement(
        adId: String,
        category: String
    ): Resource<ArrayList<SellAdvertisement>, String>
    suspend fun searchDonateAdv(searchQuery: String): Resource<ArrayList<DonateAdvertisement>, String>
    suspend fun uploadDonateAdv(donateAdvertisement: DonateAdvertisement): Resource<String, String>
    suspend fun fetchRelatedDonateAdvertisement(
        adId: String,
        category: String
    ): Resource<ArrayList<DonateAdvertisement>, String>
    suspend fun uploadExchangeAdv(exchangeAdvertisement: ExchangeAdvertisement): Resource<String, String>
}