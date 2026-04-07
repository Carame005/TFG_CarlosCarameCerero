package com.example.tfg_carloscaramecerero.viewmodel

import android.content.Context
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.tfg_carloscaramecerero.data.preferences.UserPreferencesRepository
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
    @ApplicationContext private val context: Context
) : ViewModel() {

    /** null = seguir sistema, true = oscuro, false = claro */
    val darkMode: StateFlow<Boolean?> = prefsRepository.isDarkMode
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), null)

    val notificationsEnabled: StateFlow<Boolean> = prefsRepository.notificationsEnabled
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), false)

    fun setDarkMode(enabled: Boolean?) {
        viewModelScope.launch {
            prefsRepository.setDarkMode(enabled)
        }
    }

    fun setNotificationsEnabled(enabled: Boolean) {
        viewModelScope.launch {
            prefsRepository.setNotificationsEnabled(enabled)
            if (enabled) {
                TrainingReminderWorker.scheduleDaily(context)
            } else {
                TrainingReminderWorker.cancel(context)
            }
        }
    }
}

