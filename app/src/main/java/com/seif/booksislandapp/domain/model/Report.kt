package com.seif.booksislandapp.domain.model

data class Report(
    var id: String,
    val reporterId: String,
    val reporterName: String,
    val reportedPersonId: String,
    val reportedPersonName: String,
    val comment: String,
    val category: String,
    val isReviewed: Boolean = false
)
