package com.seif.booksislandapp.presentation.home.categories.donation

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.seif.booksislandapp.R
import com.seif.booksislandapp.domain.usecase.usecase.advertisement.donate.GetAllDonateAdvertisementUseCase
import com.seif.booksislandapp.domain.usecase.usecase.advertisement.donate.GetDonateAdsByFilterUseCase
import com.seif.booksislandapp.domain.usecase.usecase.advertisement.donate.SearchDonateAdvertisementUseCase
import com.seif.booksislandapp.domain.usecase.usecase.shared_preference.GetFromSharedPreferenceUseCase
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
class DonateViewModel @Inject constructor(
    private val getAllDonateAdvertisementUseCase: GetAllDonateAdvertisementUseCase,
    private val searchDonateAdvertisementUseCase: SearchDonateAdvertisementUseCase,
    private val getDonateAdsByFilterUseCase: GetDonateAdsByFilterUseCase,
    private val resourceProvider: ResourceProvider,
    private val dispatcher: DispatcherProvider,
    private val getFromSharedPrefUseCase: GetFromSharedPreferenceUseCase,
) : ViewModel() {
    private var _donateState = MutableStateFlow<DonateState>(DonateState.Init)
    val donateState = _donateState.asStateFlow()

    private var searchJob: Job? = null
    var firstTime = true
    var isSearching = false
    fun fetchAllDonateAdvertisement() {
        setLoading(true)
        viewModelScope.launch(dispatcher.io) {
            getAllDonateAdvertisementUseCase.invoke().let {
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
                        _donateState.value = DonateState.FetchAllDonateAdvertisementSuccessfully(it.data)
                    }
                }
            }
        }
    }
    fun <T> getFromSP(key: String, clazz: Class<T>): T {
        return getFromSharedPrefUseCase(key, clazz)
    }
    fun searchDonateAdvertisements(searchQuery: String) {
        searchJob?.cancel()
        searchJob = viewModelScope.launch(Dispatchers.IO) {
            delay(500)
            withContext(Dispatchers.Main) {
                setLoading(true)
            }
            searchDonateAdvertisementUseCase(searchQuery).let {
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
                        _donateState.value = DonateState.SearchDonateAdvertisementSuccessfully(it.data)
                    }
                }
            }
        }
    }
    fun fetchDonateAdvertisementByFilter(
        filterBy: FilterBy
    ) {
        setLoading(true)
        viewModelScope.launch(Dispatchers.IO) {
            getDonateAdsByFilterUseCase.invoke(filterBy).let {
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
                        _donateState.value = DonateState.FetchAllDonateAdvertisementSuccessfully(it.data)
                    }
                }
            }
        }
    }

    private fun setLoading(status: Boolean) {
        when (status) {
            true -> {
                _donateState.value = DonateState.IsLoading(true)
            }
            false -> {
                _donateState.value = DonateState.IsLoading(false)
            }
        }
    }

    private fun showError(message: String) {
        when (message) {
            resourceProvider.string(R.string.no_internet_connection) -> {
                _donateState.value = DonateState.NoInternetConnection(message)
            }
            else -> {
                _donateState.value = DonateState.ShowError(message)
            }
        }
    }

    fun resetState() {
        _donateState.value = DonateState.Init
    }
}