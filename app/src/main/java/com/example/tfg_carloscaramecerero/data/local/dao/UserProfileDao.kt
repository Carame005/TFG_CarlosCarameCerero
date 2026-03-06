package com.example.tfg_carloscaramecerero.data.local.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.example.tfg_carloscaramecerero.data.local.entity.UserProfileEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface UserProfileDao {

    @Query("SELECT * FROM user_profile WHERE id = 1")
    fun getProfile(): Flow<UserProfileEntity?>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun upsert(profile: UserProfileEntity)

    @Query("UPDATE user_profile SET height = :height WHERE id = 1")
    suspend fun updateHeight(height: Double)

    @Query("UPDATE user_profile SET healthConditions = :conditions WHERE id = 1")
    suspend fun updateHealthConditions(conditions: String)

    @Query("UPDATE user_profile SET fitnessGoal = :goal WHERE id = 1")
    suspend fun updateFitnessGoal(goal: String)
}

