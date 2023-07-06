package com.seif.booksislandapp.data.remote.dto.recommendation

data class RecommendationDto(
    var ownerId: String = "",
    var topCategory: ArrayList<String> = arrayListOf()
)