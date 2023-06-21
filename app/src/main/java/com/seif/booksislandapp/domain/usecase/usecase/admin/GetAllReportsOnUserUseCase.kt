package com.seif.booksislandapp.domain.usecase.usecase.admin

import com.seif.booksislandapp.data.repository.AdminRepositoryImp
import com.seif.booksislandapp.domain.model.Report
import com.seif.booksislandapp.utils.Resource
import javax.inject.Inject

class GetAllReportsOnUserUseCase @Inject constructor(
    private val adminRepositoryImp: AdminRepositoryImp
) {
    suspend operator fun invoke(userId: String): Resource<ArrayList<Report>, String> {
        return adminRepositoryImp.getAllReportsOnUsers(userId)
    }
}