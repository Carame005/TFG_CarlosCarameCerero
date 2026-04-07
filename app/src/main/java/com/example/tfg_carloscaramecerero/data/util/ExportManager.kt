package com.example.tfg_carloscaramecerero.data.util

import android.content.Context
import android.content.Intent
import androidx.core.content.FileProvider
import com.example.tfg_carloscaramecerero.data.local.entity.BodyWeightEntity
import com.example.tfg_carloscaramecerero.data.local.entity.FoodEntryEntity
import com.example.tfg_carloscaramecerero.data.local.relation.SessionWithSets
import java.io.File
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

object ExportManager {

    private val dateFormat = SimpleDateFormat("dd/MM/yyyy", Locale("es", "ES"))

    /**
     * Genera un CSV de sesiones de entrenamiento y lanza el intent de compartir.
     */
    fun exportSessions(context: Context, sessions: List<SessionWithSets>) {
        val sb = StringBuilder()
        sb.appendLine("Fecha,Rutina ID,Duración (min),Sets,Ejercicios")
        sessions.forEach { s ->
            val date = dateFormat.format(Date(s.session.date))
            val routineId = s.session.routineId ?: "—"
            val duration = s.session.durationMinutes
            val sets = s.sets.size
            val exercises = s.sets.map { it.exerciseId }.distinct().size
            sb.appendLine("$date,$routineId,$duration,$sets,$exercises")
        }
        shareFile(context, sb.toString(), "sesiones_entrenamiento.csv")
    }

    /**
     * Genera un CSV del historial de peso.
     */
    fun exportWeights(context: Context, weights: List<BodyWeightEntity>) {
        val sb = StringBuilder()
        sb.appendLine("Fecha,Peso (kg)")
        weights.forEach { w ->
            val date = dateFormat.format(Date(w.date))
            sb.appendLine("$date,${w.weight}")
        }
        shareFile(context, sb.toString(), "historial_peso.csv")
    }

    /**
     * Genera un CSV del registro nutricional.
     */
    fun exportNutrition(context: Context, entries: List<FoodEntryEntity>) {
        val sb = StringBuilder()
        sb.appendLine("Día,Tipo comida,Descripción,Gramos,Calorías,Proteínas,Carbos,Grasas")
        val days = listOf("", "Lunes", "Martes", "Miércoles", "Jueves", "Viernes", "Sábado", "Domingo")
        entries.forEach { e ->
            val day = days.getOrElse(e.dayOfWeek) { "Día ${e.dayOfWeek}" }
            sb.appendLine("$day,${e.mealType},\"${e.description}\",${e.grams ?: ""},${e.calories ?: ""},${e.protein ?: ""},${e.carbs ?: ""},${e.fat ?: ""}")
        }
        shareFile(context, sb.toString(), "registro_nutricional.csv")
    }

    private fun shareFile(context: Context, content: String, fileName: String) {
        val exportDir = File(context.cacheDir, "exports").also { it.mkdirs() }
        val file = File(exportDir, fileName)
        file.writeText(content, Charsets.UTF_8)

        val uri = FileProvider.getUriForFile(
            context,
            "${context.packageName}.fileprovider",
            file
        )

        val intent = Intent(Intent.ACTION_SEND).apply {
            type = "text/csv"
            putExtra(Intent.EXTRA_STREAM, uri)
            putExtra(Intent.EXTRA_SUBJECT, "Exportación FitApp - $fileName")
            addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
        }
        context.startActivity(Intent.createChooser(intent, "Exportar como CSV"))
    }
}

