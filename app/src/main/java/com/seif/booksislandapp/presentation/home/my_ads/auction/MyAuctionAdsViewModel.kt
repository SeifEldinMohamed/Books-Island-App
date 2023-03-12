package com.seif.booksislandapp.presentation.home.my_ads.auction

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.seif.booksislandapp.R
import com.seif.booksislandapp.domain.usecase.usecase.my_ads.auction.GetMyAuctionAdsUseCase
import com.seif.booksislandapp.domain.usecase.usecase.shared_preference.GetFromSharedPreferenceUseCase
import com.seif.booksislandapp.utils.Resource
import com.seif.booksislandapp.utils.ResourceProvider
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import timber.log.Timber
import javax.inject.Inject

@HiltViewModel
class MyAuctionAdsViewModel @Inject constructor(
    private val getMyAuctionAdsUseCase: GetMyAuctionAdsUseCase,
    private val getFromSharedPreferenceUseCase: GetFromSharedPreferenceUseCase,
    private val resourceProvider: ResourceProvider
) : ViewModel() {
    private var _myAuctionAdsState = MutableStateFlow<MyAuctionAdsState>(MyAuctionAdsState.Init)
    val myAuctionAdsState get() = _myAuctionAdsState.asStateFlow()

    fun fetchAllAuctionAdvertisement(userId: String) {
        setLoading(true)
        Timber.d("fetch auction ads ...........")
        viewModelScope.launch(Dispatchers.IO) {
            getMyAuctionAdsUseCase.invoke(userId).let {
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
                            _myAuctionAdsState.value =
                                MyAuctionAdsState.FetchAllMyAuctionAdsSuccessfully(it.data)
                        }
                    }
                }
            }
        }
    }

    private fun setLoading(status: Boolean) {
        Timber.d("setLoadingn: $status")
        when (status) {
            true -> {
                _myAuctionAdsState.value = MyAuctionAdsState.IsLoading(true)
            }
            false -> {
                _myAuctionAdsState.value = MyAuctionAdsState.IsLoading(false)
            }
        }
    }

    private fun showError(message: String) {
        when (message) {
            resourceProvider.string(R.string.no_internet_connection) -> {
                _myAuctionAdsState.value = MyAuctionAdsState.NoInternetConnection(message)
            }
            else -> {
                _myAuctionAdsState.value = MyAuctionAdsState.ShowError(message)
            }
        }
    }

    fun <T> getFromSP(key: String, clazz: Class<T>): T {
        return getFromSharedPreferenceUseCase(key, clazz)
    }
}