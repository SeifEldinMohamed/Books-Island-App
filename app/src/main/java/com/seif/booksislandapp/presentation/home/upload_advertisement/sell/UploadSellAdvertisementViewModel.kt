package com.seif.booksislandapp.presentation.home.upload_advertisement.sell

import androidx.lifecycle.ViewModel
import com.seif.booksislandapp.domain.usecase.usecase.upload_adv.UploadSellAdvertisementUseCase
import javax.inject.Inject

class UploadSellAdvertisementViewModel @Inject constructor(
    private val uploadSellAdvertisementUseCase: UploadSellAdvertisementUseCase
) : ViewModel(){

}