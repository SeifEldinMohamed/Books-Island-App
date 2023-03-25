package com.seif.booksislandapp.domain.model.chat

import java.util.*

data class Message(
    var id: String,
    val senderId: String,
    val receiverId: String,
    val text: String? = null,
    val imageUrl: String? = null,
    val date: Date
)