package com.seif.booksislandapp.domain.model.chat

import android.net.Uri
import java.util.Date

data class Message(
    var id: String,
    val senderId: String,
    val receiverId: String,
    val text: String = "",
    var imageUrl: Uri? = null,
    var date: Date? = null,
    val isSeen: Boolean = false
)