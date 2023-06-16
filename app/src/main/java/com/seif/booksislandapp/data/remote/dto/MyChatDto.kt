package com.seif.booksislandapp.data.remote.dto

import java.util.Date

data class MyChatDto(
    var currentUserId: String,
    var senderId: String,
    var userIChatWith: UserDto? = null,
    var lastMessage: String = "",
    var lastMessageDate: Date? = null,
    var isSeen: Boolean,
    var unreadMessages: Int
)