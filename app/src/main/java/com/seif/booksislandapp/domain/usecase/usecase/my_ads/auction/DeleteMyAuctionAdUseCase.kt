package com.seif.booksislandapp.domain.usecase.usecase.my_ads.auction

import com.seif.booksislandapp.data.repository.AuctionAdvertisementRepositoryImp
import com.seif.booksislandapp.utils.Resource
import javax.inject.Inject

class DeleteMyAuctionAdUseCase @Inject constructor(
    private val auctionAdvertisementRepositoryImp: AuctionAdvertisementRepositoryImp
) {
    suspend operator fun invoke(myDonateAdId: String): Resource<String, String> {
        return auctionAdvertisementRepositoryImp.deleteMyAuctionAdv(myDonateAdId)
    }
}