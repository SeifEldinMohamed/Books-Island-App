package com.seif.booksislandapp.domain.repository

import com.seif.booksislandapp.domain.model.User
import com.seif.booksislandapp.domain.model.adv.donation.DonateAdvertisement
import com.seif.booksislandapp.domain.model.adv.exchange.ExchangeAdvertisement
import com.seif.booksislandapp.domain.model.adv.sell.SellAdvertisement
import com.seif.booksislandapp.presentation.home.categories.filter.FilterBy
import com.seif.booksislandapp.utils.Resource
import kotlinx.coroutines.flow.Flow

interface AdvertisementRepository {
    suspend fun getAllSellAds(): Resource<ArrayList<SellAdvertisement>, String>
    suspend fun getAllDonateAds(): Resource<ArrayList<DonateAdvertisement>, String>
    suspend fun uploadSellAdv(sellAdvertisement: SellAdvertisement): Resource<String, String>
    suspend fun editMySellAdv(sellAdvertisement: SellAdvertisement): Resource<String, String>
    suspend fun deleteMySellAdv(mySellAdId: String): Resource<String, String>
    suspend fun searchSellAdv(searchQuery: String): Resource<ArrayList<SellAdvertisement>, String>
    suspend fun getUserById(id: String): Resource<User, String>
    suspend fun fetchMySellAds(userId: String): Flow<Resource<ArrayList<SellAdvertisement>, String>>

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
    suspend fun fetchMyDonateAds(userId: String): Flow<Resource<ArrayList<DonateAdvertisement>, String>>
    suspend fun editMyDonateAdv(donateAdvertisement: DonateAdvertisement): Resource<String, String>
    suspend fun deleteMyDonateAdv(myDonateAdId: String): Resource<String, String>
    suspend fun fetchBuyWishListAds(buyIdList: List<String>): Resource<ArrayList<SellAdvertisement>, String>
    suspend fun fetchDonateWishListAds(donateIdList: List<String>): Resource<ArrayList<DonateAdvertisement>, String>
    suspend fun getSellAdsByFilter(
        filterBy: FilterBy
    ): Resource<ArrayList<SellAdvertisement>, String>
    suspend fun getDonateAdsByFilter(
        filterBy: FilterBy
    ): Resource<ArrayList<DonateAdvertisement>, String>
}