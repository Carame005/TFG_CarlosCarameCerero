package com.example.tfg_carloscaramecerero.navigation

import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.consumeWindowInsets
import androidx.compose.foundation.layout.padding
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavHostController
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.navArgument
import com.example.tfg_carloscaramecerero.screens.assistant.AssistantScreen
import com.example.tfg_carloscaramecerero.screens.assistant.ChatHistoryScreen
import com.example.tfg_carloscaramecerero.screens.body.BodyScreen
import com.example.tfg_carloscaramecerero.screens.home.DashboardScreen
import com.example.tfg_carloscaramecerero.screens.nutrition.NutritionScreen
import com.example.tfg_carloscaramecerero.screens.recommendations.RecommendationsScreen
import com.example.tfg_carloscaramecerero.screens.settings.SettingsScreen
import com.example.tfg_carloscaramecerero.screens.training.ExerciseListScreen
import com.example.tfg_carloscaramecerero.screens.training.RoutineDetailScreen
import com.example.tfg_carloscaramecerero.screens.training.SessionDetailScreen
import com.example.tfg_carloscaramecerero.screens.training.TrainingScreen
import com.example.tfg_carloscaramecerero.viewmodel.AssistantViewModel
import com.example.tfg_carloscaramecerero.viewmodel.SettingsViewModel

@Composable
fun FitnessNavGraph(
    navController: NavHostController,
    innerPadding: PaddingValues
) {
    val assistantViewModel: AssistantViewModel = hiltViewModel()
    val settingsViewModel: SettingsViewModel = hiltViewModel()

    NavHost(
        navController = navController,
        startDestination = Screen.Dashboard.route,
        modifier = Modifier
            .padding(innerPadding)
            .consumeWindowInsets(innerPadding)
    ) {
        // ── Dashboard (Inicio) ──
        composable(Screen.Dashboard.route) {
            DashboardScreen(
                viewModel = hiltViewModel(),
                onNavigateToTraining = { navController.navigate(Screen.Training.route) },
                onNavigateToNutrition = { navController.navigate(Screen.Nutrition.route) },
                onNavigateToBody = { navController.navigate(Screen.Body.route) },
                onNavigateToSettings = { navController.navigate(Screen.Settings.route) }
            )
        }

        // ── Tabs principales ──
        composable(Screen.Training.route) {
            TrainingScreen(
                navController = navController,
                viewModel = hiltViewModel()
            )
        }

        composable(Screen.Body.route) {
            BodyScreen(viewModel = hiltViewModel())
        }

        composable(Screen.Assistant.route) {
            AssistantScreen(
                viewModel = assistantViewModel,
                onNavigateToHistory = {
                    navController.navigate(Screen.ChatHistory.route)
                }
            )
        }

        composable(Screen.Nutrition.route) {
            NutritionScreen(viewModel = hiltViewModel())
        }

        composable(Screen.Recommendations.route) {
            RecommendationsScreen(viewModel = hiltViewModel())
        }

        // ── Ajustes ──
        composable(Screen.Settings.route) {
            val trainingVm: com.example.tfg_carloscaramecerero.viewmodel.TrainingViewModel = hiltViewModel()
            val bodyVm: com.example.tfg_carloscaramecerero.viewmodel.BodyViewModel = hiltViewModel()
            val nutritionVm: com.example.tfg_carloscaramecerero.viewmodel.NutritionViewModel = hiltViewModel()
            val weights by bodyVm.weights.collectAsState()
            val foodEntries by nutritionVm.allEntries.collectAsState()
            val allSessionsWithSets by trainingVm.allSessionsWithSets.collectAsState()
            SettingsScreen(
                viewModel = settingsViewModel,
                onBackClick = { navController.popBackStack() },
                sessions = allSessionsWithSets,
                weights = weights,
                foodEntries = foodEntries
            )
        }

        // ── Sub-pantallas ──
        composable(Screen.ExerciseList.route) {
            ExerciseListScreen(
                navController = navController,
                viewModel = hiltViewModel()
            )
        }

        composable(
            route = Screen.RoutineDetail.route,
            arguments = listOf(navArgument("routineId") { type = NavType.LongType })
        ) { backStackEntry ->
            val routineId = backStackEntry.arguments?.getLong("routineId") ?: 0L
            RoutineDetailScreen(
                routineId = routineId,
                navController = navController,
                viewModel = hiltViewModel()
            )
        }

        composable(
            route = Screen.SessionDetail.route,
            arguments = listOf(navArgument("sessionId") { type = NavType.LongType })
        ) { backStackEntry ->
            val sessionId = backStackEntry.arguments?.getLong("sessionId") ?: 0L
            SessionDetailScreen(
                sessionId = sessionId,
                navController = navController,
                viewModel = hiltViewModel()
            )
        }

        // ── Historial de chats ──
        composable(Screen.ChatHistory.route) {
            ChatHistoryScreen(
                viewModel = assistantViewModel,
                onBackClick = { navController.popBackStack() },
                onConversationClick = { conversationId ->
                    navController.navigate(Screen.AssistantChat.createRoute(conversationId))
                }
            )
        }

        // ── Chat con conversación específica ──
        composable(
            route = Screen.AssistantChat.route,
            arguments = listOf(
                navArgument("conversationId") {
                    type = NavType.LongType
                    defaultValue = -1L
                }
            )
        ) { backStackEntry ->
            val conversationId = backStackEntry.arguments?.getLong("conversationId") ?: -1L
            LaunchedEffect(conversationId) {
                if (conversationId > 0) {
                    assistantViewModel.loadConversation(conversationId)
                }
            }
            AssistantScreen(
                viewModel = assistantViewModel,
                onNavigateToHistory = { navController.popBackStack() },
                onBackClick = { navController.popBackStack() }
            )
        }
    }
}

