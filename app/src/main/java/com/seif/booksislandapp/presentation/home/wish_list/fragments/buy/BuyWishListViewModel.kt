package com.seif.booksislandapp.presentation.home.wish_list.fragments.buy

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.seif.booksislandapp.R
import com.seif.booksislandapp.domain.usecase.usecase.shared_preference.GetFromSharedPreferenceUseCase
import com.seif.booksislandapp.domain.usecase.usecase.user.GetUserByIdUseCase
import com.seif.booksislandapp.domain.usecase.usecase.wish_list.GetAllBuyWishListUseCase
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
class BuyWishListViewModel @Inject constructor(
    private val getUserByIdUseCase: GetUserByIdUseCase,
    private val resourceProvider: ResourceProvider,
    private val getAllBuyWishListUseCase: GetAllBuyWishListUseCase,
    private val getFromSharedPreferenceUseCase: GetFromSharedPreferenceUseCase,

) : ViewModel() {
    private var _buyWishListState = MutableStateFlow<BuyWishListState>(BuyWishListState.Init)
    val buyWishListState get() = _buyWishListState.asStateFlow()
    fun fetchAllBuyWishListAdvertisement(id: String) {

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
                        getAllBuyWishListUseCase.invoke(it.data.wishListBuy).let {
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
                                    _buyWishListState.value = BuyWishListState.FetchAllWishBuyItemsSuccessfully(it.data)
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
                _buyWishListState.value = BuyWishListState.IsLoading(true)
            }
            false -> {
                _buyWishListState.value = BuyWishListState.IsLoading(false)
            }
        }
    }

    private fun showError(message: String) {
        when (message) {
            resourceProvider.string(R.string.no_internet_connection) -> {
                _buyWishListState.value = BuyWishListState.NoInternetConnection(message)
            }
            else -> {
                _buyWishListState.value = BuyWishListState.ShowError(message)
            }
        }
    }

    fun resetState() {
        _buyWishListState.value = BuyWishListState.Init
    }
    fun <T> getFromSP(key: String, clazz: Class<T>): T {
        return getFromSharedPreferenceUseCase(key, clazz)
    }
}