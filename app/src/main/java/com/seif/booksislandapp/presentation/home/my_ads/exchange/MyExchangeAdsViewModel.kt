package com.seif.booksislandapp.presentation.home.my_ads.exchange

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.seif.booksislandapp.R
import com.seif.booksislandapp.domain.usecase.usecase.my_ads.exchange.GetMyExchangeAdsUseCase
import com.seif.booksislandapp.domain.usecase.usecase.shared_preference.GetFromSharedPreferenceUseCase
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
class MyExchangeAdsViewModel @Inject constructor(
    private val getMyExchangeAdsUseCase: GetMyExchangeAdsUseCase,
    private val getFromSharedPreferenceUseCase: GetFromSharedPreferenceUseCase,
    private val resourceProvider: ResourceProvider
) : ViewModel() {
    private var _myExchangeAdsState = MutableStateFlow<MyExchangeAdsState>(MyExchangeAdsState.Init)
    val myAuctionAdsState get() = _myExchangeAdsState.asStateFlow()

    fun fetchAllExchangeAdvertisement(userId: String) {
        setLoading(true)
        viewModelScope.launch(Dispatchers.IO) {
            getMyExchangeAdsUseCase.invoke(userId).collect {
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
                        _myExchangeAdsState.value =
                            MyExchangeAdsState.FetchAllMyExchangeAdsSuccessfully(it.data)
                    }
                }
            }
        }
    }

    private fun setLoading(status: Boolean) {
        when (status) {
            true -> {
                _myExchangeAdsState.value = MyExchangeAdsState.IsLoading(true)
            }
            false -> {
                _myExchangeAdsState.value = MyExchangeAdsState.IsLoading(false)
            }
        }
    }

    private fun showError(message: String) {
        when (message) {
            resourceProvider.string(R.string.no_internet_connection) -> {
                _myExchangeAdsState.value = MyExchangeAdsState.NoInternetConnection(message)
            }
            else -> {
                _myExchangeAdsState.value = MyExchangeAdsState.ShowError(message)
            }
        }
    }

    fun <T> getFromSP(key: String, clazz: Class<T>): T {
        return getFromSharedPreferenceUseCase(key, clazz)
    }
}