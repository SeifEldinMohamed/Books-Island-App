package com.seif.booksislandapp.presentation.home.categories

interface OnAdItemClick<T> {
    fun onAdItemClick(item: T, position: Int)
}