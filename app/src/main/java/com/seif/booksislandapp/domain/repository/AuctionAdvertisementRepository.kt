package com.seif.booksislandapp.domain.repository

import com.seif.booksislandapp.domain.model.adv.auction.AuctionAdvertisement
import com.seif.booksislandapp.domain.model.adv.auction.Bidder
import com.seif.booksislandapp.utils.Resource
import kotlinx.coroutines.flow.Flow

interface AuctionAdvertisementRepository {
    suspend fun getAllAuctionsAds(): Resource<ArrayList<AuctionAdvertisement>, String>
    suspend fun uploadAuctionAdv(auctionAdvertisement: AuctionAdvertisement): Resource<String, String>
    suspend fun searchAuctionsAdv(searchQuery: String): Resource<ArrayList<AuctionAdvertisement>, String>
    suspend fun fetchRelatedAuctionAdvertisement(
        adId: String,
        category: String
    ): Resource<ArrayList<AuctionAdvertisement>, String>

    suspend fun fetchAuctionAdByIdQuerySnapShot(adId: String): Flow<Resource<AuctionAdvertisement, String>>
    suspend fun addBidder(adId: String, bidder: Bidder): Resource<String, String>

    suspend fun fetchMyAuctionAds(userId: String): Resource<java.util.ArrayList<AuctionAdvertisement>, String>
    suspend fun editMyAuctionAdv(auctionAdvertisement: AuctionAdvertisement): Resource<String, String>
    suspend fun deleteMyAuctionAdv(myAuctionAdId: String): Resource<String, String>
    suspend fun fetchAuctionWishListAds(auctionIdList: List<String>): Resource<java.util.ArrayList<AuctionAdvertisement>, String>
}