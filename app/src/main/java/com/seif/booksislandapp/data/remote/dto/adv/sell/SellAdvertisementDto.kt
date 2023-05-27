package com.seif.booksislandapp.data.remote.dto.adv.sell

import com.seif.booksislandapp.data.remote.dto.BookDto
import com.seif.booksislandapp.data.remote.dto.adv.AdvertisementDto
import com.seif.booksislandapp.domain.model.adv.AdvStatus
import java.util.*

data class SellAdvertisementDto(
    override var id: String = "",
    override val ownerId: String = "",
    override val book: BookDto? = null,
    override val status: AdvStatus? = null,
    override val publishDate: Date? = null,
    override val location: String = "",
    override val confirmationMessageSent: Boolean? = null,
    val price: String = "",
    val confirmationRequestId: String = ""
) : AdvertisementDto()
