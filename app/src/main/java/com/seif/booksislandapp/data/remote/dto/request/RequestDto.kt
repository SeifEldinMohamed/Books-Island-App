package com.seif.booksislandapp.data.remote.dto.request

import com.google.firebase.firestore.ServerTimestamp
import java.util.*

data class RequestDto(
    val id: String = "",
    val senderId: String = "",
    val receiverId: String = "",
    val advertisementId: String = "",
    val username: String = "",
    val avatarImage: String = "",
    val bookTitle: String = "",
    val condition: String = "",
    val category: String = "",
    val adType: String = "",
    val edition: String = "",
    @ServerTimestamp
    val date: Date? = null,
    val status: String = ""
)
