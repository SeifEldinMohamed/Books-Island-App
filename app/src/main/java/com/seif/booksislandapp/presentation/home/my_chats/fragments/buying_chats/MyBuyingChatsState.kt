package com.seif.booksislandapp.presentation.home.my_chats.fragments.buying_chats

import com.seif.booksislandapp.domain.model.chat.MyChat

sealed class MyBuyingChatsState {
    object Init : MyBuyingChatsState()
    data class IsLoading(val isLoading: Boolean) : MyBuyingChatsState()
    data class ShowError(val errorMessage: String) : MyBuyingChatsState()
    data class FetchMyBuyingChatsSuccessfully(val myBuyingChat: List<MyChat>) : MyBuyingChatsState()
    data class NoInternetConnection(val message: String) : MyBuyingChatsState()
}
