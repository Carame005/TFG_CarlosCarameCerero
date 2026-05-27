package com.example.tfg_carloscaramecerero.data.local.entity

import androidx.room.Entity
import androidx.room.PrimaryKey

/**
 * Horario de comidas del usuario.
 * El usuario puede tener varios horarios (volumización, definición, IA-generado, etc.)
 * sin perder datos al crear uno nuevo.
 */
@Entity(tableName = "meal_schedules")
data class MealScheduleEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val name: String,
    val createdAt: Long = System.currentTimeMillis()
)

