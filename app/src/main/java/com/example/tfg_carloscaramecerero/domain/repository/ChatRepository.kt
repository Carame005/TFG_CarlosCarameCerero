package com.example.tfg_carloscaramecerero.domain.repository

import com.example.tfg_carloscaramecerero.data.local.entity.ChatConversationEntity
import com.example.tfg_carloscaramecerero.data.local.entity.ChatMessageEntity
import kotlinx.coroutines.flow.Flow

interface ChatRepository {
    fun getAllConversations(): Flow<List<ChatConversationEntity>>
    suspend fun getConversationById(conversationId: Long): ChatConversationEntity?
    suspend fun insertConversation(conversation: ChatConversationEntity): Long
    suspend fun updateConversation(conversation: ChatConversationEntity)
    suspend fun deleteConversation(conversationId: Long)
    suspend fun deleteOldConversations(olderThanTimestamp: Long)

    fun getMessagesByConversation(conversationId: Long): Flow<List<ChatMessageEntity>>
    suspend fun getMessagesByConversationOnce(conversationId: Long): List<ChatMessageEntity>
    suspend fun insertMessage(message: ChatMessageEntity): Long
    suspend fun getLastMessage(conversationId: Long): ChatMessageEntity?
}

