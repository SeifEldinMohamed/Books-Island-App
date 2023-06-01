package com.seif.booksislandapp.presentation.home.upload_advertisement.exchange

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.google.firebase.auth.FirebaseUser
import com.seif.booksislandapp.domain.model.adv.AdType
import com.seif.booksislandapp.domain.model.adv.exchange.ExchangeAdvertisement
import com.seif.booksislandapp.domain.model.request.MySentRequest
import com.seif.booksislandapp.domain.usecase.usecase.my_ads.exchange.DeleteMyExchangeAdUseCase
import com.seif.booksislandapp.domain.usecase.usecase.my_ads.exchange.EditMyExchangeAdvertisementUseCase
import com.seif.booksislandapp.domain.usecase.usecase.request.sent.CancelSentRequestUseCase
import com.seif.booksislandapp.domain.usecase.usecase.request.sent.SendRequestUseCase
import com.seif.booksislandapp.domain.usecase.usecase.shared_preference.GetFromSharedPreferenceUseCase
import com.seif.booksislandapp.domain.usecase.usecase.upload_adv.UploadExchangeAdvertisementUseCase
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
class UploadExchangeViewModel @Inject constructor(
    private val uploadExchangeAdvertisementUseCase: UploadExchangeAdvertisementUseCase,
    private val deleteMyExchangeAdUseCase: DeleteMyExchangeAdUseCase,
    private val editMyExchangeAdvertisementUseCase: EditMyExchangeAdvertisementUseCase,
    private val getFirebaseCurrentUserUseCase: GetFirebaseCurrentUserUseCase,
    private val getFromSharedPreference: GetFromSharedPreferenceUseCase,
    private val sendRequestUseCase: SendRequestUseCase,
    private val cancelSentRequestUseCase: CancelSentRequestUseCase
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

    fun uploadExchangeAdvertisement(exchangeAdvertisement: ExchangeAdvertisement) {
        setLoading(true)
        viewModelScope.launch(Dispatchers.IO) {
            when (val result = uploadExchangeAdvertisementUseCase(exchangeAdvertisement)) {
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
        return getFromSharedPreference(key, clazz)
    }

    fun requestUpdateMyExchangeAd(exchangeAdvertisement: ExchangeAdvertisement) {
        setLoading(true)
        viewModelScope.launch(Dispatchers.IO) {
            when (val result = editMyExchangeAdvertisementUseCase(exchangeAdvertisement)) {
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

    fun requestDeleteMyExchangeAd(myAdId: String) {
        setLoading(true)
        viewModelScope.launch(Dispatchers.IO) {
            when (val result = deleteMyExchangeAdUseCase(myAdId)) {
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

    fun cancelRequest(requestId: String, adType: AdType, advertisementId: String) {
        setLoading(true)
        viewModelScope.launch(Dispatchers.IO) {
            cancelSentRequestUseCase(requestId, adType, advertisementId).let {
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
                        _uploadState.value =
                            UploadState.CancelSentRequestsSuccessfully(it.data)
                    }
                }
            }
        }
    }
}