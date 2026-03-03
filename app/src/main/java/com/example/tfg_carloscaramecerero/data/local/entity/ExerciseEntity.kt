package com.example.tfg_carloscaramecerero.data.local.entity

import androidx.room.Entity
import androidx.room.PrimaryKey

enum class ExerciseType {
    STRENGTH,
    CARDIO
}

@Entity(tableName = "exercises")
data class ExerciseEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val name: String,
    val description: String = "",
    val muscleGroup: String,
    val exerciseType: String = ExerciseType.STRENGTH.name,
    val imageUrl: String? = null
) {
    val isCardio: Boolean get() = exerciseType == ExerciseType.CARDIO.name
}

