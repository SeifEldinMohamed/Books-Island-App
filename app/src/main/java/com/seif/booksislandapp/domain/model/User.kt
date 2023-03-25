package com.seif.booksislandapp.domain.model

import android.os.Parcelable
import kotlinx.parcelize.Parcelize

@Parcelize
data class User(
    var id: String,
    val avatarImage: String,
    val username: String,
    val email: String,
    val password: String,
    val governorate: String,
    val district: String,
    val gender: String,
    var wishListBuy: List<String>
) : Parcelable
