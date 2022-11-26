package com.seif.booksislandapp.utils

interface OnItemClickRecyclerView<T> {
    fun onEditItemClick(item: T)
    fun onDeleteItemClick(item: T, position: Int)
    fun onNoteItemClick(item: T)
}
