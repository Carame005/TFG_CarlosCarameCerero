package com.example.tfg_carloscaramecerero.di

import android.content.Context
import androidx.room.Room
import com.example.tfg_carloscaramecerero.data.local.AppDatabase
import com.example.tfg_carloscaramecerero.data.local.AppDatabaseMigrations
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
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object DatabaseModule {

    @Provides
    @Singleton
    fun provideDatabase(@ApplicationContext context: Context): AppDatabase {
        return Room.databaseBuilder(
            context,
            AppDatabase::class.java,
            "fitness_database"
        )
            .addMigrations(*AppDatabaseMigrations.ALL)
            .build()
    }

    @Provides
    fun provideExerciseDao(database: AppDatabase): ExerciseDao = database.exerciseDao()

    @Provides
    fun provideRoutineDao(database: AppDatabase): RoutineDao = database.routineDao()

    @Provides
    fun provideTrainingSessionDao(database: AppDatabase): TrainingSessionDao =
        database.trainingSessionDao()

    @Provides
    fun provideTrainingSetDao(database: AppDatabase): TrainingSetDao = database.trainingSetDao()

    @Provides
    fun provideBodyWeightDao(database: AppDatabase): BodyWeightDao = database.bodyWeightDao()

    @Provides
    fun provideBodyMeasurementDao(database: AppDatabase): BodyMeasurementDao =
        database.bodyMeasurementDao()

    @Provides
    fun provideFoodEntryDao(database: AppDatabase): FoodEntryDao = database.foodEntryDao()

    @Provides
    fun provideNutritionalGoalDao(database: AppDatabase): NutritionalGoalDao =
        database.nutritionalGoalDao()

    @Provides
    fun provideRecommendationDao(database: AppDatabase): RecommendationDao =
        database.recommendationDao()

    @Provides
    fun provideUserProfileDao(database: AppDatabase): UserProfileDao =
        database.userProfileDao()

    @Provides
    fun provideHealthDocumentDao(database: AppDatabase): HealthDocumentDao =
        database.healthDocumentDao()

    @Provides
    fun provideChatDao(database: AppDatabase): ChatDao =
        database.chatDao()
}

