package com.seif.booksislandapp.data.remote.dto.adv.exchange

import com.seif.booksislandapp.data.remote.dto.BookDto
import com.seif.booksislandapp.data.remote.dto.adv.AdvertisementDto
import com.seif.booksislandapp.domain.model.adv.AdvStatus
import com.seif.booksislandapp.domain.model.book.Book
import java.util.*

data class ExchangeAdvertisementDto(
    override var id: String,
    override val ownerId: String,
    override val book: BookDto?,
    override val status: AdvStatus,
    override val publishTime: Date,
    override val location: String,
    val booksToExchange: List<Book>
) : AdvertisementDto()
