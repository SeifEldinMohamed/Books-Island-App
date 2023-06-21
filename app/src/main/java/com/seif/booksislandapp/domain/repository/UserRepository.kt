package com.seif.booksislandapp.domain.repository

import com.google.firebase.auth.FirebaseUser
import com.seif.booksislandapp.domain.model.Report
import com.seif.booksislandapp.domain.model.User
import com.seif.booksislandapp.utils.Resource
import kotlinx.coroutines.flow.Flow

interface UserRepository {
    fun getFirebaseCurrentUser(): FirebaseUser?
    suspend fun updateUserProfile(user: User): Resource<User, String>
    suspend fun getAllUsers(): Flow<Resource<ArrayList<User>, String>>
    suspend fun fetchUsersIChatWith(currentUserId: String): Resource<List<User>, String>
    suspend fun reportUser(report: Report): Resource<String, String>
}