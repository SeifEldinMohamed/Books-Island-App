package com.seif.booksislandapp.domain.model

import android.os.Parcelable
import kotlinx.parcelize.Parcelize

@Parcelize
data class ReceivedRate(
    val reporterId: String,
    var rate: Double,
) : Parcelable
