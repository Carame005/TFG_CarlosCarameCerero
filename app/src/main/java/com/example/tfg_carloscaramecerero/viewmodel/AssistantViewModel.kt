package com.example.tfg_carloscaramecerero.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.tfg_carloscaramecerero.data.remote.GeminiContent
import com.example.tfg_carloscaramecerero.data.remote.GeminiException
import com.example.tfg_carloscaramecerero.data.remote.GeminiPart
import com.example.tfg_carloscaramecerero.data.remote.GeminiService
import com.example.tfg_carloscaramecerero.domain.repository.BodyRepository
import com.example.tfg_carloscaramecerero.domain.repository.ExerciseRepository
import com.example.tfg_carloscaramecerero.domain.repository.NutritionRepository
import com.example.tfg_carloscaramecerero.domain.repository.RoutineRepository
import com.example.tfg_carloscaramecerero.domain.repository.TrainingRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.firstOrNull
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import java.util.concurrent.atomic.AtomicLong
import javax.inject.Inject

private val messageIdCounter = AtomicLong(0)

data class ChatMessage(
    val id: Long = messageIdCounter.incrementAndGet(),
    val content: String,
    val isUser: Boolean,
    val timestamp: Long = System.currentTimeMillis()
)

@HiltViewModel
class AssistantViewModel @Inject constructor(
    private val geminiService: GeminiService,
    private val bodyRepository: BodyRepository,
    private val exerciseRepository: ExerciseRepository,
    private val routineRepository: RoutineRepository,
    private val trainingRepository: TrainingRepository,
    private val nutritionRepository: NutritionRepository
) : ViewModel() {

    private val _messages = MutableStateFlow<List<ChatMessage>>(
        listOf(
            ChatMessage(
                content = "¡Hola! Soy tu asistente fitness con IA. Puedo analizar tus rutinas, nutrición, medidas corporales y darte recomendaciones personalizadas. ¿En qué puedo ayudarte?",
                isUser = false
            )
        )
    )
    val messages: StateFlow<List<ChatMessage>> = _messages.asStateFlow()

    private val _isTyping = MutableStateFlow(false)
    val isTyping: StateFlow<Boolean> = _isTyping.asStateFlow()

    // Historial de la conversación para Gemini (multi-turno)
    private val conversationHistory = mutableListOf<GeminiContent>()

    // Job para poder cancelar respuestas en curso
    private var currentResponseJob: Job? = null

    fun sendMessage(text: String) {
        if (text.isBlank()) return

        val userMessage = ChatMessage(content = text.trim(), isUser = true)
        _messages.value = _messages.value + userMessage

        _isTyping.value = true

        currentResponseJob?.cancel()
        currentResponseJob = viewModelScope.launch {
            val botMessageId = messageIdCounter.incrementAndGet()
            try {
                // Construir system prompt con datos del usuario
                val systemPrompt = buildSystemPrompt()

                // Placeholder para la respuesta del bot (se irá rellenando con streaming)
                val botMessage = ChatMessage(
                    id = botMessageId,
                    content = "",
                    isUser = false
                )
                _messages.value = _messages.value + botMessage

                var accumulatedText = ""

                geminiService.sendMessageStream(
                    userMessage = text.trim(),
                    conversationHistory = conversationHistory.toList(),
                    systemPrompt = systemPrompt
                ).collect { token ->
                    accumulatedText += token
                    // Actualizar el mensaje del bot con el texto acumulado
                    _messages.value = _messages.value.map { msg ->
                        if (msg.id == botMessageId) msg.copy(content = accumulatedText)
                        else msg
                    }
                }

                if (accumulatedText.isBlank()) {
                    // Si no se recibió texto, reemplazar con mensaje informativo
                    _messages.value = _messages.value.map { msg ->
                        if (msg.id == botMessageId) msg.copy(content = "No se pudo generar una respuesta. Inténtalo de nuevo.")
                        else msg
                    }
                } else {
                    // Añadir al historial de conversación
                    conversationHistory.add(
                        GeminiContent(role = "user", parts = listOf(GeminiPart(text.trim())))
                    )
                    conversationHistory.add(
                        GeminiContent(role = "model", parts = listOf(GeminiPart(accumulatedText)))
                    )

                    // Limitar historial a los últimos 20 mensajes para no exceder tokens
                    if (conversationHistory.size > 20) {
                        val excess = conversationHistory.size - 20
                        repeat(excess) { conversationHistory.removeAt(0) }
                    }
                }

            } catch (e: GeminiException) {
                // Eliminar el mensaje vacío del bot si existe
                _messages.value = _messages.value.filter { it.id != botMessageId || it.content.isNotBlank() }
                val errorMessage = ChatMessage(
                    content = e.message ?: "Error al comunicarse con el asistente.",
                    isUser = false
                )
                _messages.value = _messages.value + errorMessage
            } catch (e: Exception) {
                // Eliminar el mensaje vacío del bot si existe
                _messages.value = _messages.value.filter { it.id != botMessageId || it.content.isNotBlank() }
                val errorMessage = ChatMessage(
                    content = "Error inesperado: ${e.localizedMessage ?: "Comprueba tu conexión a Internet."}",
                    isUser = false
                )
                _messages.value = _messages.value + errorMessage
            } finally {
                _isTyping.value = false
            }
        }
    }

    fun clearChat() {
        currentResponseJob?.cancel()
        conversationHistory.clear()
        _isTyping.value = false
        _messages.value = listOf(
            ChatMessage(
                content = "¡Hola! Soy tu asistente fitness con IA. Puedo analizar tus rutinas, nutrición, medidas corporales y darte recomendaciones personalizadas. ¿En qué puedo ayudarte?",
                isUser = false
            )
        )
    }

    /**
     * Construye un system prompt enriquecido con los datos del usuario
     * para que Gemini pueda dar recomendaciones personalizadas.
     */
    private suspend fun buildSystemPrompt(): String {
        val dateFormat = SimpleDateFormat("dd/MM/yyyy", Locale("es", "ES"))

        val profile = bodyRepository.getUserProfile().firstOrNull()
        val latestWeight = bodyRepository.getLatestWeight().firstOrNull()
        val latestMeasurement = bodyRepository.getLatestMeasurement().firstOrNull()
        val healthDocs = bodyRepository.getAllHealthDocuments().firstOrNull() ?: emptyList()
        val routines = routineRepository.getAllRoutinesWithExercises().firstOrNull() ?: emptyList()
        val recentSessions = trainingRepository.getAllSessionsWithSets().firstOrNull() ?: emptyList()
        val foodEntries = nutritionRepository.getAllEntries().firstOrNull() ?: emptyList()
        val exercises = exerciseRepository.getAll().firstOrNull() ?: emptyList()

        return buildString {
            appendLine("Eres un asistente fitness y de nutrición personalizado. Responde SIEMPRE en español.")
            appendLine("Tu rol es ayudar al usuario con sus entrenamientos, nutrición, salud y bienestar.")
            appendLine("Sé conciso pero útil. Usa datos del usuario cuando estén disponibles para personalizar tus respuestas.")
            appendLine("Si el usuario te pregunta sobre calorías o macros de sus comidas, haz estimaciones razonables basándote en las descripciones.")
            appendLine("No inventes datos médicos ni diagnósticos. Si el usuario tiene condiciones de salud, tenlas en cuenta en tus recomendaciones.")
            appendLine()

            // Perfil del usuario
            appendLine("=== DATOS DEL USUARIO ===")
            if (profile != null) {
                profile.height?.let { appendLine("Altura: ${it} cm") }
                if (profile.healthConditions.isNotBlank()) {
                    appendLine("Condiciones de salud: ${profile.healthConditions}")
                }
            }
            latestWeight?.let {
                appendLine("Último peso: ${it.weight} kg (${dateFormat.format(Date(it.date))})")
            }

            // Medidas corporales
            latestMeasurement?.let { m ->
                appendLine()
                appendLine("=== ÚLTIMAS MEDIDAS CORPORALES (${dateFormat.format(Date(m.date))}) ===")
                m.chest?.let { appendLine("Pecho: $it cm") }
                m.waist?.let { appendLine("Cintura: $it cm") }
                m.hips?.let { appendLine("Cadera: $it cm") }
                m.biceps?.let { appendLine("Bíceps: $it cm") }
                m.thighs?.let { appendLine("Muslos: $it cm") }
            }

            // Rutinas y ejercicios
            if (routines.isNotEmpty()) {
                appendLine()
                appendLine("=== RUTINAS DEL USUARIO ===")
                routines.take(10).forEach { routine ->
                    appendLine("- ${routine.routine.name}: ${routine.exercises.joinToString(", ") { it.name }}")
                }
            }

            // Sesiones de entrenamiento recientes (últimas 5)
            if (recentSessions.isNotEmpty()) {
                appendLine()
                appendLine("=== ÚLTIMAS SESIONES DE ENTRENAMIENTO ===")
                recentSessions.take(5).forEach { session ->
                    val routineName = routines.find { it.routine.id == session.session.routineId }?.routine?.name ?: "Sin rutina"
                    appendLine("- ${dateFormat.format(Date(session.session.date))}: $routineName (${session.session.durationMinutes} min)")
                    session.sets.take(10).forEach { set ->
                        val exerciseName = exercises.find { it.id == set.exerciseId }?.name ?: "Ejercicio #${set.exerciseId}"
                        if (set.isCardio) {
                            appendLine("  · $exerciseName: ${set.durationSeconds}s, ${set.distanceKm}km")
                        } else {
                            appendLine("  · $exerciseName: ${set.reps}reps x ${set.weight}kg")
                        }
                    }
                }
            }

            // Nutrición
            if (foodEntries.isNotEmpty()) {
                appendLine()
                appendLine("=== REGISTRO NUTRICIONAL ===")
                val dayNames = listOf("", "Lunes", "Martes", "Miércoles", "Jueves", "Viernes", "Sábado", "Domingo")
                foodEntries.groupBy { it.dayOfWeek }.forEach { (day, entries) ->
                    val dayName = dayNames.getOrElse(day) { "Día $day" }
                    appendLine("$dayName:")
                    entries.forEach { entry ->
                        val typeStr = if (entry.foodType == "bebida") "(bebida)" else ""
                        val gramsStr = entry.grams?.let { " - ${it}g" } ?: ""
                        appendLine("  · ${entry.mealType}: ${entry.description}$typeStr$gramsStr")
                    }
                }
            }

            // Documentos de salud
            if (healthDocs.isNotEmpty()) {
                appendLine()
                appendLine("=== DOCUMENTOS DE SALUD ===")
                appendLine("El usuario tiene ${healthDocs.size} documento(s) de salud subido(s):")
                healthDocs.forEach { doc ->
                    appendLine("- ${doc.fileName} (subido el ${dateFormat.format(Date(doc.uploadDate))})")
                }
            }
        }
    }
}

