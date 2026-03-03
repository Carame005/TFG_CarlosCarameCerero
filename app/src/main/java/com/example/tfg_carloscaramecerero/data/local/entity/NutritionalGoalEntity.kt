package com.example.tfg_carloscaramecerero.data.local.entity

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "nutritional_goals")
data class NutritionalGoalEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val calories: Double,
    val protein: Double,
    val carbs: Double,
    val fat: Double,
    val createdAt: Long = System.currentTimeMillis()
)

