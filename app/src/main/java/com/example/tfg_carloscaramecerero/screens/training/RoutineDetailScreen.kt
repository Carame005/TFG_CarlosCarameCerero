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
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.DirectionsRun
import androidx.compose.material.icons.filled.FitnessCenter
import androidx.compose.material.icons.filled.History
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.Timer
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExposedDropdownMenuBox
import androidx.compose.material3.ExposedDropdownMenuDefaults
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.MenuAnchorType
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Tab
import androidx.compose.material3.TabRow
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.input.KeyboardType
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
import com.example.tfg_carloscaramecerero.navigation.Screen
import com.example.tfg_carloscaramecerero.viewmodel.TrainingViewModel
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Date
import java.util.Locale

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun RoutineDetailScreen(
    routineId: Long,
    navController: NavController,
    viewModel: TrainingViewModel
) {
    val routineWithExercises by viewModel.currentRoutine.collectAsState()
    val allExercises by viewModel.allExercises.collectAsState()
    val routineSessions by viewModel.routineSessions.collectAsState()

    var showAddExerciseDialog by remember { mutableStateOf(false) }
    var showCreateExerciseDialog by remember { mutableStateOf(false) }
    var showStartSessionDialog by remember { mutableStateOf(false) }

    // Campos para crear ejercicio
    var newExerciseName by remember { mutableStateOf("") }
    var newExerciseMuscle by remember { mutableStateOf("") }
    var newExerciseDesc by remember { mutableStateOf("") }
    var newExerciseType by remember { mutableStateOf(ExerciseType.STRENGTH) }

    // Campo para seleccionar ejercicio existente
    var selectedExercise by remember { mutableStateOf<ExerciseEntity?>(null) }
    var exerciseDropdownExpanded by remember { mutableStateOf(false) }

    // Campo para tiempo de descanso
    var restSecondsText by remember { mutableStateOf("60") }

    // Tabs: Ejercicios / Historial
    var selectedTab by remember { mutableIntStateOf(0) }

    LaunchedEffect(routineId) {
        viewModel.loadRoutine(routineId)
    }

    Scaffold(
        topBar = {
            FitnessTopBar(
                title = routineWithExercises?.routine?.name ?: "Rutina",
                onBackClick = { navController.popBackStack() }
            )
        },
        floatingActionButton = {
            if (selectedTab == 0) {
                FitnessFAB(
                    onClick = { showAddExerciseDialog = true },
                    icon = Icons.Default.Add,
                    contentDescription = "Añadir ejercicio"
                )
            }
        }
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
        ) {
            // Botón iniciar sesión
            if (routineWithExercises?.exercises?.isNotEmpty() == true) {
                Button(
                    onClick = { showStartSessionDialog = true },
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = MaterialTheme.colorScheme.primary
                    )
                ) {
                    Icon(Icons.Default.PlayArrow, contentDescription = null)
                    Spacer(modifier = Modifier.width(8.dp))
                    Text("Iniciar sesión")
                }
            }

            // Tabs
            TabRow(selectedTabIndex = selectedTab) {
                Tab(
                    selected = selectedTab == 0,
                    onClick = { selectedTab = 0 },
                    text = { Text("Ejercicios") }
                )
                Tab(
                    selected = selectedTab == 1,
                    onClick = { selectedTab = 1 },
                    text = { Text("Historial") },
                    icon = { Icon(Icons.Default.History, contentDescription = null) }
                )
            }

            when (selectedTab) {
                0 -> ExercisesTab(
                    exercises = routineWithExercises?.exercises ?: emptyList(),
                    routineId = routineId,
                    viewModel = viewModel
                )
                1 -> SessionHistoryTab(
                    sessions = routineSessions,
                    allExercises = allExercises,
                    routineExercises = routineWithExercises?.exercises ?: emptyList(),
                    navController = navController,
                    viewModel = viewModel
                )
            }
        }
    }

    // Dialog para iniciar sesión con tiempo de descanso
    if (showStartSessionDialog) {
        FitnessInputDialog(
            title = "Iniciar sesión",
            onDismiss = {
                showStartSessionDialog = false
                restSecondsText = "60"
            },
            onConfirm = {
                val restSeconds = restSecondsText.toIntOrNull() ?: 60
                viewModel.startSession(routineId, restSeconds) { sessionId ->
                    navController.navigate(Screen.SessionDetail.createRoute(sessionId))
                }
                showStartSessionDialog = false
                restSecondsText = "60"
            }
        ) {
            Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                Text(
                    "Configura el tiempo de descanso entre ejercicios:",
                    style = MaterialTheme.typography.bodyMedium
                )
                OutlinedTextField(
                    value = restSecondsText,
                    onValueChange = { restSecondsText = it },
                    label = { Text("Descanso (segundos)") },
                    placeholder = { Text("60") },
                    leadingIcon = { Icon(Icons.Default.Timer, contentDescription = null) },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )
                Text(
                    text = formatRestTime(restSecondsText.toIntOrNull() ?: 0),
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }
    }

    // Dialog para añadir ejercicio existente
    if (showAddExerciseDialog) {
        FitnessInputDialog(
            title = "Añadir ejercicio",
            onDismiss = {
                showAddExerciseDialog = false
                selectedExercise = null
            },
            onConfirm = {
                selectedExercise?.let {
                    viewModel.addExerciseToRoutine(routineId, it.id)
                }
                showAddExerciseDialog = false
                selectedExercise = null
            }
        ) {
            Column {
                if (allExercises.isEmpty()) {
                    Text(
                        "No hay ejercicios creados.",
                        style = MaterialTheme.typography.bodyMedium
                    )
                } else {
                    ExposedDropdownMenuBox(
                        expanded = exerciseDropdownExpanded,
                        onExpandedChange = { exerciseDropdownExpanded = it }
                    ) {
                        OutlinedTextField(
                            value = selectedExercise?.let {
                                "${it.name} (${if (it.isCardio) "Cardio" else it.muscleGroup})"
                            } ?: "",
                            onValueChange = {},
                            readOnly = true,
                            label = { Text("Seleccionar ejercicio") },
                            trailingIcon = {
                                ExposedDropdownMenuDefaults.TrailingIcon(expanded = exerciseDropdownExpanded)
                            },
                            modifier = Modifier
                                .menuAnchor(MenuAnchorType.PrimaryNotEditable)
                                .fillMaxWidth()
                        )
                        ExposedDropdownMenu(
                            expanded = exerciseDropdownExpanded,
                            onDismissRequest = { exerciseDropdownExpanded = false }
                        ) {
                            allExercises.forEach { exercise ->
                                DropdownMenuItem(
                                    text = {
                                        Row(
                                            verticalAlignment = Alignment.CenterVertically,
                                            horizontalArrangement = Arrangement.spacedBy(6.dp)
                                        ) {
                                            Icon(
                                                if (exercise.isCardio) Icons.Default.DirectionsRun
                                                else Icons.Default.FitnessCenter,
                                                contentDescription = null,
                                                modifier = Modifier.padding(end = 4.dp),
                                                tint = MaterialTheme.colorScheme.onSurfaceVariant
                                            )
                                            Text("${exercise.name} (${if (exercise.isCardio) "Cardio" else exercise.muscleGroup})")
                                        }
                                    },
                                    onClick = {
                                        selectedExercise = exercise
                                        exerciseDropdownExpanded = false
                                    }
                                )
                            }
                        }
                    }
                }

                Spacer(modifier = Modifier.height(12.dp))

                Button(
                    onClick = {
                        showAddExerciseDialog = false
                        showCreateExerciseDialog = true
                    },
                    modifier = Modifier.align(Alignment.CenterHorizontally)
                ) {
                    Icon(Icons.Default.Add, contentDescription = null)
                    Spacer(modifier = Modifier.width(4.dp))
                    Text("Crear nuevo ejercicio")
                }
            }
        }
    }

    // Dialog para crear ejercicio nuevo
    if (showCreateExerciseDialog) {
        FitnessInputDialog(
            title = "Nuevo ejercicio",
            onDismiss = {
                showCreateExerciseDialog = false
                newExerciseName = ""
                newExerciseMuscle = ""
                newExerciseDesc = ""
                newExerciseType = ExerciseType.STRENGTH
            },
            onConfirm = {
                val muscleOrCardio = if (newExerciseType == ExerciseType.CARDIO) {
                    if (newExerciseMuscle.isBlank()) "Cardio" else newExerciseMuscle.trim()
                } else {
                    newExerciseMuscle.trim()
                }
                if (newExerciseName.isNotBlank() && muscleOrCardio.isNotBlank()) {
                    viewModel.createExercise(
                        newExerciseName.trim(),
                        muscleOrCardio,
                        newExerciseDesc.trim(),
                        newExerciseType
                    )
                    showCreateExerciseDialog = false
                    newExerciseName = ""
                    newExerciseMuscle = ""
                    newExerciseDesc = ""
                    newExerciseType = ExerciseType.STRENGTH
                }
            }
        ) {
            Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                // Selector tipo
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
                    value = newExerciseName,
                    onValueChange = { newExerciseName = it },
                    label = { Text("Nombre") },
                    placeholder = {
                        Text(
                            if (newExerciseType == ExerciseType.CARDIO)
                                "Ej: Correr, Bicicleta..."
                            else
                                "Ej: Press banca, Sentadilla..."
                        )
                    },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )
                OutlinedTextField(
                    value = newExerciseMuscle,
                    onValueChange = { newExerciseMuscle = it },
                    label = {
                        Text(
                            if (newExerciseType == ExerciseType.CARDIO)
                                "Categoría (opcional)"
                            else
                                "Grupo muscular"
                        )
                    },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )
                OutlinedTextField(
                    value = newExerciseDesc,
                    onValueChange = { newExerciseDesc = it },
                    label = { Text("Descripción (opcional)") },
                    modifier = Modifier.fillMaxWidth()
                )
            }
        }
    }
}

@Composable
private fun ExercisesTab(
    exercises: List<ExerciseEntity>,
    routineId: Long,
    viewModel: TrainingViewModel
) {
    var exerciseToDelete by remember { mutableStateOf<ExerciseEntity?>(null) }

    if (exercises.isEmpty()) {
        EmptyStateMessage(
            message = "No hay ejercicios.\nPulsa + para añadir uno.",
            modifier = Modifier.fillMaxSize()
        )
    } else {
        val strengthExercises = exercises.filter { !it.isCardio }
        val cardioExercises = exercises.filter { it.isCardio }

        LazyColumn(
            modifier = Modifier.fillMaxSize(),
            verticalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            // Sección musculación
            if (strengthExercises.isNotEmpty()) {
                item { SectionHeader(title = "Musculación (${strengthExercises.size})") }
                items(strengthExercises, key = { it.id }) { exercise ->
                    FitnessCard(
                        title = exercise.name,
                        subtitle = if (exercise.description.isNotBlank()) exercise.description else null,
                        icon = Icons.Default.FitnessCenter,
                        onDelete = { exerciseToDelete = exercise },
                        modifier = Modifier.padding(horizontal = 16.dp)
                    ) {
                        Box(
                            modifier = Modifier
                                .clip(RoundedCornerShape(8.dp))
                                .background(MaterialTheme.colorScheme.tertiary.copy(alpha = 0.1f))
                                .padding(horizontal = 10.dp, vertical = 3.dp)
                        ) {
                            Text(
                                text = exercise.muscleGroup,
                                style = MaterialTheme.typography.labelSmall,
                                color = MaterialTheme.colorScheme.tertiary
                            )
                        }
                    }
                }
            }

            // Sección cardio
            if (cardioExercises.isNotEmpty()) {
                item { SectionHeader(title = "Cardio (${cardioExercises.size})") }
                items(cardioExercises, key = { it.id }) { exercise ->
                    FitnessCard(
                        title = exercise.name,
                        subtitle = if (exercise.description.isNotBlank()) exercise.description else null,
                        icon = Icons.Default.DirectionsRun,
                        accentColor = MaterialTheme.colorScheme.secondary,
                        onDelete = { exerciseToDelete = exercise },
                        modifier = Modifier.padding(horizontal = 16.dp)
                    ) {
                        Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
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
                            if (exercise.muscleGroup.lowercase() != "cardio") {
                                Box(
                                    modifier = Modifier
                                        .clip(RoundedCornerShape(8.dp))
                                        .background(MaterialTheme.colorScheme.tertiary.copy(alpha = 0.1f))
                                        .padding(horizontal = 10.dp, vertical = 3.dp)
                                ) {
                                    Text(
                                        text = exercise.muscleGroup,
                                        style = MaterialTheme.typography.labelSmall,
                                        color = MaterialTheme.colorScheme.tertiary
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

    exerciseToDelete?.let { exercise ->
        ConfirmDeleteDialog(
            title = "Quitar ejercicio",
            message = "¿Quitar \"${exercise.name}\" de esta rutina?",
            onConfirm = { viewModel.removeExerciseFromRoutine(routineId, exercise.id) },
            onDismiss = { exerciseToDelete = null }
        )
    }
}

@Composable
private fun SessionHistoryTab(
    sessions: List<com.example.tfg_carloscaramecerero.data.local.relation.SessionWithSets>,
    allExercises: List<ExerciseEntity>,
    routineExercises: List<ExerciseEntity>,
    navController: NavController,
    viewModel: TrainingViewModel
) {
    val exerciseMap = (routineExercises + allExercises).distinctBy { it.id }.associateBy { it.id }
    val dateFormat = remember { SimpleDateFormat("EEEE, d 'de' MMMM yyyy", Locale("es", "ES")) }
    val timeFormat = remember { SimpleDateFormat("HH:mm", Locale("es", "ES")) }
    var sessionToDelete by remember {
        mutableStateOf<com.example.tfg_carloscaramecerero.data.local.entity.TrainingSessionEntity?>(null)
    }

    if (sessions.isEmpty()) {
        EmptyStateMessage(
            message = "No hay sesiones registradas.\nInicia una sesión para verla aquí.",
            icon = Icons.Default.History,
            modifier = Modifier.fillMaxSize()
        )
    } else {
        // Agrupar sesiones por día
        val groupedByDay = sessions.groupBy { session ->
            val cal = Calendar.getInstance().apply { timeInMillis = session.session.date }
            cal.set(Calendar.HOUR_OF_DAY, 0)
            cal.set(Calendar.MINUTE, 0)
            cal.set(Calendar.SECOND, 0)
            cal.set(Calendar.MILLISECOND, 0)
            cal.timeInMillis
        }.toSortedMap(compareByDescending { it })

        LazyColumn(
            modifier = Modifier.fillMaxSize(),
            verticalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            groupedByDay.forEach { (dayMillis, daySessions) ->
                val dayLabel = dateFormat.format(Date(dayMillis)).replaceFirstChar { it.uppercase() }
                item {
                    SectionHeader(title = dayLabel)
                }
                items(daySessions, key = { it.session.id }) { sessionWithSets ->
                    val session = sessionWithSets.session
                    val sets = sessionWithSets.sets
                    val exerciseNames = sets
                        .map { it.exerciseId }
                        .distinct()
                        .mapNotNull { exerciseMap[it]?.name }

                    FitnessCard(
                        title = "Sesión — ${timeFormat.format(Date(session.date))}",
                        subtitle = "${sets.size} sets · ${exerciseNames.size} ejercicios · Descanso: ${formatRestTime(session.restSeconds)}",
                        icon = Icons.Default.History,
                        onClick = {
                            navController.navigate(Screen.SessionDetail.createRoute(session.id))
                        },
                        onDelete = { sessionToDelete = session },
                        modifier = Modifier.padding(horizontal = 16.dp)
                    ) {
                        if (exerciseNames.isNotEmpty()) {
                            Text(
                                text = exerciseNames.joinToString(" · "),
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant,
                                maxLines = 2,
                                overflow = TextOverflow.Ellipsis
                            )
                        }
                    }
                }
            }
            item { Spacer(modifier = Modifier.height(80.dp)) }
        }
    }

    sessionToDelete?.let { session ->
        ConfirmDeleteDialog(
            title = "Eliminar sesión",
            message = "¿Eliminar esta sesión y todos sus sets? Esta acción no se puede deshacer.",
            onConfirm = { viewModel.deleteSession(session) },
            onDismiss = { sessionToDelete = null }
        )
    }
}

private fun formatRestTime(seconds: Int): String {
    return when {
        seconds < 60 -> "${seconds}s"
        seconds % 60 == 0 -> "${seconds / 60}min"
        else -> "${seconds / 60}min ${seconds % 60}s"
    }
}

