package com.seif.booksislandapp.domain.model.adv.auction

import android.os.Parcelable
import kotlinx.parcelize.Parcelize

@Parcelize
data class Bidder(
    val bidderId: String,
    val bidderName: String,
    val suggestedPrice: String
) : Parcelable
