package com.seif.booksislandapp.presentation.home.ad_provider_profile.bottom_sheet.rate

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.seif.booksislandapp.R
import com.seif.booksislandapp.domain.model.Rate
import com.seif.booksislandapp.domain.usecase.usecase.user.RateUserUseCase
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
class RateSheetViewModel @Inject constructor(
    private val rateUserUseCase: RateUserUseCase,
    private val resourceProvider: ResourceProvider,
) : ViewModel() {

    private var _rateSheetState =
        MutableStateFlow<RateSheetState>(RateSheetState.Init)
    val rateSheetState = _rateSheetState.asStateFlow()

    private val mutableRateSent = MutableLiveData<Pair<String, String>>()
    val rateSent: LiveData<Pair<String, String>> get() = mutableRateSent

    fun rateAdProvider(
        currentUserId: String,
        rate: Rate
    ) {
        setLoading(true)
        viewModelScope.launch(Dispatchers.IO) {
            rateUserUseCase(currentUserId, rate).let {
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
                            _rateSheetState.value =
                                RateSheetState.RateUserSuccessfully(it.data)
                        }
                    }
                }
            }
        }
    }

    private fun setLoading(status: Boolean) {
        when (status) {
            true -> {
                _rateSheetState.value = RateSheetState.IsLoading(true)
            }

            false -> {
                _rateSheetState.value = RateSheetState.IsLoading(false)
            }
        }
    }

    private fun showError(message: String) {
        when (message) {
            resourceProvider.string(R.string.no_internet_connection) -> {
                _rateSheetState.value = RateSheetState.NoInternetConnection(message)
            }

            else -> {
                _rateSheetState.value = RateSheetState.ShowError(message)
            }
        }
    }

    fun updateRateState(rates: Pair<String, String>) {
        mutableRateSent.value = rates
    }
}