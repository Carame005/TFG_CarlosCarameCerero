package com.example.tfg_carloscaramecerero.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.tfg_carloscaramecerero.data.local.entity.BodyWeightEntity
import com.example.tfg_carloscaramecerero.data.local.entity.FoodEntryEntity
import com.example.tfg_carloscaramecerero.data.local.entity.TrainingSessionEntity
import com.example.tfg_carloscaramecerero.data.local.entity.UserProfileEntity
import com.example.tfg_carloscaramecerero.data.local.relation.RoutineWithExercises
import com.example.tfg_carloscaramecerero.domain.repository.BodyRepository
import com.example.tfg_carloscaramecerero.domain.repository.NutritionRepository
import com.example.tfg_carloscaramecerero.domain.repository.RoutineRepository
import com.example.tfg_carloscaramecerero.domain.repository.TrainingRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.stateIn
import java.util.Calendar
import javax.inject.Inject

@HiltViewModel
class DashboardViewModel @Inject constructor(
    private val trainingRepository: TrainingRepository,
    private val bodyRepository: BodyRepository,
    private val nutritionRepository: NutritionRepository,
    private val routineRepository: RoutineRepository
) : ViewModel() {

    val latestWeight: StateFlow<BodyWeightEntity?> =
        bodyRepository.getLatestWeight()
            .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), null)

    val userProfile: StateFlow<UserProfileEntity?> =
        bodyRepository.getUserProfile()
            .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), null)

    val allSessions: StateFlow<List<TrainingSessionEntity>> =
        trainingRepository.getAllSessions()
            .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val todayFoodEntries: StateFlow<List<FoodEntryEntity>> =
        nutritionRepository.getEntriesByDayOfWeek(todayDayOfWeek())
            .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val routines: StateFlow<List<RoutineWithExercises>> =
        routineRepository.getAllRoutinesWithExercises()
            .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    private fun todayDayOfWeek(): Int {
        val cal = Calendar.getInstance()
        // Calendar: Sunday=1, Monday=2... → convertir a 1=Lunes..7=Domingo
        return when (cal.get(Calendar.DAY_OF_WEEK)) {
            Calendar.MONDAY -> 1
            Calendar.TUESDAY -> 2
            Calendar.WEDNESDAY -> 3
            Calendar.THURSDAY -> 4
            Calendar.FRIDAY -> 5
            Calendar.SATURDAY -> 6
            Calendar.SUNDAY -> 7
            else -> 1
        }
    }
}

