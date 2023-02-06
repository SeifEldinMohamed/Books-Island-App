package com.seif.booksislandapp.data.remote.dto.adv

import com.seif.booksislandapp.data.remote.dto.BookDto
import com.seif.booksislandapp.domain.model.adv.AdvStatus
import java.util.*

data class SellAdvertisementDto(
    override var id: String = "",
    override val ownerId: String = "",
    override val book: BookDto? = null,
    override val status: AdvStatus? = null,
    override val publishTime: Date? = null,
    override val location: String = "",
    val price: String = ""
) : AdvertisementDto()
