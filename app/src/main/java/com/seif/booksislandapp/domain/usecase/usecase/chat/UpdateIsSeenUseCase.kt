package com.seif.booksislandapp.domain.usecase.usecase.chat

import com.seif.booksislandapp.data.repository.ChatRepositoryImp
import com.seif.booksislandapp.domain.model.chat.Message
import javax.inject.Inject

class UpdateIsSeenUseCase @Inject constructor(
    private val chatRepositoryImp: ChatRepositoryImp
) {
    suspend operator fun invoke(
        senderId: String,
        receiverId: String,
        messages: List<Message>
    ) {
        chatRepositoryImp.updateIsSeen(senderId, receiverId, messages)
    }
}