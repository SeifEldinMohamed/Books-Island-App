package com.seif.booksislandapp.domain.model.chat

import com.seif.booksislandapp.domain.model.User
import java.util.*

data class MyChat(
    val userIChatWith: User,
    val lastMessage: String,
    val lastMessageDate: Date?
)
