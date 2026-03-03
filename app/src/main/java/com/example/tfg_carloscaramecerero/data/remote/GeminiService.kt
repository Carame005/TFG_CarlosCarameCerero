package com.example.tfg_carloscaramecerero.data.remote

import android.util.Log
import com.google.gson.Gson
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.withContext
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.RequestBody.Companion.toRequestBody
import java.io.BufferedReader
import java.util.concurrent.TimeUnit
import javax.inject.Inject
import javax.inject.Singleton

// ── Modelos de datos para la API REST de Gemini ──

data class GeminiRequest(
    val contents: List<GeminiContent>,
    val systemInstruction: GeminiContent? = null,
    val generationConfig: GeminiGenerationConfig = GeminiGenerationConfig()
)

data class GeminiContent(
    val role: String,
    val parts: List<GeminiPart>
)

data class GeminiPart(
    val text: String
)

data class GeminiGenerationConfig(
    val temperature: Float = 0.7f,
    val topK: Int = 40,
    val topP: Float = 0.95f,
    val maxOutputTokens: Int = 2048
)

data class GeminiResponse(
    val candidates: List<GeminiCandidate>?,
    val error: GeminiError? = null
)

data class GeminiCandidate(
    val content: GeminiContent?
)

data class GeminiError(
    val code: Int? = null,
    val message: String? = null,
    val status: String? = null
)

// Para streaming
data class GeminiStreamChunk(
    val candidates: List<GeminiStreamCandidate>?,
    val error: GeminiError? = null
)

data class GeminiStreamCandidate(
    val content: GeminiStreamContent?
)

data class GeminiStreamContent(
    val parts: List<GeminiPart>?,
    val role: String?
)

private const val TAG = "GeminiService"

/**
 * Servicio que se comunica con la API REST de Google Gemini directamente.
 */
@Singleton
class GeminiService @Inject constructor(
    private val apiKey: String
) {
    private val gson = Gson()
    private val client = OkHttpClient.Builder()
        .connectTimeout(30, TimeUnit.SECONDS)
        .readTimeout(120, TimeUnit.SECONDS)
        .writeTimeout(30, TimeUnit.SECONDS)
        .build()

    private val baseUrl = "https://generativelanguage.googleapis.com/v1beta/models"
    private val model = "gemini-2.5-flash"
    private val jsonMediaType = "application/json; charset=utf-8".toMediaType()

    /**
     * Envía un mensaje con streaming. Devuelve un Flow que emite tokens progresivamente.
     */
    fun sendMessageStream(
        userMessage: String,
        conversationHistory: List<GeminiContent>,
        systemPrompt: String
    ): Flow<String> = flow {
        val contents = buildContentList(userMessage, conversationHistory)
        val systemInstruction = GeminiContent(
            role = "user",
            parts = listOf(GeminiPart(systemPrompt))
        )
        val request = GeminiRequest(
            contents = contents,
            systemInstruction = systemInstruction
        )
        val jsonBody = gson.toJson(request)

        Log.d(TAG, "Sending streaming request to Gemini API...")

        val httpRequest = Request.Builder()
            .url("$baseUrl/$model:streamGenerateContent?alt=sse&key=$apiKey")
            .post(jsonBody.toRequestBody(jsonMediaType))
            .build()

        val response = withContext(Dispatchers.IO) {
            client.newCall(httpRequest).execute()
        }

        if (!response.isSuccessful) {
            val errorBody = withContext(Dispatchers.IO) {
                response.body?.string() ?: ""
            }
            response.close()
            Log.e(TAG, "API error ${response.code}: $errorBody")
            throw GeminiException(mapHttpError(response.code, errorBody))
        }

        val reader = withContext(Dispatchers.IO) {
            response.body?.charStream()?.let { BufferedReader(it) }
        } ?: run {
            response.close()
            throw GeminiException("Respuesta vacía del servidor.")
        }

        try {
            var line: String?
            while (true) {
                line = withContext(Dispatchers.IO) { reader.readLine() } ?: break
                if (line.startsWith("data: ")) {
                    val jsonData = line.removePrefix("data: ").trim()
                    if (jsonData.isNotEmpty() && jsonData != "[DONE]") {
                        try {
                            val chunk = gson.fromJson(jsonData, GeminiStreamChunk::class.java)

                            // Comprobar si hay error en el chunk
                            chunk.error?.let { error ->
                                throw GeminiException(
                                    error.message ?: "Error del servidor: ${error.status}"
                                )
                            }

                            val text = chunk.candidates
                                ?.firstOrNull()
                                ?.content
                                ?.parts
                                ?.firstOrNull()
                                ?.text
                            if (!text.isNullOrEmpty()) {
                                emit(text)
                            }
                        } catch (e: GeminiException) {
                            throw e
                        } catch (_: Exception) {
                            // Ignorar chunks malformados
                        }
                    }
                }
            }
        } finally {
            withContext(Dispatchers.IO) {
                reader.close()
                response.close()
            }
        }
    }

    /**
     * Envía un mensaje sin streaming (respuesta completa).
     */
    suspend fun sendMessage(
        userMessage: String,
        conversationHistory: List<GeminiContent>,
        systemPrompt: String
    ): String {
        val contents = buildContentList(userMessage, conversationHistory)
        val systemInstruction = GeminiContent(
            role = "user",
            parts = listOf(GeminiPart(systemPrompt))
        )
        val request = GeminiRequest(
            contents = contents,
            systemInstruction = systemInstruction
        )
        val jsonBody = gson.toJson(request)

        val httpRequest = Request.Builder()
            .url("$baseUrl/$model:generateContent?key=$apiKey")
            .post(jsonBody.toRequestBody(jsonMediaType))
            .build()

        val response = withContext(Dispatchers.IO) {
            client.newCall(httpRequest).execute()
        }

        val body = withContext(Dispatchers.IO) {
            response.body?.string() ?: ""
        }
        response.close()

        if (!response.isSuccessful) {
            Log.e(TAG, "API error ${response.code}: $body")
            throw GeminiException(mapHttpError(response.code, body))
        }

        val geminiResponse = gson.fromJson(body, GeminiResponse::class.java)

        geminiResponse.error?.let { error ->
            throw GeminiException(error.message ?: "Error desconocido del servidor.")
        }

        return geminiResponse.candidates
            ?.firstOrNull()
            ?.content
            ?.parts
            ?.joinToString("") { it.text }
            ?: "No se pudo generar una respuesta."
    }

    private fun buildContentList(
        userMessage: String,
        conversationHistory: List<GeminiContent>
    ): List<GeminiContent> {
        return buildList {
            // Historial de la conversación
            addAll(conversationHistory)
            // Mensaje actual del usuario
            add(GeminiContent(role = "user", parts = listOf(GeminiPart(userMessage))))
        }
    }

    private fun mapHttpError(code: Int, body: String): String {
        // Intentar extraer mensaje de error del JSON
        val apiMessage = try {
            val errorResponse = gson.fromJson(body, GeminiErrorResponse::class.java)
            errorResponse?.error?.message
        } catch (_: Exception) {
            null
        }

        val defaultMessage = when (code) {
            400 -> "Solicitud incorrecta. Reformula tu mensaje."
            401, 403 -> "Error de autenticación. Verifica tu API key de Gemini."
            404 -> "Modelo no encontrado. Verifica la configuración."
            429 -> "Se ha superado la cuota de la API de Gemini. Tu plan gratuito puede haberse agotado. Revisa tu cuota en https://ai.dev/rate-limit o activa la facturación en Google Cloud. Inténtalo de nuevo más tarde."
            500, 503 -> "El servicio de Gemini no está disponible. Inténtalo más tarde."
            else -> "Error del servidor ($code)."
        }

        return apiMessage ?: defaultMessage
    }
}

data class GeminiErrorResponse(
    val error: GeminiError?
)

class GeminiException(message: String, cause: Throwable? = null) : Exception(message, cause)

