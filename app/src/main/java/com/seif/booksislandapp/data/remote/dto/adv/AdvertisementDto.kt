package com.seif.booksislandapp.data.remote.dto.adv

import com.seif.booksislandapp.data.remote.dto.BookDto
import com.seif.booksislandapp.domain.model.adv.AdvStatus
import java.util.*

abstract class AdvertisementDto {
    abstract var id: String
    abstract val ownerId: String
    abstract val book: BookDto?
    abstract val status: AdvStatus?
    abstract val publishDate: Date?
    abstract val location: String
}
