package com.seif.booksislandapp.data.remote.dto.adv

import com.seif.booksislandapp.domain.model.adv.AdvStatus
import com.seif.booksislandapp.domain.model.book.Book
import java.util.*

abstract class AdvertisementDto {
    abstract var id: String
    abstract val ownerId: String
    abstract val book: Book
    abstract val status: AdvStatus
    abstract val publishTime: Date
    abstract val location: String
}
