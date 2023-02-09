package com.seif.booksislandapp.data.remote.dto.adv

import com.seif.booksislandapp.data.remote.dto.BookDto
import com.seif.booksislandapp.domain.model.adv.AdvStatus
import java.util.*

data class BidAdvertisementDto(
    override var id: String,
    override val ownerId: String,
    override val book: BookDto?,
    override val status: AdvStatus,
    override val publishTime: Date,
    override val location: String,
    val initialPrice: Double,
    val closeDate: String,
    val isOpen: Boolean,
    val endPrice: Double,
    val numOfBidders: Int,
    val lastBidderName: String,

) : AdvertisementDto()
