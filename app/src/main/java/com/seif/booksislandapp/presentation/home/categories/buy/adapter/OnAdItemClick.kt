package com.seif.booksislandapp.presentation.home.categories.buy.adapter

interface OnAdItemClick<T> {
    fun onAdItemClick(item: T, position: Int)
}