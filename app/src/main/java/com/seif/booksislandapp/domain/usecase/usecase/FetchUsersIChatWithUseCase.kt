package com.seif.booksislandapp.domain.usecase.usecase

import com.seif.booksislandapp.data.repository.UserRepositoryImp
import com.seif.booksislandapp.domain.model.User
import com.seif.booksislandapp.utils.Resource
import javax.inject.Inject

class FetchUsersIChatWithUseCase @Inject constructor(
    private val userRepositoryImp: UserRepositoryImp
) {
    suspend operator fun invoke(currentUserId: String): Resource<List<User>, String> {
        return userRepositoryImp.fetchUsersIChatWith(currentUserId)
    }
}