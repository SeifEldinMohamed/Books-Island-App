package com.seif.booksislandapp.presentation.home.ad_details.auction

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.seif.booksislandapp.R
import com.seif.booksislandapp.domain.model.User
import com.seif.booksislandapp.domain.model.adv.AdType
import com.seif.booksislandapp.domain.usecase.usecase.advertisement.auction.FetchRelatedAuctionAdsUseCase
import com.seif.booksislandapp.domain.usecase.usecase.shared_preference.GetFromSharedPreferenceUseCase
import com.seif.booksislandapp.domain.usecase.usecase.user.GetUserByIdUseCase
import com.seif.booksislandapp.domain.usecase.usecase.wish_list.UpdateUserWishListUseCase
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
class AuctionAdDetailsViewModel @Inject constructor(
    private val resourceProvider: ResourceProvider,
    private val getUserByIdUseCase: GetUserByIdUseCase,
    private val getFromSharedPreferenceUseCase: GetFromSharedPreferenceUseCase,
    private val fetchRelatedAuctionAdsUseCase: FetchRelatedAuctionAdsUseCase,
    private val updateUserWishListUseCase: UpdateUserWishListUseCase
) : ViewModel() {
    private var _auctionDetailsState =
        MutableStateFlow<AuctionDetailsState>(AuctionDetailsState.Init)
    val auctionDetailsState = _auctionDetailsState.asStateFlow()

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
                        _auctionDetailsState.value = AuctionDetailsState.GetCurrentUserByIdSuccessfully(it.data)
                        getUserByIdUseCase(ownerId).let { result ->
                            when (result) {
                                is Resource.Error -> {
                                    showError(result.message)
                                }
                                is Resource.Success -> {
                                    _auctionDetailsState.value = AuctionDetailsState.GetUserByIdSuccessfully(result.data)
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
            updateUserWishListUseCase.invoke(user.id, AdType.Auction, user.wishListAuction).let {
                when (it) {
                    is Resource.Error -> showError(it.message)
                    is Resource.Success -> {
                        _auctionDetailsState.value =
                            AuctionDetailsState.AddedToFavorite("Added Successfully")
                    }
                }
            }
        }
    }

    fun fetchRelatedAds(adId: String, category: String) {
        setLoading(true)
        viewModelScope.launch(Dispatchers.IO) {
            fetchRelatedAuctionAdsUseCase(adId, category).let {
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
                        _auctionDetailsState.value =
                            AuctionDetailsState.FetchRelatedAuctionAdvertisementSuccessfully(it.data)
                    }
                }
            }
        }
    }

    private fun setLoading(status: Boolean) {
        when (status) {
            true -> {
                _auctionDetailsState.value = AuctionDetailsState.IsLoading(true)
            }
            false -> {
                _auctionDetailsState.value = AuctionDetailsState.IsLoading(false)
            }
        }
    }

    private fun showError(message: String) {
        when (message) {
            resourceProvider.string(R.string.no_internet_connection) -> {
                _auctionDetailsState.value = AuctionDetailsState.NoInternetConnection(message)
            }
            else -> {
                _auctionDetailsState.value = AuctionDetailsState.ShowError(message)
            }
        }
    }

    fun <T> readFromSP(key: String, clazz: Class<T>): T {
        return getFromSharedPreferenceUseCase(key = key, clazz = clazz)
    }
}
