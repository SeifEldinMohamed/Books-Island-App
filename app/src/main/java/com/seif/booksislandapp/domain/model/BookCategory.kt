package com.seif.booksislandapp.domain.model

import androidx.annotation.ColorRes
import kotlin.String

data class BookCategory(
    val name: String,
    @ColorRes
    val color: Int
)
