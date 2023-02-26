package com.seif.booksislandapp.data.remote.dto.adv.exchange

import com.seif.booksislandapp.data.remote.dto.BookDto
import com.seif.booksislandapp.data.remote.dto.adv.AdvertisementDto
import com.seif.booksislandapp.domain.model.adv.AdvStatus
import java.util.*

data class ExchangeAdvertisementDto(
    override var id: String = "",
    override val ownerId: String = "",
    override val book: BookDto? = null,
    override val status: AdvStatus? = null,
    override val publishDate: Date? = null,
    override val location: String = "",
    val booksToExchange: List<BooksToExchangeDto> = emptyList()
) : AdvertisementDto()
