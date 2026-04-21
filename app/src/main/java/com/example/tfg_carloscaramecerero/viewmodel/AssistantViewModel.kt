package com.example.tfg_carloscaramecerero.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.tfg_carloscaramecerero.data.local.entity.ChatConversationEntity
import com.example.tfg_carloscaramecerero.data.local.entity.ChatMessageEntity
import com.example.tfg_carloscaramecerero.data.local.entity.ExerciseEntity
import com.example.tfg_carloscaramecerero.data.local.entity.FoodEntryEntity
import com.example.tfg_carloscaramecerero.data.local.entity.RoutineEntity
import com.example.tfg_carloscaramecerero.data.local.entity.RoutineExerciseCrossRef
import com.example.tfg_carloscaramecerero.data.preferences.UserPreferencesRepository
import com.example.tfg_carloscaramecerero.data.remote.GeminiContent
import com.example.tfg_carloscaramecerero.data.remote.GeminiException
import com.example.tfg_carloscaramecerero.data.remote.GeminiPart
import com.example.tfg_carloscaramecerero.data.remote.GeminiService
import com.example.tfg_carloscaramecerero.data.remote.RateLimitExceededException
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
import org.json.JSONArray
import org.json.JSONObject
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
    private val chatRepository: ChatRepository,
    private val userPrefsRepository: UserPreferencesRepository
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
                    // Ocultar indicador de "escribiendo" cuando llega el primer token
                    if (_isTyping.value) {
                        _isTyping.value = false
                    }
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
                    // Extraer y ejecutar acciones antes de mostrar el texto final
                    val (cleanText, actionResults) = parseAndExecuteActions(accumulatedText)
                    val finalText = if (actionResults.isNotEmpty()) {
                        "$cleanText\n\n${actionResults.joinToString("\n")}"
                    } else cleanText

                    // Actualizar mensaje mostrado (sin las etiquetas de acción)
                    _messages.value = _messages.value.map { msg ->
                        if (msg.id == botMessageId) msg.copy(content = finalText)
                        else msg
                    }
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

            } catch (e: RateLimitExceededException) {
                _messages.value = _messages.value.filter { it.id != botMessageId || it.content.isNotBlank() }
                val errorMessage = ChatMessage(
                    content = "⏳ ${e.message}",
                    isUser = false
                )
                _messages.value = _messages.value + errorMessage
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
     * Parsea bloques [ACTION:TYPE]{json}[/ACTION] del texto del asistente,
     * ejecuta las acciones correspondientes si el usuario tiene el permiso,
     * y devuelve el texto limpio + lista de confirmaciones.
     */
    private suspend fun parseAndExecuteActions(text: String): Pair<String, List<String>> {
        val canCreateRoutines = userPrefsRepository.aiCanCreateRoutines.firstOrNull() ?: false
        val canCreateExercises = userPrefsRepository.aiCanCreateExercises.firstOrNull() ?: false
        val canCreateFoodSchedule = userPrefsRepository.aiCanCreateFoodSchedule.firstOrNull() ?: false

        val actionRegex = Regex("""\[ACTION:(\w+)\](.*?)\[/ACTION\]""", RegexOption.DOT_MATCHES_ALL)
        val results = mutableListOf<String>()
        var cleanText = text

        for (match in actionRegex.findAll(text)) {
            val actionType = match.groupValues[1]
            val jsonStr = match.groupValues[2].trim()
            cleanText = cleanText.replace(match.value, "")
            try {
                when (actionType) {
                    "CREATE_ROUTINE" -> if (canCreateRoutines) {
                        results.add(executeCreateRoutine(jsonStr))
                    } else {
                        results.add("⚠️ No tienes activado el permiso para que el asistente cree rutinas (puedes activarlo en Ajustes).")
                    }
                    "CREATE_EXERCISE" -> if (canCreateExercises) {
                        results.add(executeCreateExercise(jsonStr))
                    } else {
                        results.add("⚠️ No tienes activado el permiso para que el asistente cree ejercicios (puedes activarlo en Ajustes).")
                    }
                    "CREATE_FOOD_SCHEDULE" -> if (canCreateFoodSchedule) {
                        results.add(executeCreateFoodSchedule(jsonStr))
                    } else {
                        results.add("⚠️ No tienes activado el permiso para que el asistente cree el horario de comidas (puedes activarlo en Ajustes).")
                    }
                }
            } catch (e: Exception) {
                results.add("⚠️ Error al ejecutar acción $actionType: ${e.localizedMessage}")
            }
        }
        return cleanText.trim() to results
    }

    private fun inferExerciseType(name: String, muscleGroup: String, rawType: String): String {
        if (rawType.equals("CARDIO", ignoreCase = true)) return "CARDIO"
        if (rawType.equals("STRENGTH", ignoreCase = true)) return "STRENGTH"
        // Inferir por nombre o grupo muscular
        val combined = "$name $muscleGroup".lowercase()
        val cardioKeywords = listOf("cardio", "correr", "carrera", "ciclismo", "bicicleta",
            "natación", "nadar", "elíptica", "remo cardio", "saltar", "cuerda", "hiit",
            "aeróbic", "aerobic", "caminar", "marcha", "spinning", "running", "trote")
        return if (cardioKeywords.any { combined.contains(it) }) "CARDIO" else "STRENGTH"
    }

    private suspend fun executeCreateRoutine(json: String): String {
        val obj = JSONObject(json)
        val routineName = obj.getString("name")
        val routineId = routineRepository.insert(RoutineEntity(name = routineName))
        val exercises = obj.optJSONArray("exercises") ?: JSONArray()
        var exercisesAdded = 0
        for (i in 0 until exercises.length()) {
            val ex = exercises.getJSONObject(i)
            val exName = ex.getString("name")
            val muscleGroup = ex.optString("muscleGroup", "General")
            val exerciseType = inferExerciseType(exName, muscleGroup, ex.optString("exerciseType", ""))
            val exerciseId = exerciseRepository.insert(
                ExerciseEntity(
                    name = exName,
                    muscleGroup = muscleGroup,
                    exerciseType = exerciseType,
                    description = ex.optString("description", "")
                )
            )
            routineRepository.addExerciseToRoutine(
                RoutineExerciseCrossRef(routineId = routineId, exerciseId = exerciseId)
            )
            exercisesAdded++
        }
        return "✅ **Rutina creada:** \"$routineName\" con $exercisesAdded ejercicio(s)."
    }

    private suspend fun executeCreateExercise(json: String): String {
        val obj = JSONObject(json)
        val name = obj.getString("name")
        val muscleGroup = obj.optString("muscleGroup", "General")
        val exerciseType = inferExerciseType(name, muscleGroup, obj.optString("exerciseType", ""))
        exerciseRepository.insert(
            ExerciseEntity(
                name = name,
                muscleGroup = muscleGroup,
                exerciseType = exerciseType,
                description = obj.optString("description", "")
            )
        )
        return "✅ **Ejercicio creado:** \"$name\" (${if (exerciseType == "CARDIO") "Cardio" else "Fuerza"})."
    }

    private suspend fun executeCreateFoodSchedule(json: String): String {
        val obj = JSONObject(json)
        val entries = obj.getJSONArray("entries")
        var count = 0
        for (i in 0 until entries.length()) {
            val e = entries.getJSONObject(i)
            val rawFoodType = e.optString("foodType", "")
            // Detectar automáticamente si no viene especificado
            val foodType = when {
                rawFoodType.equals("bebida", ignoreCase = true) -> "bebida"
                rawFoodType.equals("comida", ignoreCase = true) -> "comida"
                else -> {
                    // Inferir por descripción si el modelo no lo envió
                    val desc = e.optString("description", "").lowercase()
                    val bebidasKeywords = listOf("café", "cafe", "té", "te", "infusión", "infusion",
                        "zumo", "jugo", "agua", "leche", "batido", "refresco", "cerveza", "vino",
                        "bebida", "proteína líquida", "shake", "smoothie", "limonada", "cola")
                    if (bebidasKeywords.any { desc.contains(it) }) "bebida" else "comida"
                }
            }
            nutritionRepository.insertEntry(
                FoodEntryEntity(
                    description = e.getString("description"),
                    mealType = e.optString("mealType", "desayuno"),
                    dayOfWeek = e.optInt("dayOfWeek", 1),
                    time = e.optString("time", ""),
                    foodType = foodType
                )
            )
            count++
        }
        return "✅ **Horario de comidas creado:** $count entrada(s) añadida(s)."
    }

    /**
     * Construye un system prompt enriquecido con los datos del usuario.
     */
    private suspend fun buildSystemPrompt(): String {
        val dateFormat = SimpleDateFormat("dd/MM/yyyy", Locale("es", "ES"))

        val canCreateRoutines = userPrefsRepository.aiCanCreateRoutines.firstOrNull() ?: false
        val canCreateExercises = userPrefsRepository.aiCanCreateExercises.firstOrNull() ?: false
        val canCreateFoodSchedule = userPrefsRepository.aiCanCreateFoodSchedule.firstOrNull() ?: false

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
            appendLine("Si el usuario tiene un objetivo fitness definido, adapta todas tus recomendaciones de entrenamiento y nutrición a ese objetivo.")
            appendLine("Si el usuario te pregunta sobre calorías o macros de sus comidas, haz estimaciones razonables basándote en las descripciones.")
            appendLine("No inventes datos médicos ni diagnósticos. Si el usuario tiene condiciones de salud, tenlas en cuenta en tus recomendaciones.")
            appendLine("Si tienes acceso al contenido de documentos de salud (analíticas), analízalos y da recomendaciones basadas en los valores.")
            appendLine()

            // Instrucciones para creación de contenido
            if (canCreateRoutines || canCreateExercises || canCreateFoodSchedule) {
                appendLine("=== CAPACIDADES DE CREACIÓN ===")
                appendLine("Cuando el usuario te pida explícitamente crear contenido, debes incluir al FINAL de tu respuesta los bloques de acción correspondientes.")
                appendLine("IMPORTANTE: Incluye SIEMPRE tu explicación primero y el bloque de acción JSON al final. El JSON debe ser válido y sin saltos de línea dentro del bloque.")
                appendLine()
                if (canCreateRoutines) {
                    appendLine("── CREAR RUTINA ──")
                    appendLine("Usa [ACTION:CREATE_ROUTINE] cuando el usuario pida crear una rutina de entrenamiento.")
                    appendLine("Formato:")
                    appendLine("""[ACTION:CREATE_ROUTINE]{"name":"Nombre de la Rutina","exercises":[{"name":"Press Banca","muscleGroup":"Pecho","exerciseType":"STRENGTH","description":"Ejercicio de empuje horizontal"},{"name":"Carrera","muscleGroup":"Cardio","exerciseType":"CARDIO","description":"Correr a ritmo moderado"}]}[/ACTION]""")
                    appendLine()
                    appendLine("REGLAS para exerciseType:")
                    appendLine("  - Usa STRENGTH para: musculación, pesas, fuerza, hipertrofia, ejercicios con carga (press, sentadilla, peso muerto, curl, extensión, remo, dominadas, fondos, etc.)")
                    appendLine("  - Usa CARDIO para: correr, ciclismo, natación, saltar cuerda, HIIT, bicicleta, elíptica, remo (máquina cardio), caminar, aeróbicos, etc.")
                    appendLine()
                    appendLine("REGLAS para muscleGroup (ejemplos orientativos):")
                    appendLine("  STRENGTH → Pecho, Espalda, Hombros, Bíceps, Tríceps, Piernas, Glúteos, Abdominales, Core, Trapecio, Antebrazos")
                    appendLine("  CARDIO → Cardio, Cardio General, Full Body Cardio")
                    appendLine()
                }
                if (canCreateExercises) {
                    appendLine("── CREAR EJERCICIO ──")
                    appendLine("Usa [ACTION:CREATE_EXERCISE] cuando el usuario pida añadir un ejercicio individual.")
                    appendLine("Formato:")
                    appendLine("""[ACTION:CREATE_EXERCISE]{"name":"Nombre Ejercicio","muscleGroup":"Grupo Muscular","exerciseType":"STRENGTH","description":"Descripción breve"}[/ACTION]""")
                    appendLine()
                    appendLine("Aplica las mismas reglas de exerciseType y muscleGroup descritas arriba.")
                    appendLine()
                }
                if (canCreateFoodSchedule) {
                    appendLine("── CREAR HORARIO DE COMIDAS ──")
                    appendLine("Usa [ACTION:CREATE_FOOD_SCHEDULE] cuando el usuario pida añadir comidas o bebidas al horario.")
                    appendLine("Formato:")
                    appendLine("""[ACTION:CREATE_FOOD_SCHEDULE]{"entries":[{"description":"Pan con aceite","mealType":"desayuno","dayOfWeek":1,"time":"08:00","foodType":"comida"},{"description":"Café","mealType":"desayuno","dayOfWeek":1,"time":"08:00","foodType":"bebida"}]}[/ACTION]""")
                    appendLine()
                    appendLine("REGLAS para foodType (OBLIGATORIO especificarlo siempre):")
                    appendLine("  - Usa \"comida\" para: pan, tostadas, cereales, frutas, huevos, carne, pescado, arroz, pasta, verduras, legumbres, yogur, queso, bocadillos, ensaladas, sopas, etc.")
                    appendLine("  - Usa \"bebida\" para: café, té, infusiones, zumo, agua, leche, batido, refresco, cerveza, vino, proteína en polvo disuelta, cualquier líquido.")
                    appendLine()
                    appendLine("REGLAS para mealType: desayuno, almuerzo, cena, snack")
                    appendLine("REGLAS para dayOfWeek: 1=Lunes, 2=Martes, 3=Miércoles, 4=Jueves, 5=Viernes, 6=Sábado, 7=Domingo")
                    appendLine("Si el usuario menciona un día específico, úsalo. Si no lo especifica, usa 1 (Lunes) como valor por defecto.")
                    appendLine("Crea UNA entrada separada por cada alimento/bebida individual que mencione el usuario.")
                    appendLine()
                }
            }

            // Perfil del usuario
            appendLine("=== DATOS DEL USUARIO ===")
            if (profile != null) {
                profile.height?.let { appendLine("Altura: ${it} cm") }
                if (profile.fitnessGoal.isNotBlank()) {
                    appendLine("Objetivo fitness: ${profile.fitnessGoal}")
                }
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
                recentSessions.take(10).forEach { session ->
                    val routineName = routines.find { it.routine.id == session.session.routineId }?.routine?.name ?: "Sin rutina"
                    appendLine("- ${dateFormat.format(Date(session.session.date))}: $routineName (${session.session.durationMinutes} min)")
                    // Agrupar sets por ejercicio para mayor claridad
                    val setsByExercise = session.sets.groupBy { it.exerciseId }
                    setsByExercise.forEach { (exerciseId, sets) ->
                        val exerciseName = exercises.find { it.id == exerciseId }?.name ?: "Ejercicio #$exerciseId"
                        if (sets.firstOrNull()?.isCardio == true) {
                            sets.forEach { set ->
                                appendLine("  · $exerciseName (set ${set.setNumber}): ${set.durationSeconds}s, ${set.distanceKm}km")
                            }
                        } else {
                            val setsStr = sets.joinToString(" | ") { "Set ${it.setNumber}: ${it.reps}reps x ${it.weight}kg" }
                            appendLine("  · $exerciseName: $setsStr")
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

