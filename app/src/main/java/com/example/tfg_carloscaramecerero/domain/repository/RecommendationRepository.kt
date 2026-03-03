package com.example.tfg_carloscaramecerero.domain.repository

import com.example.tfg_carloscaramecerero.data.local.entity.RecommendationEntity
import kotlinx.coroutines.flow.Flow

interface RecommendationRepository {
    fun getAll(): Flow<List<RecommendationEntity>>
    fun getUnread(): Flow<List<RecommendationEntity>>
    suspend fun insert(recommendation: RecommendationEntity): Long
    suspend fun insertAll(recommendations: List<RecommendationEntity>)
    suspend fun markAsRead(id: Long)
    suspend fun deleteById(id: Long)
    suspend fun deleteOlderThan(timestamp: Long)
    suspend fun deleteAll()
}

