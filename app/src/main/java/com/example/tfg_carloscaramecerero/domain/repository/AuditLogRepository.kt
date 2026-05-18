package com.example.tfg_carloscaramecerero.domain.repository

import com.example.tfg_carloscaramecerero.data.local.entity.AuditLogEntity
import kotlinx.coroutines.flow.Flow

interface AuditLogRepository {
    fun getAll(): Flow<List<AuditLogEntity>>
    fun getByCategory(category: String): Flow<List<AuditLogEntity>>
    suspend fun logAction(category: String, action: String, detail: String = "")
    suspend fun deleteAll()
}

