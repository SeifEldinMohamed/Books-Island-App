package com.seif.booksislandapp.domain.usecase.usecase.user

import com.seif.booksislandapp.data.repository.UserRepositoryImp
import com.seif.booksislandapp.domain.model.Report
import com.seif.booksislandapp.utils.Resource
import com.seif.booksislandapp.utils.isValidReport
import javax.inject.Inject

class ReportUserUseCase @Inject constructor(
    private val userRepositoryImp: UserRepositoryImp
) {
    suspend operator fun invoke(report: Report): Resource<String, String> {
        return when (val result = report.isValidReport()) {
            is Resource.Error -> result
            is Resource.Success -> userRepositoryImp.reportUser(report)
        }
    }
}