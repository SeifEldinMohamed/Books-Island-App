package com.seif.booksislandapp.domain.model.adv

import com.seif.booksislandapp.domain.model.book.Book
import java.util.*

data class SellAdvertisement(
    override var id: String,
    override val ownerId: String,
    override val book: Book,
    override val status: AdvStatus,
    override val publishTime: Date,
    override val location: String,
    val price: String
) : Advertisement()
