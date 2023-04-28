package com.seif.booksislandapp.presentation.home.categories.buy

import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel

class FilterViewModel : ViewModel() {
    var category = MutableLiveData<String>()
    var governorate = MutableLiveData<String>()
    var district = MutableLiveData<String>()
}