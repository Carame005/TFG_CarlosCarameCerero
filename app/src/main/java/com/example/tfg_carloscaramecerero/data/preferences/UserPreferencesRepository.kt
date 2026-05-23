package com.example.tfg_carloscaramecerero.data.preferences

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.preferencesDataStore
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import javax.inject.Inject
import javax.inject.Singleton

private val Context.dataStore: DataStore<Preferences> by preferencesDataStore(name = "user_preferences")

@Singleton
class UserPreferencesRepository @Inject constructor(
    @ApplicationContext private val context: Context
) {
    private val DARK_MODE_KEY = booleanPreferencesKey("dark_mode")
    private val NOTIFICATIONS_ENABLED_KEY = booleanPreferencesKey("notifications_enabled")
    private val AI_CREATE_ROUTINES_KEY = booleanPreferencesKey("ai_create_routines")
    private val AI_CREATE_EXERCISES_KEY = booleanPreferencesKey("ai_create_exercises")
    private val AI_CREATE_FOOD_SCHEDULE_KEY = booleanPreferencesKey("ai_create_food_schedule")
    private val BIOMETRIC_LOCK_KEY = booleanPreferencesKey("biometric_lock")
    private val TERMS_ACCEPTED_KEY = booleanPreferencesKey("terms_accepted")

    val isDarkMode: Flow<Boolean?> = context.dataStore.data.map { prefs ->
        prefs[DARK_MODE_KEY] // null = seguir sistema
    }

    val notificationsEnabled: Flow<Boolean> = context.dataStore.data.map { prefs ->
        prefs[NOTIFICATIONS_ENABLED_KEY] ?: false
    }

    val aiCanCreateRoutines: Flow<Boolean> = context.dataStore.data.map { prefs ->
        prefs[AI_CREATE_ROUTINES_KEY] ?: false
    }

    val aiCanCreateExercises: Flow<Boolean> = context.dataStore.data.map { prefs ->
        prefs[AI_CREATE_EXERCISES_KEY] ?: false
    }

    val aiCanCreateFoodSchedule: Flow<Boolean> = context.dataStore.data.map { prefs ->
        prefs[AI_CREATE_FOOD_SCHEDULE_KEY] ?: false
    }

    /** true = bloqueo biométrico activo, false = sin bloqueo (valor por defecto) */
    val biometricLock: Flow<Boolean> = context.dataStore.data.map { prefs ->
        prefs[BIOMETRIC_LOCK_KEY] ?: false
    }

    /** true = el usuario ya aceptó los términos y condiciones */
    val termsAccepted: Flow<Boolean> = context.dataStore.data.map { prefs ->
        prefs[TERMS_ACCEPTED_KEY] ?: false
    }

    suspend fun setDarkMode(enabled: Boolean?) {
        context.dataStore.edit { prefs ->
            if (enabled == null) prefs.remove(DARK_MODE_KEY)
            else prefs[DARK_MODE_KEY] = enabled
        }
    }

    suspend fun setNotificationsEnabled(enabled: Boolean) {
        context.dataStore.edit { prefs ->
            prefs[NOTIFICATIONS_ENABLED_KEY] = enabled
        }
    }

    suspend fun setAiCanCreateRoutines(enabled: Boolean) {
        context.dataStore.edit { prefs -> prefs[AI_CREATE_ROUTINES_KEY] = enabled }
    }

    suspend fun setAiCanCreateExercises(enabled: Boolean) {
        context.dataStore.edit { prefs -> prefs[AI_CREATE_EXERCISES_KEY] = enabled }
    }

    suspend fun setAiCanCreateFoodSchedule(enabled: Boolean) {
        context.dataStore.edit { prefs -> prefs[AI_CREATE_FOOD_SCHEDULE_KEY] = enabled }
    }

    suspend fun setBiometricLock(enabled: Boolean) {
        context.dataStore.edit { prefs -> prefs[BIOMETRIC_LOCK_KEY] = enabled }
    }

    suspend fun setTermsAccepted(accepted: Boolean) {
        context.dataStore.edit { prefs -> prefs[TERMS_ACCEPTED_KEY] = accepted }
    }
}

