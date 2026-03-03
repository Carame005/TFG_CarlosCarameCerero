package com.example.tfg_carloscaramecerero.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.tfg_carloscaramecerero.data.local.entity.FoodEntryEntity
import com.example.tfg_carloscaramecerero.domain.repository.NutritionRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import java.util.Calendar
import javax.inject.Inject

data class MealItemInput(
    val description: String = "",
    val foodType: String = "comida",  // "comida" o "bebida"
    val grams: Int? = null
)

@HiltViewModel
class NutritionViewModel @Inject constructor(
    private val nutritionRepository: NutritionRepository
) : ViewModel() {

    companion object {
        val DAY_NAMES = listOf("Lunes", "Martes", "Miércoles", "Jueves", "Viernes", "Sábado", "Domingo")
        val MEAL_TYPES = listOf("desayuno", "almuerzo", "cena", "snack")
        val MEAL_LABELS = mapOf(
            "desayuno" to "Desayuno",
            "almuerzo" to "Almuerzo",
            "cena" to "Cena",
            "snack" to "Snack"
        )
    }

    /** Día seleccionado actualmente (1=Lunes ... 7=Domingo) */
    private val _selectedDay = MutableStateFlow(currentDayOfWeek())
    val selectedDay: StateFlow<Int> = _selectedDay.asStateFlow()

    /** Todas las entradas del horario */
    val allEntries: StateFlow<List<FoodEntryEntity>> =
        nutritionRepository.getAllEntries()
            .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    /** Días que ya tienen registros */
    val daysWithEntries: StateFlow<List<Int>> =
        nutritionRepository.getDaysWithEntries()
            .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    fun selectDay(day: Int) {
        _selectedDay.value = day
    }

    fun addMealEntry(
        description: String,
        mealType: String,
        dayOfWeek: Int,
        time: String = "",
        foodType: String = "comida",
        grams: Int? = null
    ) {
        viewModelScope.launch {
            nutritionRepository.insertEntry(
                FoodEntryEntity(
                    description = description,
                    mealType = mealType,
                    dayOfWeek = dayOfWeek,
                    time = time,
                    foodType = foodType,
                    grams = grams
                )
            )
        }
    }

    fun addMultipleMealEntries(
        entries: List<MealItemInput>,
        mealType: String,
        dayOfWeek: Int,
        time: String = ""
    ) {
        viewModelScope.launch {
            entries.filter { it.description.isNotBlank() }.forEach { item ->
                nutritionRepository.insertEntry(
                    FoodEntryEntity(
                        description = item.description.trim(),
                        mealType = mealType,
                        dayOfWeek = dayOfWeek,
                        time = time,
                        foodType = item.foodType,
                        grams = item.grams
                    )
                )
            }
        }
    }

    fun deleteEntry(entry: FoodEntryEntity) {
        viewModelScope.launch { nutritionRepository.deleteEntry(entry) }
    }

    fun updateEntry(entry: FoodEntryEntity) {
        viewModelScope.launch { nutritionRepository.updateEntry(entry) }
    }

    /** Devuelve el día de la semana actual (1=Lunes ... 7=Domingo) */
    private fun currentDayOfWeek(): Int {
        val calendarDay = Calendar.getInstance().get(Calendar.DAY_OF_WEEK)
        // Calendar: 1=Domingo, 2=Lunes, ..., 7=Sábado → convertir a 1=Lunes, ..., 7=Domingo
        return if (calendarDay == Calendar.SUNDAY) 7 else calendarDay - 1
    }
}

