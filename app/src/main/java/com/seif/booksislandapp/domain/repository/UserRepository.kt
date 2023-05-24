package com.seif.booksislandapp.domain.repository

import com.google.firebase.auth.FirebaseUser
import com.seif.booksislandapp.domain.model.User
import com.seif.booksislandapp.utils.Resource

interface UserRepository {
    fun getFirebaseCurrentUser(): FirebaseUser?
    suspend fun updateUserProfile(user: User): Resource<User, String>
    suspend fun fetchUsersIChatWith(currentUserId: String): Resource<List<User>, String>
}