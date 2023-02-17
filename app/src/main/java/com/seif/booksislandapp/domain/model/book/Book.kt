package com.seif.booksislandapp.domain.model.book

import android.net.Uri
import android.os.Parcelable
import kotlinx.parcelize.Parcelize
import kotlin.String
@Parcelize
data class Book(
    val id: String,
    var images: List<Uri>,
    val title: String,
    val author: String,
    val category: String,
    val isUsed: Boolean?,
    val description: String,
    val edition: String,
) : Parcelable
