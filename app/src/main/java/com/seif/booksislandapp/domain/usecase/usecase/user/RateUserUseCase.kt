package com.seif.booksislandapp.domain.usecase.usecase.user

import com.seif.booksislandapp.data.repository.UserRepositoryImp
import com.seif.booksislandapp.domain.model.Rate
import com.seif.booksislandapp.utils.Resource
import com.seif.booksislandapp.utils.isValidRate
import javax.inject.Inject

class RateUserUseCase @Inject constructor(
    private val userRepositoryImp: UserRepositoryImp
) {
    suspend operator fun invoke(
        currentUserId: String,
        rate: Rate
    ): Resource<Pair<String, String>, String> {
        return when (val result = rate.isValidRate()) {
            is Resource.Error -> result
            is Resource.Success -> userRepositoryImp.rateUser(
                currentUserId = currentUserId,
                adProviderId = rate.reportedPersonId,
                rate = rate.rate
            )
        }
    }
}