package com.seif.booksislandapp.domain.repository

import com.seif.booksislandapp.domain.model.adv.auction.AuctionAdvertisement
import com.seif.booksislandapp.utils.Resource

interface AuctionAdvertisementRepository {
    suspend fun uploadAuctionAdv(auctionAdvertisement: AuctionAdvertisement): Resource<String, String>
}