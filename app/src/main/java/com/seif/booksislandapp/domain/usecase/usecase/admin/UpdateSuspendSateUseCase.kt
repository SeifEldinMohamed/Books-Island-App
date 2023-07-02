package com.seif.booksislandapp.domain.usecase.usecase.admin

import com.seif.booksislandapp.domain.repository.UserRepository
import com.seif.booksislandapp.utils.Resource
import javax.inject.Inject

class UpdateSuspendSateUseCase @Inject constructor(
    private val userRepository: UserRepository
) {
    suspend operator fun invoke(isSuspended: Boolean, userId: String): Resource<Boolean, String> {
        return userRepository.updateSuspendState(isSuspended, userId)
    }
}