package com.seif.booksislandapp.presentation.home.chat_room

import com.seif.booksislandapp.domain.model.User
import com.seif.booksislandapp.domain.model.chat.Message

sealed class ChatRoomState {
    object Init : ChatRoomState()
    data class IsLoading(val isLoading: Boolean) : ChatRoomState()
    data class ShowError(val message: String) : ChatRoomState()
    data class FetchMessagesSuccessfully(val messages: List<Message>) : ChatRoomState()
    data class FetchUserSuccessfully(val user: User) : ChatRoomState()
    data class SendMessageSuccessfully(val message: Message) : ChatRoomState()
    data class NoInternetConnection(val message: String) : ChatRoomState()
}
