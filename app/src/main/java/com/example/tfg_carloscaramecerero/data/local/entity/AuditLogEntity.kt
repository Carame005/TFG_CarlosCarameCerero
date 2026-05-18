package com.example.tfg_carloscaramecerero.data.local.entity

import androidx.room.Entity
import androidx.room.PrimaryKey

/**
 * Registro de auditoría de acciones del usuario en la aplicación.
 * Permite verificar la autoría de operaciones y documentar incidencias.
 */
@Entity(tableName = "audit_log")
data class AuditLogEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    /** Módulo de la aplicación: "Entrenamiento", "Nutrición", "Cuerpo", "Sistema" */
    val category: String,
    /** Descripción breve de la acción realizada */
    val action: String,
    /** Detalle adicional (nombre del elemento, valor, etc.) */
    val detail: String = "",
    /** Epoch millis del momento en que se realizó la acción */
    val timestamp: Long = System.currentTimeMillis()
)

