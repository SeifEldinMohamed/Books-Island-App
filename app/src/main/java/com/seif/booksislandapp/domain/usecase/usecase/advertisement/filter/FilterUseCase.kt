package com.seif.booksislandapp.domain.usecase.usecase.advertisement.filter

import com.seif.booksislandapp.presentation.home.categories.filter.FilterBy
import com.seif.booksislandapp.utils.Resource
import com.seif.booksislandapp.utils.validateFilter
import javax.inject.Inject

class FilterUseCase @Inject constructor() {

    operator fun invoke(
        filterBy: FilterBy
    ): Resource<String, String> {

        return when (val result = filterBy.validateFilter()) {
            is Resource.Error -> Resource.Error(result.message)
            is Resource.Success -> Resource.Success("Valid Filter")
        }
    }
}