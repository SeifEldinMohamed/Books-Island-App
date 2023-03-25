package com.seif.booksislandapp.domain.usecase.usecase.chat

import com.seif.booksislandapp.data.repository.ChatRepositoryImp
import com.seif.booksislandapp.domain.model.chat.Message
import com.seif.booksislandapp.utils.Resource
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject

class FetchMessagesBetweenTwoUsersUseCase @Inject constructor(
    private val chatRepositoryImp: ChatRepositoryImp
) {
    suspend operator fun invoke(
        senderId: String,
        recipientId: String
    ): Flow<Resource<List<Message>, String>> {
        return chatRepositoryImp.getMessages(senderId, recipientId)
    }
}