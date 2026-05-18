package com.example.tfg_carloscaramecerero.data.repository

import com.example.tfg_carloscaramecerero.data.local.dao.AuditLogDao
import com.example.tfg_carloscaramecerero.data.local.entity.AuditLogEntity
import com.example.tfg_carloscaramecerero.domain.repository.AuditLogRepository
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject

class AuditLogRepositoryImpl @Inject constructor(
    private val auditLogDao: AuditLogDao
) : AuditLogRepository {

    override fun getAll(): Flow<List<AuditLogEntity>> = auditLogDao.getAll()

    override fun getByCategory(category: String): Flow<List<AuditLogEntity>> =
        auditLogDao.getByCategory(category)

    override suspend fun logAction(category: String, action: String, detail: String) {
        auditLogDao.insert(
            AuditLogEntity(
                category = category,
                action = action,
                detail = detail
            )
        )
    }

    override suspend fun deleteAll() = auditLogDao.deleteAll()
}

