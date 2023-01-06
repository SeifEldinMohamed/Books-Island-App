package com.seif.booksislandapp.domain.repository

import com.seif.booksislandapp.domain.model.Advertisement
import com.seif.booksislandapp.utils.Resource

interface AdvertisementRepository {
    suspend fun getAllBuyAds(): Resource<List<Advertisement>, String>
    suspend fun addBuyAd(advertisement: Advertisement): Resource<String, String>
}