package com.example.tfg_carloscaramecerero.data.local.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.example.tfg_carloscaramecerero.data.local.entity.AuditLogEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface AuditLogDao {

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(entry: AuditLogEntity): Long

    @Query("SELECT * FROM audit_log ORDER BY timestamp DESC")
    fun getAll(): Flow<List<AuditLogEntity>>

    @Query("SELECT * FROM audit_log WHERE category = :category ORDER BY timestamp DESC")
    fun getByCategory(category: String): Flow<List<AuditLogEntity>>

    @Query("SELECT COUNT(*) FROM audit_log")
    fun getCount(): Flow<Int>

    @Query("DELETE FROM audit_log")
    suspend fun deleteAll()

    @Query("DELETE FROM audit_log WHERE timestamp < :timestamp")
    suspend fun deleteOlderThan(timestamp: Long)
}

