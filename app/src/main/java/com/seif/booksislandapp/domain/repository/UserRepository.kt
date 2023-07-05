package com.seif.booksislandapp.domain.repository

import com.google.firebase.auth.FirebaseUser
import com.seif.booksislandapp.domain.model.Recommendation
import com.seif.booksislandapp.domain.model.Report
import com.seif.booksislandapp.domain.model.User
import com.seif.booksislandapp.domain.model.adv.AdType
import com.seif.booksislandapp.utils.Resource
import kotlinx.coroutines.flow.Flow

interface UserRepository {
    fun getFirebaseCurrentUser(): FirebaseUser?
    suspend fun updateUserProfile(user: User): Resource<User, String>
    suspend fun getAllUsers(): Flow<Resource<ArrayList<User>, String>>
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
    suspend fun updateUserWishList(userId: String, adType: AdType, wishList: ArrayList<String>): Resource<String, String>
    suspend fun updateSuspendState(suspended: Boolean, userId: String): Resource<Boolean, String>
    suspend fun recommendForUser(userId: String): Resource<Recommendation, String>
}