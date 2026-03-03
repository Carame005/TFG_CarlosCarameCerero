package com.example.tfg_carloscaramecerero.screens.training

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
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
import androidx.compose.material.icons.filled.SportsGymnastics
import androidx.compose.material.icons.filled.SportsMartialArts
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
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
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import com.example.tfg_carloscaramecerero.components.ConfirmDeleteDialog
import com.example.tfg_carloscaramecerero.components.EmptyStateMessage
import com.example.tfg_carloscaramecerero.components.FitnessCard
import com.example.tfg_carloscaramecerero.components.FitnessFAB
import com.example.tfg_carloscaramecerero.components.FitnessInputDialog
import com.example.tfg_carloscaramecerero.components.FitnessTopBar
import com.example.tfg_carloscaramecerero.components.SectionHeader
import com.example.tfg_carloscaramecerero.data.local.entity.ExerciseEntity
import com.example.tfg_carloscaramecerero.data.local.entity.ExerciseType
import com.example.tfg_carloscaramecerero.viewmodel.TrainingViewModel

@Composable
fun ExerciseListScreen(
    navController: NavController,
    viewModel: TrainingViewModel
) {
    val exercises by viewModel.allExercises.collectAsState()
    val muscleGroups by viewModel.muscleGroups.collectAsState()

    var showCreateDialog by remember { mutableStateOf(false) }
    var newName by remember { mutableStateOf("") }
    var newMuscle by remember { mutableStateOf("") }
    var newDesc by remember { mutableStateOf("") }
    var newExerciseType by remember { mutableStateOf(ExerciseType.STRENGTH) }
    var exerciseToDelete by remember { mutableStateOf<ExerciseEntity?>(null) }

    Scaffold(
        topBar = {
            FitnessTopBar(
                title = "Mis ejercicios",
                onBackClick = { navController.popBackStack() }
            )
        },
        floatingActionButton = {
            FitnessFAB(
                onClick = { showCreateDialog = true },
                icon = Icons.Default.Add,
                contentDescription = "Crear ejercicio"
            )
        }
    ) { padding ->
        if (exercises.isEmpty()) {
            EmptyStateMessage(
                message = "No tienes ejercicios creados.\nPulsa + para crear uno.",
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
                verticalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                // Resumen rápido
                item {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 16.dp, vertical = 8.dp),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Box(
                            modifier = Modifier
                                .clip(RoundedCornerShape(12.dp))
                                .background(MaterialTheme.colorScheme.primary.copy(alpha = 0.1f))
                                .padding(horizontal = 14.dp, vertical = 8.dp)
                        ) {
                            Text(
                                text = "${exercises.size} ejercicios",
                                style = MaterialTheme.typography.labelMedium,
                                color = MaterialTheme.colorScheme.primary
                            )
                        }
                        Box(
                            modifier = Modifier
                                .clip(RoundedCornerShape(12.dp))
                                .background(MaterialTheme.colorScheme.tertiary.copy(alpha = 0.1f))
                                .padding(horizontal = 14.dp, vertical = 8.dp)
                        ) {
                            Text(
                                text = "${muscleGroups.size} grupos",
                                style = MaterialTheme.typography.labelMedium,
                                color = MaterialTheme.colorScheme.tertiary
                            )
                        }
                    }
                }

                // Agrupar ejercicios por grupo muscular
                if (muscleGroups.isEmpty()) {
                    item { SectionHeader(title = "Todos los ejercicios") }
                    items(exercises, key = { it.id }) { exercise ->
                        ExerciseItem(
                            name = exercise.name,
                            muscleGroup = exercise.muscleGroup,
                            description = exercise.description,
                            exerciseType = exercise.exerciseType,
                            onDelete = { exerciseToDelete = exercise }
                        )
                    }
                } else {
                    // Primero ejercicios de cardio si los hay
                    val cardioExercises = exercises.filter { it.isCardio }
                    if (cardioExercises.isNotEmpty()) {
                        item {
                            SectionHeader(title = "Cardio (${cardioExercises.size})")
                        }
                        items(cardioExercises, key = { it.id }) { exercise ->
                            ExerciseItem(
                                name = exercise.name,
                                muscleGroup = exercise.muscleGroup,
                                description = exercise.description,
                                exerciseType = exercise.exerciseType,
                                onDelete = { exerciseToDelete = exercise }
                            )
                        }
                    }

                    // Luego por grupo muscular (solo strength)
                    val strengthExercises = exercises.filter { !it.isCardio }
                    muscleGroups.forEach { group ->
                        val groupExercises = strengthExercises.filter { it.muscleGroup == group }
                        if (groupExercises.isNotEmpty()) {
                            item {
                                SectionHeader(
                                    title = "$group (${groupExercises.size})"
                                )
                            }
                            items(groupExercises, key = { it.id }) { exercise ->
                                ExerciseItem(
                                    name = exercise.name,
                                    muscleGroup = exercise.muscleGroup,
                                    description = exercise.description,
                                    exerciseType = exercise.exerciseType,
                                    onDelete = { exerciseToDelete = exercise }
                                )
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
            title = "Nuevo ejercicio",
            onDismiss = {
                showCreateDialog = false
                newName = ""; newMuscle = ""; newDesc = ""
                newExerciseType = ExerciseType.STRENGTH
            },
            onConfirm = {
                val muscleOrCardio = if (newExerciseType == ExerciseType.CARDIO) {
                    if (newMuscle.isBlank()) "Cardio" else newMuscle.trim()
                } else {
                    newMuscle.trim()
                }
                if (newName.isNotBlank() && muscleOrCardio.isNotBlank()) {
                    viewModel.createExercise(
                        newName.trim(),
                        muscleOrCardio,
                        newDesc.trim(),
                        newExerciseType
                    )
                    showCreateDialog = false
                    newName = ""; newMuscle = ""; newDesc = ""
                    newExerciseType = ExerciseType.STRENGTH
                }
            }
        ) {
            Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                // Selector tipo: Musculación / Cardio
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    FilterChip(
                        selected = newExerciseType == ExerciseType.STRENGTH,
                        onClick = { newExerciseType = ExerciseType.STRENGTH },
                        label = { Text("Musculación") },
                        colors = FilterChipDefaults.filterChipColors(
                            selectedContainerColor = MaterialTheme.colorScheme.primary.copy(alpha = 0.15f)
                        ),
                        modifier = Modifier.weight(1f)
                    )
                    FilterChip(
                        selected = newExerciseType == ExerciseType.CARDIO,
                        onClick = { newExerciseType = ExerciseType.CARDIO },
                        label = { Text("Cardio") },
                        colors = FilterChipDefaults.filterChipColors(
                            selectedContainerColor = MaterialTheme.colorScheme.secondary.copy(alpha = 0.15f)
                        ),
                        modifier = Modifier.weight(1f)
                    )
                }

                OutlinedTextField(
                    value = newName,
                    onValueChange = { newName = it },
                    label = { Text("Nombre del ejercicio") },
                    placeholder = {
                        Text(
                            if (newExerciseType == ExerciseType.CARDIO)
                                "Ej: Correr, Bicicleta, Elíptica..."
                            else
                                "Ej: Press banca, Sentadilla..."
                        )
                    },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )
                OutlinedTextField(
                    value = newMuscle,
                    onValueChange = { newMuscle = it },
                    label = {
                        Text(
                            if (newExerciseType == ExerciseType.CARDIO)
                                "Categoría (opcional)"
                            else
                                "Grupo muscular"
                        )
                    },
                    placeholder = {
                        Text(
                            if (newExerciseType == ExerciseType.CARDIO)
                                "Ej: Correr, Ciclismo, Natación..."
                            else
                                "Ej: Pecho, Espalda, Pierna..."
                        )
                    },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )
                OutlinedTextField(
                    value = newDesc,
                    onValueChange = { newDesc = it },
                    label = { Text("Descripción (opcional)") },
                    placeholder = { Text("Notas o instrucciones sobre el ejercicio") },
                    modifier = Modifier.fillMaxWidth()
                )
            }
        }
    }

    exerciseToDelete?.let { exercise ->
        ConfirmDeleteDialog(
            title = "Eliminar ejercicio",
            message = "¿Eliminar \"${exercise.name}\"? Se eliminará de todas las rutinas donde esté asignado.",
            onConfirm = { viewModel.deleteExercise(exercise) },
            onDismiss = { exerciseToDelete = null }
        )
    }
}

@Composable
private fun ExerciseItem(
    name: String,
    muscleGroup: String,
    description: String,
    exerciseType: String = ExerciseType.STRENGTH.name,
    onDelete: () -> Unit
) {
    val exerciseIcon = getExerciseIcon(muscleGroup, exerciseType)
    val isCardio = exerciseType == ExerciseType.CARDIO.name

    FitnessCard(
        title = name,
        subtitle = if (description.isNotBlank()) description else null,
        icon = exerciseIcon,
        onDelete = onDelete,
        modifier = Modifier.padding(horizontal = 16.dp)
    ) {
        Row(
            horizontalArrangement = Arrangement.spacedBy(6.dp)
        ) {
            if (isCardio) {
                // Solo un badge "Cardio"
                Box(
                    modifier = Modifier
                        .clip(RoundedCornerShape(8.dp))
                        .background(MaterialTheme.colorScheme.secondary.copy(alpha = 0.1f))
                        .padding(horizontal = 10.dp, vertical = 3.dp)
                ) {
                    Text(
                        text = "Cardio",
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.secondary
                    )
                }
                // Si la categoría no es "Cardio", mostrarla también
                if (muscleGroup.lowercase() != "cardio") {
                    Box(
                        modifier = Modifier
                            .clip(RoundedCornerShape(8.dp))
                            .background(MaterialTheme.colorScheme.tertiary.copy(alpha = 0.1f))
                            .padding(horizontal = 10.dp, vertical = 3.dp)
                    ) {
                        Text(
                            text = muscleGroup,
                            style = MaterialTheme.typography.labelSmall,
                            color = MaterialTheme.colorScheme.tertiary
                        )
                    }
                }
            } else {
                // Badge grupo muscular
                Box(
                    modifier = Modifier
                        .clip(RoundedCornerShape(8.dp))
                        .background(MaterialTheme.colorScheme.tertiary.copy(alpha = 0.1f))
                        .padding(horizontal = 10.dp, vertical = 3.dp)
                ) {
                    Text(
                        text = muscleGroup,
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.tertiary
                    )
                }
            }
        }
    }
}

private fun getExerciseIcon(muscleGroup: String, exerciseType: String = ExerciseType.STRENGTH.name): ImageVector {
    if (exerciseType == ExerciseType.CARDIO.name) return Icons.Default.DirectionsRun
    val group = muscleGroup.lowercase()
    return when {
        group.contains("pecho") || group.contains("chest") -> Icons.Default.FitnessCenter
        group.contains("pierna") || group.contains("leg") -> Icons.Default.SportsMartialArts
        group.contains("espalda") || group.contains("back") -> Icons.Default.SportsGymnastics
        else -> Icons.Default.FitnessCenter
    }
}

