package com.example.tfg_carloscaramecerero.data.local.dao

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.example.tfg_carloscaramecerero.data.local.entity.BodyWeightEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface BodyWeightDao {

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(bodyWeight: BodyWeightEntity): Long

    @Delete
    suspend fun delete(bodyWeight: BodyWeightEntity)

    @Query("SELECT * FROM body_weight ORDER BY date DESC")
    fun getAll(): Flow<List<BodyWeightEntity>>

    @Query("SELECT * FROM body_weight ORDER BY date DESC LIMIT 1")
    fun getLatest(): Flow<BodyWeightEntity?>

    @Query("SELECT * FROM body_weight WHERE date BETWEEN :from AND :to ORDER BY date ASC")
    fun getBetweenDates(from: Long, to: Long): Flow<List<BodyWeightEntity>>
}

