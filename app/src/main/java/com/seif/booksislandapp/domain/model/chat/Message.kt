package com.seif.booksislandapp.domain.model.chat

import android.net.Uri
import java.util.*

data class Message(
    var id: String,
    val senderId: String,
    val receiverId: String,
    val text: String? = null,
    var imageUrl: Uri? = null,
    val date: Date
)