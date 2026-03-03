package com.example.tfg_carloscaramecerero.domain.repository

import com.example.tfg_carloscaramecerero.data.local.entity.RoutineEntity
import com.example.tfg_carloscaramecerero.data.local.entity.RoutineExerciseCrossRef
import com.example.tfg_carloscaramecerero.data.local.relation.RoutineWithExercises
import kotlinx.coroutines.flow.Flow

interface RoutineRepository {
    fun getAll(): Flow<List<RoutineEntity>>
    fun getById(id: Long): Flow<RoutineEntity?>
    fun getRoutineWithExercises(routineId: Long): Flow<RoutineWithExercises?>
    fun getAllRoutinesWithExercises(): Flow<List<RoutineWithExercises>>
    fun getCrossRefsForRoutine(routineId: Long): Flow<List<RoutineExerciseCrossRef>>
    suspend fun insert(routine: RoutineEntity): Long
    suspend fun update(routine: RoutineEntity)
    suspend fun delete(routine: RoutineEntity)
    suspend fun addExerciseToRoutine(crossRef: RoutineExerciseCrossRef)
    suspend fun removeExerciseFromRoutine(crossRef: RoutineExerciseCrossRef)
    suspend fun deleteAllExercisesFromRoutine(routineId: Long)
}

