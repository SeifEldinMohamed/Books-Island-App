package com.seif.booksislandapp.domain.usecase.usecase.my_chats

import com.seif.booksislandapp.domain.model.chat.MyChat
import com.seif.booksislandapp.domain.repository.MyChatsRepository
import com.seif.booksislandapp.utils.Resource
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject

class GetMyBuyingChatsUseCase @Inject constructor(
    private val myChatsRepository: MyChatsRepository
) {
    suspend operator fun invoke(myId: String): Flow<Resource<List<MyChat>, String>> {
        return myChatsRepository.getMyChats(myId)
    }
}