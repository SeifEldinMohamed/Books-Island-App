package com.seif.booksislandapp.data.remote.dto

data class BookDto(
    val id: String = "",
    var images: List<String> = emptyList(),
    val title: String = "",
    val author: String = "",
    val category: String = "",
    val condition: String? = null,
    val description: String = "",
    val edition: String = ""
)
