package com.seif.booksislandapp.domain.model.adv

import com.seif.booksislandapp.domain.model.AdvStatus
import com.seif.booksislandapp.domain.model.Book
import java.util.*

abstract class Advertisement {
    abstract var id: String
    abstract val ownerId: String
    abstract val book: Book
    abstract val status: AdvStatus
    abstract val publishTime: Date
    abstract val location: String
}
