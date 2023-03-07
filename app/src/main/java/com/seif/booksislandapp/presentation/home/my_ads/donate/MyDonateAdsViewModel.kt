package com.seif.booksislandapp.presentation.home.my_ads.donate

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.seif.booksislandapp.R
import com.seif.booksislandapp.domain.usecase.usecase.my_ads.donate.GetMyDonateAdsUseCase
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
class MyDonateAdsViewModel @Inject constructor(
    private val getMyDonateAdsUseCase: GetMyDonateAdsUseCase,
    private val getFromSharedPreferenceUseCase: GetFromSharedPreferenceUseCase,
    private val resourceProvider: ResourceProvider
) : ViewModel() {
    private var _myDonateAdsState = MutableStateFlow<MyDonateAdsState>(MyDonateAdsState.Init)
    val myDonateAdsState get() = _myDonateAdsState.asStateFlow()

    fun fetchAllDonateAdvertisement(userId: String) {
        setLoading(true)
        viewModelScope.launch(Dispatchers.IO) {
            getMyDonateAdsUseCase.invoke(userId).let {
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
                        _myDonateAdsState.value =
                            MyDonateAdsState.FetchAllMyDonateAdsSuccessfully(it.data)
                    }
                }
            }
        }
    }

    private fun setLoading(status: Boolean) {
        when (status) {
            true -> {
                _myDonateAdsState.value = MyDonateAdsState.IsLoading(true)
            }
            false -> {
                _myDonateAdsState.value = MyDonateAdsState.IsLoading(false)
            }
        }
    }

    private fun showError(message: String) {
        when (message) {
            resourceProvider.string(R.string.no_internet_connection) -> {
                _myDonateAdsState.value = MyDonateAdsState.NoInternetConnection(message)
            }
            else -> {
                _myDonateAdsState.value = MyDonateAdsState.ShowError(message)
            }
        }
    }

    fun <T> getFromSP(key: String, clazz: Class<T>): T {
        return getFromSharedPreferenceUseCase(key, clazz)
    }
}