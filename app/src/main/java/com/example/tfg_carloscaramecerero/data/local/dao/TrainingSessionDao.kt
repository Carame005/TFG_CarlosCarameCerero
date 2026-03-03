package com.example.tfg_carloscaramecerero.data.local.dao

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Transaction
import com.example.tfg_carloscaramecerero.data.local.entity.TrainingSessionEntity
import com.example.tfg_carloscaramecerero.data.local.relation.SessionWithSets
import kotlinx.coroutines.flow.Flow

@Dao
interface TrainingSessionDao {

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(session: TrainingSessionEntity): Long

    @Delete
    suspend fun delete(session: TrainingSessionEntity)

    @Query("SELECT * FROM training_sessions ORDER BY date DESC")
    fun getAll(): Flow<List<TrainingSessionEntity>>

    @Query("SELECT * FROM training_sessions WHERE id = :id")
    fun getById(id: Long): Flow<TrainingSessionEntity?>

    @Query("SELECT * FROM training_sessions WHERE routineId = :routineId ORDER BY date DESC")
    fun getSessionsByRoutine(routineId: Long): Flow<List<TrainingSessionEntity>>

    @Query("SELECT * FROM training_sessions WHERE date BETWEEN :from AND :to ORDER BY date DESC")
    fun getSessionsBetweenDates(from: Long, to: Long): Flow<List<TrainingSessionEntity>>

    @Transaction
    @Query("SELECT * FROM training_sessions WHERE id = :sessionId")
    fun getSessionWithSets(sessionId: Long): Flow<SessionWithSets?>

    @Transaction
    @Query("SELECT * FROM training_sessions ORDER BY date DESC")
    fun getAllSessionsWithSets(): Flow<List<SessionWithSets>>

    @Transaction
    @Query("SELECT * FROM training_sessions WHERE routineId = :routineId ORDER BY date DESC")
    fun getSessionsWithSetsByRoutine(routineId: Long): Flow<List<SessionWithSets>>
}

