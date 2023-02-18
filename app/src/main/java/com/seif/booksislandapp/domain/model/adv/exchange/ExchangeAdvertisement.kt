package com.seif.booksislandapp.domain.model.adv.exchange

import com.seif.booksislandapp.domain.model.adv.AdvStatus
import com.seif.booksislandapp.domain.model.adv.Advertisement
import com.seif.booksislandapp.domain.model.book.Book
import java.util.*

data class ExchangeAdvertisement(
    override var id: String,
    override val ownerId: String,
    override val book: Book,
    override val status: AdvStatus,
    override val publishDate: Date,
    val booksToExchange: List<Book>,
    override val location: String
) : Advertisement()
