package com.seif.booksislandapp.presentation.home.upload_advertisement.auction

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.google.firebase.auth.FirebaseUser
import com.seif.booksislandapp.domain.model.adv.auction.AuctionAdvertisement
import com.seif.booksislandapp.domain.model.request.MySentRequest
import com.seif.booksislandapp.domain.usecase.usecase.my_ads.auction.DeleteMyAuctionAdUseCase
import com.seif.booksislandapp.domain.usecase.usecase.my_ads.auction.EditMyAuctionAdvertisementUseCase
import com.seif.booksislandapp.domain.usecase.usecase.request.sent.SendRequestUseCase
import com.seif.booksislandapp.domain.usecase.usecase.shared_preference.GetFromSharedPreferenceUseCase
import com.seif.booksislandapp.domain.usecase.usecase.upload_adv.UploadAuctionAdvertisementUseCase
import com.seif.booksislandapp.domain.usecase.usecase.user.GetFirebaseCurrentUserUseCase
import com.seif.booksislandapp.presentation.home.upload_advertisement.UploadState
import com.seif.booksislandapp.utils.Resource
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import javax.inject.Inject

@HiltViewModel
class UploadAuctionViewModel @Inject constructor(
    private val uploadAuctionAdvertisementUseCase: UploadAuctionAdvertisementUseCase,
    private val deleteMyAuctionAdUseCase: DeleteMyAuctionAdUseCase,
    private val editMyAuctionAdvertisementUseCase: EditMyAuctionAdvertisementUseCase,
    private val getFirebaseCurrentUserUseCase: GetFirebaseCurrentUserUseCase,
    private val getFromSharedPreferenceUseCase: GetFromSharedPreferenceUseCase,
    private val sendRequestUseCase: SendRequestUseCase
) : ViewModel() {

    private val _uploadState = MutableStateFlow<UploadState>(UploadState.Init)
    val uploadState: StateFlow<UploadState> = _uploadState
    var isFirstTime: Boolean = true

    private fun showError(message: String) {
        _uploadState.value = UploadState.ShowError(message)
    }

    private fun setLoading(isLoading: Boolean) {
        when (isLoading) {
            true -> _uploadState.value = UploadState.IsLoading(true)
            false -> _uploadState.value = UploadState.IsLoading(false)
        }
    }

    fun uploadAuctionAdvertisement(auctionAdvertisement: AuctionAdvertisement) {
        setLoading(true)
        viewModelScope.launch(Dispatchers.IO) {
            when (val result = uploadAuctionAdvertisementUseCase(auctionAdvertisement)) {
                is Resource.Error -> {
                    withContext(Dispatchers.Main) {
                        setLoading(false)
                        showError(result.message)
                    }
                }
                is Resource.Success -> {
                    withContext(Dispatchers.Main) {
                        setLoading(false)
                    }
                    _uploadState.value = UploadState.UploadedSuccessfully(result.data)
                }
            }
        }
    }

    fun resetUploadStatus() {
        _uploadState.value = UploadState.Init
    }

    fun getFirebaseCurrentUser(): FirebaseUser? {
        return getFirebaseCurrentUserUseCase()
    }

    fun <T> getFromSP(key: String, clazz: Class<T>): T {
        return getFromSharedPreferenceUseCase(key, clazz)
    }

    fun requestUpdateMyAuctionAd(auctionAdvertisement: AuctionAdvertisement) {
        setLoading(true)
        viewModelScope.launch(Dispatchers.IO) {
            when (val result = editMyAuctionAdvertisementUseCase(auctionAdvertisement)) {
                is Resource.Error -> {
                    withContext(Dispatchers.Main) {
                        setLoading(false)
                        showError(result.message)
                    }
                }
                is Resource.Success -> {
                    withContext(Dispatchers.Main) {
                        setLoading(false)
                    }
                    _uploadState.value = UploadState.UpdatedSuccessfully(result.data)
                }
            }
        }
    }

    fun requestDeleteMyAuctionAd(myAdId: String) {
        setLoading(true)
        viewModelScope.launch(Dispatchers.IO) {
            when (val result = deleteMyAuctionAdUseCase(myAdId)) {
                is Resource.Error -> {
                    withContext(Dispatchers.Main) {
                        setLoading(false)
                        showError(result.message)
                    }
                }
                is Resource.Success -> {
                    withContext(Dispatchers.Main) {
                        setLoading(false)
                    }
                    _uploadState.value = UploadState.DeletedSuccessfully(result.data)
                }
            }
        }
    }

    fun sendRequest(mySentRequest: MySentRequest) {
        setLoading(true)
        viewModelScope.launch(Dispatchers.IO) {
            sendRequestUseCase(mySentRequest).let {
                when (it) {
                    is Resource.Error -> {
                        withContext(Dispatchers.Main) {
                            setLoading(false)
                            showError(it.message)
                        }
                    }
                    is Resource.Success -> {
                        withContext(Dispatchers.Main) {
                            setLoading(false)
                        }
                        _uploadState.value = UploadState.SendRequestSuccessfully(it.data)
                    }
                }
            }
        }
    }
}