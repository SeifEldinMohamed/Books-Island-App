package com.seif.booksislandapp.data.remote.dto

import android.net.Uri

data class BookDto(
    val id: String,
    val images: List<Uri>,
    val title: String,
    val author: String,
    val category: String,
    val isUsed: Boolean,
    val description: String,
)
