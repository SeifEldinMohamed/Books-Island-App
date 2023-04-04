package com.seif.booksislandapp.presentation.home.chat_room

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.google.firebase.auth.FirebaseUser
import com.seif.booksislandapp.R
import com.seif.booksislandapp.domain.model.chat.Message
import com.seif.booksislandapp.domain.usecase.usecase.chat.FetchMessagesBetweenTwoUsersUseCase
import com.seif.booksislandapp.domain.usecase.usecase.chat.SendMessageUseCase
import com.seif.booksislandapp.domain.usecase.usecase.user.GetFirebaseCurrentUserUseCase
import com.seif.booksislandapp.utils.Resource
import com.seif.booksislandapp.utils.ResourceProvider
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import javax.inject.Inject

@HiltViewModel
class ChatRoomViewModel @Inject constructor(
    private val fetchMessagesBetweenTwoUsersUseCase: FetchMessagesBetweenTwoUsersUseCase,
    private val sendMessageUseCase: SendMessageUseCase,
    private val getFirebaseCurrentUserUseCase: GetFirebaseCurrentUserUseCase,
    private val resourceProvider: ResourceProvider
) : ViewModel() {
    private var _chatRoomState = MutableStateFlow<ChatRoomState>(ChatRoomState.Init)
    val chatRoomState = _chatRoomState.asStateFlow()

    fun requestSendMessage(message: Message) {
        viewModelScope.launch(Dispatchers.IO) {
            sendMessageUseCase(message).let {
                when (it) {
                    is Resource.Error -> showError(it.message)
                    is Resource.Success -> {
                        _chatRoomState.value =
                            ChatRoomState.SendMessageSuccessfully(it.data)
                    }
                }
            }
        }
    }

    fun requestFetchMessagesBetweenTwoUsers(
        senderId: String,
        receiverId: String
    ) {
        setLoading(true)
        viewModelScope.launch(Dispatchers.IO) {
            fetchMessagesBetweenTwoUsersUseCase(senderId, receiverId).collect {
                when (it) {
                    is Resource.Error -> {
                        withContext(Dispatchers.Main) {
                            setLoading(false)
                            showError(it.message)
                        }
                    }
                    is Resource.Success -> {
                        withContext(Dispatchers.Main) {
                            setLoading(false)
                            _chatRoomState.value =
                                ChatRoomState.FetchMessagesSuccessfullySuccessfully(it.data)
                        }
                    }
                }
            }
        }
    }

    fun getFirebaseCurrentUser(): FirebaseUser? {
        return getFirebaseCurrentUserUseCase()
    }

    private fun setLoading(status: Boolean) {
        when (status) {
            true -> {
                _chatRoomState.value = ChatRoomState.IsLoading(true)
            }
            false -> {
                _chatRoomState.value = ChatRoomState.IsLoading(false)
            }
        }
    }

    private fun showError(message: String) {
        when (message) {
            resourceProvider.string(R.string.no_internet_connection) -> {
                _chatRoomState.value = ChatRoomState.NoInternetConnection(message)
            }
            else -> {
                _chatRoomState.value = ChatRoomState.ShowError(message)
            }
        }
    }
}