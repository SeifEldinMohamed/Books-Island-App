package com.seif.booksislandapp.presentation.home.categories

import androidx.lifecycle.ViewModel
import kotlinx.coroutines.flow.MutableStateFlow

class ItemCategoryViewModel : ViewModel() {
    private val mutableSelectedCategoryItem = MutableStateFlow("Choose Category")
    val selectedCategoryItem get() = mutableSelectedCategoryItem

    fun selectItem(categoryName: String) {
        mutableSelectedCategoryItem.value = categoryName
    }
    fun reset() {
        selectedCategoryItem.value = "Choose Category"
    }
}