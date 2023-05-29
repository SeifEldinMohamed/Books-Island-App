package com.seif.booksislandapp.presentation.home.requests.sent_requests

import com.seif.booksislandapp.domain.model.request.MySentRequest

sealed class SentRequestsState {
    object Init : SentRequestsState()
    data class IsLoading(val isLoading: Boolean) : SentRequestsState()
    data class ShowError(val message: String) : SentRequestsState()
    data class NoInternetConnection(val message: String) : SentRequestsState()
    data class FetchSentRequestsSuccessfully(val sentRequests: List<MySentRequest>) :
        SentRequestsState()

    data class CancelSentRequestsSuccessfully(val message: String) : SentRequestsState()
    data class DeleteSentRequestsSuccessfully(val message: String) : SentRequestsState()
}
