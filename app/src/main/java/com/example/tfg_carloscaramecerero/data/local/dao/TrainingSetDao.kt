package com.example.tfg_carloscaramecerero.data.local.dao

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.example.tfg_carloscaramecerero.data.local.entity.TrainingSetEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface TrainingSetDao {

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(set: TrainingSetEntity): Long

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAll(sets: List<TrainingSetEntity>)

    @Delete
    suspend fun delete(set: TrainingSetEntity)

    @Query("SELECT * FROM training_sets WHERE sessionId = :sessionId ORDER BY setNumber ASC")
    fun getSetsBySession(sessionId: Long): Flow<List<TrainingSetEntity>>

    @Query("SELECT * FROM training_sets WHERE exerciseId = :exerciseId ORDER BY sessionId DESC, setNumber ASC")
    fun getSetsByExercise(exerciseId: Long): Flow<List<TrainingSetEntity>>

    @Query("DELETE FROM training_sets WHERE sessionId = :sessionId")
    suspend fun deleteAllBySession(sessionId: Long)
}

