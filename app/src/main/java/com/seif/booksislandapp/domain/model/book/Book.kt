package com.seif.booksislandapp.domain.model.book

import android.net.Uri
import android.os.Parcelable
import kotlinx.parcelize.Parcelize

@Parcelize
data class Book(
    val id: String = "",
    var images: List<Uri> = emptyList(),
    val title: String = "",
    val author: String = "",
    val category: String = "",
    val condition: String? = null,
    val description: String = "",
    val edition: String = "",
) : Parcelable
