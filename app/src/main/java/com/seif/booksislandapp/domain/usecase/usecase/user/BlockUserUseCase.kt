package com.seif.booksislandapp.domain.usecase.usecase.user

import com.seif.booksislandapp.data.repository.UserRepositoryImp
import com.seif.booksislandapp.utils.Resource
import javax.inject.Inject

class BlockUserUseCase @Inject constructor(
    private val userRepositoryImp: UserRepositoryImp
) {
    suspend operator fun invoke(
        currentUserId: String,
        adProviderId: String,
        blockUser: Boolean
    ): Resource<String, String> {
        return userRepositoryImp.blockUser(currentUserId, adProviderId, blockUser)
    }
}