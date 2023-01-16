package com.seif.booksislandapp.domain.usecase.usecase.auth

import com.google.firebase.auth.FirebaseUser
import com.seif.booksislandapp.data.repository.AuthRepositoryImp
import javax.inject.Inject

class GetFirebaseCurrentUserUseCase @Inject constructor(
    private val authRepository: AuthRepositoryImp
) {
    operator fun invoke(): FirebaseUser? {
        return authRepository.getFirebaseCurrentUser()
    }
}