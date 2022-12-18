package com.seif.booksislandapp.data.repository

import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.seif.booksislandapp.data.mapper.toUserDto
import com.seif.booksislandapp.domain.model.User
import com.seif.booksislandapp.domain.repository.AuthRepository
import com.seif.booksislandapp.utils.Constants.Companion.USER_FireStore_Collection
import com.seif.booksislandapp.utils.Resource
import kotlinx.coroutines.tasks.await
import java.lang.Exception
import javax.inject.Inject

class AuthRepositoryImp @Inject constructor(
    private val firestore: FirebaseFirestore,
    private val auth: FirebaseAuth
) : AuthRepository {
    private val TAG = "AuthRepositoryImp"
    override suspend fun register(user: User): Resource<User, String> {
        return try {
            val authResult = auth.createUserWithEmailAndPassword(user.email, user.password).await()
            authResult.user?.let { firebaseUser ->
                user.id = firebaseUser.uid
            }
            when (val result: Resource<String, String> = addUser(user)) {
                is Resource.Error -> Resource.Error(result.message)
                is Resource.Success -> Resource.Success(user)
            }
        } catch (e: Exception) {
            Resource.Error(e.message.toString())
        }
    }

    private suspend fun addUser(user: User): Resource<String, String> {
        return try {
            firestore.collection(USER_FireStore_Collection).document(user.id).set(user.toUserDto())
                .await()
            Resource.Success("User Added Successfully")
        } catch (e: Exception) {
            Resource.Error(e.message.toString())
        }
    }
}