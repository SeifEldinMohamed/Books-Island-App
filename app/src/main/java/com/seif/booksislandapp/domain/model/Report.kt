package com.seif.booksislandapp.domain.model

import android.os.Parcelable
import kotlinx.parcelize.Parcelize

@Parcelize
data class Report(
    var id: String,
    val reporterId: String,
    val reporterName: String,
    val reportedPersonId: String,
    val reportedPersonName: String,
    val comment: String,
    val category: String,
    val isReviewed: Boolean = false
    ) : Parcelable

