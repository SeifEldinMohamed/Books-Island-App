package com.seif.booksislandapp.presentation.home.ad_details.sell.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.seif.booksislandapp.R
import com.seif.booksislandapp.domain.model.User
import com.seif.booksislandapp.domain.model.adv.AdType
import com.seif.booksislandapp.domain.usecase.usecase.advertisement.sell.FetchRelatedSellAdsUseCase
import com.seif.booksislandapp.domain.usecase.usecase.shared_preference.GetFromSharedPreferenceUseCase
import com.seif.booksislandapp.domain.usecase.usecase.user.GetUserByIdUseCase
import com.seif.booksislandapp.domain.usecase.usecase.wish_list.UpdateUserWishListUseCase
import com.seif.booksislandapp.presentation.home.ad_details.sell.SellDetailsState
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
class SellAdDetailsViewModel @Inject constructor(
    private val resourceProvider: ResourceProvider,
    private val getUserByIdUseCase: GetUserByIdUseCase,
    private val fetchRelatedSellAdsUseCase: FetchRelatedSellAdsUseCase,
    private val getFromSharedPreferenceUseCase: GetFromSharedPreferenceUseCase,
    private val updateUserWishListUseCase: UpdateUserWishListUseCase
) : ViewModel() {
    private var _sellDetailsState = MutableStateFlow<SellDetailsState>(SellDetailsState.Init)
    val sellDetailsState = _sellDetailsState.asStateFlow()

    fun getUserById(ownerId: String, currUserId: String) {
        setLoading(true)
        viewModelScope.launch(Dispatchers.IO) {
            getUserByIdUseCase(currUserId).let {
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
                        _sellDetailsState.value =
                            SellDetailsState.GetCurrentUserByIdSuccessfully(it.data)
                        getUserByIdUseCase(ownerId).let { result ->
                            when (result) {
                                is Resource.Error -> {
                                    showError(result.message)
                                }
                                is Resource.Success -> {
                                    _sellDetailsState.value =
                                        SellDetailsState.GetUserByIdSuccessfully(result.data)
                                }
                            }
                        }
                    }
                }
            }
        }
    }
    fun updateUserWishList(user: User) {
        viewModelScope.launch {
            updateUserWishListUseCase.invoke(user.id, AdType.Buying, user.wishListBuy).let {
                when (it) {
                    is Resource.Error -> showError(it.message)
                    is Resource.Success -> {
                        _sellDetailsState.value =
                            SellDetailsState.AddedToFavorite("Added Successfully")
                    }
                }
            }
        }
    }
    fun fetchRelatedAds(adId: String, category: String) {
        setLoading(true)
        viewModelScope.launch(Dispatchers.IO) {
            fetchRelatedSellAdsUseCase(adId, category).let {
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
                        _sellDetailsState.value =
                            SellDetailsState.FetchRelatedSellAdvertisementSuccessfully(it.data)
                    }
                }
            }
        }
    }

    private fun setLoading(status: Boolean) {
        when (status) {
            true -> {
                _sellDetailsState.value = SellDetailsState.IsLoading(true)
            }
            false -> {
                _sellDetailsState.value = SellDetailsState.IsLoading(false)
            }
        }
    }

    private fun showError(message: String) {
        when (message) {
            resourceProvider.string(R.string.no_internet_connection) -> {
                _sellDetailsState.value = SellDetailsState.NoInternetConnection(message)
            }
            else -> {
                _sellDetailsState.value = SellDetailsState.ShowError(message)
            }
        }
    }

    fun <T> readFromSP(key: String, clazz: Class<T>): T {
        return getFromSharedPreferenceUseCase(key = key, clazz = clazz)
    }
}