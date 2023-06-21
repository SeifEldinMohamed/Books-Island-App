package com.seif.booksislandapp.data.repository

import android.net.ConnectivityManager
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.firestore.FirebaseFirestore
import com.seif.booksislandapp.R
import com.seif.booksislandapp.data.mapper.toReportDto
import com.seif.booksislandapp.data.mapper.toUser
import com.seif.booksislandapp.data.remote.dto.UserDto
import com.seif.booksislandapp.domain.model.Report
import com.seif.booksislandapp.domain.model.User
import com.seif.booksislandapp.domain.repository.UserRepository
import com.seif.booksislandapp.utils.Constants
import com.seif.booksislandapp.utils.Constants.Companion.CHAT_LIST_FIIRESTORE_COLLECTION
import com.seif.booksislandapp.utils.Constants.Companion.REPORTS_FIIRESTORE_COLLECTION
import com.seif.booksislandapp.utils.Constants.Companion.USER_FIRESTORE_COLLECTION
import com.seif.booksislandapp.utils.Resource
import com.seif.booksislandapp.utils.ResourceProvider
import com.seif.booksislandapp.utils.SharedPrefs
import com.seif.booksislandapp.utils.checkInternetConnection
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.callbackFlow
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

    override suspend fun reportUser(report: Report): Resource<String, String> {
        return try {
            val documentReference = firestore.collection(REPORTS_FIIRESTORE_COLLECTION).document()
            report.id = documentReference.id
            documentReference.set(report.toReportDto()).await()

            val userDocumentReference = firestore.collection(USER_FIRESTORE_COLLECTION)
                .document(report.reporterId)

            val userDocumentSnapshot = userDocumentReference.get().await()
            val userDto = userDocumentSnapshot.toObject(UserDto::class.java)
            val reportedIds: List<String> = userDto?.reportedPersonsIds ?: emptyList()
            val reportedIdsArrayList = reportedIds.toCollection(ArrayList())

            reportedIdsArrayList.add(report.reportedPersonId)
            userDocumentReference.update("reportedPersonsIds", reportedIdsArrayList)
                .await()

            Resource.Success("report sent successfully!")
        } catch (e: Exception) {
            Resource.Error(e.message.toString())
        }
    }

    private fun saveUserData(user: User) {
        sharedPrefs.put(Constants.USER_ID_KEY, user.id)
        sharedPrefs.put(Constants.USERNAME_KEY, user.username)
        sharedPrefs.put(Constants.USER_GOVERNORATE_KEY, user.governorate)
        sharedPrefs.put(Constants.USER_DISTRICT_KEY, user.district)
        sharedPrefs.put(Constants.USER_AVATAR_KEY, user.avatarImage)
    }
    override suspend fun getAllUsers() = callbackFlow {
        if (!connectivityManager.checkInternetConnection())
            trySend(Resource.Error(resourceProvider.string(R.string.no_internet_connection)))

        try {
            withTimeout(Constants.TIMEOUT) {
                firestore.collection(USER_FIRESTORE_COLLECTION)
                    .addSnapshotListener { usersQuerySnapshot, error ->
                        if (error != null) {
                            trySend(Resource.Error(error.message.toString()))
                        }
                        if (usersQuerySnapshot != null) {
                            val allUsersDto = arrayListOf<UserDto>()
                            for (document in usersQuerySnapshot) {
                                val userDto = document.toObject(UserDto::class.java)
                                allUsersDto.add(userDto)
                            }
                            trySend(
                                Resource.Success(
                                    data = allUsersDto.map { userDto ->
                                        userDto.toUser()
                                    }.toCollection(java.util.ArrayList())
                                )
                            )
                        }
                    }
            }
        } catch (e: Exception) {
            trySend(Resource.Error(e.message.toString()))
        }
        awaitClose { }
    }
}