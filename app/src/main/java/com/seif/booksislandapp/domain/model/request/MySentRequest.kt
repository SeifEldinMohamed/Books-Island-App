package com.seif.booksislandapp.domain.model.request

import java.util.*

data class MySentRequest(
    var id: String,
    val senderId: String,
    val receiverId: String,
    val advertisementId: String,
    val username: String,
    val avatarImage: String,
    val bookTitle: String,
    val condition: String,
    val category: String,
    val adType: String,
    val edition: String,
    val date: Date? = null,
    val status: String
)
