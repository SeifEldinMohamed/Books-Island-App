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
    var myBuyingChats: List<String> = emptyList(),
    var mySellingChats: List<String> = emptyList(),
    var reportedPersonsIds: List<String> = emptyList(),
    var blockedUsersIds: List<String> = emptyList(),
)
