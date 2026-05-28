package com.example.tfg_carloscaramecerero.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.tfg_carloscaramecerero.data.local.entity.FoodCatalogEntity
import com.example.tfg_carloscaramecerero.data.local.entity.FoodEntryEntity
import com.example.tfg_carloscaramecerero.data.local.entity.MealScheduleEntity
import com.example.tfg_carloscaramecerero.domain.repository.NutritionRepository
import com.example.tfg_carloscaramecerero.domain.repository.AuditLogRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.flowOf
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
    private val nutritionRepository: NutritionRepository,
    private val auditLogRepository: AuditLogRepository
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

    /** Lista de horarios disponibles */
    val schedules: StateFlow<List<MealScheduleEntity>> =
        nutritionRepository.getAllSchedules()
            .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    /** ID del horario activo */
    private val _currentScheduleId = MutableStateFlow<Long?>(null)
    val currentScheduleId: StateFlow<Long?> = _currentScheduleId.asStateFlow()

    /** Día seleccionado actualmente (1=Lunes ... 7=Domingo) */
    private val _selectedDay = MutableStateFlow(currentDayOfWeek())
    val selectedDay: StateFlow<Int> = _selectedDay.asStateFlow()

    /** Entradas del horario activo */
    val allEntries: StateFlow<List<FoodEntryEntity>> =
        _currentScheduleId.flatMapLatest { scheduleId ->
            if (scheduleId != null) nutritionRepository.getEntriesBySchedule(scheduleId)
            else flowOf(emptyList())
        }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    /** Días con registros en el horario activo */
    val daysWithEntries: StateFlow<List<Int>> =
        _currentScheduleId.flatMapLatest { scheduleId ->
            if (scheduleId != null) nutritionRepository.getDaysWithEntriesBySchedule(scheduleId)
            else flowOf(emptyList())
        }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    init {
        // Asegura que siempre haya al menos un horario y que currentScheduleId sea válido
        viewModelScope.launch {
            nutritionRepository.getAllSchedules().collect { list ->
                val currentId = _currentScheduleId.value
                when {
                    list.isEmpty() -> {
                        // Primer arranque: crear horario por defecto
                        val id = nutritionRepository.insertSchedule(
                            MealScheduleEntity(name = "Mi dieta")
                        )
                        _currentScheduleId.value = id
                    }
                    currentId == null -> _currentScheduleId.value = list.first().id
                    list.none { it.id == currentId } -> _currentScheduleId.value = list.first().id
                }
            }
        }
    }

    // ── Gestión de horarios ───────────────────────────────────────────────────

    fun selectSchedule(id: Long) {
        _currentScheduleId.value = id
    }

    fun createSchedule(name: String) {
        viewModelScope.launch {
            val id = nutritionRepository.insertSchedule(
                MealScheduleEntity(name = name.trim())
            )
            _currentScheduleId.value = id   // navega al nuevo horario automáticamente
            auditLogRepository.logAction("Nutrición", "Horario creado", name.trim())
        }
    }

    fun deleteSchedule(schedule: MealScheduleEntity) {
        viewModelScope.launch {
            // Eliminar entradas del horario y luego el horario (el init redirige a otro)
            nutritionRepository.deleteEntriesBySchedule(schedule.id)
            nutritionRepository.deleteSchedule(schedule)
            auditLogRepository.logAction("Nutrición", "Horario eliminado", schedule.name)
        }
    }

    // ── Día ──────────────────────────────────────────────────────────────────

    fun selectDay(day: Int) {
        _selectedDay.value = day
    }

    // ── Entradas ─────────────────────────────────────────────────────────────

    fun addMealEntry(
        description: String,
        mealType: String,
        dayOfWeek: Int,
        time: String = "",
        foodType: String = "comida",
        grams: Int? = null
    ) {
        val scheduleId = _currentScheduleId.value ?: return
        viewModelScope.launch {
            nutritionRepository.insertEntry(
                FoodEntryEntity(
                    scheduleId = scheduleId,
                    description = description,
                    mealType = mealType,
                    dayOfWeek = dayOfWeek,
                    time = time,
                    foodType = foodType,
                    grams = grams
                )
            )
            val dayName = DAY_NAMES.getOrElse(dayOfWeek - 1) { "Día $dayOfWeek" }
            auditLogRepository.logAction(
                "Nutrición", "Comida añadida",
                "${MEAL_LABELS[mealType] ?: mealType} ($dayName): $description"
            )
        }
    }

    fun addMultipleMealEntries(
        entries: List<MealItemInput>,
        mealType: String,
        dayOfWeek: Int,
        time: String = ""
    ) {
        val scheduleId = _currentScheduleId.value ?: return
        viewModelScope.launch {
            entries.filter { it.description.isNotBlank() }.forEach { item ->
                nutritionRepository.insertEntry(
                    FoodEntryEntity(
                        scheduleId = scheduleId,
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
        viewModelScope.launch {
            nutritionRepository.deleteEntry(entry)
            auditLogRepository.logAction("Nutrición", "Comida eliminada", entry.description)
        }
    }

    fun updateEntry(entry: FoodEntryEntity) {
        viewModelScope.launch { nutritionRepository.updateEntry(entry) }
    }

    // ── Catálogo de alimentos ─────────────────────────────────────────────────

    /** Todos los items del catálogo personal (ordenados por nombre). */
    val catalogItems: StateFlow<List<FoodCatalogEntity>> =
        nutritionRepository.getAllCatalogItems()
            .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    fun addToCatalog(
        name: String,
        foodType: String = "comida",
        defaultGrams: Int? = null
    ) {
        val trimmed = name.trim()
        if (trimmed.isBlank()) return
        viewModelScope.launch {
            nutritionRepository.insertCatalogItem(
                FoodCatalogEntity(name = trimmed, foodType = foodType, defaultGrams = defaultGrams)
            )
            auditLogRepository.logAction("Nutrición", "Alimento añadido al catálogo", trimmed)
        }
    }

    fun deleteCatalogItem(item: FoodCatalogEntity) {
        viewModelScope.launch {
            nutritionRepository.deleteCatalogItem(item)
            auditLogRepository.logAction("Nutrición", "Alimento eliminado del catálogo", item.name)
        }
    }

    fun updateCatalogItem(item: FoodCatalogEntity) {
        viewModelScope.launch { nutritionRepository.updateCatalogItem(item) }
    }

    /** Devuelve el día de la semana actual (1=Lunes ... 7=Domingo) */
    private fun currentDayOfWeek(): Int {
        val calendarDay = Calendar.getInstance().get(Calendar.DAY_OF_WEEK)
        return if (calendarDay == Calendar.SUNDAY) 7 else calendarDay - 1
    }
}

