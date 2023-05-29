package com.seif.booksislandapp.domain.model.adv.exchange

import android.os.Parcelable
import com.seif.booksislandapp.domain.model.adv.AdvStatus
import com.seif.booksislandapp.domain.model.adv.Advertisement
import com.seif.booksislandapp.domain.model.book.Book
import com.seif.booksislandapp.domain.model.book.BooksToExchange
import kotlinx.parcelize.Parcelize
import java.util.*
@Parcelize
data class ExchangeAdvertisement(
    override var id: String,
    override val ownerId: String,
    override val book: Book,
    override val status: AdvStatus,
    override val publishDate: Date,
    var booksToExchange: List<BooksToExchange>,
    override val location: String,
    override val confirmationMessageSent: Boolean,
    val confirmationRequestId: String = ""
) : Advertisement(), Parcelable
