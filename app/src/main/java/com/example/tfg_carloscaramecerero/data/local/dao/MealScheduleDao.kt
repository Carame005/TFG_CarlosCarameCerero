package com.example.tfg_carloscaramecerero.data.local.dao

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.example.tfg_carloscaramecerero.data.local.entity.MealScheduleEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface MealScheduleDao {

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(schedule: MealScheduleEntity): Long

    @Delete
    suspend fun delete(schedule: MealScheduleEntity)

    @Query("SELECT * FROM meal_schedules ORDER BY createdAt ASC")
    fun getAll(): Flow<List<MealScheduleEntity>>
}

