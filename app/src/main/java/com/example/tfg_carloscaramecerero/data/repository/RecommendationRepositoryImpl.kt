package com.example.tfg_carloscaramecerero.data.repository

import com.example.tfg_carloscaramecerero.data.local.dao.RecommendationDao
import com.example.tfg_carloscaramecerero.data.local.entity.RecommendationEntity
import com.example.tfg_carloscaramecerero.domain.repository.RecommendationRepository
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject

class RecommendationRepositoryImpl @Inject constructor(
    private val recommendationDao: RecommendationDao
) : RecommendationRepository {

    override fun getAll(): Flow<List<RecommendationEntity>> = recommendationDao.getAll()

    override fun getUnread(): Flow<List<RecommendationEntity>> = recommendationDao.getUnread()

    override suspend fun insert(recommendation: RecommendationEntity): Long =
        recommendationDao.insert(recommendation)

    override suspend fun insertAll(recommendations: List<RecommendationEntity>) =
        recommendationDao.insertAll(recommendations)

    override suspend fun markAsRead(id: Long) = recommendationDao.markAsRead(id)

    override suspend fun deleteById(id: Long) = recommendationDao.deleteById(id)

    override suspend fun deleteOlderThan(timestamp: Long) =
        recommendationDao.deleteOlderThan(timestamp)

    override suspend fun deleteAll() = recommendationDao.deleteAll()
}

