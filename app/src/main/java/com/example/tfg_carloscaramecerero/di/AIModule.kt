package com.example.tfg_carloscaramecerero.di

import com.example.tfg_carloscaramecerero.BuildConfig
import com.example.tfg_carloscaramecerero.data.remote.GeminiService
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object AIModule {

    @Provides
    @Singleton
    fun provideGeminiService(): GeminiService {
        return GeminiService(apiKey = BuildConfig.GEMINI_API_KEY)
    }
}

