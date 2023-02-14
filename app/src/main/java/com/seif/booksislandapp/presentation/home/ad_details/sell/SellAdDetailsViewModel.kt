package com.seif.booksislandapp.presentation.home.ad_details.sell

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.seif.booksislandapp.R
import com.seif.booksislandapp.domain.usecase.usecase.advertisement.sell.FetchRelatedSellAdsUseCase
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
class SellAdDetailsViewModel @Inject constructor(
    private val resourceProvider: ResourceProvider,
    private val getUserByIdUseCase: GetUserByIdUseCase,
    private val fetchRelatedSellAdsUseCase: FetchRelatedSellAdsUseCase
) : ViewModel() {
    private var _sellDetailsState = MutableStateFlow<SellDetailsState>(SellDetailsState.Init)
    val sellDetailsState = _sellDetailsState.asStateFlow()

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
                        _sellDetailsState.value = SellDetailsState.GetUserByIdSuccessfully(it.data)
                    }
                }
            }
        }
    }

    fun fetchRelatedAds(adId: String, category: String) {
        setLoading(true)
        viewModelScope.launch(Dispatchers.IO) {
            fetchRelatedSellAdsUseCase(adId, category).let {
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
                        _sellDetailsState.value =
                            SellDetailsState.FetchRelatedSellAdvertisementSuccessfully(it.data)
                    }
                }
            }
        }
    }

    private fun setLoading(status: Boolean) {
        when (status) {
            true -> {
                _sellDetailsState.value = SellDetailsState.IsLoading(true)
            }
            false -> {
                _sellDetailsState.value = SellDetailsState.IsLoading(false)
            }
        }
    }

    private fun showError(message: String) {
        when (message) {
            resourceProvider.string(R.string.no_internet_connection) -> {
                _sellDetailsState.value = SellDetailsState.NoInternetConnection(message)
            }
            else -> {
                _sellDetailsState.value = SellDetailsState.ShowError(message)
            }
        }
    }
}