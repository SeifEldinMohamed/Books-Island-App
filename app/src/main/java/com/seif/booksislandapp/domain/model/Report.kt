package com.seif.booksislandapp.domain.model

data class Report(
    var id: String,
    val reporterId: String,
    val reportedPersonId: String,
    val comment: String,
    val category: String,
)
