package com.example.tfg_carloscaramecerero.navigation

import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInHorizontally
import androidx.compose.animation.slideOutHorizontally
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
import com.example.tfg_carloscaramecerero.screens.settings.AuditLogScreen
import com.example.tfg_carloscaramecerero.screens.settings.HelpScreen
import com.example.tfg_carloscaramecerero.screens.settings.SettingsScreen
import com.example.tfg_carloscaramecerero.screens.settings.TermsScreen
import com.example.tfg_carloscaramecerero.screens.training.ExerciseListScreen
import com.example.tfg_carloscaramecerero.screens.training.RoutineDetailScreen
import com.example.tfg_carloscaramecerero.screens.training.SessionDetailScreen
import com.example.tfg_carloscaramecerero.screens.training.TrainingScreen
import com.example.tfg_carloscaramecerero.viewmodel.AssistantViewModel
import com.example.tfg_carloscaramecerero.viewmodel.SettingsViewModel

private const val TRANSITION_DURATION = 280

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
            .consumeWindowInsets(innerPadding),
        enterTransition = {
            slideInHorizontally(
                initialOffsetX = { it },
                animationSpec = tween(TRANSITION_DURATION)
            ) + fadeIn(animationSpec = tween(TRANSITION_DURATION))
        },
        exitTransition = {
            slideOutHorizontally(
                targetOffsetX = { -it / 4 },
                animationSpec = tween(TRANSITION_DURATION)
            ) + fadeOut(animationSpec = tween(TRANSITION_DURATION))
        },
        popEnterTransition = {
            slideInHorizontally(
                initialOffsetX = { -it / 4 },
                animationSpec = tween(TRANSITION_DURATION)
            ) + fadeIn(animationSpec = tween(TRANSITION_DURATION))
        },
        popExitTransition = {
            slideOutHorizontally(
                targetOffsetX = { it },
                animationSpec = tween(TRANSITION_DURATION)
            ) + fadeOut(animationSpec = tween(TRANSITION_DURATION))
        }
    ) {
        // ── Dashboard (Inicio) ──
        composable(Screen.Dashboard.route) {
            DashboardScreen(
                viewModel = hiltViewModel(),
                onNavigateToTraining = { navController.navigate(Screen.Training.route) },
                onNavigateToNutrition = { navController.navigate(Screen.Nutrition.route) },
                onNavigateToBody = { navController.navigate(Screen.Body.route) },
                onNavigateToSettings = { navController.navigate(Screen.Settings.route) },
                onNavigateToAssistant = { navController.navigate(Screen.Assistant.route) },
                onNavigateToHealth = { navController.navigate(Screen.Body.createRoute(tab = 2)) },
                onNavigateToHelp = { navController.navigate(Screen.Help.route) },
                onNavigateToRoutine = { routineId ->
                    navController.navigate(Screen.RoutineDetail.createRoute(routineId))
                },
                onNavigateToRecommendations = { navController.navigate(Screen.Recommendations.route) }
            )
        }

        // ── Tabs principales ──
        composable(Screen.Training.route) {
            TrainingScreen(
                navController = navController,
                viewModel = hiltViewModel()
            )
        }

        composable(
            route = "${Screen.Body.route}?tab={tab}",
            arguments = listOf(navArgument("tab") {
                type = NavType.IntType
                defaultValue = 0
            })
        ) { backStackEntry ->
            val initialTab = backStackEntry.arguments?.getInt("tab") ?: 0
            BodyScreen(viewModel = hiltViewModel(), initialTab = initialTab)
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
            val allRoutines by trainingVm.routinesWithExercises.collectAsState()
            val allExercises by trainingVm.allExercises.collectAsState()
            SettingsScreen(
                viewModel = settingsViewModel,
                onBackClick = { navController.popBackStack() },
                onNavigateToAuditLog = { navController.navigate(Screen.AuditLog.route) },
                onNavigateToHelp = { navController.navigate(Screen.Help.route) },
                onNavigateToTerms = { navController.navigate(Screen.Terms.route) },
                sessions = allSessionsWithSets,
                weights = weights,
                foodEntries = foodEntries,
                allRoutines = allRoutines.map { it.routine },
                allExercises = allExercises
            )
        }

        // ── Registro de auditoría ──
        composable(Screen.AuditLog.route) {
            AuditLogScreen(
                viewModel = hiltViewModel(),
                onBackClick = { navController.popBackStack() }
            )
        }

        // ── Ayuda / Onboarding ──
        composable(Screen.Help.route) {
            HelpScreen(onBackClick = { navController.popBackStack() })
        }

        // ── Términos y condiciones (lectura desde Ajustes) ──
        composable(Screen.Terms.route) {
            TermsScreen(
                readOnly = true,
                onBackClick = { navController.popBackStack() }
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
