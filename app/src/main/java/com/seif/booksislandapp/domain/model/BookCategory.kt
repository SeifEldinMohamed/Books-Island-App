package com.seif.booksislandapp.domain.model

import androidx.annotation.ColorRes

data class BookCategory(
    val name: String,
    @ColorRes
    val color: Int
)
