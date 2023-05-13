package com.seif.booksislandapp.presentation.home.categories.auction

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.seif.booksislandapp.R
import com.seif.booksislandapp.domain.usecase.usecase.advertisement.auction.GetAllAuctionAdsUseCase
import com.seif.booksislandapp.domain.usecase.usecase.advertisement.auction.GetAuctionAdsByFilterUseCase
import com.seif.booksislandapp.domain.usecase.usecase.advertisement.auction.SearchAuctionsAdsUseCase
import com.seif.booksislandapp.presentation.home.categories.filter.FilterBy
import com.seif.booksislandapp.utils.Resource
import com.seif.booksislandapp.utils.ResourceProvider
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.*
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import timber.log.Timber
import javax.inject.Inject

@HiltViewModel
class AuctionViewModel @Inject constructor(
    private val getAllAuctionAdsUseCase: GetAllAuctionAdsUseCase,
    private val searchAuctionsAdsUseCase: SearchAuctionsAdsUseCase,
    private val getAuctionAdsByFilterUseCase: GetAuctionAdsByFilterUseCase,
    private val resourceProvider: ResourceProvider
) : ViewModel() {
    private var _auctionState = MutableStateFlow<AuctionState>(AuctionState.Init)
    val auctionState get() = _auctionState.asStateFlow()
    private var searchJob: Job? = null
    var firstTime = true
    var isSearching = false

    fun fetchAllAuctionsAdvertisements() {
        setLoading(true)
        viewModelScope.launch(Dispatchers.IO) {
            getAllAuctionAdsUseCase.invoke().let {
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
                        _auctionState.value = AuctionState.FetchAllAuctionsAdsSuccessfully(it.data)
                    }
                }
            }
        }
    }

    fun searchAuctionsAdvertisements(searchQuery: String) {
        searchJob?.cancel()
        searchJob = viewModelScope.launch(Dispatchers.IO) {
            delay(500)
            withContext(Dispatchers.Main) {
                setLoading(true)
            }
            Timber.d("searchSellAdvertisements: hello")
            searchAuctionsAdsUseCase(searchQuery).let {
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
                        _auctionState.value = AuctionState.SearchAuctionsAdsSuccessfully(it.data)
                    }
                }
            }
        }
    }

    fun fetchAuctionAdvertisementByFilter(
        filterBy: FilterBy
    ) {
        setLoading(true)
        viewModelScope.launch(Dispatchers.IO) {
            getAuctionAdsByFilterUseCase.invoke(filterBy).let {
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
                        _auctionState.value = AuctionState.FetchAllAuctionsAdsSuccessfully(it.data)
                    }
                }
            }
        }
    }

    private fun setLoading(status: Boolean) {
        when (status) {
            true -> {
                _auctionState.value = AuctionState.IsLoading(true)
            }
            false -> {
                _auctionState.value = AuctionState.IsLoading(false)
            }
        }
    }

    private fun showError(message: String) {
        when (message) {
            resourceProvider.string(R.string.no_internet_connection) -> {
                _auctionState.value = AuctionState.NoInternetConnection(message)
            }
            else -> {
                _auctionState.value = AuctionState.ShowError(message)
            }
        }
    }

    fun resetState() {
        _auctionState.value = AuctionState.Init
    }
}