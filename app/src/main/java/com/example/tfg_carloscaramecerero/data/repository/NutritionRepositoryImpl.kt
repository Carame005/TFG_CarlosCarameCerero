package com.example.tfg_carloscaramecerero.data.repository

import com.example.tfg_carloscaramecerero.data.local.dao.FoodEntryDao
import com.example.tfg_carloscaramecerero.data.local.dao.NutritionalGoalDao
import com.example.tfg_carloscaramecerero.data.local.entity.FoodEntryEntity
import com.example.tfg_carloscaramecerero.data.local.entity.NutritionalGoalEntity
import com.example.tfg_carloscaramecerero.domain.repository.NutritionRepository
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject

class NutritionRepositoryImpl @Inject constructor(
    private val foodEntryDao: FoodEntryDao,
    private val nutritionalGoalDao: NutritionalGoalDao
) : NutritionRepository {

    // Horario de comidas
    override fun getAllEntries(): Flow<List<FoodEntryEntity>> = foodEntryDao.getAll()

    override fun getEntriesByDayOfWeek(dayOfWeek: Int): Flow<List<FoodEntryEntity>> =
        foodEntryDao.getByDayOfWeek(dayOfWeek)

    override fun getEntriesForWeek(weekStart: Long, weekEnd: Long): Flow<List<FoodEntryEntity>> =
        foodEntryDao.getEntriesForWeek(weekStart, weekEnd)

    override fun getUnanalyzedEntries(): Flow<List<FoodEntryEntity>> =
        foodEntryDao.getUnanalyzed()

    override fun getDaysWithEntries(): Flow<List<Int>> = foodEntryDao.getDaysWithEntries()

    override suspend fun insertEntry(entry: FoodEntryEntity): Long = foodEntryDao.insert(entry)

    override suspend fun updateEntry(entry: FoodEntryEntity) = foodEntryDao.update(entry)

    override suspend fun deleteEntry(entry: FoodEntryEntity) = foodEntryDao.delete(entry)

    // Objetivos nutricionales
    override fun getCurrentGoal(): Flow<NutritionalGoalEntity?> =
        nutritionalGoalDao.getCurrentGoal()

    override suspend fun insertGoal(goal: NutritionalGoalEntity): Long =
        nutritionalGoalDao.insert(goal)

    override suspend fun updateGoal(goal: NutritionalGoalEntity) =
        nutritionalGoalDao.update(goal)
}

