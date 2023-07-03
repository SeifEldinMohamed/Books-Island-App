package com.seif.booksislandapp.data.repository

import android.net.ConnectivityManager
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.Query
import com.seif.booksislandapp.R
import com.seif.booksislandapp.data.mapper.toReport
import com.seif.booksislandapp.data.remote.dto.ReportDto
import com.seif.booksislandapp.domain.model.Report
import com.seif.booksislandapp.domain.repository.AdminRepository
import com.seif.booksislandapp.utils.*
import com.seif.booksislandapp.utils.Constants.Companion.REPORTS_FIIRESTORE_COLLECTION
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.tasks.await
import kotlinx.coroutines.withTimeout
import javax.inject.Inject

class AdminRepositoryImp @Inject constructor(
    private val firestore: FirebaseFirestore,
    private val resourceProvider: ResourceProvider,
    private val connectivityManager: ConnectivityManager
) : AdminRepository {
    override suspend fun getAllReports() = callbackFlow {
        if (!connectivityManager.checkInternetConnection())
            trySend(Resource.Error(resourceProvider.string(R.string.no_internet_connection)))

        try {
            withTimeout(Constants.TIMEOUT) {
                firestore.collection(REPORTS_FIIRESTORE_COLLECTION)
                    .addSnapshotListener { reportsQuerySnapshot, error ->
                        if (error != null) {
                            trySend(Resource.Error(error.message.toString()))
                        }
                        if (reportsQuerySnapshot != null) {
                            val allReportsDto = arrayListOf<ReportDto>()
                            for (document in reportsQuerySnapshot) {
                                val reportDto = document.toObject(ReportDto::class.java)
                                if (!reportDto.reviewed)
                                    allReportsDto.add(reportDto)
                            }
                            trySend(
                                Resource.Success(
                                    data = allReportsDto.map { reportDto ->
                                        reportDto.toReport()
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

    override suspend fun getAllReportsOnUsers(userId: String): Resource<ArrayList<Report>, String> {
        if (!connectivityManager.checkInternetConnection())
            return Resource.Error(resourceProvider.string(R.string.no_internet_connection))
        return try {
            withTimeout(Constants.TIMEOUT) {

                delay(500) // to show loading progress

                val querySnapshot = firestore.collection(REPORTS_FIIRESTORE_COLLECTION)
                    .whereEqualTo("reportedPersonId", userId)
                    .whereNotEqualTo("reviewed", true)
                    .orderBy("reviewed", Query.Direction.DESCENDING)
                    .get()
                    .await()
                val reportsDto = arrayListOf<ReportDto>()
                for (document in querySnapshot) {
                    val reportDto =
                        document.toObject(ReportDto::class.java)
                    reportsDto.add(reportDto)
                }
                Resource.Success(
                    data = reportsDto.map { reportDto ->
                        reportDto.toReport()
                    }.toCollection(ArrayList())
                )
            }
        } catch (e: Exception) {
            Resource.Error(e.message.toString())
        }
    }

    override suspend fun setReportReviewed(reportId: String): Resource<String, String> {
        if (!connectivityManager.checkInternetConnection()) // remove this check if we want get cached data
            return Resource.Error(resourceProvider.string(R.string.no_internet_connection))

        return try {
            withTimeout(Constants.TIMEOUT) {
                firestore.collection(REPORTS_FIIRESTORE_COLLECTION).document(reportId)
                    .update("reviewed", true)
                    .await()
                Resource.Success("Reviewed")
            }
        } catch (e: Exception) {
            Resource.Error(message = e.message.toString())
        }
    }
}