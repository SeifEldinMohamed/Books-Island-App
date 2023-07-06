package com.seif.booksislandapp.domain.usecase.usecase.user

import com.seif.booksislandapp.domain.model.User
import com.seif.booksislandapp.domain.repository.UserRepository
import com.seif.booksislandapp.utils.Resource
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject

class GetUserByIdRealTime @Inject constructor(
    private val userRepository: UserRepository
) {
    suspend operator fun invoke(id: String): Flow<Resource<User, String>> {
        return userRepository.getUserByIdRealTime(id)
    }
}