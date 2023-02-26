package com.seif.booksislandapp.domain.model.book

import android.net.Uri
import android.os.Parcelable
import kotlinx.parcelize.Parcelize

@Parcelize
data class BooksToExchange(
    val imageUri: Uri?,
    val title: String,
    val author: String,
) : Parcelable
