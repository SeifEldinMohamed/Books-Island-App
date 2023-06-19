package com.seif.booksislandapp.domain.repository

import com.google.firebase.auth.FirebaseUser
import com.seif.booksislandapp.domain.model.Report
import com.seif.booksislandapp.domain.model.User
import com.seif.booksislandapp.utils.Resource

interface UserRepository {
    fun getFirebaseCurrentUser(): FirebaseUser?
    suspend fun updateUserProfile(user: User): Resource<User, String>
    suspend fun fetchUsersIChatWith(currentUserId: String): Resource<List<User>, String>
    suspend fun reportUser(report: Report): Resource<String, String>
    suspend fun blockUser(
        currentUserId: String,
        adProviderId: String,
        blockUser: Boolean
    ): Resource<String, String>

    suspend fun rateUser(
        currentUserId: String,
        adProviderId: String,
        rate: Double
    ): Resource<Pair<String, String>, String>
}