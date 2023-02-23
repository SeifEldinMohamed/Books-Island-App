package com.seif.booksislandapp.presentation.home.ad_details.exchange

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.seif.booksislandapp.R
import com.seif.booksislandapp.domain.usecase.usecase.advertisement.exchange.FetchAllExchangeRelatedAdvertisementsUseCase
import com.seif.booksislandapp.domain.usecase.usecase.user.GetUserByIdUseCase
import com.seif.booksislandapp.utils.Resource
import com.seif.booksislandapp.utils.ResourceProvider
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import javax.inject.Inject
@HiltViewModel
class ExchangeDetailsViewModel @Inject constructor(
    private val resourceProvider: ResourceProvider,
    private val getUserByIdUseCase: GetUserByIdUseCase,
    private val fetchAllExchangeRelatedAdvertisementsUseCase: FetchAllExchangeRelatedAdvertisementsUseCase
) : ViewModel() {
    private var _exchangeDetailsState = MutableStateFlow<ExchangeDetailsState>(ExchangeDetailsState.Init)
    val exchangeDetailsState = _exchangeDetailsState.asStateFlow()

    fun getUserById(id: String) {
        setLoading(true)
        viewModelScope.launch(Dispatchers.IO) {
            getUserByIdUseCase(id).let {
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
                        _exchangeDetailsState.value = ExchangeDetailsState.GetUserByIdSuccessfully(it.data)
                    }
                }
            }
        }
    }

    fun fetchRelatedAds(adId: String, category: String) {
        setLoading(true)
        viewModelScope.launch(Dispatchers.IO) {
            fetchAllExchangeRelatedAdvertisementsUseCase(adId, category).let {
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
                        _exchangeDetailsState.value =
                            ExchangeDetailsState.FetchRelatedExchangeAdvertisementSuccessfully(it.data)
                    }
                }
            }
        }
    }

    private fun setLoading(status: Boolean) {
        when (status) {
            true -> {
                _exchangeDetailsState.value = ExchangeDetailsState.IsLoading(true)
            }
            false -> {
                _exchangeDetailsState.value = ExchangeDetailsState.IsLoading(false)
            }
        }
    }

    private fun showError(message: String) {
        when (message) {
            resourceProvider.string(R.string.no_internet_connection) -> {
                _exchangeDetailsState.value = ExchangeDetailsState.NoInternetConnection(message)
            }
            else -> {
                _exchangeDetailsState.value = ExchangeDetailsState.ShowError(message)
            }
        }
    }
}