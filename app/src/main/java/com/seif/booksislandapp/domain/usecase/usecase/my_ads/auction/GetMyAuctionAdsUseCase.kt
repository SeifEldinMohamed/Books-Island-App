package com.seif.booksislandapp.domain.usecase.usecase.my_ads.auction

import com.seif.booksislandapp.data.repository.AuctionAdvertisementRepositoryImp
import com.seif.booksislandapp.domain.model.adv.auction.AuctionAdvertisement
import com.seif.booksislandapp.utils.Resource
import javax.inject.Inject

class GetMyAuctionAdsUseCase @Inject constructor(
    private val auctionAdvertisementRepositoryImp: AuctionAdvertisementRepositoryImp
) {
    suspend operator fun invoke(userId: String): Resource<ArrayList<AuctionAdvertisement>, String> {
        return auctionAdvertisementRepositoryImp.fetchMyAuctionAds(userId)
    }
}