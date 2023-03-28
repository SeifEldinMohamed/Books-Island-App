package com.seif.booksislandapp.presentation.home.ad_details.donate

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.seif.booksislandapp.R
import com.seif.booksislandapp.domain.model.User
import com.seif.booksislandapp.domain.usecase.usecase.advertisement.donate.FetchAllDonateRelatedAdvertisementsUseCase
import com.seif.booksislandapp.domain.usecase.usecase.shared_preference.GetFromSharedPreferenceUseCase
import com.seif.booksislandapp.domain.usecase.usecase.user.GetUserByIdUseCase
import com.seif.booksislandapp.domain.usecase.usecase.user.UpdateUserProfileUseCase
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
class DonateAdDetailsViewModel @Inject constructor(
    private val getUserByIdUseCase: GetUserByIdUseCase,
    private val resourceProvider: ResourceProvider,
    private val getFromSharedPreferenceUseCase: GetFromSharedPreferenceUseCase,
    private val fetchAllDonateRelatedAdvertisementsUseCase: FetchAllDonateRelatedAdvertisementsUseCase,
    private val updateUserProfileUseCase: UpdateUserProfileUseCase

) : ViewModel() {
    private var _donateDetailsState =
        MutableStateFlow<DonateAdDetailsState>(DonateAdDetailsState.Int)
    val donateDetailsState = _donateDetailsState.asStateFlow()
    fun getUserById(ownerId: String, currUserId: String) {
        setLoadingState(true)
        viewModelScope.launch(Dispatchers.IO) {
            getUserByIdUseCase(currUserId).let {
                when (it) {
                    is Resource.Error -> {
                        withContext(Dispatchers.Main) {
                            setLoadingState(false)
                            showError(it.message)
                        }
                    }
                    is Resource.Success -> {
                        withContext(Dispatchers.Main) {
                            setLoadingState(false)
                        }
                        _donateDetailsState.value = DonateAdDetailsState.GetCurrentUserByIdSuccessfully(it.data)
                        getUserByIdUseCase(ownerId).let { result ->
                            when (result) {
                                is Resource.Error -> {
                                    showError(result.message)
                                }
                                is Resource.Success -> {
                                    _donateDetailsState.value = DonateAdDetailsState.GetUserByIdSuccessfully(result.data)
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
            updateUserProfileUseCase.invoke(user).let {
                when (it) {
                    is Resource.Error -> showError(it.message)
                    is Resource.Success -> {
                        _donateDetailsState.value =
                            DonateAdDetailsState.AddedToFavorite("Added Successfully")
                    }
                }
            }
        }
    }

    fun getAllRelatedAds(adId: String, category: String) {
        setLoadingState(true)
        viewModelScope.launch(Dispatchers.IO) {
            fetchAllDonateRelatedAdvertisementsUseCase(adId, category).let {
                when (it) {
                    is Resource.Error -> {
                        withContext(Dispatchers.Main) {
                            setLoadingState(false)
                            showError(it.message)
                        }
                    }
                    is Resource.Success -> {
                        withContext(Dispatchers.Main) {
                            setLoadingState(false)
                        }
                        _donateDetailsState.value =
                            DonateAdDetailsState.FetchRelatedDonateAdvertisementSuccessfully(it.data)
                    }
                }
            }
        }
    }

    private fun showError(message: String) {
        if (message == resourceProvider.string(R.string.no_internet_connection))
            _donateDetailsState.value = DonateAdDetailsState.NoInternetConnection(message)
        else
            _donateDetailsState.value = DonateAdDetailsState.ShowError(message)
    }

    private fun setLoadingState(status: Boolean) {
        when (status) {
            true -> {
                _donateDetailsState.value = DonateAdDetailsState.IsLoading(true)
            }
            false -> {
                _donateDetailsState.value = DonateAdDetailsState.IsLoading(false)
            }
        }
    }

    fun <T> readFromSP(key: String, clazz: Class<T>): T {
        return getFromSharedPreferenceUseCase(key = key, clazz = clazz)
    }

    fun resetState() {
        _donateDetailsState.value = DonateAdDetailsState.Int
    }
}
