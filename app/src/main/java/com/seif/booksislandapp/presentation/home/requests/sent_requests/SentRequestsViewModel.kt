package com.seif.booksislandapp.presentation.home.requests.sent_requests

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.seif.booksislandapp.R
import com.seif.booksislandapp.domain.usecase.usecase.request.sent.CancelSentRequestUseCase
import com.seif.booksislandapp.domain.usecase.usecase.request.sent.FetchMySentRequestsUseCase
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
class SentRequestsViewModel @Inject constructor(
    private val fetchMySentRequestsUseCase: FetchMySentRequestsUseCase,
    private val resourceProvider: ResourceProvider,
    private val getFirebaseCurrentUserUseCase: GetFirebaseCurrentUserUseCase,
    private val cancelSentRequestUseCase: CancelSentRequestUseCase
) : ViewModel() {
    private var _sentRequestsState = MutableStateFlow<SentRequestsState>(SentRequestsState.Init)
    val sentRequestsState = _sentRequestsState.asStateFlow()

    fun cancelRequest(requestId: String, adType: String, advertisementId: String) {
        setLoading(true)
        viewModelScope.launch(Dispatchers.IO) {
            cancelSentRequestUseCase(requestId, adType, advertisementId).let {
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
                        _sentRequestsState.value =
                            SentRequestsState.CancelSentRequestsSuccessfully(it.data)
                    }
                }
            }
        }
    }

    fun getMySentRequests(id: String) {
        setLoading(true)
        viewModelScope.launch(Dispatchers.IO) {
            fetchMySentRequestsUseCase(id).collect {
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
                        _sentRequestsState.value =
                            SentRequestsState.FetchSentRequestsSuccessfully(it.data)
                    }
                }
            }
        }
    }

    private fun setLoading(status: Boolean) {
        when (status) {
            true -> {
                _sentRequestsState.value = SentRequestsState.IsLoading(true)
            }
            false -> {
                _sentRequestsState.value = SentRequestsState.IsLoading(false)
            }
        }
    }

    private fun showError(message: String) {
        when (message) {
            resourceProvider.string(R.string.no_internet_connection) -> {
                _sentRequestsState.value = SentRequestsState.NoInternetConnection(message)
            }
            else -> {
                _sentRequestsState.value = SentRequestsState.ShowError(message)
            }
        }
    }

    fun getCurrentUserId(): String {
        return getFirebaseCurrentUserUseCase()!!.uid
    }
}