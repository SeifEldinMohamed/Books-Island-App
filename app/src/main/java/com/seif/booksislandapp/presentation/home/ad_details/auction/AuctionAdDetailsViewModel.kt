package com.seif.booksislandapp.presentation.home.ad_details.auction

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.seif.booksislandapp.R
import com.seif.booksislandapp.domain.usecase.usecase.advertisement.auction.FetchRelatedAuctionAdsUseCase
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
class AuctionAdDetailsViewModel @Inject constructor(
    private val resourceProvider: ResourceProvider,
    private val getUserByIdUseCase: GetUserByIdUseCase,
    private val fetchRelatedAuctionAdsUseCase: FetchRelatedAuctionAdsUseCase
) : ViewModel() {
    private var _auctionDetailsState =
        MutableStateFlow<AuctionDetailsState>(AuctionDetailsState.Init)
    val auctionDetailsState = _auctionDetailsState.asStateFlow()

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
                        _auctionDetailsState.value =
                            AuctionDetailsState.GetUserByIdSuccessfully(it.data)
                    }
                }
            }
        }
    }

    fun fetchRelatedAds(adId: String, category: String) {
        setLoading(true)
        viewModelScope.launch(Dispatchers.IO) {
            fetchRelatedAuctionAdsUseCase(adId, category).let {
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
                        _auctionDetailsState.value =
                            AuctionDetailsState.FetchRelatedAuctionAdvertisementSuccessfully(it.data)
                    }
                }
            }
        }
    }

    private fun setLoading(status: Boolean) {
        when (status) {
            true -> {
                _auctionDetailsState.value = AuctionDetailsState.IsLoading(true)
            }
            false -> {
                _auctionDetailsState.value = AuctionDetailsState.IsLoading(false)
            }
        }
    }

    private fun showError(message: String) {
        when (message) {
            resourceProvider.string(R.string.no_internet_connection) -> {
                _auctionDetailsState.value = AuctionDetailsState.NoInternetConnection(message)
            }
            else -> {
                _auctionDetailsState.value = AuctionDetailsState.ShowError(message)
            }
        }
    }
}
