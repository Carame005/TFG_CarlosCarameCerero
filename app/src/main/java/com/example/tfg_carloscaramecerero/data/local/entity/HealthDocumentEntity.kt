package com.example.tfg_carloscaramecerero.data.local.entity

import androidx.room.Entity
import androidx.room.PrimaryKey

/**
 * Documento de salud subido por el usuario (analíticas, informes médicos, etc.).
 * El archivo PDF se copia al almacenamiento interno y aquí se guarda la ruta.
 */
@Entity(tableName = "health_documents")
data class HealthDocumentEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val fileName: String,
    val filePath: String,
    val uploadDate: Long = System.currentTimeMillis()
)

