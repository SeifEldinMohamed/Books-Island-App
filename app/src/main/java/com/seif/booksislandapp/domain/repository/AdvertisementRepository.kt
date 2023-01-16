package com.seif.booksislandapp.domain.repository

import com.seif.booksislandapp.domain.model.Advertisement
import com.seif.booksislandapp.utils.Resource

interface AdvertisementRepository {
    suspend fun getAllSellAds(): Resource<List<Advertisement>, String>
    suspend fun addSellAd(advertisement: Advertisement): Resource<String, String>
}