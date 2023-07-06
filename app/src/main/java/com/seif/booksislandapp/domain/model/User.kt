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
    var wishListBuy: ArrayList<String> = arrayListOf(),
    var wishListDonate: ArrayList<String> = arrayListOf(),
    var wishListExchange: ArrayList<String> = arrayListOf(),
    var wishListAuction: ArrayList<String> = arrayListOf(),
    var reportedPersonsIds: List<String> = emptyList(),
    var blockedUsersIds: List<String> = emptyList(),
    var usersBlockedMe: List<String> = emptyList(),
    var averageRate: String = "0.0",
    var givenRates: List<Rate> = emptyList(),
    var receivedRates: List<ReceivedRate> = emptyList(),
    var numberOfCompletedSellAds: Int = 0,
    var numberOfCompletedDonateAds: Int = 0,
    var numberOfCompletedExchangeAds: Int = 0,
    var numberOfCompletedAuctionAds: Int = 0,
    val isSuspended: Boolean = false
) : Parcelable
