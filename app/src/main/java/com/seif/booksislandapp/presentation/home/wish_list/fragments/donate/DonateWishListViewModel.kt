package com.seif.booksislandapp.presentation.home.wish_list.fragments.donate

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.seif.booksislandapp.R
import com.seif.booksislandapp.domain.usecase.usecase.shared_preference.GetFromSharedPreferenceUseCase
import com.seif.booksislandapp.domain.usecase.usecase.user.GetUserByIdUseCase
import com.seif.booksislandapp.domain.usecase.usecase.wish_list.GetAllDonateWishListUseCase
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
class DonateWishListViewModel @Inject constructor(
    private val getUserByIdUseCase: GetUserByIdUseCase,
    private val resourceProvider: ResourceProvider,
    private val getAllDonateWishListUseCase: GetAllDonateWishListUseCase,
    private val getFromSharedPreferenceUseCase: GetFromSharedPreferenceUseCase,

) : ViewModel() {
    private var _donateWishListState = MutableStateFlow<DonateWishListState>(DonateWishListState.Init)
    val donateWishListState get() = _donateWishListState.asStateFlow()
    fun fetchAllDonateWishListAdvertisement(id: String) {

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
                        getAllDonateWishListUseCase.invoke(it.data.wishListDonate).let { result ->
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
                                        _donateWishListState.value = DonateWishListState.FetchAllWishDonateItemsSuccessfully(result.data)
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
                _donateWishListState.value = DonateWishListState.IsLoading(true)
            }
            false -> {
                _donateWishListState.value = DonateWishListState.IsLoading(false)
            }
        }
    }

    private fun showError(message: String) {
        when (message) {
            resourceProvider.string(R.string.no_internet_connection) -> {
                _donateWishListState.value = DonateWishListState.NoInternetConnection(message)
            }
            else -> {
                _donateWishListState.value = DonateWishListState.ShowError(message)
            }
        }
    }
    fun <T> getFromSP(key: String, clazz: Class<T>): T {
        return getFromSharedPreferenceUseCase(key, clazz)
    }
}