package com.seif.booksislandapp.presentation.home.categories.buy

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.seif.booksislandapp.R
import com.seif.booksislandapp.domain.usecase.usecase.advertisement.sell.GetAllSellAdvertisementUseCase
import com.seif.booksislandapp.domain.usecase.usecase.advertisement.sell.GetSellAdsByFilterUseCase
import com.seif.booksislandapp.domain.usecase.usecase.advertisement.sell.SearchSellAdvertisementUseCase
import com.seif.booksislandapp.domain.usecase.usecase.shared_preference.GetFromSharedPreferenceUseCase
import com.seif.booksislandapp.presentation.home.categories.filter.FilterBy
import com.seif.booksislandapp.utils.DispatcherProvider
import com.seif.booksislandapp.utils.Resource
import com.seif.booksislandapp.utils.ResourceProvider
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import javax.inject.Inject

@HiltViewModel
class BuyViewModel @Inject constructor(
    private val getAllSellAdvertisementUseCase: GetAllSellAdvertisementUseCase,
    private val getSellAdsByFilterUseCase: GetSellAdsByFilterUseCase,
    private val searchSellAdvertisementUseCase: SearchSellAdvertisementUseCase,
    private val resourceProvider: ResourceProvider,
    private val getFromSharedPrefUseCase: GetFromSharedPreferenceUseCase,
    private val dispatcher: DispatcherProvider
) : ViewModel() {
    private var _buyState = MutableStateFlow<BuyState>(BuyState.Init)
    val buyState get() = _buyState.asStateFlow()
    private var searchJob: Job? = null
    var firstTime = true
    var isSearching = false

    fun fetchAllSellAdvertisement() {
        setLoading(true)
        viewModelScope.launch(dispatcher.io) {
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

    fun fetchSellAdvertisementByFilter(
        filterBy: FilterBy
    ) {
        setLoading(true)
        viewModelScope.launch(Dispatchers.IO) {
            getSellAdsByFilterUseCase.invoke(filterBy).let {
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
        searchJob?.cancel()
        searchJob = viewModelScope.launch(Dispatchers.IO) {
            delay(500)
            withContext(Dispatchers.Main) {
                setLoading(true)
            }
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

    fun resetState() {
        _buyState.value = BuyState.Init
    }
    fun <T> getFromSP(key: String, clazz: Class<T>): T {
        return getFromSharedPrefUseCase(key, clazz)
    }
}