package com.seif.booksislandapp.data.remote.dto.adv

import com.seif.booksislandapp.domain.model.AdvStatus
import com.seif.booksislandapp.domain.model.Book
import java.util.*

data class SellAdvertisementDto(
    override var id: String,
    override val ownerId: String,
    override val book: Book,
    override val status: AdvStatus,
    override val publishTime: Date,
    override val location: String,
    val price: String

) : AdvertisementDto()
