package com.seif.booksislandapp.domain.usecase.usecase.user

import com.seif.booksislandapp.data.repository.UserRepositoryImp
import com.seif.booksislandapp.domain.model.User
import com.seif.booksislandapp.utils.Resource
import com.seif.booksislandapp.utils.isValidUser
import javax.inject.Inject

class UpdateUserProfileUseCase @Inject constructor(
    private val userRepositoryImp: UserRepositoryImp
) {
    suspend operator fun invoke(user: User): Resource<User, String> {
        return when (val result = user.isValidUser()) {
            is Resource.Error -> Resource.Error(result.message)
            is Resource.Success -> {
                userRepositoryImp.updateUserProfile(user)
            }
        }
    }
}