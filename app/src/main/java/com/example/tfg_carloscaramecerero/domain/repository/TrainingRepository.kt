package com.example.tfg_carloscaramecerero.domain.repository

import com.example.tfg_carloscaramecerero.data.local.entity.TrainingSessionEntity
import com.example.tfg_carloscaramecerero.data.local.entity.TrainingSetEntity
import com.example.tfg_carloscaramecerero.data.local.relation.SessionWithSets
import kotlinx.coroutines.flow.Flow

interface TrainingRepository {
    // Sessions
    fun getAllSessions(): Flow<List<TrainingSessionEntity>>
    fun getSessionById(id: Long): Flow<TrainingSessionEntity?>
    fun getSessionsByRoutine(routineId: Long): Flow<List<TrainingSessionEntity>>
    fun getSessionsBetweenDates(from: Long, to: Long): Flow<List<TrainingSessionEntity>>
    fun getSessionWithSets(sessionId: Long): Flow<SessionWithSets?>
    fun getAllSessionsWithSets(): Flow<List<SessionWithSets>>
    fun getSessionsWithSetsByRoutine(routineId: Long): Flow<List<SessionWithSets>>
    suspend fun insertSession(session: TrainingSessionEntity): Long
    suspend fun deleteSession(session: TrainingSessionEntity)

    // Sets
    fun getSetsBySession(sessionId: Long): Flow<List<TrainingSetEntity>>
    fun getSetsByExercise(exerciseId: Long): Flow<List<TrainingSetEntity>>
    suspend fun insertSet(set: TrainingSetEntity): Long
    suspend fun insertSets(sets: List<TrainingSetEntity>)
    suspend fun deleteSet(set: TrainingSetEntity)
    suspend fun deleteAllSetsBySession(sessionId: Long)
}

