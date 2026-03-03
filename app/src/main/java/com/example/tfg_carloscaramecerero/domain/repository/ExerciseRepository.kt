package com.example.tfg_carloscaramecerero.domain.repository

import com.example.tfg_carloscaramecerero.data.local.entity.ExerciseEntity
import kotlinx.coroutines.flow.Flow

interface ExerciseRepository {
    fun getAll(): Flow<List<ExerciseEntity>>
    fun getById(id: Long): Flow<ExerciseEntity?>
    fun getByMuscleGroup(muscleGroup: String): Flow<List<ExerciseEntity>>
    fun getAllMuscleGroups(): Flow<List<String>>
    fun getByType(type: String): Flow<List<ExerciseEntity>>
    suspend fun insert(exercise: ExerciseEntity): Long
    suspend fun update(exercise: ExerciseEntity)
    suspend fun delete(exercise: ExerciseEntity)
}

