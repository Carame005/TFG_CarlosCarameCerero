package com.example.tfg_carloscaramecerero.navigation

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.FitnessCenter
import androidx.compose.material.icons.filled.Lightbulb
import androidx.compose.material.icons.filled.MonitorWeight
import androidx.compose.material.icons.filled.Restaurant
import androidx.compose.material.icons.filled.SmartToy
import androidx.compose.ui.graphics.vector.ImageVector

sealed class Screen(
    val route: String,
    val label: String = "",
    val icon: ImageVector? = null
) {
    // ── Tabs principales ──
    data object Training : Screen(
        route = "training",
        label = "Entreno",
        icon = Icons.Default.FitnessCenter
    )

    data object Body : Screen(
        route = "body",
        label = "Cuerpo",
        icon = Icons.Default.MonitorWeight
    )

    data object Assistant : Screen(
        route = "assistant",
        label = "Asistente",
        icon = Icons.Default.SmartToy
    )

    data object Nutrition : Screen(
        route = "nutrition",
        label = "Nutrición",
        icon = Icons.Default.Restaurant
    )

    data object Recommendations : Screen(
        route = "recommendations",
        label = "Consejos",
        icon = Icons.Default.Lightbulb
    )

    // ── Sub-pantallas ──
    data object RoutineDetail : Screen(route = "routine_detail/{routineId}") {
        fun createRoute(routineId: Long) = "routine_detail/$routineId"
    }

    data object SessionDetail : Screen(route = "session_detail/{sessionId}") {
        fun createRoute(sessionId: Long) = "session_detail/$sessionId"
    }

    data object ExerciseList : Screen(route = "exercise_list")

    companion object {
        val bottomNavItems = listOf(Training, Body, Assistant, Nutrition, Recommendations)
    }
}

