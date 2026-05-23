package com.example.tfg_carloscaramecerero.viewmodel

import android.content.Context
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.tfg_carloscaramecerero.data.local.entity.ExerciseEntity
import com.example.tfg_carloscaramecerero.data.local.entity.FoodEntryEntity
import com.example.tfg_carloscaramecerero.data.local.entity.RoutineEntity
import com.example.tfg_carloscaramecerero.data.local.entity.BodyWeightEntity
import com.example.tfg_carloscaramecerero.data.preferences.UserPreferencesRepository
import com.example.tfg_carloscaramecerero.domain.repository.AuditLogRepository
import com.example.tfg_carloscaramecerero.domain.repository.BodyRepository
import com.example.tfg_carloscaramecerero.domain.repository.ExerciseRepository
import com.example.tfg_carloscaramecerero.domain.repository.NutritionRepository
import com.example.tfg_carloscaramecerero.domain.repository.RoutineRepository
import com.example.tfg_carloscaramecerero.notifications.TrainingReminderWorker
import dagger.hilt.android.lifecycle.HiltViewModel
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class SettingsViewModel @Inject constructor(
    private val prefsRepository: UserPreferencesRepository,
    private val auditLogRepository: AuditLogRepository,
    private val bodyRepository: BodyRepository,
    private val nutritionRepository: NutritionRepository,
    private val routineRepository: RoutineRepository,
    private val exerciseRepository: ExerciseRepository,
    @ApplicationContext private val context: Context
) : ViewModel() {

    /** null = seguir sistema, true = oscuro, false = claro */
    val darkMode: StateFlow<Boolean?> = prefsRepository.isDarkMode
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), null)

    val notificationsEnabled: StateFlow<Boolean> = prefsRepository.notificationsEnabled
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), false)

    val aiCanCreateRoutines: StateFlow<Boolean> = prefsRepository.aiCanCreateRoutines
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), false)

    val aiCanCreateExercises: StateFlow<Boolean> = prefsRepository.aiCanCreateExercises
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), false)

    val aiCanCreateFoodSchedule: StateFlow<Boolean> = prefsRepository.aiCanCreateFoodSchedule
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), false)

    /** true = bloqueo biométrico activo, false = sin bloqueo (valor por defecto) */
    val biometricLock: StateFlow<Boolean> = prefsRepository.biometricLock
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), false)

    /** true = el usuario ya aceptó los T&C */
    val termsAccepted: StateFlow<Boolean> = prefsRepository.termsAccepted
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), false)

    fun setDarkMode(enabled: Boolean?) {
        viewModelScope.launch {
            prefsRepository.setDarkMode(enabled)
            val label = when (enabled) { true -> "Oscuro"; false -> "Claro"; else -> "Auto" }
            auditLogRepository.logAction("Sistema", "Tema cambiado", label)
        }
    }

    fun setNotificationsEnabled(enabled: Boolean) {
        viewModelScope.launch {
            prefsRepository.setNotificationsEnabled(enabled)
            if (enabled) TrainingReminderWorker.scheduleDaily(context)
            else TrainingReminderWorker.cancel(context)
            auditLogRepository.logAction("Sistema", "Notificaciones ${if (enabled) "activadas" else "desactivadas"}")
        }
    }

    fun setAiCanCreateRoutines(enabled: Boolean) {
        viewModelScope.launch {
            prefsRepository.setAiCanCreateRoutines(enabled)
            auditLogRepository.logAction("Sistema", "Permiso IA – rutinas ${if (enabled) "activado" else "desactivado"}")
        }
    }

    fun setAiCanCreateExercises(enabled: Boolean) {
        viewModelScope.launch {
            prefsRepository.setAiCanCreateExercises(enabled)
            auditLogRepository.logAction("Sistema", "Permiso IA – ejercicios ${if (enabled) "activado" else "desactivado"}")
        }
    }

    fun setAiCanCreateFoodSchedule(enabled: Boolean) {
        viewModelScope.launch {
            prefsRepository.setAiCanCreateFoodSchedule(enabled)
            auditLogRepository.logAction("Sistema", "Permiso IA – nutrición ${if (enabled) "activado" else "desactivado"}")
        }
    }

    fun logDataExport(type: String) {
        viewModelScope.launch {
            auditLogRepository.logAction("Sistema", "Datos exportados", type)
        }
    }

    // ─── Importación ──────────────────────────────────────────────────────────

    fun logDataImport(type: String) {
        viewModelScope.launch {
            auditLogRepository.logAction("Sistema", "Datos importados", type)
        }
    }

    fun importWeights(weights: List<BodyWeightEntity>) {
        viewModelScope.launch {
            weights.forEach { bodyRepository.insertWeight(it.copy(id = 0)) }
            auditLogRepository.logAction("Sistema", "Datos importados", "Historial de peso (${weights.size} registros)")
        }
    }

    fun importNutrition(entries: List<FoodEntryEntity>) {
        viewModelScope.launch {
            entries.forEach { nutritionRepository.insertEntry(it.copy(id = 0)) }
            auditLogRepository.logAction("Sistema", "Datos importados", "Registro nutricional (${entries.size} entradas)")
        }
    }

    fun importRoutines(routines: List<RoutineEntity>) {
        viewModelScope.launch {
            routines.forEach { routineRepository.insert(it.copy(id = 0)) }
            auditLogRepository.logAction("Sistema", "Datos importados", "Rutinas (${routines.size} rutinas)")
        }
    }

    fun importExercises(exercises: List<ExerciseEntity>) {
        viewModelScope.launch {
            exercises.forEach { exerciseRepository.insert(it.copy(id = 0)) }
            auditLogRepository.logAction("Sistema", "Datos importados", "Ejercicios (${exercises.size} ejercicios)")
        }
    }

    fun setBiometricLock(enabled: Boolean) {
        viewModelScope.launch {
            prefsRepository.setBiometricLock(enabled)
            auditLogRepository.logAction("Sistema", "Bloqueo biométrico ${if (enabled) "activado" else "desactivado"}")
        }
    }

    fun acceptTerms() {
        viewModelScope.launch {
            prefsRepository.setTermsAccepted(true)
            auditLogRepository.logAction("Sistema", "Términos y condiciones aceptados")
        }
    }
}

