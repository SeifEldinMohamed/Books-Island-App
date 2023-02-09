package com.seif.booksislandapp.presentation.home.categories.buy

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.seif.booksislandapp.R
import com.seif.booksislandapp.domain.usecase.usecase.advertisement.sell.GetAllSellAdvertisementUseCase
import com.seif.booksislandapp.domain.usecase.usecase.advertisement.sell.SearchSellAdvertisementUseCase
import com.seif.booksislandapp.utils.Resource
import com.seif.booksislandapp.utils.ResourceProvider
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.*
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import timber.log.Timber
import javax.inject.Inject

@HiltViewModel
class BuyViewModel @Inject constructor(
    private val getAllSellAdvertisementUseCase: GetAllSellAdvertisementUseCase,
    private val searchSellAdvertisementUseCase: SearchSellAdvertisementUseCase,
    private val resourceProvider: ResourceProvider
) : ViewModel() {
    private var _buyState = MutableStateFlow<BuyState>(BuyState.Init)
    val buyState = _buyState.asStateFlow()
    private var searchJob: Job? = null

    init {
        fetchAllSellAdvertisement()
    }

    fun fetchAllSellAdvertisement() {
        setLoading(true)
        viewModelScope.launch(Dispatchers.IO) {
            getAllSellAdvertisementUseCase.invoke().let {
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
                        _buyState.value = BuyState.FetchAllSellAdvertisementSuccessfully(it.data)
                    }
                }
            }
        }
    }

    fun searchSellAdvertisements(searchQuery: String) {
        setLoading(true)

        searchJob?.cancel()
        searchJob = viewModelScope.launch(Dispatchers.IO) {
            Timber.d("searchSellAdvertisements: hello")
            searchSellAdvertisementUseCase(searchQuery).let {
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
                        _buyState.value = BuyState.SearchSellAdvertisementSuccessfully(it.data)
                    }
                }
            }
        }
    }

    private fun setLoading(status: Boolean) {
        when (status) {
            true -> {
                _buyState.value = BuyState.IsLoading(true)
            }
            false -> {
                _buyState.value = BuyState.IsLoading(false)
            }
        }
    }

    private fun showError(message: String) {
        when (message) {
            resourceProvider.string(R.string.no_internet_connection) -> {
                _buyState.value = BuyState.NoInternetConnection(message)
            }
            else -> {
                _buyState.value = BuyState.ShowError(message)
            }
        }
    }
}