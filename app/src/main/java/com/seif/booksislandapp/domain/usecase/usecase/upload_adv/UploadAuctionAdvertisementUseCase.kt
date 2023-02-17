package com.seif.booksislandapp.domain.usecase.usecase.upload_adv

import com.seif.booksislandapp.domain.model.adv.auction.AuctionAdvertisement
import com.seif.booksislandapp.domain.repository.AuctionAdvertisementRepository
import com.seif.booksislandapp.utils.Resource
import com.seif.booksislandapp.utils.checkAuctionAdvertisementUpload
import timber.log.Timber
import java.util.*
import javax.inject.Inject

class UploadAuctionAdvertisementUseCase @Inject constructor(
    private val auctionAdvertisementRepository: AuctionAdvertisementRepository
) {
    suspend operator fun invoke(auctionAdvertisement: AuctionAdvertisement): Resource<String, String> {
        return when (val result = auctionAdvertisement.checkAuctionAdvertisementUpload()) {
            is Resource.Error -> Resource.Error(result.message)
            is Resource.Success -> {
                auctionAdvertisement.closeDate =
                    calculateCloseDate(auctionAdvertisement.postDuration.toInt())
                auctionAdvertisementRepository.uploadAuctionAdv(auctionAdvertisement)
            }
        }
    }

    private fun calculateCloseDate(postDuration: Int): Date {
        val currentDate = Date()
        Timber.d("calculateCloseDate: current date = $currentDate")
        val calendar = Calendar.getInstance()
        calendar.time = currentDate
        calendar.add(Calendar.DATE, postDuration)
        Timber.d("calculateCloseDate: close date = ${calendar.time}")
        return calendar.time // close date
    }
}