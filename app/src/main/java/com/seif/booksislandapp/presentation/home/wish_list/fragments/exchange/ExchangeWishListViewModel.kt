package com.seif.booksislandapp.presentation.home.wish_list.fragments.exchange

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.seif.booksislandapp.R
import com.seif.booksislandapp.domain.usecase.usecase.shared_preference.GetFromSharedPreferenceUseCase
import com.seif.booksislandapp.domain.usecase.usecase.user.GetUserByIdUseCase
import com.seif.booksislandapp.domain.usecase.usecase.wish_list.GetAllExchangeWishListUseCase
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
class ExchangeWishListViewModel @Inject constructor(
    private val getUserByIdUseCase: GetUserByIdUseCase,
    private val resourceProvider: ResourceProvider,
    private val getAllExchangeWishListUseCase: GetAllExchangeWishListUseCase,
    private val getFromSharedPreferenceUseCase: GetFromSharedPreferenceUseCase,

) : ViewModel() {
    private var _exchangeWishListState = MutableStateFlow<ExchangeWishListState>(ExchangeWishListState.Init)
    val exchangeWishListState get() = _exchangeWishListState.asStateFlow()
    fun fetchAllExchangeWishListAdvertisement(id: String) {

        setLoading(true)
        viewModelScope.launch(Dispatchers.IO) {
            getUserByIdUseCase.invoke(id).let {
                when (it) {
                    is Resource.Error -> {
                        withContext(Dispatchers.Main) {
                            showError(it.message)
                        }
                    }

                    is Resource.Success -> {
                        getAllExchangeWishListUseCase.invoke(it.data.wishListExchange).let { result ->
                            when (result) {
                                is Resource.Error -> {
                                    withContext(Dispatchers.Main) {
                                        setLoading(false)
                                        showError(result.message)
                                    }
                                }
                                is Resource.Success -> {
                                    withContext(Dispatchers.Main) {
                                        setLoading(false)
                                        _exchangeWishListState.value = ExchangeWishListState.FetchAllWishExchangeItemsSuccessfully(result.data)
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }
    }

    private fun setLoading(status: Boolean) {
        when (status) {
            true -> {
                _exchangeWishListState.value = ExchangeWishListState.IsLoading(true)
            }
            false -> {
                _exchangeWishListState.value = ExchangeWishListState.IsLoading(false)
            }
        }
    }

    private fun showError(message: String) {
        when (message) {
            resourceProvider.string(R.string.no_internet_connection) -> {
                _exchangeWishListState.value = ExchangeWishListState.NoInternetConnection(message)
            }
            else -> {
                _exchangeWishListState.value = ExchangeWishListState.ShowError(message)
            }
        }
    }
    fun <T> getFromSP(key: String, clazz: Class<T>): T {
        return getFromSharedPreferenceUseCase(key, clazz)
    }
}