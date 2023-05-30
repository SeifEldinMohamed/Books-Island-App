package com.seif.booksislandapp.presentation.home.requests.received_requests

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.seif.booksislandapp.R
import com.seif.booksislandapp.domain.model.adv.AdType
import com.seif.booksislandapp.domain.usecase.usecase.request.received.AcceptConfirmationRequestUseCase
import com.seif.booksislandapp.domain.usecase.usecase.request.received.FetchReceivedRequestsUseCase
import com.seif.booksislandapp.domain.usecase.usecase.request.received.RejectedConfirmationRequestUseCase
import com.seif.booksislandapp.domain.usecase.usecase.user.GetFirebaseCurrentUserUseCase
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
class ReceivedRequestsViewModel @Inject constructor(
    private val fetchReceivedRequestsUseCase: FetchReceivedRequestsUseCase,
    private val resourceProvider: ResourceProvider,
    private val getFirebaseCurrentUserUseCase: GetFirebaseCurrentUserUseCase,
    private val acceptConfirmationRequestUseCase: AcceptConfirmationRequestUseCase,
    private val rejectConfirmationRequestUseCase: RejectedConfirmationRequestUseCase
) : ViewModel() {
    private var _receivedRequestsState =
        MutableStateFlow<ReceivedRequestsState>(ReceivedRequestsState.Init)
    val receivedRequestsState = _receivedRequestsState.asStateFlow()

    fun getMyReceivedRequests(id: String) {
        setLoading(true)
        viewModelScope.launch(Dispatchers.IO) {
            fetchReceivedRequestsUseCase(id).collect {
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
                        _receivedRequestsState.value =
                            ReceivedRequestsState.FetchReceivedRequestsSuccessfully(it.data)
                    }
                }
            }
        }
    }

    fun acceptConfirmationRequest(
        requestId: String,
        sellerId: String,
        adType: AdType,
        acceptStatus: String,
        advertisementId: String
    ) {
        setLoading(true)
        viewModelScope.launch(Dispatchers.IO) {
            acceptConfirmationRequestUseCase(
                requestId, sellerId, adType, acceptStatus, advertisementId
            ).let {
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
                        _receivedRequestsState.value =
                            ReceivedRequestsState.AcceptedConfirmationRequestSuccessfully(it.data)
                    }
                }
            }
        }
    }

    fun rejectConfirmationRequest(
        requestId: String,
        advertisementId: String,
        adType: AdType,
        rejectStatus: String
    ) {
        setLoading(true)
        viewModelScope.launch(Dispatchers.IO) {
            rejectConfirmationRequestUseCase(
                requestId, advertisementId, adType, rejectStatus
            ).let {
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
                        _receivedRequestsState.value =
                            ReceivedRequestsState.RejectedConfirmationRequestSuccessfully(it.data)
                    }
                }
            }
        }
    }

    private fun setLoading(status: Boolean) {
        when (status) {
            true -> {
                _receivedRequestsState.value = ReceivedRequestsState.IsLoading(true)
            }
            false -> {
                _receivedRequestsState.value = ReceivedRequestsState.IsLoading(false)
            }
        }
    }

    private fun showError(message: String) {
        when (message) {
            resourceProvider.string(R.string.no_internet_connection) -> {
                _receivedRequestsState.value = ReceivedRequestsState.NoInternetConnection(message)
            }
            else -> {
                _receivedRequestsState.value = ReceivedRequestsState.ShowError(message)
            }
        }
    }

    fun getCurrentUserId(): String {
        return getFirebaseCurrentUserUseCase()!!.uid
    }
}