package com.seif.booksislandapp.presentation.home.bidders_history

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.seif.booksislandapp.R
import com.seif.booksislandapp.domain.usecase.usecase.advertisement.auction.FetchAuctionAdByIdUseCase
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
class BiddersHistoryViewModel @Inject constructor(
    private val fetchAuctionAdByIdUseCase: FetchAuctionAdByIdUseCase,
    private val resourceProvider: ResourceProvider

) : ViewModel() {

    private var _bidderHistoryState =
        MutableStateFlow<BiddersHistoryState>(BiddersHistoryState.Init)
    val bidderHistoryState get() = _bidderHistoryState.asStateFlow()

    fun fetchAuctionAdById(adId: String) {
        setLoading(true)
        viewModelScope.launch(Dispatchers.IO) {
            fetchAuctionAdByIdUseCase.invoke(adId = adId).collect {
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
                        _bidderHistoryState.value =
                            BiddersHistoryState.FetchAuctionAdByIdSuccessfully(it.data)
                    }
                }
            }
        }
    }

    private fun setLoading(status: Boolean) {
        when (status) {
            true -> {
                _bidderHistoryState.value = BiddersHistoryState.IsLoading(true)
            }

            false -> {
                _bidderHistoryState.value = BiddersHistoryState.IsLoading(false)
            }
        }
    }

    private fun showError(message: String) {
        when (message) {
            resourceProvider.string(R.string.no_internet_connection) -> {
                _bidderHistoryState.value = BiddersHistoryState.NoInternetConnection(message)
            }

            else -> {
                _bidderHistoryState.value = BiddersHistoryState.ShowError(message)
            }
        }
    }
}