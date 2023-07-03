package com.seif.booksislandapp.domain.usecase.usecase.admin

import com.seif.booksislandapp.data.repository.UserRepositoryImp
import com.seif.booksislandapp.domain.model.User
import com.seif.booksislandapp.utils.Resource
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject

class GetAllUsersUseCase @Inject constructor(
    private val userRepositoryImp: UserRepositoryImp
) {
    suspend operator fun invoke(): Flow<Resource<ArrayList<User>, String>> {
        return userRepositoryImp.getAllUsers()
    }
}