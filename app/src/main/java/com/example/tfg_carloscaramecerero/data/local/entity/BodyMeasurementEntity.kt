package com.example.tfg_carloscaramecerero.data.local.entity

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "body_measurements")
data class BodyMeasurementEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val date: Long = System.currentTimeMillis(),
    val chest: Double? = null,
    val waist: Double? = null,
    val hips: Double? = null,
    val biceps: Double? = null,
    val thighs: Double? = null
)

