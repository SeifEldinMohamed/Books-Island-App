package com.seif.booksislandapp.data.remote.dto.chat

import java.util.*

data class MessageDto(
    val id: String = "",
    val senderId: String = "",
    val receiverId: String = "",
    val text: String? = null,
    val imageUrl: String? = null,
    val date: Date? = null,
    val chatUsers: ArrayList<String>? = null
)