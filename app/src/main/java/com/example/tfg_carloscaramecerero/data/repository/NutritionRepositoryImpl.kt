package com.example.tfg_carloscaramecerero.data.repository

import com.example.tfg_carloscaramecerero.data.local.dao.FoodCatalogDao
import com.example.tfg_carloscaramecerero.data.local.dao.FoodEntryDao
import com.example.tfg_carloscaramecerero.data.local.dao.MealScheduleDao
import com.example.tfg_carloscaramecerero.data.local.dao.NutritionalGoalDao
import com.example.tfg_carloscaramecerero.data.local.entity.FoodCatalogEntity
import com.example.tfg_carloscaramecerero.data.local.entity.FoodEntryEntity
import com.example.tfg_carloscaramecerero.data.local.entity.MealScheduleEntity
import com.example.tfg_carloscaramecerero.data.local.entity.NutritionalGoalEntity
import com.example.tfg_carloscaramecerero.domain.repository.NutritionRepository
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject

class NutritionRepositoryImpl @Inject constructor(
    private val foodEntryDao: FoodEntryDao,
    private val mealScheduleDao: MealScheduleDao,
    private val nutritionalGoalDao: NutritionalGoalDao,
    private val foodCatalogDao: FoodCatalogDao
) : NutritionRepository {

    // ── Horarios ──────────────────────────────────────────────────────────────
    override fun getAllSchedules(): Flow<List<MealScheduleEntity>> = mealScheduleDao.getAll()
    override suspend fun insertSchedule(schedule: MealScheduleEntity): Long = mealScheduleDao.insert(schedule)
    override suspend fun deleteSchedule(schedule: MealScheduleEntity) = mealScheduleDao.delete(schedule)

    // ── Entradas de comida ────────────────────────────────────────────────────
    override fun getAllEntries(): Flow<List<FoodEntryEntity>> = foodEntryDao.getAll()
    override fun getEntriesByDayOfWeek(dayOfWeek: Int): Flow<List<FoodEntryEntity>> = foodEntryDao.getByDayOfWeek(dayOfWeek)
    override fun getEntriesForWeek(weekStart: Long, weekEnd: Long): Flow<List<FoodEntryEntity>> = foodEntryDao.getEntriesForWeek(weekStart, weekEnd)
    override fun getUnanalyzedEntries(): Flow<List<FoodEntryEntity>> = foodEntryDao.getUnanalyzed()
    override fun getDaysWithEntries(): Flow<List<Int>> = foodEntryDao.getDaysWithEntries()

    override fun getEntriesBySchedule(scheduleId: Long): Flow<List<FoodEntryEntity>> = foodEntryDao.getBySchedule(scheduleId)
    override fun getDaysWithEntriesBySchedule(scheduleId: Long): Flow<List<Int>> = foodEntryDao.getDaysWithEntriesBySchedule(scheduleId)
    override suspend fun deleteEntriesBySchedule(scheduleId: Long) = foodEntryDao.deleteBySchedule(scheduleId)

    override suspend fun insertEntry(entry: FoodEntryEntity): Long = foodEntryDao.insert(entry)
    override suspend fun updateEntry(entry: FoodEntryEntity) = foodEntryDao.update(entry)
    override suspend fun deleteEntry(entry: FoodEntryEntity) = foodEntryDao.delete(entry)

    // ── Objetivos nutricionales ───────────────────────────────────────────────
    override fun getCurrentGoal(): Flow<NutritionalGoalEntity?> = nutritionalGoalDao.getCurrentGoal()
    override suspend fun insertGoal(goal: NutritionalGoalEntity): Long = nutritionalGoalDao.insert(goal)
    override suspend fun updateGoal(goal: NutritionalGoalEntity) = nutritionalGoalDao.update(goal)

    // ── Catálogo de alimentos ─────────────────────────────────────────────────
    override fun getAllCatalogItems(): Flow<List<FoodCatalogEntity>> = foodCatalogDao.getAll()
    override fun searchCatalogItems(query: String): Flow<List<FoodCatalogEntity>> = foodCatalogDao.searchByName(query)
    override suspend fun insertCatalogItem(item: FoodCatalogEntity): Long = foodCatalogDao.insert(item)
    override suspend fun updateCatalogItem(item: FoodCatalogEntity) = foodCatalogDao.update(item)
    override suspend fun deleteCatalogItem(item: FoodCatalogEntity) = foodCatalogDao.delete(item)
}

