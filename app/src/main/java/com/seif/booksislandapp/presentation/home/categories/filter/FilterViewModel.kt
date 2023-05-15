package com.seif.booksislandapp.presentation.home.categories.filter

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.seif.booksislandapp.domain.usecase.usecase.advertisement.filter.FilterUseCase
import com.seif.booksislandapp.utils.Resource
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject
@HiltViewModel
class FilterViewModel @Inject constructor(
    private val filterUseCase: FilterUseCase

) :
    ViewModel() {
    var filter = false
    private val mutableLiveData = MutableLiveData<FilterBy?>()
    val liveData: LiveData<FilterBy?> get() = mutableLiveData

    fun filter(filterBy: FilterBy?) {
        mutableLiveData.value = filterBy
    }

    fun isValidFilter(filterBy: FilterBy): Resource<String, String> {
        return filterUseCase(filterBy)
    }
}
