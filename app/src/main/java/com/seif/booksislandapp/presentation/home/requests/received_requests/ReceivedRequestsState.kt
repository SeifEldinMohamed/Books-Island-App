package com.seif.booksislandapp.presentation.home.requests.received_requests

import com.seif.booksislandapp.domain.model.request.MyReceivedRequest

sealed class ReceivedRequestsState {
    object Init : ReceivedRequestsState()
    data class IsLoading(val isLoading: Boolean) : ReceivedRequestsState()
    data class ShowError(val message: String) : ReceivedRequestsState()
    data class NoInternetConnection(val message: String) : ReceivedRequestsState()
    data class FetchReceivedRequestsSuccessfully(val receivedRequests: List<MyReceivedRequest>) :
        ReceivedRequestsState()

    data class AcceptedConfirmationRequestSuccessfully(val message: String) :
        ReceivedRequestsState()

    data class RejectedConfirmationRequestSuccessfully(val message: String) :
        ReceivedRequestsState()
}
