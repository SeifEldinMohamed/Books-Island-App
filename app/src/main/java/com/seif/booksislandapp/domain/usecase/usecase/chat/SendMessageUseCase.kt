package com.seif.booksislandapp.domain.usecase.usecase.chat

import com.seif.booksislandapp.R
import com.seif.booksislandapp.data.repository.ChatRepositoryImp
import com.seif.booksislandapp.domain.model.chat.Message
import com.seif.booksislandapp.utils.Resource
import com.seif.booksislandapp.utils.ResourceProvider
import com.seif.booksislandapp.utils.isValidMessage
import javax.inject.Inject

class SendMessageUseCase @Inject constructor(
    val chatRepositoryImp: ChatRepositoryImp,
    val resourceProvider: ResourceProvider
) {
    suspend operator fun invoke(message: Message): Resource<Message, String> {
        return when (message.isValidMessage()) {
            true -> chatRepositoryImp.sendMessage(message)
            false -> Resource.Error(resourceProvider.string(R.string.message_cannot_be_empty))
        }
    }
}