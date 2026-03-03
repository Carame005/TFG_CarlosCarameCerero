package com.example.tfg_carloscaramecerero.data.local.dao

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Transaction
import androidx.room.Update
import com.example.tfg_carloscaramecerero.data.local.entity.RoutineEntity
import com.example.tfg_carloscaramecerero.data.local.entity.RoutineExerciseCrossRef
import com.example.tfg_carloscaramecerero.data.local.relation.RoutineWithExercises
import kotlinx.coroutines.flow.Flow

@Dao
interface RoutineDao {

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(routine: RoutineEntity): Long

    @Update
    suspend fun update(routine: RoutineEntity)

    @Delete
    suspend fun delete(routine: RoutineEntity)

    @Query("SELECT * FROM routines ORDER BY createdAt DESC")
    fun getAll(): Flow<List<RoutineEntity>>

    @Query("SELECT * FROM routines WHERE id = :id")
    fun getById(id: Long): Flow<RoutineEntity?>

    @Transaction
    @Query("SELECT * FROM routines WHERE id = :routineId")
    fun getRoutineWithExercises(routineId: Long): Flow<RoutineWithExercises?>

    @Transaction
    @Query("SELECT * FROM routines ORDER BY createdAt DESC")
    fun getAllRoutinesWithExercises(): Flow<List<RoutineWithExercises>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertRoutineExerciseCrossRef(crossRef: RoutineExerciseCrossRef)

    @Delete
    suspend fun deleteRoutineExerciseCrossRef(crossRef: RoutineExerciseCrossRef)

    @Query("DELETE FROM routine_exercise_cross_ref WHERE routineId = :routineId")
    suspend fun deleteAllExercisesFromRoutine(routineId: Long)

    @Query("SELECT * FROM routine_exercise_cross_ref WHERE routineId = :routineId ORDER BY orderIndex ASC")
    fun getCrossRefsForRoutine(routineId: Long): Flow<List<RoutineExerciseCrossRef>>
}

