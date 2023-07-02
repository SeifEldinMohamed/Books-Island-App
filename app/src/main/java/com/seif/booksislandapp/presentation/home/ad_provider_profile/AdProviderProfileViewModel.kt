package com.seif.booksislandapp.presentation.home.ad_provider_profile

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.seif.booksislandapp.R
import com.seif.booksislandapp.domain.usecase.usecase.user.BlockUserUseCase
import com.seif.booksislandapp.domain.usecase.usecase.user.GetUserByIdUseCase
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
class AdProviderProfileViewModel @Inject constructor(
    private val resourceProvider: ResourceProvider,
    private val getUserByIdUseCase: GetUserByIdUseCase,
    private val blockUserUseCase: BlockUserUseCase,

) : ViewModel() {
    private var _adProviderProfileState =
        MutableStateFlow<AdProviderProfileState>(AdProviderProfileState.Init)
    val adProviderProfileState = _adProviderProfileState.asStateFlow()

    fun getAdProviderUserById(currUserId: String) {
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
                        _adProviderProfileState.value =
                            AdProviderProfileState.FetchAdProviderUserSuccessfully(it.data)
                    }
                }
            }
        }
    }

    fun blockUser(currentUserId: String, adProviderUserId: String, blockUser: Boolean) {
        setLoading(true)
        viewModelScope.launch(Dispatchers.IO) {
            blockUserUseCase.invoke(currentUserId, adProviderUserId, blockUser).let {
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
                        _adProviderProfileState.value =
                            AdProviderProfileState.BlockUserSuccessfully(it.data)
                    }
                }
            }
        }
    }

    fun fetchCurrentUser(currentUserId: String) {
        viewModelScope.launch(Dispatchers.IO) {
            getUserByIdUseCase(currentUserId).let {
                when (it) {
                    is Resource.Error -> {
                        withContext(Dispatchers.Main) {
                            showError(it.message)
                        }
                    }

                    is Resource.Success -> {
                        _adProviderProfileState.value =
                            AdProviderProfileState.FetchCurrentUserSuccessfully(it.data)
                    }
                }
            }
        }
    }

    private fun setLoading(status: Boolean) {
        when (status) {
            true -> {
                _adProviderProfileState.value = AdProviderProfileState.IsLoading(true)
            }

            false -> {
                _adProviderProfileState.value = AdProviderProfileState.IsLoading(false)
            }
        }
    }

    private fun showError(message: String) {
        when (message) {
            resourceProvider.string(R.string.no_internet_connection) -> {
                _adProviderProfileState.value = AdProviderProfileState.NoInternetConnection(message)
            }

            else -> {
                _adProviderProfileState.value = AdProviderProfileState.ShowError(message)
            }
        }
    }
}