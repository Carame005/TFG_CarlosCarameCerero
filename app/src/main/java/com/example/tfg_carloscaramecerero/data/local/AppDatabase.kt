package com.example.tfg_carloscaramecerero.data.local

import androidx.room.Database
import androidx.room.RoomDatabase
import com.example.tfg_carloscaramecerero.data.local.dao.BodyMeasurementDao
import com.example.tfg_carloscaramecerero.data.local.dao.BodyWeightDao
import com.example.tfg_carloscaramecerero.data.local.dao.ChatDao
import com.example.tfg_carloscaramecerero.data.local.dao.ExerciseDao
import com.example.tfg_carloscaramecerero.data.local.dao.FoodEntryDao
import com.example.tfg_carloscaramecerero.data.local.dao.HealthDocumentDao
import com.example.tfg_carloscaramecerero.data.local.dao.NutritionalGoalDao
import com.example.tfg_carloscaramecerero.data.local.dao.RecommendationDao
import com.example.tfg_carloscaramecerero.data.local.dao.RoutineDao
import com.example.tfg_carloscaramecerero.data.local.dao.TrainingSessionDao
import com.example.tfg_carloscaramecerero.data.local.dao.TrainingSetDao
import com.example.tfg_carloscaramecerero.data.local.dao.UserProfileDao
import com.example.tfg_carloscaramecerero.data.local.entity.BodyMeasurementEntity
import com.example.tfg_carloscaramecerero.data.local.entity.BodyWeightEntity
import com.example.tfg_carloscaramecerero.data.local.entity.ChatConversationEntity
import com.example.tfg_carloscaramecerero.data.local.entity.ChatMessageEntity
import com.example.tfg_carloscaramecerero.data.local.entity.ExerciseEntity
import com.example.tfg_carloscaramecerero.data.local.entity.FoodEntryEntity
import com.example.tfg_carloscaramecerero.data.local.entity.HealthDocumentEntity
import com.example.tfg_carloscaramecerero.data.local.entity.NutritionalGoalEntity
import com.example.tfg_carloscaramecerero.data.local.entity.RecommendationEntity
import com.example.tfg_carloscaramecerero.data.local.entity.RoutineEntity
import com.example.tfg_carloscaramecerero.data.local.entity.RoutineExerciseCrossRef
import com.example.tfg_carloscaramecerero.data.local.entity.TrainingSessionEntity
import com.example.tfg_carloscaramecerero.data.local.entity.TrainingSetEntity
import com.example.tfg_carloscaramecerero.data.local.entity.UserProfileEntity

@Database(
    entities = [
        ExerciseEntity::class,
        RoutineEntity::class,
        RoutineExerciseCrossRef::class,
        TrainingSessionEntity::class,
        TrainingSetEntity::class,
        BodyWeightEntity::class,
        BodyMeasurementEntity::class,
        FoodEntryEntity::class,
        NutritionalGoalEntity::class,
        RecommendationEntity::class,
        UserProfileEntity::class,
        HealthDocumentEntity::class,
        ChatConversationEntity::class,
        ChatMessageEntity::class
    ],
    version = 8,
    exportSchema = true
)
abstract class AppDatabase : RoomDatabase() {
    abstract fun exerciseDao(): ExerciseDao
    abstract fun routineDao(): RoutineDao
    abstract fun trainingSessionDao(): TrainingSessionDao
    abstract fun trainingSetDao(): TrainingSetDao
    abstract fun bodyWeightDao(): BodyWeightDao
    abstract fun bodyMeasurementDao(): BodyMeasurementDao
    abstract fun foodEntryDao(): FoodEntryDao
    abstract fun nutritionalGoalDao(): NutritionalGoalDao
    abstract fun recommendationDao(): RecommendationDao
    abstract fun userProfileDao(): UserProfileDao
    abstract fun healthDocumentDao(): HealthDocumentDao
    abstract fun chatDao(): ChatDao
}

