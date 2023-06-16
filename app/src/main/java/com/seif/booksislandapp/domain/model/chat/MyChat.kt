package com.seif.booksislandapp.domain.model.chat

import com.seif.booksislandapp.domain.model.User
import java.util.Date

data class MyChat(
    val currentUserId: String,
    val senderId: String,
    val userIChatWith: User,
    val lastMessage: String,
    val lastMessageDate: Date? = null,
    val isSeen: Boolean,
    val unreadMessages: Int
)
