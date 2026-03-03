package com.example.tfg_carloscaramecerero.data.local.dao

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.example.tfg_carloscaramecerero.data.local.entity.BodyMeasurementEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface BodyMeasurementDao {

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(measurement: BodyMeasurementEntity): Long

    @Delete
    suspend fun delete(measurement: BodyMeasurementEntity)

    @Query("SELECT * FROM body_measurements ORDER BY date DESC")
    fun getAll(): Flow<List<BodyMeasurementEntity>>

    @Query("SELECT * FROM body_measurements ORDER BY date DESC LIMIT 1")
    fun getLatest(): Flow<BodyMeasurementEntity?>

    @Query("SELECT * FROM body_measurements WHERE date BETWEEN :from AND :to ORDER BY date ASC")
    fun getBetweenDates(from: Long, to: Long): Flow<List<BodyMeasurementEntity>>
}

