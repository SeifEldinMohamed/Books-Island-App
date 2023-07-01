package com.seif.booksislandapp.data.repository

import android.net.ConnectivityManager
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.messaging.FirebaseMessaging
import com.seif.booksislandapp.R
import com.seif.booksislandapp.data.mapper.toDistricts
import com.seif.booksislandapp.data.mapper.toGovernorate
import com.seif.booksislandapp.data.mapper.toUser
import com.seif.booksislandapp.data.mapper.toUserDto
import com.seif.booksislandapp.data.remote.dto.UserDto
import com.seif.booksislandapp.data.remote.dto.auth.DistrictDto
import com.seif.booksislandapp.data.remote.dto.auth.GovernorateDto
import com.seif.booksislandapp.domain.model.User
import com.seif.booksislandapp.domain.model.admin.Admin
import com.seif.booksislandapp.domain.model.auth.District
import com.seif.booksislandapp.domain.model.auth.Governorate
import com.seif.booksislandapp.domain.repository.AuthRepository
import com.seif.booksislandapp.utils.*
import com.seif.booksislandapp.utils.Constants.Companion.DISTRICTS_FIRESTORE_COLLECTION
import com.seif.booksislandapp.utils.Constants.Companion.GOVERNORATES_FIRESTORE_COLLECTION
import com.seif.booksislandapp.utils.Constants.Companion.IS_LOGGED_IN_KEY
import com.seif.booksislandapp.utils.Constants.Companion.USERNAME_KEY
import com.seif.booksislandapp.utils.Constants.Companion.USER_AVATAR_KEY
import com.seif.booksislandapp.utils.Constants.Companion.USER_DISTRICT_KEY
import com.seif.booksislandapp.utils.Constants.Companion.USER_FIRESTORE_COLLECTION
import com.seif.booksislandapp.utils.Constants.Companion.USER_GOVERNORATE_KEY
import com.seif.booksislandapp.utils.Constants.Companion.USER_ID_KEY
import kotlinx.coroutines.delay
import kotlinx.coroutines.tasks.await
import kotlinx.coroutines.withTimeout
import timber.log.Timber
import javax.inject.Inject

class AuthRepositoryImp @Inject constructor(
    private val firestore: FirebaseFirestore,
    private val auth: FirebaseAuth,
    private val resourceProvider: ResourceProvider,
    private val sharedPrefs: SharedPrefs,
    private val connectivityManager: ConnectivityManager,
    private val fcm: FirebaseMessaging
) : AuthRepository {
    override suspend fun register(user: User): Resource<String, String> {
        if (!connectivityManager.checkInternetConnection())
            return Resource.Error(resourceProvider.string(R.string.no_internet_connection))

        return try {
            withTimeout(Constants.TIMEOUT_AUTH) {
                val authResult =
                    auth.createUserWithEmailAndPassword(user.email, user.password).await()
                authResult.user?.let { firebaseUser ->
                    user.id = firebaseUser.uid
                }
                // create user with email and password
                when (val result: Resource<String, String> = addUser(user)) {
                    is Resource.Error -> Resource.Error(result.message)
                    is Resource.Success -> {
                        // save user data in shared preference
                        Timber.d("register: upload user data to firestore")
                        saveUserData(user)
                        Timber.d("register: saved user data in shared preference")
                        // update token
                        val token = fcm.token.await()
                        updateToken(user.id, token)
                        Timber.d("register: update token")
                        Resource.Success(result.data)
                    }
                }
            }
        } catch (e: Exception) {
            Resource.Error(e.message.toString())
        }
    }

    private fun updateToken(userId: String, token: String) {
        firestore.collection(Constants.TOKENS_FIIRESTORE_COLLECTION).document(userId)
            .set(hashMapOf("token" to token))
    }

    private fun saveUserData(user: User) {
        sharedPrefs.put(USER_ID_KEY, user.id)
        sharedPrefs.put(USERNAME_KEY, user.username)
        sharedPrefs.put(USER_GOVERNORATE_KEY, user.governorate)
        sharedPrefs.put(USER_DISTRICT_KEY, user.district)
        sharedPrefs.put(USER_AVATAR_KEY, user.avatarImage)
    }
    private fun saveAdminData(admin: Admin) {
        sharedPrefs.put(USER_ID_KEY, admin.id)
    }

    private suspend fun addUser(user: User): Resource<String, String> {
        return try {
            withTimeout(Constants.TIMEOUT) {
                firestore.collection(USER_FIRESTORE_COLLECTION).document(user.id)
                    .set(user.toUserDto())
                    .await()
                Resource.Success(resourceProvider.string(R.string.user_added_successfully))
            }
        } catch (e: Exception) {
            Resource.Error(e.message.toString())
        }
    }

    override suspend fun login(email: String, password: String): Resource<String, String> {
        if (!connectivityManager.checkInternetConnection())
            return Resource.Error(resourceProvider.string(R.string.no_internet_connection))

        return try {
            withTimeout(Constants.TIMEOUT_AUTH) {
                if (Constants.ADMINS_LIST.contains(Admin("", email, password))) {
                    saveAdminData(Admin("", email, password))
                    Resource.Success(resourceProvider.string(R.string.admin))
                } else {
                    auth.signInWithEmailAndPassword(email, password).await()
                    when (val result = getUserById(auth.currentUser!!.uid)) {
                        is Resource.Error -> Resource.Error(result.message)
                        is Resource.Success -> {
                            // save user data
                            saveUserData(result.data)
                            // update token
                            val token = fcm.token.await()
                            updateToken(auth.currentUser!!.uid, token)
                            Resource.Success(resourceProvider.string(R.string.welcome_back))
                        }
                    }
                }
            }
        } catch (e: Exception) {
            Resource.Error(e.message.toString())
        }
    }

    private suspend fun getUserById(id: String): Resource<User, String> {
        if (!connectivityManager.checkInternetConnection())
            return Resource.Error(resourceProvider.string(R.string.no_internet_connection))
        return try {
            withTimeout(Constants.TIMEOUT) {

                delay(500) // to show loading progress
                val querySnapshot = firestore.collection(USER_FIRESTORE_COLLECTION).document(id)
                    .get()
                    .await()
                val user = querySnapshot.toObject(UserDto::class.java)
                Resource.Success(
                    data = user!!.toUser()
                )
            }
        } catch (e: Exception) {
            Resource.Error(e.message.toString())
        }
    }

    override suspend fun forgetPassword(email: String): Resource<String, String> {
        if (!connectivityManager.checkInternetConnection())
            return Resource.Error(resourceProvider.string(R.string.no_internet_connection))

        return try {
            withTimeout(Constants.TIMEOUT_AUTH) {
                auth.sendPasswordResetEmail(email).await()
                Resource.Success(resourceProvider.string(R.string.send_mail_to_reset_password))
            }
        } catch (e: Exception) {
            Resource.Error(e.message.toString())
        }
    }

    override suspend fun logout(): Resource<String, String> {
        if (!connectivityManager.checkInternetConnection())
            return Resource.Error(resourceProvider.string(R.string.no_internet_connection))

        return try {
            withTimeout(Constants.TIMEOUT_AUTH) {
                auth.signOut()
                saveInSharedPreference(IS_LOGGED_IN_KEY, false)
                Resource.Success(resourceProvider.string(R.string.logged_out_successfully))
            }
        } catch (e: Exception) {
            Resource.Error(e.message.toString())
        }
    }

    override fun <T> saveInSharedPreference(key: String, data: T) {
        sharedPrefs.put(key, data)
    }

    override fun <T> getFromSharedPreference(key: String, clazz: Class<T>): T {
        return sharedPrefs.get(key, clazz)
    }

    override suspend fun getGovernorates(): Resource<List<Governorate>, String> {
        if (!connectivityManager.checkInternetConnection())
            return Resource.Error(resourceProvider.string(R.string.no_internet_connection))

        return try {
            withTimeout(Constants.TIMEOUT_AUTH) {
                val querySnapShot = firestore.collection(GOVERNORATES_FIRESTORE_COLLECTION)
                    .orderBy("name").get()
                    .await()
                val governorates = arrayListOf<GovernorateDto>()
                for (document in querySnapShot) {
                    val governorate = document.toObject(GovernorateDto::class.java)
                    governorates.add(governorate)
                }
                Resource.Success(data = governorates.map { it.toGovernorate() })
            }
        } catch (e: Exception) {
            Resource.Error(e.message.toString())
        }
    }

    override suspend fun getDistrictsInGovernorate(governorateId: String): Resource<List<District>, String> {
        if (!connectivityManager.checkInternetConnection())
            return Resource.Error(resourceProvider.string(R.string.no_internet_connection))

        return try {
            withTimeout(Constants.TIMEOUT_AUTH) {
                val querySnapShot = firestore.collection(DISTRICTS_FIRESTORE_COLLECTION)
                    .whereEqualTo("governorateId", governorateId)
                    .orderBy("name").get()
                    .await()
                val districts = arrayListOf<DistrictDto>()
                for (document in querySnapShot) {
                    val district = document.toObject(DistrictDto::class.java)
                    districts.add(district)
                }
                Resource.Success(data = districts.map { it.toDistricts() })
            }
        } catch (e: Exception) {
            Resource.Error(e.message.toString())
        }
    }
}
// val user:User = sharedPrefs.get(USER_KEY, User::class.java)