package com.seif.booksislandapp.domain.model.book

import android.os.Parcelable
import kotlinx.parcelize.Parcelize

@Parcelize
data class BooksToExchange(
    val imageUrl: String = "",
    val title: String = ""
) : Parcelable
