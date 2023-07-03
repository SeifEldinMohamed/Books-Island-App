package com.seif.booksislandapp.domain.usecase.usecase.admin

import com.seif.booksislandapp.data.repository.AdminRepositoryImp
import com.seif.booksislandapp.domain.model.Report
import com.seif.booksislandapp.utils.Resource
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject

class GetAllReportsUseCase @Inject constructor(
    private val adminRepositoryImp: AdminRepositoryImp
) {
    suspend operator fun invoke(): Flow<Resource<ArrayList<Report>, String>> {
        return adminRepositoryImp.getAllReports()
    }
}