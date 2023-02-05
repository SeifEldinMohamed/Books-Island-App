package com.seif.booksislandapp.domain.model.book

import androidx.annotation.ColorRes
import kotlin.String

data class BookCategory(
    val name: String,
    @ColorRes
    val color: Int
)
