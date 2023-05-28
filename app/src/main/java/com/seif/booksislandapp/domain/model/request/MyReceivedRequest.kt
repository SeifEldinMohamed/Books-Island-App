package com.seif.booksislandapp.domain.model.request

import com.seif.booksislandapp.domain.model.adv.AdType
import java.util.*

data class MyReceivedRequest(
    var id: String,
    val senderId: String,
    val receiverId: String,
    val advertisementId: String,
    val username: String,
    val avatarImage: String,
    val bookTitle: String,
    val condition: String,
    val category: String,
    val adType: AdType,
    val edition: String,
    val date: Date? = null,
)
