package com.seif.booksislandapp.domain.usecase.usecase.my_ads.auction

import com.seif.booksislandapp.data.repository.AuctionAdvertisementRepositoryImp
import com.seif.booksislandapp.domain.model.adv.auction.AuctionAdvertisement
import com.seif.booksislandapp.utils.Resource
import com.seif.booksislandapp.utils.checkAuctionAdvertisementUpload
import javax.inject.Inject

class EditMyAuctionAdvertisementUseCase @Inject constructor(
    private val auctionAdvertisementRepositoryImp: AuctionAdvertisementRepositoryImp
) {
    suspend operator fun invoke(auctionAdvertisement: AuctionAdvertisement): Resource<String, String> {
        return when (val result = auctionAdvertisement.checkAuctionAdvertisementUpload()) {
            is Resource.Error -> Resource.Error(result.message)
            is Resource.Success -> auctionAdvertisementRepositoryImp.editMyAuctionAdv(
                auctionAdvertisement
            )
        }
    }
}