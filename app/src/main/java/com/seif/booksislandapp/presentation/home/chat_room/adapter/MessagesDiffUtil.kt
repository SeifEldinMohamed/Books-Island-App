package com.seif.booksislandapp.presentation.home.chat_room.adapter

import androidx.recyclerview.widget.DiffUtil
import com.seif.booksislandapp.domain.model.chat.Message

class MessagesDiffUtil(
    private val oldList: List<Message>,
    private val newList: List<Message>
) : DiffUtil.Callback() {
    override fun getOldListSize(): Int {
        return oldList.size
    }

    override fun getNewListSize(): Int {
        return newList.size
    }

    override fun areItemsTheSame(oldItemPosition: Int, newItemPosition: Int): Boolean {
        return oldList[oldItemPosition].id == newList[newItemPosition].id
    }

    override fun areContentsTheSame(oldItemPosition: Int, newItemPosition: Int): Boolean {
        return oldList[oldItemPosition] === newList[newItemPosition]
    }
}