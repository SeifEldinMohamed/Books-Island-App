package com.seif.booksislandapp.domain.model

import android.net.Uri
import kotlin.String

data class Book(
    val id: String,
    var images: List<Uri>,
    val title: String,
    val author: String,
    val category: String,
    val condition: BookCondition?,
    val description: String,
)
