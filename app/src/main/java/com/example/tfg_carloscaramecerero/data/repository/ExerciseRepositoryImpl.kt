package com.example.tfg_carloscaramecerero.data.repository

import com.example.tfg_carloscaramecerero.data.local.dao.ExerciseDao
import com.example.tfg_carloscaramecerero.data.local.entity.ExerciseEntity
import com.example.tfg_carloscaramecerero.domain.repository.ExerciseRepository
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject

class ExerciseRepositoryImpl @Inject constructor(
    private val exerciseDao: ExerciseDao
) : ExerciseRepository {

    override fun getAll(): Flow<List<ExerciseEntity>> = exerciseDao.getAll()

    override fun getById(id: Long): Flow<ExerciseEntity?> = exerciseDao.getById(id)

    override fun getByMuscleGroup(muscleGroup: String): Flow<List<ExerciseEntity>> =
        exerciseDao.getByMuscleGroup(muscleGroup)

    override fun getAllMuscleGroups(): Flow<List<String>> = exerciseDao.getAllMuscleGroups()

    override fun getByType(type: String): Flow<List<ExerciseEntity>> = exerciseDao.getByType(type)

    override suspend fun insert(exercise: ExerciseEntity): Long = exerciseDao.insert(exercise)

    override suspend fun update(exercise: ExerciseEntity) = exerciseDao.update(exercise)

    override suspend fun delete(exercise: ExerciseEntity) = exerciseDao.delete(exercise)
}

