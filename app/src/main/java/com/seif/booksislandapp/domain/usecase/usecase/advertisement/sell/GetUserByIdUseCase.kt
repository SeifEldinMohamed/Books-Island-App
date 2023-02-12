package com.seif.booksislandapp.domain.usecase.usecase.advertisement.sell

import com.seif.booksislandapp.data.repository.AdvertisementRepositoryImp
import com.seif.booksislandapp.domain.model.User
import com.seif.booksislandapp.utils.Resource
import javax.inject.Inject

class GetUserByIdUseCase @Inject constructor(
    private val advertisementRepositoryImp: AdvertisementRepositoryImp
) {
    suspend operator fun invoke(id: String): Resource<User, String> {
        return if (id.isNotEmpty()) {
            advertisementRepositoryImp.getUserById(id)
        } else {
            Resource.Error("user id is empty!")
        }
    }
}