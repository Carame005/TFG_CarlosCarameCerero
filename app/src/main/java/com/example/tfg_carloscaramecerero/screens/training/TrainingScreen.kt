package com.example.tfg_carloscaramecerero.screens.training

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.DirectionsRun
import androidx.compose.material.icons.filled.FitnessCenter
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import com.example.tfg_carloscaramecerero.components.ConfirmDeleteDialog
import com.example.tfg_carloscaramecerero.components.EmptyStateMessage
import com.example.tfg_carloscaramecerero.components.FitnessCard
import com.example.tfg_carloscaramecerero.components.FitnessFAB
import com.example.tfg_carloscaramecerero.components.FitnessInputDialog
import com.example.tfg_carloscaramecerero.components.SectionHeader
import com.example.tfg_carloscaramecerero.components.FitnessTopBar
import com.example.tfg_carloscaramecerero.navigation.Screen
import com.example.tfg_carloscaramecerero.data.local.entity.RoutineEntity
import com.example.tfg_carloscaramecerero.viewmodel.TrainingViewModel

@OptIn(ExperimentalLayoutApi::class)
@Composable
fun TrainingScreen(
    navController: NavController,
    viewModel: TrainingViewModel
) {
    val routines by viewModel.routinesWithExercises.collectAsState()
    var showCreateDialog by remember { mutableStateOf(false) }
    var routineName by remember { mutableStateOf("") }
    var routineDescription by remember { mutableStateOf("") }
    var routineToDelete by remember { mutableStateOf<RoutineEntity?>(null) }

    Scaffold(
        topBar = { FitnessTopBar(title = "Entrenamiento") },
        floatingActionButton = {
            FitnessFAB(
                onClick = { showCreateDialog = true },
                icon = Icons.Default.Add,
                contentDescription = "Crear rutina"
            )
        }
    ) { padding ->
        if (routines.isEmpty()) {
            EmptyStateMessage(
                message = "No tienes rutinas aún.\nPulsa + para crear una.",
                icon = Icons.Default.FitnessCenter,
                modifier = Modifier
                    .fillMaxSize()
                    .padding(padding)
            )
        } else {
            LazyColumn(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(padding),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                item {
                    SectionHeader(
                        title = "Mis ejercicios",
                        actionText = "Ver todos →",
                        onAction = { navController.navigate(Screen.ExerciseList.route) }
                    )
                }
                item {
                    SectionHeader(title = "Mis rutinas")
                }
                items(routines, key = { it.routine.id }) { routineWithExercises ->
                    val hasCardio = routineWithExercises.exercises.any { it.isCardio }
                    val hasStrength = routineWithExercises.exercises.any { !it.isCardio }
                    val typeLabel = when {
                        hasCardio && hasStrength -> "Mixta"
                        hasCardio -> "Cardio"
                        else -> "Musculación"
                    }
                    FitnessCard(
                        title = routineWithExercises.routine.name,
                        subtitle = if (routineWithExercises.routine.description.isNotBlank())
                            routineWithExercises.routine.description
                        else
                            "${routineWithExercises.exercises.size} ejercicios · $typeLabel",
                        icon = if (hasCardio && !hasStrength) Icons.Default.DirectionsRun
                               else Icons.Default.FitnessCenter,
                        onClick = {
                            navController.navigate(
                                Screen.RoutineDetail.createRoute(routineWithExercises.routine.id)
                            )
                        },
                        onDelete = {
                            routineToDelete = routineWithExercises.routine
                        },
                        modifier = Modifier.padding(horizontal = 16.dp)
                    ) {
                        // Chips de ejercicios
                        if (routineWithExercises.exercises.isNotEmpty()) {
                            FlowRow(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.spacedBy(6.dp),
                                verticalArrangement = Arrangement.spacedBy(6.dp)
                            ) {
                                routineWithExercises.exercises.take(5).forEach { exercise ->
                                    val chipColor = if (exercise.isCardio)
                                        MaterialTheme.colorScheme.secondary
                                    else
                                        MaterialTheme.colorScheme.primary
                                    Box(
                                        modifier = Modifier
                                            .clip(RoundedCornerShape(8.dp))
                                            .background(chipColor.copy(alpha = 0.1f))
                                            .padding(horizontal = 10.dp, vertical = 4.dp)
                                    ) {
                                        Text(
                                            text = exercise.name,
                                            style = MaterialTheme.typography.labelSmall,
                                            color = chipColor,
                                            maxLines = 1,
                                            overflow = TextOverflow.Ellipsis
                                        )
                                    }
                                }
                                if (routineWithExercises.exercises.size > 5) {
                                    Box(
                                        modifier = Modifier
                                            .clip(RoundedCornerShape(8.dp))
                                            .background(
                                                MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.08f)
                                            )
                                            .padding(horizontal = 10.dp, vertical = 4.dp)
                                    ) {
                                        Text(
                                            text = "+${routineWithExercises.exercises.size - 5} más",
                                            style = MaterialTheme.typography.labelSmall,
                                            color = MaterialTheme.colorScheme.onSurfaceVariant
                                        )
                                    }
                                }
                            }
                        }
                    }
                }
                item { Spacer(modifier = Modifier.height(80.dp)) }
            }
        }
    }

    if (showCreateDialog) {
        FitnessInputDialog(
            title = "Nueva rutina",
            onDismiss = {
                showCreateDialog = false
                routineName = ""
                routineDescription = ""
            },
            onConfirm = {
                if (routineName.isNotBlank()) {
                    viewModel.createRoutine(routineName.trim(), routineDescription.trim())
                    showCreateDialog = false
                    routineName = ""
                    routineDescription = ""
                }
            }
        ) {
            Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                OutlinedTextField(
                    value = routineName,
                    onValueChange = { routineName = it },
                    label = { Text("Nombre de la rutina") },
                    placeholder = { Text("Ej: Push Day, Piernas...") },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )
                OutlinedTextField(
                    value = routineDescription,
                    onValueChange = { routineDescription = it },
                    label = { Text("Descripción (opcional)") },
                    placeholder = { Text("Ej: Rutina de fuerza para pecho y tríceps") },
                    modifier = Modifier.fillMaxWidth()
                )
            }
        }
    }

    routineToDelete?.let { routine ->
        ConfirmDeleteDialog(
            title = "Eliminar rutina",
            message = "¿Eliminar \"${routine.name}\" y todo su contenido? Esta acción no se puede deshacer.",
            onConfirm = { viewModel.deleteRoutine(routine) },
            onDismiss = { routineToDelete = null }
        )
    }
}

