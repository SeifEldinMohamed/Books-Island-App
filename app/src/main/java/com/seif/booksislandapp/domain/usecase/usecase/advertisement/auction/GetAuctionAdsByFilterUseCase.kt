package com.seif.booksislandapp.domain.usecase.usecase.advertisement.auction

import com.seif.booksislandapp.data.repository.AuctionAdvertisementRepositoryImp
import com.seif.booksislandapp.domain.model.adv.auction.AuctionAdvertisement
import com.seif.booksislandapp.presentation.home.categories.filter.FilterBy
import com.seif.booksislandapp.utils.Resource
import com.seif.booksislandapp.utils.validateFilter
import javax.inject.Inject

class GetAuctionAdsByFilterUseCase @Inject constructor(
    private val auctionAdvertisementRepositoryImp: AuctionAdvertisementRepositoryImp
) {

    suspend operator fun invoke(
        filterBy: FilterBy
    ): Resource<ArrayList<AuctionAdvertisement>, String> {

        return when (val result = filterBy.validateFilter()) {
            is Resource.Error -> Resource.Error(result.message)
            is Resource.Success -> auctionAdvertisementRepositoryImp.getAuctionAdsByFilter(filterBy)
        }
    }
}