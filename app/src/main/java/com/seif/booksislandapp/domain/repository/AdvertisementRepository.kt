package com.seif.booksislandapp.domain.repository

import com.seif.booksislandapp.domain.model.User
import com.seif.booksislandapp.domain.model.adv.SellAdvertisement
import com.seif.booksislandapp.utils.Resource

interface AdvertisementRepository {
    suspend fun getAllSellAds(): Resource<ArrayList<SellAdvertisement>, String>
    suspend fun uploadSellAdv(sellAdvertisement: SellAdvertisement): Resource<String, String>
    suspend fun searchSellAdv(searchQuery: String): Resource<ArrayList<SellAdvertisement>, String>
    suspend fun getUserById(id: String): Resource<User, String>
    suspend fun fetchRelatedSellAdvertisement(
        adId: String,
        category: String
    ): Resource<ArrayList<SellAdvertisement>, String>
}