package com.seif.booksislandapp.data.remote.dto.adv.auction

import com.seif.booksislandapp.data.remote.dto.BookDto
import com.seif.booksislandapp.data.remote.dto.adv.AdvertisementDto
import com.seif.booksislandapp.domain.model.adv.AdvStatus
import java.util.*

data class AuctionAdvertisementDto(
    override var id: String = "",
    override val ownerId: String = "",
    override val book: BookDto? = null,
    override val status: AdvStatus? = null,
    override val publishTime: Date? = null,
    override val location: String = "",
    val startPrice: Double = 0.0,
    val endPrice: Double? = null,
    val closeDate: Date? = null,
    val isOpen: Boolean = true,
    val bidders: List<BidderDto>? = null

) : AdvertisementDto()
