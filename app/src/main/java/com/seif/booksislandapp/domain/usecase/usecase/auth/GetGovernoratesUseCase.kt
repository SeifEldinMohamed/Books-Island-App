package com.seif.booksislandapp.domain.usecase.usecase.auth

import com.seif.booksislandapp.domain.model.auth.Governorate
import com.seif.booksislandapp.domain.repository.AuthRepository
import com.seif.booksislandapp.utils.Resource
import javax.inject.Inject

class GetGovernoratesUseCase @Inject constructor(
    private val authRepository: AuthRepository
) {
    suspend operator fun invoke(): Resource<List<Governorate>, String> {
        return authRepository.getGovernorates()
    }
}