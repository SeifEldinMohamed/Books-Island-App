package com.seif.booksislandapp.domain.model

import android.os.Parcelable
import kotlinx.parcelize.Parcelize

@Parcelize
data class Rate(
    val reportedPersonId: String,
    var rate: Double
) : Parcelable
