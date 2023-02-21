package com.seif.booksislandapp.domain.usecase.usecase.advertisement.auction

import com.seif.booksislandapp.data.repository.AuctionAdvertisementRepositoryImp
import com.seif.booksislandapp.domain.model.adv.auction.AuctionAdvertisement
import com.seif.booksislandapp.utils.Resource
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject

class FetchAuctionAdByIdUseCase @Inject constructor(
    private val auctionAdvertisementRepositoryImp: AuctionAdvertisementRepositoryImp
) {
    suspend operator fun invoke(adId: String): Flow<Resource<AuctionAdvertisement, String>> {
        return auctionAdvertisementRepositoryImp.fetchAuctionAdByIdQuerySnapShot(adId)
    }
}