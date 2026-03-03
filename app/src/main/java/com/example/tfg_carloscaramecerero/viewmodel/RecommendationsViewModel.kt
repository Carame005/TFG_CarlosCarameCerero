package com.example.tfg_carloscaramecerero.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.tfg_carloscaramecerero.data.local.entity.RecommendationEntity
import com.example.tfg_carloscaramecerero.data.remote.GeminiContent
import com.example.tfg_carloscaramecerero.data.remote.GeminiPart
import com.example.tfg_carloscaramecerero.data.remote.GeminiService
import com.example.tfg_carloscaramecerero.domain.repository.BodyRepository
import com.example.tfg_carloscaramecerero.domain.repository.ExerciseRepository
import com.example.tfg_carloscaramecerero.domain.repository.NutritionRepository
import com.example.tfg_carloscaramecerero.domain.repository.RecommendationRepository
import com.example.tfg_carloscaramecerero.domain.repository.RoutineRepository
import com.example.tfg_carloscaramecerero.domain.repository.TrainingRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.firstOrNull
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import javax.inject.Inject

@HiltViewModel
class RecommendationsViewModel @Inject constructor(
    private val recommendationRepository: RecommendationRepository,
    private val geminiService: GeminiService,
    private val bodyRepository: BodyRepository,
    private val exerciseRepository: ExerciseRepository,
    private val routineRepository: RoutineRepository,
    private val trainingRepository: TrainingRepository,
    private val nutritionRepository: NutritionRepository
) : ViewModel() {

    private val _selectedFilter = MutableStateFlow("all")
    val selectedFilter: StateFlow<String> = _selectedFilter.asStateFlow()

    private val _isGenerating = MutableStateFlow(false)
    val isGenerating: StateFlow<Boolean> = _isGenerating.asStateFlow()

    private val _generationError = MutableStateFlow<String?>(null)
    val generationError: StateFlow<String?> = _generationError.asStateFlow()

    val allRecommendations: StateFlow<List<RecommendationEntity>> =
        recommendationRepository.getAll()
            .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val unreadCount: StateFlow<Int> =
        recommendationRepository.getUnread()
            .map { it.size }
            .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), 0)

    fun setFilter(type: String) {
        _selectedFilter.value = type
    }

    fun markAsRead(id: Long) {
        viewModelScope.launch { recommendationRepository.markAsRead(id) }
    }

    fun deleteRecommendation(id: Long) {
        viewModelScope.launch { recommendationRepository.deleteById(id) }
    }

    fun deleteAll() {
        viewModelScope.launch { recommendationRepository.deleteAll() }
    }

    fun clearError() {
        _generationError.value = null
    }

    /**
     * Genera consejos personalizados usando Gemini AI analizando los datos del usuario.
     */
    fun generateRecommendations() {
        if (_isGenerating.value) return
        _isGenerating.value = true
        _generationError.value = null

        viewModelScope.launch {
            try {
                val userData = buildUserDataSummary()

                val prompt = """
Analiza los siguientes datos de un usuario de una app de fitness y nutrición.
Genera exactamente entre 3 y 5 consejos personalizados basados en sus datos reales.

Para cada consejo, usa EXACTAMENTE este formato (una línea por consejo):
[TIPO]|[MENSAJE]

Donde TIPO es uno de: training, nutrition, body
Y MENSAJE es el consejo (máximo 2 frases, directo y útil).

Reglas:
- Basa los consejos en los datos reales del usuario, no des consejos genéricos.
- Si no hay datos suficientes de una categoría, sugiere al usuario que registre más datos.
- Sé específico: menciona ejercicios, comidas, o medidas concretas del usuario.
- No uses ** ni formato markdown en los mensajes.
- Responde SOLO con las líneas de consejos, sin texto adicional.

DATOS DEL USUARIO:
$userData
""".trimIndent()

                val response = geminiService.sendMessage(
                    userMessage = prompt,
                    conversationHistory = emptyList(),
                    systemPrompt = "Eres un asistente fitness experto. Genera consejos personalizados en español. Responde SOLO en el formato solicitado."
                )

                val recommendations = parseRecommendations(response)

                if (recommendations.isNotEmpty()) {
                    recommendationRepository.insertAll(recommendations)
                } else {
                    _generationError.value = "No se pudieron generar consejos. Inténtalo de nuevo."
                }

            } catch (e: Exception) {
                _generationError.value = e.message ?: "Error al generar consejos."
            } finally {
                _isGenerating.value = false
            }
        }
    }

    private fun parseRecommendations(response: String): List<RecommendationEntity> {
        val validTypes = setOf("training", "nutrition", "body")
        return response.lines()
            .map { it.trim() }
            .filter { it.contains("|") }
            .mapNotNull { line ->
                val parts = line.split("|", limit = 2)
                if (parts.size == 2) {
                    val type = parts[0].trim().lowercase()
                        .removePrefix("[").removeSuffix("]")
                        .trim()
                    val message = parts[1].trim()
                    if (type in validTypes && message.isNotBlank()) {
                        RecommendationEntity(type = type, message = message)
                    } else null
                } else null
            }
    }

    private suspend fun buildUserDataSummary(): String {
        val dateFormat = SimpleDateFormat("dd/MM/yyyy", Locale("es", "ES"))

        val profile = bodyRepository.getUserProfile().firstOrNull()
        val latestWeight = bodyRepository.getLatestWeight().firstOrNull()
        val latestMeasurement = bodyRepository.getLatestMeasurement().firstOrNull()
        val routines = routineRepository.getAllRoutinesWithExercises().firstOrNull() ?: emptyList()
        val recentSessions = trainingRepository.getAllSessionsWithSets().firstOrNull() ?: emptyList()
        val foodEntries = nutritionRepository.getAllEntries().firstOrNull() ?: emptyList()
        val exercises = exerciseRepository.getAll().firstOrNull() ?: emptyList()

        return buildString {
            appendLine("=== PERFIL ===")
            if (profile != null) {
                profile.height?.let { appendLine("Altura: $it cm") }
                if (profile.healthConditions.isNotBlank()) {
                    appendLine("Condiciones de salud: ${profile.healthConditions}")
                }
            } else {
                appendLine("Sin perfil registrado")
            }
            latestWeight?.let {
                appendLine("Peso: ${it.weight} kg (${dateFormat.format(Date(it.date))})")
            } ?: appendLine("Sin peso registrado")

            latestMeasurement?.let { m ->
                appendLine("\n=== MEDIDAS ===")
                m.chest?.let { appendLine("Pecho: $it cm") }
                m.waist?.let { appendLine("Cintura: $it cm") }
                m.hips?.let { appendLine("Cadera: $it cm") }
                m.biceps?.let { appendLine("Bíceps: $it cm") }
                m.thighs?.let { appendLine("Muslos: $it cm") }
            }

            if (routines.isNotEmpty()) {
                appendLine("\n=== RUTINAS ===")
                routines.take(10).forEach { r ->
                    appendLine("- ${r.routine.name}: ${r.exercises.joinToString(", ") { it.name }}")
                }
            } else {
                appendLine("\n=== RUTINAS ===\nSin rutinas creadas")
            }

            if (recentSessions.isNotEmpty()) {
                appendLine("\n=== ÚLTIMAS SESIONES ===")
                recentSessions.take(5).forEach { session ->
                    val routineName = routines.find { it.routine.id == session.session.routineId }?.routine?.name ?: "Sin rutina"
                    appendLine("- ${dateFormat.format(Date(session.session.date))}: $routineName (${session.session.durationMinutes} min)")
                    session.sets.take(8).forEach { set ->
                        val exName = exercises.find { it.id == set.exerciseId }?.name ?: "Ejercicio"
                        if (set.isCardio) {
                            appendLine("  · $exName: ${set.durationSeconds}s, ${set.distanceKm}km")
                        } else {
                            appendLine("  · $exName: ${set.reps}reps x ${set.weight}kg")
                        }
                    }
                }
            } else {
                appendLine("\n=== ÚLTIMAS SESIONES ===\nSin sesiones registradas")
            }

            if (foodEntries.isNotEmpty()) {
                appendLine("\n=== NUTRICIÓN ===")
                val dayNames = listOf("", "Lunes", "Martes", "Miércoles", "Jueves", "Viernes", "Sábado", "Domingo")
                foodEntries.groupBy { it.dayOfWeek }.forEach { (day, entries) ->
                    val dayName = dayNames.getOrElse(day) { "Día $day" }
                    appendLine("$dayName:")
                    entries.forEach { entry ->
                        val typeStr = if (entry.foodType == "bebida") " (bebida)" else ""
                        val gramsStr = entry.grams?.let { " - ${it}g" } ?: ""
                        appendLine("  · ${entry.mealType}: ${entry.description}$typeStr$gramsStr")
                    }
                }
            } else {
                appendLine("\n=== NUTRICIÓN ===\nSin comidas registradas")
            }
        }
    }
}

