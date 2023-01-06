package com.seif.booksislandapp.domain.model

import com.seif.booksislandapp.data.remote.dto.BookDto
import com.seif.booksislandapp.data.remote.dto.UserDto

data class Advertisement(
    var id: String,
    val owner: UserDto,
    val book: BookDto,
    val isDonateAdv: Boolean,
    val status: String,
    val publishTime: String
)
