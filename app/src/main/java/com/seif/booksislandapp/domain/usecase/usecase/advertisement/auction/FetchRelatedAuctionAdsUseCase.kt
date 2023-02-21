package com.seif.booksislandapp.domain.usecase.usecase.advertisement.auction

import com.seif.booksislandapp.data.repository.AuctionAdvertisementRepositoryImp
import com.seif.booksislandapp.domain.model.adv.auction.AuctionAdvertisement
import com.seif.booksislandapp.utils.Resource
import javax.inject.Inject

class FetchRelatedAuctionAdsUseCase @Inject constructor(
    private val auctionAdvertisementRepositoryImp: AuctionAdvertisementRepositoryImp
) {
    suspend operator fun invoke(
        adId: String,
        category: String
    ): Resource<List<AuctionAdvertisement>, String> {
        return if (category.isNotEmpty()) {
            auctionAdvertisementRepositoryImp.fetchRelatedAuctionAdvertisement(adId, category)
        } else {
            Resource.Error("category is empty!")
        }
    }
}