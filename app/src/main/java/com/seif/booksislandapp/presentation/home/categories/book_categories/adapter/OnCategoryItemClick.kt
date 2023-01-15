package com.seif.booksislandapp.presentation.home.categories.book_categories.adapter

import com.seif.booksislandapp.domain.model.BookCategory

interface OnCategoryItemClick<T> {
    fun onCategoryItemClick(bookCategory: BookCategory)
}