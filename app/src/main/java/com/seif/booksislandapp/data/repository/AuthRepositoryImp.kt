package com.seif.booksislandapp.data.repository

import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.gson.Gson
import com.seif.booksislandapp.R
import com.seif.booksislandapp.data.mapper.toUserDto
import com.seif.booksislandapp.domain.model.User
import com.seif.booksislandapp.domain.repository.AuthRepository
import com.seif.booksislandapp.utils.Constants.Companion.USER_FireStore_Collection
import com.seif.booksislandapp.utils.Constants.Companion.USER_KEY
import com.seif.booksislandapp.utils.Resource
import com.seif.booksislandapp.utils.ResourceProvider
import com.seif.booksislandapp.utils.SharedPrefs
import kotlinx.coroutines.tasks.await
import java.lang.Exception
import javax.inject.Inject

class AuthRepositoryImp @Inject constructor(
    private val firestore: FirebaseFirestore,
    private val auth: FirebaseAuth,
    private val resourceProvider: ResourceProvider,
    private val sharedPrefs: SharedPrefs
) : AuthRepository {
    private val TAG = "AuthRepositoryImp"
    override suspend fun register(user: User): Resource<String, String> {
        return try {
            val authResult = auth.createUserWithEmailAndPassword(user.email, user.password).await()
            authResult.user?.let { firebaseUser ->
                user.id = firebaseUser.uid
            }

            when (val result: Resource<String, String> = addUser(user)) {
                is Resource.Error -> Resource.Error(result.message)
                is Resource.Success -> {
                    // save user data in shared preference
                    val userJson = Gson().toJson(user)
                    sharedPrefs.put(USER_KEY, userJson)

                    Resource.Success(result.data)
                }
            }
        } catch (e: Exception) {
            Resource.Error(e.message.toString())
        }
    }

    private suspend fun addUser(user: User): Resource<String, String> {
        return try {
            firestore.collection(USER_FireStore_Collection).document(user.id).set(user.toUserDto())
                .await()
            Resource.Success(resourceProvider.string(R.string.user_added_successfully))
        } catch (e: Exception) {
            Resource.Error(e.message.toString())
        }
    }

    override suspend fun login(email: String, password: String): Resource<String, String> {
        return try {
            auth.signInWithEmailAndPassword(email, password).await()
            Resource.Success("Welcome Back")
        } catch (e: Exception) {
            Resource.Error(e.message.toString())
        }
    }
}
// val user:User = sharedPrefs.get(USER_KEY, User::class.java)