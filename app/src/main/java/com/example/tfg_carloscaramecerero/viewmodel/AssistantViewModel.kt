package com.example.tfg_carloscaramecerero.viewmodel

import androidx.lifecycle.ViewModel
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
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
class AssistantViewModel @Inject constructor() : ViewModel() {

    private val _messages = MutableStateFlow<List<ChatMessage>>(
        listOf(
            ChatMessage(
                content = "¡Hola! Soy tu asistente fitness. Puedo ayudarte con tus rutinas, nutrición y entrenamiento. ¿En qué puedo ayudarte?",
                isUser = false
            )
        )
    )
    val messages: StateFlow<List<ChatMessage>> = _messages.asStateFlow()

    private val _isTyping = MutableStateFlow(false)
    val isTyping: StateFlow<Boolean> = _isTyping.asStateFlow()

    fun sendMessage(text: String) {
        if (text.isBlank()) return

        val userMessage = ChatMessage(content = text.trim(), isUser = true)
        _messages.value = _messages.value + userMessage

        // Placeholder: respuesta automática temporal hasta integrar IA real
        _isTyping.value = true
        val botReply = ChatMessage(
            content = "Esta funcionalidad estará disponible próximamente. Cuando se integre el modelo de IA, podré analizar tus entrenamientos, nutrición y darte recomendaciones personalizadas.",
            isUser = false
        )
        _messages.value = _messages.value + botReply
        _isTyping.value = false
    }

    fun clearChat() {
        _messages.value = listOf(
            ChatMessage(
                content = "¡Hola! Soy tu asistente fitness. Puedo ayudarte con tus rutinas, nutrición y entrenamiento. ¿En qué puedo ayudarte?",
                isUser = false
            )
        )
    }
}

