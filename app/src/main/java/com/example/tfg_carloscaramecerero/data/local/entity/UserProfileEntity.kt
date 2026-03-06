package com.example.tfg_carloscaramecerero.data.local.entity

import androidx.room.Entity
import androidx.room.PrimaryKey

/**
 * Perfil del usuario.
 * Se almacena un único registro (id = 1) que se actualiza cuando el usuario modifica sus datos.
 * - height: altura en cm
 * - healthConditions: texto libre con enfermedades crónicas, trastornos, alergias, etc.
 * - fitnessGoal: objetivo fitness del usuario (musculación, perder peso, etc.)
 */
@Entity(tableName = "user_profile")
data class UserProfileEntity(
    @PrimaryKey
    val id: Long = 1,
    val height: Double? = null,           // Altura en cm
    val healthConditions: String = "",    // Enfermedades, alergias, trastornos, etc.
    val fitnessGoal: String = ""          // Objetivo: musculación, perder peso, etc.
)

