package com.seif.booksislandapp.domain.usecase.usecase.advertisement.auction

import com.seif.booksislandapp.data.repository.AuctionAdvertisementRepositoryImp
import com.seif.booksislandapp.domain.model.adv.auction.AuctionAdvertisement
import com.seif.booksislandapp.utils.Resource
import javax.inject.Inject

class SearchAuctionsAdsUseCase @Inject constructor(
    private val auctionAdvertisementRepositoryImp: AuctionAdvertisementRepositoryImp
) {
    suspend operator fun invoke(searchQuery: String): Resource<ArrayList<AuctionAdvertisement>, String> {
        return auctionAdvertisementRepositoryImp.searchAuctionsAdv(searchQuery)
    }
}