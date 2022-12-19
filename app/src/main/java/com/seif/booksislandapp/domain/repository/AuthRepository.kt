package com.seif.booksislandapp.domain.repository

import com.seif.booksislandapp.domain.model.User
import com.seif.booksislandapp.utils.Resource

interface AuthRepository {
    suspend fun register(user: User): Resource<String, String>
    suspend fun login(email: String, password: String): Resource<String, String>
}