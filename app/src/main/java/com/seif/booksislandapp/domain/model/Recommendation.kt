package com.seif.booksislandapp.domain.model

data class Recommendation(
    var ownerId: String,
    var topCategory: ArrayList<String> = arrayListOf()
)
