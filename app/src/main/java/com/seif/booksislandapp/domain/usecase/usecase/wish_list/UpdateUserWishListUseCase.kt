package com.seif.booksislandapp.domain.usecase.usecase.wish_list

import com.seif.booksislandapp.data.repository.UserRepositoryImp
import com.seif.booksislandapp.domain.model.adv.AdType
import com.seif.booksislandapp.utils.Resource
import javax.inject.Inject

class UpdateUserWishListUseCase@Inject constructor(
    private val userRepositoryImp: UserRepositoryImp
) {

    suspend operator fun invoke(userId: String, adType: AdType, wishList: ArrayList<String>): Resource<String, String> {

        return userRepositoryImp.updateUserWishList(userId, adType, wishList)
    }
}