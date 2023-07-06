package com.seif.booksislandapp.domain.usecase.usecase.recommendation

import com.seif.booksislandapp.domain.model.Recommendation
import com.seif.booksislandapp.domain.repository.UserRepository
import com.seif.booksislandapp.utils.Resource
import javax.inject.Inject

class GetRecommendationForUserUseCase @Inject constructor(
    private val userRepository: UserRepository
) {
    suspend operator fun invoke(userId: String): Resource<Recommendation, String> {
        return userRepository.recommendForUser(userId)
    }
}