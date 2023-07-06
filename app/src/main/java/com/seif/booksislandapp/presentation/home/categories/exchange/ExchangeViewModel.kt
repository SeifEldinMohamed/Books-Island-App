package com.seif.booksislandapp.presentation.home.categories.exchange

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.seif.booksislandapp.R
import com.seif.booksislandapp.domain.usecase.usecase.advertisement.exchange.GetAllExchangeAdvertisementUseCase
import com.seif.booksislandapp.domain.usecase.usecase.advertisement.exchange.GetExchangeAdsByFilterUseCase
import com.seif.booksislandapp.domain.usecase.usecase.advertisement.exchange.SearchExchangeAdvertisementUseCase
import com.seif.booksislandapp.presentation.home.categories.filter.FilterBy
import com.seif.booksislandapp.utils.DispatcherProvider
import com.seif.booksislandapp.utils.Resource
import com.seif.booksislandapp.utils.ResourceProvider
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.*
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import javax.inject.Inject
@HiltViewModel
class ExchangeViewModel @Inject constructor(
    private val getAllExchangeAdvertisementUseCase: GetAllExchangeAdvertisementUseCase,
    private val searchExchangeAdvertisementUseCase: SearchExchangeAdvertisementUseCase,
    private val getExchangeAdsByFilterUseCase: GetExchangeAdsByFilterUseCase,
    private val resourceProvider: ResourceProvider,
    private val dispatcher: DispatcherProvider
) : ViewModel() {
    private var _exchangeState = MutableStateFlow<ExchangeState>(ExchangeState.Init)
    val exchangeState = _exchangeState.asStateFlow()
    private var searchJob: Job? = null
    var firstTime = true
    var isSearching = false
    fun fetchAllExchangeAds() {
        setLoading(true)
        viewModelScope.launch(dispatcher.io) {
            getAllExchangeAdvertisementUseCase.invoke().let {
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
                        _exchangeState.value = ExchangeState.FetchAllExchangeAdsSuccessfully(it.data)
                    }
                }
            }
        }
    }

    fun searchExchangeAds(searchQuery: String) {
        searchJob?.cancel()
        searchJob = viewModelScope.launch(Dispatchers.IO) {
            delay(500)
            withContext(Dispatchers.Main) {
                setLoading(true)
            }
            searchExchangeAdvertisementUseCase(searchQuery).let {
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
                        _exchangeState.value = ExchangeState.SearchExchangeAdsSuccessfully(it.data)
                    }
                }
            }
        }
    }

    fun fetchExchangeAdvertisementByFilter(
        filterBy: FilterBy
    ) {
        setLoading(true)
        viewModelScope.launch(Dispatchers.IO) {
            getExchangeAdsByFilterUseCase.invoke(filterBy).let {
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
                        _exchangeState.value = ExchangeState.FetchAllExchangeAdsSuccessfully(it.data)
                    }
                }
            }
        }
    }

    private fun setLoading(status: Boolean) {
        when (status) {
            true -> {
                _exchangeState.value = ExchangeState.IsLoading(true)
            }
            false -> {
                _exchangeState.value = ExchangeState.IsLoading(false)
            }
        }
    }
    private fun showError(message: String) {
        when (message) {
            resourceProvider.string(R.string.no_internet_connection) -> {
                _exchangeState.value = ExchangeState.NoInternetConnection(message)
            }
            else -> {
                _exchangeState.value = ExchangeState.ShowError(message)
            }
        }
    }
    fun resetState() {
        _exchangeState.value = ExchangeState.Init
    }
}