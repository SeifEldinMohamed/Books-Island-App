package com.seif.booksislandapp.domain.model.adv.auction

import com.seif.booksislandapp.domain.model.adv.AdvStatus
import com.seif.booksislandapp.domain.model.adv.Advertisement
import com.seif.booksislandapp.domain.model.book.Book
import java.util.*

data class AuctionAdvertisement(
    override var id: String,
    override val ownerId: String,
    override val book: Book,
    override val status: AdvStatus,
    override val publishTime: Date,
    override val location: String,
    val startPrice: Double?,
    val endPrice: Double?,
    var closeDate: Date?,
    val postDuration: String,
    val isOpen: Boolean,
    val bidders: List<Bidder>

) : Advertisement()
