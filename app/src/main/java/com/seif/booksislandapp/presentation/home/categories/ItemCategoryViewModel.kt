package com.seif.booksislandapp.presentation.home.categories

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel

class ItemCategoryViewModel : ViewModel() {
    private val mutableSelectedCategoryItem = MutableLiveData<String>()
    val selectedCategoryItem: LiveData<String> get() = mutableSelectedCategoryItem

    fun selectItem(categoryName: String) {
        mutableSelectedCategoryItem.value = categoryName
    }
}