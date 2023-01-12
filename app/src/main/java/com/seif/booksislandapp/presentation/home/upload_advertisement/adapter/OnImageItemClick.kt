package com.seif.booksislandapp.presentation.home.upload_advertisement.adapter

interface OnImageItemClick<T> {
    fun onRemoveImageItemClick(item: T, position: Int)
}