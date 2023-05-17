package com.seif.booksislandapp.data.remote.dto.notification

data class FCMMessageDto(
    val title: String = "",
    val body: String = "",
    val senderId: String = "",
    val receiverId: String = "",
    val image: String = ""
)
