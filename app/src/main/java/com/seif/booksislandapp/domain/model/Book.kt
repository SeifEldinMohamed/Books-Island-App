package com.seif.booksislandapp.domain.model

import android.net.Uri

data class Book(
    val id: String,
    val images: List<Uri>,
    val title: String,
    val author: String,
    val category: String,
    val isUsed: Boolean,
    val description: String,
)
