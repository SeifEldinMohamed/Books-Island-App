package com.seif.booksislandapp.domain.model.adv

import com.seif.booksislandapp.domain.model.AdvStatus
import com.seif.booksislandapp.domain.model.Book
import java.util.*

data class ExchangeAdvertisement(
    override var id: String,
    override val ownerId: String,
    override val book: Book,
    override val status: AdvStatus,
    override val publishTime: Date,
    val booksToExchange: List<Book>,
    override val location: String
) : Advertisement()
