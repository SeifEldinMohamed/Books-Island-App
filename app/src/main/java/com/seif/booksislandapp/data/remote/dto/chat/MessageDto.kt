package com.seif.booksislandapp.data.remote.dto.chat

import com.google.firebase.firestore.ServerTimestamp
import java.util.Date

data class MessageDto(
    val id: String = "",
    val senderId: String = "",
    val receiverId: String = "",
    val text: String = "",
    val imageUrl: String? = null,
    @ServerTimestamp
    val date: Date? = null,
    val chatUsers: ArrayList<String>? = null,
    val seen: Boolean? = null
)