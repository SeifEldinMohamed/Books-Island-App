package com.seif.booksislandapp.domain.repository

import com.seif.booksislandapp.domain.model.Report
import com.seif.booksislandapp.utils.Resource
import kotlinx.coroutines.flow.Flow

interface AdminRepository {
    suspend fun getAllReports(): Flow<Resource<ArrayList<Report>, String>>
    suspend fun getAllReportsOnUsers(userId: String): Resource<ArrayList<Report>, String>
}