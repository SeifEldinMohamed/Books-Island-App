package com.seif.booksislandapp.domain.usecase.usecase.upload_adv

import com.seif.booksislandapp.domain.model.book.BooksToExchange
import com.seif.booksislandapp.utils.Resource
import com.seif.booksislandapp.utils.checkIsValidExchangeFor
import javax.inject.Inject

class UploadBookForExchangeUseCase @Inject constructor() {
    operator fun invoke(booksToExchange: BooksToExchange): Resource<BooksToExchange, String> {
        return when (val result = booksToExchange.checkIsValidExchangeFor()) {
            is Resource.Error -> {
                Resource.Error(result.message)
            }
            is Resource.Success -> Resource.Success(result.data)
        }
    }
}