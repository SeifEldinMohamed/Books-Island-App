package com.seif.booksislandapp.data.remote.dto

import com.google.firebase.firestore.ServerTimestamp
import java.util.Date

data class ReportDto(
    val id: String = "",
    val reporterId: String = "",
    val reporterName: String = "",
    val reportedPersonId: String = "",
    val reportedPersonName: String = "",
    val comment: String = "",
    val category: String = "",
    @ServerTimestamp
    val date: Date? = null,
    val reviewed: Boolean = false
)
