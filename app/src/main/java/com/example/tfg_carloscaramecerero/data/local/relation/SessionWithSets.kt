package com.example.tfg_carloscaramecerero.data.local.relation

import androidx.room.Embedded
import androidx.room.Relation
import com.example.tfg_carloscaramecerero.data.local.entity.TrainingSessionEntity
import com.example.tfg_carloscaramecerero.data.local.entity.TrainingSetEntity

data class SessionWithSets(
    @Embedded val session: TrainingSessionEntity,
    @Relation(
        parentColumn = "id",
        entityColumn = "sessionId"
    )
    val sets: List<TrainingSetEntity>
)

