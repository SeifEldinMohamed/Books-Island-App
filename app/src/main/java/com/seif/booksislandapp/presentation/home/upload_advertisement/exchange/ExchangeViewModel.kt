package com.seif.booksislandapp.presentation.home.upload_advertisement.exchange

import android.media.Image
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel

class ExchangeViewModel : ViewModel() {
    var name = MutableLiveData<String>()
    var auther = MutableLiveData<String>()
    var image = MutableLiveData<Image>()
}