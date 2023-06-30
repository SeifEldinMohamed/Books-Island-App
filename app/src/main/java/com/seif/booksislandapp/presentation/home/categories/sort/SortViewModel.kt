package com.seif.booksislandapp.presentation.home.categories.sort

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject

@HiltViewModel
class SortViewModel @Inject constructor() :
    ViewModel() {
    private val mutableLiveData = MutableLiveData<String?>()
    val liveData: LiveData<String?> get() = mutableLiveData
    private var lastSort: String = ""

    fun sort(sortBy: String?) {
        mutableLiveData.value = sortBy
    }

    fun setLastSort(lastSort: String) {
        this.lastSort = lastSort
    }

    fun getLastSort(): String {
        return this.lastSort
    }
}