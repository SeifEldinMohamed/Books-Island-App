package com.seif.booksislandapp.domain.model.adv

import android.os.Parcelable
import com.seif.booksislandapp.domain.model.book.Book
import kotlinx.parcelize.Parcelize
import java.util.*

@Parcelize
data class SellAdvertisement(
    override var id: String,
    override val ownerId: String,
    override val book: Book,
    override val status: AdvStatus,
    override val publishTime: Date,
    override val location: String,
    val price: String
) : Advertisement(), Parcelable
