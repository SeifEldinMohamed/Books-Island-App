package com.seif.booksislandapp.domain.usecase.usecase.advertisement.auction

import com.seif.booksislandapp.data.repository.AuctionAdvertisementRepositoryImp
import com.seif.booksislandapp.domain.model.adv.auction.Bidder
import com.seif.booksislandapp.utils.Resource
import javax.inject.Inject

class AddBidderUseCase @Inject constructor(
    private val auctionAdvertisementRepositoryImp: AuctionAdvertisementRepositoryImp

) {
    suspend operator fun invoke(
        adId: String,
        bidder: Bidder,
        currentBidValue: Int
    ): Resource<String, String> {
        return when (val result = checkBidValue(bidder.suggestedPrice, currentBidValue)) {
            is Resource.Error -> Resource.Error(result.message)
            is Resource.Success -> {
                auctionAdvertisementRepositoryImp.addBidder(adId, bidder)
            }
        }
    }

    private fun checkBidValue(bidValue: String, currentBidValue: Int): Resource<String, String> {
        return if (bidValue.isEmpty()) {
            Resource.Error("Add your bid first!")
        } else if (bidValue.toInt() <= currentBidValue) {
            Resource.Error("Your bid must be more than current price!")
        } else { // greater than current price
            Resource.Success("valid price")
        }
    }
}