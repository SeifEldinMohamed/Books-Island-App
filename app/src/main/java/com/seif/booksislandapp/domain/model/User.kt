package com.seif.booksislandapp.domain.model

data class User(
    var id: String,
    val avatarImage: String,
    val username: String,
    val email: String,
    val password: String,
    val address: String,
    val gender: String
)
