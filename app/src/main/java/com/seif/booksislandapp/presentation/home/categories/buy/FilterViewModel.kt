package com.seif.booksislandapp.presentation.home.categories.buy

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel

class FilterViewModel : ViewModel() {
    private val mutableLiveData = MutableLiveData<FilterBy>()
    val liveData: LiveData<FilterBy> get() = mutableLiveData
    fun filter(filterBy: FilterBy) {
        mutableLiveData.value = filterBy
    }

    fun reset() {
        mutableLiveData.value = FilterBy()
    }
}
