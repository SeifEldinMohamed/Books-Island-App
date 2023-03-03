package com.seif.booksislandapp.domain.usecase.usecase.user

import com.google.firebase.auth.FirebaseUser
import com.seif.booksislandapp.domain.repository.UserRepository
import javax.inject.Inject

class GetFirebaseCurrentUserUseCase @Inject constructor(
    private val userRepository: UserRepository
) {
    operator fun invoke(): FirebaseUser? {
        return userRepository.getFirebaseCurrentUser()
    }
}