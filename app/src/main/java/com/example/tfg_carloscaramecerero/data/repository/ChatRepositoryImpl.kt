package com.example.tfg_carloscaramecerero.data.repository

import com.example.tfg_carloscaramecerero.data.local.dao.ChatDao
import com.example.tfg_carloscaramecerero.data.local.entity.ChatConversationEntity
import com.example.tfg_carloscaramecerero.data.local.entity.ChatMessageEntity
import com.example.tfg_carloscaramecerero.domain.repository.ChatRepository
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class ChatRepositoryImpl @Inject constructor(
    private val chatDao: ChatDao
) : ChatRepository {

    override fun getAllConversations(): Flow<List<ChatConversationEntity>> =
        chatDao.getAllConversations()

    override suspend fun getConversationById(conversationId: Long): ChatConversationEntity? =
        chatDao.getConversationById(conversationId)

    override suspend fun insertConversation(conversation: ChatConversationEntity): Long =
        chatDao.insertConversation(conversation)

    override suspend fun updateConversation(conversation: ChatConversationEntity) =
        chatDao.updateConversation(conversation)

    override suspend fun deleteConversation(conversationId: Long) =
        chatDao.deleteConversation(conversationId)

    override suspend fun deleteOldConversations(olderThanTimestamp: Long) =
        chatDao.deleteConversationsOlderThan(olderThanTimestamp)

    override fun getMessagesByConversation(conversationId: Long): Flow<List<ChatMessageEntity>> =
        chatDao.getMessagesByConversation(conversationId)

    override suspend fun getMessagesByConversationOnce(conversationId: Long): List<ChatMessageEntity> =
        chatDao.getMessagesByConversationOnce(conversationId)

    override suspend fun insertMessage(message: ChatMessageEntity): Long =
        chatDao.insertMessage(message)

    override suspend fun getLastMessage(conversationId: Long): ChatMessageEntity? =
        chatDao.getLastMessage(conversationId)
}

