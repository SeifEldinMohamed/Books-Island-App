package com.seif.booksislandapp.data.remote.dto

data class UserDto(
    val id: String = "",
    val avatarImage: String = "",
    val username: String = "",
    val email: String = "",
    val password: String = "",
    val government: String,
    val district: String,
    val gender: String = ""
)
