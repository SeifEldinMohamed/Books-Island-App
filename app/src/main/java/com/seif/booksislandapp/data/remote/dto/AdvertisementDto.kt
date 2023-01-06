package com.seif.booksislandapp.data.remote.dto

data class AdvertisementDto(
    val id: String,
    val owner: UserDto,
    val book: BookDto,
    val isDonateAdv: Boolean,
    val status: String,
    val publishTime: String
)
