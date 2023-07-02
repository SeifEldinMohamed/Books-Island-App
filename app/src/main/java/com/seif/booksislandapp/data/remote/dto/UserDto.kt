package com.seif.booksislandapp.data.remote.dto

data class UserDto(
    val id: String = "",
    val avatarImage: String = "",
    val username: String = "",
    val email: String = "",
    val password: String = "",
    val governorate: String = "",
    val district: String = "",
    val gender: String = "",
    var wishListBuy: List<String> = emptyList(),
    var wishListDonate: List<String> = emptyList(),
    var wishListExchange: List<String> = emptyList(),
    var wishListAuction: List<String> = emptyList(),
    var reportedPersonsIds: List<String> = emptyList(),
    var blockedUsersIds: List<String> = emptyList(),
    var averageRate: Double = 0.0,
    var givenRates: List<RateDto> = emptyList(),
    var receivedRates: List<ReceivedRateDto> = emptyList(),
    var numberOfCompletedSellAds: Int = 0,
    var numberOfCompletedDonateAds: Int = 0,
    var numberOfCompletedExchangeAds: Int = 0,
    var numberOfCompletedAuctionAds: Int = 0,
    var isSuspended: Boolean = false

)
