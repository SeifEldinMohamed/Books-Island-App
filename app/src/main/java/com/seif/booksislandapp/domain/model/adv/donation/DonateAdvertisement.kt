package com.seif.booksislandapp.domain.model.adv.donation

import com.seif.booksislandapp.domain.model.adv.AdvStatus
import com.seif.booksislandapp.domain.model.adv.Advertisement
import com.seif.booksislandapp.domain.model.book.Book
import java.util.*

data class DonateAdvertisement(
    override var id: String,
    override val ownerId: String,
    override val book: Book,
    override val status: AdvStatus,
    override val publishTime: Date,
    override val location: String,
) : Advertisement()