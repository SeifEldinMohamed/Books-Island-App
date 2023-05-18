package com.seif.booksislandapp.domain.model.adv

import com.seif.booksislandapp.domain.model.book.Book
import java.util.*

abstract class Advertisement {
    abstract var id: String
    abstract val ownerId: String
    abstract val book: Book
    abstract val status: AdvStatus
    abstract val publishDate: Date
    abstract val location: String
    abstract val confirmationMessageSent: Boolean
}
