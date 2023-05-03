package com.seif.booksislandapp.data.remote.dto

import java.util.*

data class MyChatDto(
    val senderId: String = "",
    var userIChatWith: UserDto? = null,
    var lastMessage: String = "",
    var lastMessageDate: Date? = null
)