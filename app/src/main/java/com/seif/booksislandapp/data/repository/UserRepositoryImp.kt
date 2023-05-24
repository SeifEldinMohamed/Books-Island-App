package com.seif.booksislandapp.data.repository

import android.net.ConnectivityManager
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.firestore.FirebaseFirestore
import com.seif.booksislandapp.R
import com.seif.booksislandapp.data.mapper.toUser
import com.seif.booksislandapp.data.remote.dto.UserDto
import com.seif.booksislandapp.domain.model.User
import com.seif.booksislandapp.domain.repository.UserRepository
import com.seif.booksislandapp.utils.*
import com.seif.booksislandapp.utils.Constants.Companion.CHAT_LIST_FIIRESTORE_COLLECTION
import com.seif.booksislandapp.utils.Constants.Companion.USER_FIRESTORE_COLLECTION
import kotlinx.coroutines.tasks.await
import kotlinx.coroutines.withTimeout
import javax.inject.Inject

class UserRepositoryImp @Inject constructor(
    private val firestore: FirebaseFirestore,
    private val auth: FirebaseAuth,
    private val resourceProvider: ResourceProvider,
    private val sharedPrefs: SharedPrefs,
    private val connectivityManager: ConnectivityManager
) : UserRepository {

    override fun getFirebaseCurrentUser(): FirebaseUser? {
        return auth.currentUser
    }

    override suspend fun updateUserProfile(user: User): Resource<User, String> {
        if (!connectivityManager.checkInternetConnection()) // remove this check if we want get cached data
            return Resource.Error(resourceProvider.string(R.string.no_internet_connection))

        return try {
            withTimeout(Constants.TIMEOUT) {
//                val credential =  EmailAuthProvider.getCredential(email,oldpass)
//                auth.currentUser.reauthenticate()
//                auth.currentUser!!.updateEmail(user.email).await()
                firestore.collection(USER_FIRESTORE_COLLECTION).document(user.id)
                    .set(user)
                    .await()
                saveUserData(user)
                Resource.Success(user)
            }
        } catch (e: Exception) {
            Resource.Error(message = e.message.toString())
        }
    }

    override suspend fun fetchUsersIChatWith(currentUserId: String): Resource<List<User>, String> {
        if (!connectivityManager.checkInternetConnection()) // remove this check if we want get cached data
            return Resource.Error(resourceProvider.string(R.string.no_internet_connection))

        return try {
            withTimeout(Constants.TIMEOUT) {
                val chatListDocumentSnapshot =
                    firestore.collection(CHAT_LIST_FIIRESTORE_COLLECTION).document(currentUserId)
                        .get()
                        .await()
                val usersIds = chatListDocumentSnapshot.get("ids") as ArrayList<String>

                // get users
                val usersQuerySnapShot = firestore.collection(USER_FIRESTORE_COLLECTION)
                    .get()
                    .await()

                val usersIChatWith = arrayListOf<UserDto>()
                for (snapShot in usersQuerySnapShot) {
                    val userDto = snapShot.toObject(UserDto::class.java)
                    usersIds.forEach { id ->
                        if (userDto.id == id)
                            usersIChatWith.add(userDto)
                    }
                }

                Resource.Success(usersIChatWith.map { it.toUser() })
            }
        } catch (e: Exception) {
            Resource.Error(message = e.message.toString())
        }
    }

    private fun saveUserData(user: User) {
        sharedPrefs.put(Constants.USER_ID_KEY, user.id)
        sharedPrefs.put(Constants.USERNAME_KEY, user.username)
        sharedPrefs.put(Constants.USER_GOVERNORATE_KEY, user.governorate)
        sharedPrefs.put(Constants.USER_DISTRICT_KEY, user.district)
        sharedPrefs.put(Constants.USER_AVATAR_KEY, user.avatarImage)
    }
}