package com.seif.booksislandapp.domain.model.adv.auction

import android.os.Parcelable
import com.seif.booksislandapp.domain.model.adv.AdvStatus
import com.seif.booksislandapp.domain.model.adv.Advertisement
import com.seif.booksislandapp.domain.model.book.Book
import kotlinx.parcelize.Parcelize
import java.util.*

@Parcelize
data class AuctionAdvertisement(
    override var id: String,
    override val ownerId: String,
    override val book: Book,
    override val status: AdvStatus,
    override val publishDate: Date,
    override val location: String,
    override val confirmationMessageSent: Boolean,
    val startPrice: Double?,
    val endPrice: Double?,
    var closeDate: Date?,
    val postDuration: String,
    val auctionStatus: AuctionStatus,
    val bidders: List<Bidder>,
    val confirmationRequestId: String = ""
) : Advertisement(), Parcelable
