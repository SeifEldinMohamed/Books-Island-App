package com.seif.booksislandapp.presentation.home.my_chats.fragments.buying_chats

import com.seif.booksislandapp.domain.model.chat.MyChat

sealed class MyChatsState {
    object Init : MyChatsState()
    data class IsLoading(val isLoading: Boolean) : MyChatsState()
    data class ShowError(val errorMessage: String) : MyChatsState()
    data class FetchMyChatsSuccessfully(val myBuyingChat: List<MyChat>) : MyChatsState()
    data class NoInternetConnection(val message: String) : MyChatsState()
}
