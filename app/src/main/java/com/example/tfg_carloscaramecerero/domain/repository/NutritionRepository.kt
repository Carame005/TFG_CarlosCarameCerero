package com.example.tfg_carloscaramecerero.domain.repository

import com.example.tfg_carloscaramecerero.data.local.entity.FoodEntryEntity
import com.example.tfg_carloscaramecerero.data.local.entity.NutritionalGoalEntity
import kotlinx.coroutines.flow.Flow

interface NutritionRepository {
    // Horario de comidas
    fun getAllEntries(): Flow<List<FoodEntryEntity>>
    fun getEntriesByDayOfWeek(dayOfWeek: Int): Flow<List<FoodEntryEntity>>
    fun getEntriesForWeek(weekStart: Long, weekEnd: Long): Flow<List<FoodEntryEntity>>
    fun getUnanalyzedEntries(): Flow<List<FoodEntryEntity>>
    fun getDaysWithEntries(): Flow<List<Int>>
    suspend fun insertEntry(entry: FoodEntryEntity): Long
    suspend fun updateEntry(entry: FoodEntryEntity)
    suspend fun deleteEntry(entry: FoodEntryEntity)

    // Objetivos nutricionales (para uso futuro de la IA)
    fun getCurrentGoal(): Flow<NutritionalGoalEntity?>
    suspend fun insertGoal(goal: NutritionalGoalEntity): Long
    suspend fun updateGoal(goal: NutritionalGoalEntity)
}

