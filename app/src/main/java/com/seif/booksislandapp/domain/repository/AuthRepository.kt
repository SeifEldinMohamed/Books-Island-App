package com.seif.booksislandapp.domain.repository

import com.seif.booksislandapp.domain.model.User
import com.seif.booksislandapp.domain.model.auth.District
import com.seif.booksislandapp.domain.model.auth.Governorate
import com.seif.booksislandapp.utils.Resource

interface AuthRepository {
    suspend fun register(user: User): Resource<String, String>
    suspend fun login(email: String, password: String): Resource<String, String>
    suspend fun forgetPassword(email: String): Resource<String, String>
    suspend fun logout(): Resource<String, String>
    fun <T> saveInSharedPreference(key: String, data: T)
    fun <T> getFromSharedPreference(key: String, clazz: Class<T>): T
    suspend fun getGovernorates(): Resource<List<Governorate>, String>
    suspend fun getDistrictsInGovernorate(governorateId: String): Resource<List<District>, String>
}