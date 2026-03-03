package com.example.tfg_carloscaramecerero.data.local.entity

import androidx.room.Entity
import androidx.room.PrimaryKey

/**
 * Registro de comida del usuario.
 * El usuario solo describe lo que come (description) y selecciona el tipo de comida y día.
 * Los campos de macros (calories, protein, carbs, fat) son opcionales y serán
 * rellenados por el asistente de IA cuando se implemente.
 */
@Entity(tableName = "food_entries")
data class FoodEntryEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val description: String,            // "Tostada con aguacate y huevo revuelto"
    val mealType: String,               // "desayuno", "almuerzo", "cena", "snack"
    val dayOfWeek: Int,                 // 1=Lunes, 2=Martes, ..., 7=Domingo
    val time: String = "",              // Hora aproximada: "08:30", "14:00"
    val foodType: String = "comida",    // "comida" o "bebida"
    val grams: Int? = null,             // Gramos (opcional)
    val date: Long = System.currentTimeMillis(),

    // ── Campos que rellenará la IA ──
    val calories: Double? = null,
    val protein: Double? = null,
    val carbs: Double? = null,
    val fat: Double? = null,
    val aiAnalyzed: Boolean = false     // true cuando la IA haya procesado esta entrada
)

