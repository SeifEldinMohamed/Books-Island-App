package com.seif.booksislandapp.domain.usecase.usecase.wish_list

import com.seif.booksislandapp.data.repository.AuctionAdvertisementRepositoryImp
import com.seif.booksislandapp.domain.model.adv.auction.AuctionAdvertisement
import com.seif.booksislandapp.utils.Resource
import javax.inject.Inject

class GetAllAuctionWishListUseCase @Inject constructor(
    private val auctionAdvertisementRepositoryImp: AuctionAdvertisementRepositoryImp
) {
    suspend operator fun invoke(auctionIdList: List<String>): Resource<ArrayList<AuctionAdvertisement>, String> {
        return auctionAdvertisementRepositoryImp.fetchAuctionWishListAds(auctionIdList)
    }
}