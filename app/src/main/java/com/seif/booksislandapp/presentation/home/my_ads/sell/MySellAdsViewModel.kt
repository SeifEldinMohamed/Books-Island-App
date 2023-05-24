package com.seif.booksislandapp.presentation.home.my_ads.sell

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.seif.booksislandapp.R
import com.seif.booksislandapp.domain.usecase.usecase.my_ads.sell.GetMySellAdsUseCase
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
class MySellAdsViewModel @Inject constructor(
    private val getMySellAdsUseCase: GetMySellAdsUseCase,
    private val getFromSharedPreferenceUseCase: GetFromSharedPreferenceUseCase,
    private val resourceProvider: ResourceProvider
) : ViewModel() {
    private var _mySellAdsState = MutableStateFlow<MySellAdsState>(MySellAdsState.Init)
    val mySellAdsState get() = _mySellAdsState.asStateFlow()

    fun fetchAllSellAdvertisement(userId: String) {
        setLoading(true)
        viewModelScope.launch(Dispatchers.IO) {
            getMySellAdsUseCase.invoke(userId).collect {
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
                        _mySellAdsState.value =
                            MySellAdsState.FetchAllMySellAdsSuccessfully(it.data)
                    }
                }
            }
        }
    }

    private fun setLoading(status: Boolean) {
        when (status) {
            true -> {
                _mySellAdsState.value = MySellAdsState.IsLoading(true)
            }
            false -> {
                _mySellAdsState.value = MySellAdsState.IsLoading(false)
            }
        }
    }

    private fun showError(message: String) {
        when (message) {
            resourceProvider.string(R.string.no_internet_connection) -> {
                _mySellAdsState.value = MySellAdsState.NoInternetConnection(message)
            }
            else -> {
                _mySellAdsState.value = MySellAdsState.ShowError(message)
            }
        }
    }

    fun <T> getFromSP(key: String, clazz: Class<T>): T {
        return getFromSharedPreferenceUseCase(key, clazz)
    }
}