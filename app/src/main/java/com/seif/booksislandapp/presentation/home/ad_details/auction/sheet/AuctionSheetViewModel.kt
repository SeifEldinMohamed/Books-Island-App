package com.seif.booksislandapp.presentation.home.ad_details.auction.sheet

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.seif.booksislandapp.R
import com.seif.booksislandapp.domain.model.adv.auction.AuctionAdvertisement
import com.seif.booksislandapp.domain.model.adv.auction.Bidder
import com.seif.booksislandapp.domain.usecase.usecase.advertisement.auction.AddBidderUseCase
import com.seif.booksislandapp.domain.usecase.usecase.advertisement.auction.FetchAuctionAdByIdUseCase
import com.seif.booksislandapp.domain.usecase.usecase.shared_preference.GetFromSharedPreferenceUseCase
import com.seif.booksislandapp.utils.Resource
import com.seif.booksislandapp.utils.ResourceProvider
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import javax.inject.Inject

@HiltViewModel
class AuctionSheetViewModel @Inject constructor(
    private val fetchAuctionAdByIdUseCase: FetchAuctionAdByIdUseCase,
    private val addBidderUseCase: AddBidderUseCase,
    private val getFromSharedPreferenceUseCase: GetFromSharedPreferenceUseCase,
    private val resourceProvider: ResourceProvider

) : ViewModel() {
    private val mutableAuctionAdvertisement = MutableLiveData<AuctionAdvertisement>()
    val auctionAdvertisement: LiveData<AuctionAdvertisement> get() = mutableAuctionAdvertisement

    private var _auctionSheetState = MutableStateFlow<AuctionSheetState>(AuctionSheetState.Init)
    val auctionSheetState get() = _auctionSheetState.asStateFlow()

    fun sendAdvertisement(auctionAdvertisement: AuctionAdvertisement) {
        mutableAuctionAdvertisement.value = auctionAdvertisement
    }

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
                        _auctionSheetState.value =
                            AuctionSheetState.FetchAuctionAdByIdSuccessfully(it.data)
                    }
                }
            }
        }
    }

    fun addBidder(adId: String, bidder: Bidder, currentAuctionValue: Int) {
        setLoading(true)
        viewModelScope.launch(Dispatchers.IO) {
            addBidderUseCase.invoke(adId, bidder, currentAuctionValue).let {
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
                        _auctionSheetState.value =
                            AuctionSheetState.AddBidderSuccessfully(it.data)
                    }
                }
            }
        }
    }

    private fun setLoading(status: Boolean) {
        when (status) {
            true -> {
                _auctionSheetState.value = AuctionSheetState.IsLoading(true)
            }
            false -> {
                _auctionSheetState.value = AuctionSheetState.IsLoading(false)
            }
        }
    }

    private fun showError(message: String) {
        when (message) {
            resourceProvider.string(R.string.no_internet_connection) -> {
                _auctionSheetState.value = AuctionSheetState.NoInternetConnection(message)
            }
            else -> {
                _auctionSheetState.value = AuctionSheetState.ShowError(message)
            }
        }
    }

    fun <T> readFromSP(key: String, clazz: Class<T>): T {
        return getFromSharedPreferenceUseCase(key = key, clazz = clazz)
    }
}