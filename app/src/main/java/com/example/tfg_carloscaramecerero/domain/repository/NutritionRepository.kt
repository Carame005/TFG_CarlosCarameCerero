package com.example.tfg_carloscaramecerero.domain.repository

import com.example.tfg_carloscaramecerero.data.local.entity.FoodCatalogEntity
import com.example.tfg_carloscaramecerero.data.local.entity.FoodEntryEntity
import com.example.tfg_carloscaramecerero.data.local.entity.MealScheduleEntity
import com.example.tfg_carloscaramecerero.data.local.entity.NutritionalGoalEntity
import kotlinx.coroutines.flow.Flow

interface NutritionRepository {

    // ── Horarios ──────────────────────────────────────────────────────────────
    fun getAllSchedules(): Flow<List<MealScheduleEntity>>
    suspend fun insertSchedule(schedule: MealScheduleEntity): Long
    suspend fun deleteSchedule(schedule: MealScheduleEntity)

    // ── Entradas de comida ────────────────────────────────────────────────────
    /** Todas las entradas (todos los horarios) — se usa para contexto IA */
    fun getAllEntries(): Flow<List<FoodEntryEntity>>
    fun getEntriesByDayOfWeek(dayOfWeek: Int): Flow<List<FoodEntryEntity>>
    fun getEntriesForWeek(weekStart: Long, weekEnd: Long): Flow<List<FoodEntryEntity>>
    fun getUnanalyzedEntries(): Flow<List<FoodEntryEntity>>
    fun getDaysWithEntries(): Flow<List<Int>>

    /** Entradas filtradas por horario */
    fun getEntriesBySchedule(scheduleId: Long): Flow<List<FoodEntryEntity>>
    fun getDaysWithEntriesBySchedule(scheduleId: Long): Flow<List<Int>>
    suspend fun deleteEntriesBySchedule(scheduleId: Long)

    suspend fun insertEntry(entry: FoodEntryEntity): Long
    suspend fun updateEntry(entry: FoodEntryEntity)
    suspend fun deleteEntry(entry: FoodEntryEntity)

    // ── Objetivos nutricionales (uso futuro IA) ───────────────────────────────
    fun getCurrentGoal(): Flow<NutritionalGoalEntity?>
    suspend fun insertGoal(goal: NutritionalGoalEntity): Long
    suspend fun updateGoal(goal: NutritionalGoalEntity)

    // ── Catálogo de alimentos ──────────────────────────────────────────────────
    fun getAllCatalogItems(): Flow<List<FoodCatalogEntity>>
    fun searchCatalogItems(query: String): Flow<List<FoodCatalogEntity>>
    suspend fun insertCatalogItem(item: FoodCatalogEntity): Long
    suspend fun updateCatalogItem(item: FoodCatalogEntity)
    suspend fun deleteCatalogItem(item: FoodCatalogEntity)
}

