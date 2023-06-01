package com.seif.booksislandapp.data.remote.dto.adv.auction

import com.seif.booksislandapp.data.remote.dto.BookDto
import com.seif.booksislandapp.data.remote.dto.adv.AdvertisementDto
import com.seif.booksislandapp.domain.model.adv.AdvStatus
import com.seif.booksislandapp.domain.model.adv.auction.AuctionStatus
import java.util.*

data class AuctionAdvertisementDto(
    override var id: String = "",
    override val ownerId: String = "",
    override val book: BookDto? = null,
    override val status: AdvStatus? = null,
    override val publishDate: Date? = null,
    override val location: String = "",
    override val confirmationMessageSent: Boolean? = null,
    val startPrice: Double = 0.0,
    val endPrice: Double? = null,
    val closeDate: Date? = null,
    val postDuration: String = "",
    var auctionStatus: AuctionStatus? = null,
    val bidders: List<BidderDto>? = null,
    val confirmationRequestId: String = ""
) : AdvertisementDto()
