package com.example.tfg_carloscaramecerero.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.tfg_carloscaramecerero.data.local.entity.ChatConversationEntity
import com.example.tfg_carloscaramecerero.data.local.entity.ChatMessageEntity
import com.example.tfg_carloscaramecerero.data.remote.GeminiContent
import com.example.tfg_carloscaramecerero.data.remote.GeminiException
import com.example.tfg_carloscaramecerero.data.remote.GeminiPart
import com.example.tfg_carloscaramecerero.data.remote.GeminiService
import com.example.tfg_carloscaramecerero.data.util.PdfTextExtractor
import com.example.tfg_carloscaramecerero.domain.repository.BodyRepository
import com.example.tfg_carloscaramecerero.domain.repository.ChatRepository
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
    private val nutritionRepository: NutritionRepository,
    private val chatRepository: ChatRepository
) : ViewModel() {

    companion object {
        private const val WELCOME_MESSAGE =
            "¡Hola! Soy tu asistente fitness con IA. Puedo analizar tus rutinas, nutrición, medidas corporales y darte recomendaciones personalizadas. ¿En qué puedo ayudarte?"
        private const val THIRTY_DAYS_MS = 30L * 24 * 60 * 60 * 1000
    }

    private val _messages = MutableStateFlow<List<ChatMessage>>(
        listOf(ChatMessage(content = WELCOME_MESSAGE, isUser = false))
    )
    val messages: StateFlow<List<ChatMessage>> = _messages.asStateFlow()

    private val _isTyping = MutableStateFlow(false)
    val isTyping: StateFlow<Boolean> = _isTyping.asStateFlow()

    // Conversación activa
    private val _currentConversationId = MutableStateFlow<Long?>(null)
    val currentConversationId: StateFlow<Long?> = _currentConversationId.asStateFlow()

    // Lista de conversaciones para el historial
    val conversations = chatRepository.getAllConversations()

    // Historial de la conversación para Gemini (multi-turno)
    private val conversationHistory = mutableListOf<GeminiContent>()

    // Job para poder cancelar respuestas en curso
    private var currentResponseJob: Job? = null

    init {
        // Limpiar conversaciones mayores a 30 días al iniciar
        viewModelScope.launch {
            chatRepository.deleteOldConversations(System.currentTimeMillis() - THIRTY_DAYS_MS)
        }
    }

    fun sendMessage(text: String) {
        if (text.isBlank()) return

        val userMessage = ChatMessage(content = text.trim(), isUser = true)
        _messages.value = _messages.value + userMessage

        _isTyping.value = true

        currentResponseJob?.cancel()
        currentResponseJob = viewModelScope.launch {
            val botMessageId = messageIdCounter.incrementAndGet()
            try {
                // Si no hay conversación activa, crear una nueva
                if (_currentConversationId.value == null) {
                    val title = text.trim().take(50)
                    val conversationId = chatRepository.insertConversation(
                        ChatConversationEntity(title = title)
                    )
                    _currentConversationId.value = conversationId
                }

                val convId = _currentConversationId.value!!

                // Guardar mensaje del usuario en Room
                chatRepository.insertMessage(
                    ChatMessageEntity(
                        conversationId = convId,
                        content = text.trim(),
                        isUser = true
                    )
                )

                // Construir system prompt con datos del usuario
                val systemPrompt = buildSystemPrompt()

                // Placeholder para la respuesta del bot
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
                    _messages.value = _messages.value.map { msg ->
                        if (msg.id == botMessageId) msg.copy(content = accumulatedText)
                        else msg
                    }
                }

                if (accumulatedText.isBlank()) {
                    _messages.value = _messages.value.map { msg ->
                        if (msg.id == botMessageId) msg.copy(content = "No se pudo generar una respuesta. Inténtalo de nuevo.")
                        else msg
                    }
                } else {
                    // Añadir al historial de conversación de Gemini
                    conversationHistory.add(
                        GeminiContent(role = "user", parts = listOf(GeminiPart(text.trim())))
                    )
                    conversationHistory.add(
                        GeminiContent(role = "model", parts = listOf(GeminiPart(accumulatedText)))
                    )

                    // Limitar historial a los últimos 20 mensajes
                    if (conversationHistory.size > 20) {
                        val excess = conversationHistory.size - 20
                        repeat(excess) { conversationHistory.removeAt(0) }
                    }

                    // Guardar respuesta del bot en Room
                    chatRepository.insertMessage(
                        ChatMessageEntity(
                            conversationId = convId,
                            content = accumulatedText,
                            isUser = false
                        )
                    )

                    // Actualizar timestamp de la conversación
                    chatRepository.getConversationById(convId)?.let { conv ->
                        chatRepository.updateConversation(
                            conv.copy(updatedAt = System.currentTimeMillis())
                        )
                    }
                }

            } catch (e: GeminiException) {
                _messages.value = _messages.value.filter { it.id != botMessageId || it.content.isNotBlank() }
                val errorMessage = ChatMessage(
                    content = e.message ?: "Error al comunicarse con el asistente.",
                    isUser = false
                )
                _messages.value = _messages.value + errorMessage
            } catch (e: Exception) {
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

    /**
     * Inicia un nuevo chat limpio.
     */
    fun newChat() {
        currentResponseJob?.cancel()
        conversationHistory.clear()
        _currentConversationId.value = null
        _isTyping.value = false
        _messages.value = listOf(
            ChatMessage(content = WELCOME_MESSAGE, isUser = false)
        )
    }

    /**
     * Carga una conversación existente desde Room.
     */
    fun loadConversation(conversationId: Long) {
        currentResponseJob?.cancel()
        conversationHistory.clear()
        _isTyping.value = false

        viewModelScope.launch {
            _currentConversationId.value = conversationId
            val savedMessages = chatRepository.getMessagesByConversationOnce(conversationId)

            if (savedMessages.isEmpty()) {
                _messages.value = listOf(
                    ChatMessage(content = WELCOME_MESSAGE, isUser = false)
                )
            } else {
                _messages.value = savedMessages.map { entity ->
                    ChatMessage(
                        content = entity.content,
                        isUser = entity.isUser,
                        timestamp = entity.timestamp
                    )
                }

                // Reconstruir historial de Gemini desde los mensajes guardados
                val recentMessages = savedMessages.takeLast(20)
                for (msg in recentMessages) {
                    conversationHistory.add(
                        GeminiContent(
                            role = if (msg.isUser) "user" else "model",
                            parts = listOf(GeminiPart(msg.content))
                        )
                    )
                }
            }
        }
    }

    /**
     * Elimina una conversación.
     */
    fun deleteConversation(conversationId: Long) {
        viewModelScope.launch {
            chatRepository.deleteConversation(conversationId)
            // Si es la conversación actual, iniciar nuevo chat
            if (_currentConversationId.value == conversationId) {
                newChat()
            }
        }
    }

    // Mantener compatibilidad con el botón de limpiar chat
    fun clearChat() = newChat()

    /**
     * Construye un system prompt enriquecido con los datos del usuario.
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
            appendLine("Si tienes acceso al contenido de documentos de salud (analíticas), analízalos y da recomendaciones basadas en los valores.")
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

            // Sesiones de entrenamiento recientes
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

            // Documentos de salud CON contenido del PDF
            if (healthDocs.isNotEmpty()) {
                appendLine()
                appendLine("=== DOCUMENTOS DE SALUD ===")
                appendLine("El usuario tiene ${healthDocs.size} documento(s) de salud subido(s):")
                healthDocs.take(3).forEach { doc ->
                    appendLine()
                    appendLine("--- ${doc.fileName} (subido el ${dateFormat.format(Date(doc.uploadDate))}) ---")
                    val pdfContent = PdfTextExtractor.extractText(doc.filePath)
                    if (pdfContent.isNotBlank()) {
                        appendLine("Contenido extraído:")
                        appendLine(pdfContent)
                    } else {
                        appendLine("[No se pudo extraer contenido del documento]")
                    }
                }
            }
        }
    }
}

