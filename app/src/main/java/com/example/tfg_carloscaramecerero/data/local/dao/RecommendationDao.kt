package com.example.tfg_carloscaramecerero.data.local.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.example.tfg_carloscaramecerero.data.local.entity.RecommendationEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface RecommendationDao {

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(recommendation: RecommendationEntity): Long

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAll(recommendations: List<RecommendationEntity>)

    @Query("SELECT * FROM recommendations ORDER BY createdAt DESC")
    fun getAll(): Flow<List<RecommendationEntity>>

    @Query("SELECT * FROM recommendations WHERE isRead = 0 ORDER BY createdAt DESC")
    fun getUnread(): Flow<List<RecommendationEntity>>

    @Query("UPDATE recommendations SET isRead = 1 WHERE id = :id")
    suspend fun markAsRead(id: Long)

    @Query("DELETE FROM recommendations WHERE id = :id")
    suspend fun deleteById(id: Long)

    @Query("DELETE FROM recommendations WHERE createdAt < :timestamp")
    suspend fun deleteOlderThan(timestamp: Long)

    @Query("DELETE FROM recommendations")
    suspend fun deleteAll()
}

