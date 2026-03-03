package com.example.tfg_carloscaramecerero.domain.repository

import com.example.tfg_carloscaramecerero.data.local.entity.BodyMeasurementEntity
import com.example.tfg_carloscaramecerero.data.local.entity.BodyWeightEntity
import com.example.tfg_carloscaramecerero.data.local.entity.HealthDocumentEntity
import com.example.tfg_carloscaramecerero.data.local.entity.UserProfileEntity
import kotlinx.coroutines.flow.Flow

interface BodyRepository {
    // Weight
    fun getAllWeights(): Flow<List<BodyWeightEntity>>
    fun getLatestWeight(): Flow<BodyWeightEntity?>
    fun getWeightsBetweenDates(from: Long, to: Long): Flow<List<BodyWeightEntity>>
    suspend fun insertWeight(bodyWeight: BodyWeightEntity): Long
    suspend fun deleteWeight(bodyWeight: BodyWeightEntity)

    // Measurements
    fun getAllMeasurements(): Flow<List<BodyMeasurementEntity>>
    fun getLatestMeasurement(): Flow<BodyMeasurementEntity?>
    fun getMeasurementsBetweenDates(from: Long, to: Long): Flow<List<BodyMeasurementEntity>>
    suspend fun insertMeasurement(measurement: BodyMeasurementEntity): Long
    suspend fun deleteMeasurement(measurement: BodyMeasurementEntity)

    // User Profile
    fun getUserProfile(): Flow<UserProfileEntity?>
    suspend fun saveUserProfile(profile: UserProfileEntity)

    // Health Documents
    fun getAllHealthDocuments(): Flow<List<HealthDocumentEntity>>
    suspend fun insertHealthDocument(document: HealthDocumentEntity): Long
    suspend fun deleteHealthDocument(document: HealthDocumentEntity)
}

