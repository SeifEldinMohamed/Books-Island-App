package com.seif.booksislandapp.domain.repository

import com.seif.booksislandapp.domain.model.adv.SellAdvertisement
import com.seif.booksislandapp.utils.Resource

interface AdvertisementRepository {
    suspend fun getAllSellAds(): Resource<List<SellAdvertisement>, String>
    suspend fun uploadSellAdv(sellAdvertisement: SellAdvertisement): Resource<String, String>
}