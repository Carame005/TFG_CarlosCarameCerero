package com.example.tfg_carloscaramecerero.data.repository

import com.example.tfg_carloscaramecerero.data.local.dao.BodyMeasurementDao
import com.example.tfg_carloscaramecerero.data.local.dao.BodyWeightDao
import com.example.tfg_carloscaramecerero.data.local.dao.HealthDocumentDao
import com.example.tfg_carloscaramecerero.data.local.dao.UserProfileDao
import com.example.tfg_carloscaramecerero.data.local.entity.BodyMeasurementEntity
import com.example.tfg_carloscaramecerero.data.local.entity.BodyWeightEntity
import com.example.tfg_carloscaramecerero.data.local.entity.HealthDocumentEntity
import com.example.tfg_carloscaramecerero.data.local.entity.UserProfileEntity
import com.example.tfg_carloscaramecerero.domain.repository.BodyRepository
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject

class BodyRepositoryImpl @Inject constructor(
    private val bodyWeightDao: BodyWeightDao,
    private val bodyMeasurementDao: BodyMeasurementDao,
    private val userProfileDao: UserProfileDao,
    private val healthDocumentDao: HealthDocumentDao
) : BodyRepository {

    // Weight
    override fun getAllWeights(): Flow<List<BodyWeightEntity>> = bodyWeightDao.getAll()

    override fun getLatestWeight(): Flow<BodyWeightEntity?> = bodyWeightDao.getLatest()

    override fun getWeightsBetweenDates(from: Long, to: Long): Flow<List<BodyWeightEntity>> =
        bodyWeightDao.getBetweenDates(from, to)

    override suspend fun insertWeight(bodyWeight: BodyWeightEntity): Long =
        bodyWeightDao.insert(bodyWeight)

    override suspend fun deleteWeight(bodyWeight: BodyWeightEntity) =
        bodyWeightDao.delete(bodyWeight)

    // Measurements
    override fun getAllMeasurements(): Flow<List<BodyMeasurementEntity>> =
        bodyMeasurementDao.getAll()

    override fun getLatestMeasurement(): Flow<BodyMeasurementEntity?> =
        bodyMeasurementDao.getLatest()

    override fun getMeasurementsBetweenDates(
        from: Long,
        to: Long
    ): Flow<List<BodyMeasurementEntity>> =
        bodyMeasurementDao.getBetweenDates(from, to)

    override suspend fun insertMeasurement(measurement: BodyMeasurementEntity): Long =
        bodyMeasurementDao.insert(measurement)

    override suspend fun deleteMeasurement(measurement: BodyMeasurementEntity) =
        bodyMeasurementDao.delete(measurement)

    // User Profile
    override fun getUserProfile(): Flow<UserProfileEntity?> =
        userProfileDao.getProfile()

    override suspend fun saveUserProfile(profile: UserProfileEntity) =
        userProfileDao.upsert(profile)

    // Health Documents
    override fun getAllHealthDocuments(): Flow<List<HealthDocumentEntity>> =
        healthDocumentDao.getAll()

    override suspend fun insertHealthDocument(document: HealthDocumentEntity): Long =
        healthDocumentDao.insert(document)

    override suspend fun deleteHealthDocument(document: HealthDocumentEntity) =
        healthDocumentDao.delete(document)
}

