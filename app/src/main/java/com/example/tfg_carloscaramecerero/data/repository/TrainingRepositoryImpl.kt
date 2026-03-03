package com.example.tfg_carloscaramecerero.data.repository

import com.example.tfg_carloscaramecerero.data.local.dao.TrainingSessionDao
import com.example.tfg_carloscaramecerero.data.local.dao.TrainingSetDao
import com.example.tfg_carloscaramecerero.data.local.entity.TrainingSessionEntity
import com.example.tfg_carloscaramecerero.data.local.entity.TrainingSetEntity
import com.example.tfg_carloscaramecerero.data.local.relation.SessionWithSets
import com.example.tfg_carloscaramecerero.domain.repository.TrainingRepository
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject

class TrainingRepositoryImpl @Inject constructor(
    private val sessionDao: TrainingSessionDao,
    private val setDao: TrainingSetDao
) : TrainingRepository {

    // Sessions
    override fun getAllSessions(): Flow<List<TrainingSessionEntity>> = sessionDao.getAll()

    override fun getSessionById(id: Long): Flow<TrainingSessionEntity?> = sessionDao.getById(id)

    override fun getSessionsByRoutine(routineId: Long): Flow<List<TrainingSessionEntity>> =
        sessionDao.getSessionsByRoutine(routineId)

    override fun getSessionsBetweenDates(from: Long, to: Long): Flow<List<TrainingSessionEntity>> =
        sessionDao.getSessionsBetweenDates(from, to)

    override fun getSessionWithSets(sessionId: Long): Flow<SessionWithSets?> =
        sessionDao.getSessionWithSets(sessionId)

    override fun getAllSessionsWithSets(): Flow<List<SessionWithSets>> =
        sessionDao.getAllSessionsWithSets()

    override fun getSessionsWithSetsByRoutine(routineId: Long): Flow<List<SessionWithSets>> =
        sessionDao.getSessionsWithSetsByRoutine(routineId)

    override suspend fun insertSession(session: TrainingSessionEntity): Long =
        sessionDao.insert(session)

    override suspend fun deleteSession(session: TrainingSessionEntity) =
        sessionDao.delete(session)

    // Sets
    override fun getSetsBySession(sessionId: Long): Flow<List<TrainingSetEntity>> =
        setDao.getSetsBySession(sessionId)

    override fun getSetsByExercise(exerciseId: Long): Flow<List<TrainingSetEntity>> =
        setDao.getSetsByExercise(exerciseId)

    override suspend fun insertSet(set: TrainingSetEntity): Long = setDao.insert(set)

    override suspend fun insertSets(sets: List<TrainingSetEntity>) = setDao.insertAll(sets)

    override suspend fun deleteSet(set: TrainingSetEntity) = setDao.delete(set)

    override suspend fun deleteAllSetsBySession(sessionId: Long) =
        setDao.deleteAllBySession(sessionId)
}

