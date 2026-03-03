package com.example.tfg_carloscaramecerero.data.local.dao

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import com.example.tfg_carloscaramecerero.data.local.entity.FoodEntryEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface FoodEntryDao {

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(foodEntry: FoodEntryEntity): Long

    @Update
    suspend fun update(foodEntry: FoodEntryEntity)

    @Delete
    suspend fun delete(foodEntry: FoodEntryEntity)

    /** Todas las entradas ordenadas por día de la semana y tipo de comida */
    @Query("SELECT * FROM food_entries ORDER BY dayOfWeek ASC, mealType ASC")
    fun getAll(): Flow<List<FoodEntryEntity>>

    /** Entradas de un día concreto de la semana (1=Lunes ... 7=Domingo) */
    @Query("SELECT * FROM food_entries WHERE dayOfWeek = :dayOfWeek ORDER BY mealType ASC, time ASC")
    fun getByDayOfWeek(dayOfWeek: Int): Flow<List<FoodEntryEntity>>

    /** Entradas de la semana actual (entre dos timestamps) */
    @Query("SELECT * FROM food_entries WHERE date BETWEEN :weekStart AND :weekEnd ORDER BY dayOfWeek ASC, mealType ASC")
    fun getEntriesForWeek(weekStart: Long, weekEnd: Long): Flow<List<FoodEntryEntity>>

    /** Entradas que aún no han sido analizadas por la IA */
    @Query("SELECT * FROM food_entries WHERE aiAnalyzed = 0 ORDER BY date DESC")
    fun getUnanalyzed(): Flow<List<FoodEntryEntity>>

    /** Contar registros por día de la semana (para ver qué días faltan por rellenar) */
    @Query("SELECT dayOfWeek FROM food_entries GROUP BY dayOfWeek")
    fun getDaysWithEntries(): Flow<List<Int>>
}

