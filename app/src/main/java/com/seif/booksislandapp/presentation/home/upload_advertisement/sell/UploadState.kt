package com.seif.booksislandapp.presentation.home.upload_advertisement.sell

sealed class UploadState {
    object Init : UploadState()
    data class IsLoading(val isLoading: Boolean) : UploadState()
    data class ShowError(val message: String) : UploadState()
    data class UploadedSuccessfully(val message: String) : UploadState()
    data class NoInternetConnection(val message: String) : UploadState()
}