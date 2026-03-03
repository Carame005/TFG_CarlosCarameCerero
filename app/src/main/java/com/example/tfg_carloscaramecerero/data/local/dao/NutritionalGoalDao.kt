package com.example.tfg_carloscaramecerero.data.local.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import com.example.tfg_carloscaramecerero.data.local.entity.NutritionalGoalEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface NutritionalGoalDao {

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(goal: NutritionalGoalEntity): Long

    @Update
    suspend fun update(goal: NutritionalGoalEntity)

    @Query("SELECT * FROM nutritional_goals ORDER BY createdAt DESC LIMIT 1")
    fun getCurrentGoal(): Flow<NutritionalGoalEntity?>

    @Query("SELECT * FROM nutritional_goals ORDER BY createdAt DESC")
    fun getAll(): Flow<List<NutritionalGoalEntity>>
}

