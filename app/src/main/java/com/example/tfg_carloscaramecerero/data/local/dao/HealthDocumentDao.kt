package com.example.tfg_carloscaramecerero.data.local.dao

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.example.tfg_carloscaramecerero.data.local.entity.HealthDocumentEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface HealthDocumentDao {

    @Query("SELECT * FROM health_documents ORDER BY uploadDate DESC")
    fun getAll(): Flow<List<HealthDocumentEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(document: HealthDocumentEntity): Long

    @Delete
    suspend fun delete(document: HealthDocumentEntity)
}

