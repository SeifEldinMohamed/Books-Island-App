package com.seif.booksislandapp.domain.usecase.usecase.advertisement.auction

import com.seif.booksislandapp.data.repository.AuctionAdvertisementRepositoryImp
import com.seif.booksislandapp.domain.model.adv.auction.AuctionAdvertisement
import com.seif.booksislandapp.utils.Resource
import javax.inject.Inject

class GetAllAuctionAdsUseCase @Inject constructor(
    private val auctionAdvertisementRepositoryImp: AuctionAdvertisementRepositoryImp
) {
    suspend operator fun invoke(): Resource<ArrayList<AuctionAdvertisement>, String> {
        return auctionAdvertisementRepositoryImp.getAllAuctionsAds()
    }
}