package com.example.tfg_carloscaramecerero.data.local.entity

import androidx.room.Entity
import androidx.room.PrimaryKey

/**
 * Catálogo personal de alimentos y bebidas.
 * El usuario puede guardar aquí sus comidas/bebidas habituales para reutilizarlas
 * rápidamente al registrar entradas nutricionales, evitando repetir texto.
 */
@Entity(tableName = "food_catalog")
data class FoodCatalogEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val name: String,                       // "Tostada con aguacate"
    val foodType: String = "comida",        // "comida" o "bebida"
    val defaultGrams: Int? = null,          // Cantidad habitual (g o ml)
    // ── Macros opcionales (pueden rellenarse con la IA) ──
    val calories: Double? = null,
    val protein: Double? = null,
    val carbs: Double? = null,
    val fat: Double? = null,
    val createdAt: Long = System.currentTimeMillis()
)

