package com.seif.booksislandapp.domain.usecase.usecase.auth

import com.seif.booksislandapp.domain.model.auth.District
import com.seif.booksislandapp.domain.repository.AuthRepository
import com.seif.booksislandapp.utils.Resource
import javax.inject.Inject

class GetDistrictsUseCase @Inject constructor(
    private val authRepository: AuthRepository
) {
    suspend operator fun invoke(governorateId: String): Resource<List<District>, String> {
        return authRepository.getDistrictsInGovernorate(governorateId)
    }
}