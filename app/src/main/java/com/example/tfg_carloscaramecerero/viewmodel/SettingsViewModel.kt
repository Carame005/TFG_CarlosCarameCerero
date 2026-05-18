package com.example.tfg_carloscaramecerero.viewmodel

import android.content.Context
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.tfg_carloscaramecerero.data.preferences.UserPreferencesRepository
import com.example.tfg_carloscaramecerero.domain.repository.AuditLogRepository
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
}

