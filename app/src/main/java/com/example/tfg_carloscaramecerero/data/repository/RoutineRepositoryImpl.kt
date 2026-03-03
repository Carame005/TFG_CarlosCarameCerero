package com.example.tfg_carloscaramecerero.data.repository

import com.example.tfg_carloscaramecerero.data.local.dao.RoutineDao
import com.example.tfg_carloscaramecerero.data.local.entity.RoutineEntity
import com.example.tfg_carloscaramecerero.data.local.entity.RoutineExerciseCrossRef
import com.example.tfg_carloscaramecerero.data.local.relation.RoutineWithExercises
import com.example.tfg_carloscaramecerero.domain.repository.RoutineRepository
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject

class RoutineRepositoryImpl @Inject constructor(
    private val routineDao: RoutineDao
) : RoutineRepository {

    override fun getAll(): Flow<List<RoutineEntity>> = routineDao.getAll()

    override fun getById(id: Long): Flow<RoutineEntity?> = routineDao.getById(id)

    override fun getRoutineWithExercises(routineId: Long): Flow<RoutineWithExercises?> =
        routineDao.getRoutineWithExercises(routineId)

    override fun getAllRoutinesWithExercises(): Flow<List<RoutineWithExercises>> =
        routineDao.getAllRoutinesWithExercises()

    override fun getCrossRefsForRoutine(routineId: Long): Flow<List<RoutineExerciseCrossRef>> =
        routineDao.getCrossRefsForRoutine(routineId)

    override suspend fun insert(routine: RoutineEntity): Long = routineDao.insert(routine)

    override suspend fun update(routine: RoutineEntity) = routineDao.update(routine)

    override suspend fun delete(routine: RoutineEntity) = routineDao.delete(routine)

    override suspend fun addExerciseToRoutine(crossRef: RoutineExerciseCrossRef) =
        routineDao.insertRoutineExerciseCrossRef(crossRef)

    override suspend fun removeExerciseFromRoutine(crossRef: RoutineExerciseCrossRef) =
        routineDao.deleteRoutineExerciseCrossRef(crossRef)

    override suspend fun deleteAllExercisesFromRoutine(routineId: Long) =
        routineDao.deleteAllExercisesFromRoutine(routineId)
}

