package com.seif.booksislandapp.presentation.home.categories.recommendation

import com.seif.booksislandapp.domain.model.Recommendation

sealed class RecommendationState {
    object Init : RecommendationState()
    data class ShowError(val message: String) : RecommendationState()
    data class RecommendedSuccessfully(
        val recommendation: Recommendation
    ) : RecommendationState()
}
