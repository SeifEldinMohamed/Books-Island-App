package com.seif.booksislandapp.presentation.home.requests.received_requests

interface OnReceivedRequestItemClick<T> {
    fun onAcceptButtonClick(item: T, position: Int)
    fun onRejectButtonClick(item: T, position: Int)
}