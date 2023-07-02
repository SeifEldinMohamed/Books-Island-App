package com.seif.booksislandapp.domain.usecase.usecase.admin

import com.seif.booksislandapp.domain.repository.AdminRepository
import com.seif.booksislandapp.utils.Resource
import javax.inject.Inject

class SetReviewedUseCase @Inject constructor(
    private val adminRepository: AdminRepository
) {
    suspend operator fun invoke(reportId: String): Resource<String, String> {
        return adminRepository.setReportReviewed(reportId)
    }
}