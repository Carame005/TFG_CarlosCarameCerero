package com.example.tfg_carloscaramecerero.screens.training

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.slideInVertically
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.SuggestionChip
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.DirectionsRun
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.DirectionsRun
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.FitnessCenter
import androidx.compose.material.icons.filled.History
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.Timer
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Checkbox
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExposedDropdownMenuBox
import androidx.compose.material3.ExposedDropdownMenuDefaults
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
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
import androidx.compose.ui.graphics.Color
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
import com.example.tfg_carloscaramecerero.data.local.entity.TrainingSessionEntity
import com.example.tfg_carloscaramecerero.data.local.relation.SessionWithSets
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

    // Campo para tiempo de descanso
    var restSecondsText by remember { mutableStateOf("60") }

    // Tabs: Ejercicios / Historial
    var selectedTab by remember { mutableIntStateOf(0) }

    // Estado para editar rutina
    var showEditRoutineDialog by remember { mutableStateOf(false) }
    var editRoutineName by remember { mutableStateOf(routineWithExercises?.routine?.name ?: "") }
    var editRoutineDesc by remember { mutableStateOf(routineWithExercises?.routine?.description ?: "") }

    // Estado para editar ejercicio
    var exerciseToEdit by remember { mutableStateOf<ExerciseEntity?>(null) }
    var editExerciseName by remember { mutableStateOf("") }
    var editExerciseMuscle by remember { mutableStateOf("") }
    var editExerciseDesc by remember { mutableStateOf("") }
    var editExerciseType by remember { mutableStateOf(ExerciseType.STRENGTH) }

    // Cuando se selecciona un ejercicio para editar, inicializar correctamente el tipo
    if (exerciseToEdit != null) {
        LaunchedEffect(exerciseToEdit) {
            editExerciseName = exerciseToEdit?.name ?: ""
            editExerciseMuscle = exerciseToEdit?.muscleGroup ?: ""
            editExerciseDesc = exerciseToEdit?.description ?: ""
            editExerciseType = exerciseToEdit?.exerciseType?.let { ExerciseType.valueOf(it) } ?: ExerciseType.STRENGTH
        }
    }

    LaunchedEffect(routineId) {
        viewModel.loadRoutine(routineId)
    }

    Scaffold(
        topBar = {
            FitnessTopBar(
                title = routineWithExercises?.routine?.name ?: "Rutina",
                onBackClick = { navController.popBackStack() },
                actions = {
                    IconButton(onClick = {
                        editRoutineName = routineWithExercises?.routine?.name ?: ""
                        editRoutineDesc = routineWithExercises?.routine?.description ?: ""
                        showEditRoutineDialog = true
                    }) {
                        Icon(Icons.Default.Edit, contentDescription = "Editar rutina")
                    }
                }
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
        // Filtrar ejercicios ya añadidos
        val ejerciciosYaEnRutina = routineWithExercises?.exercises?.map { it.id } ?: emptyList()
        val availableExercises = allExercises.filter { it.id !in ejerciciosYaEnRutina }
        var selectedExercises by remember { mutableStateOf(setOf<Long>()) }
        FitnessInputDialog(
            title = "Añadir ejercicios",
            onDismiss = {
                showAddExerciseDialog = false
                selectedExercises = emptySet()
            },
            onConfirm = {
                selectedExercises.forEach { id ->
                    viewModel.addExerciseToRoutine(routineId, id)
                }
                showAddExerciseDialog = false
                selectedExercises = emptySet()
            }
        ) {
            Column {
                if (availableExercises.isEmpty()) {
                    Text(
                        "No hay ejercicios disponibles para añadir.",
                        style = MaterialTheme.typography.bodyMedium
                    )
                } else {
                    LazyColumn(modifier = Modifier.heightIn(max = 300.dp)) {
                        items(availableExercises) { exercise ->
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(vertical = 4.dp)
                                    .background(
                                        if (selectedExercises.contains(exercise.id)) MaterialTheme.colorScheme.primary.copy(alpha = 0.08f) else Color.Transparent
                                    )
                                    .clip(RoundedCornerShape(8.dp))
                                    .padding(horizontal = 8.dp)
                            ) {
                                Checkbox(
                                    checked = selectedExercises.contains(exercise.id),
                                    onCheckedChange = { checked ->
                                        selectedExercises = if (checked) selectedExercises + exercise.id else selectedExercises - exercise.id
                                    }
                                )
                                Text(
                                    "${exercise.name} (${if (exercise.isCardio) "Cardio" else exercise.muscleGroup})",
                                    modifier = Modifier.weight(1f)
                                )
                                IconButton(onClick = {
                                    // Editar ejercicio desde el diálogo
                                    editExerciseName = exercise.name
                                    editExerciseMuscle = exercise.muscleGroup
                                    editExerciseDesc = exercise.description
                                    editExerciseType = ExerciseType.valueOf(exercise.exerciseType)
                                    exerciseToEdit = exercise
                                }) {
                                    Icon(Icons.Default.Edit, contentDescription = "Editar ejercicio")
                                }
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

    // Diálogo para editar rutina
    if (showEditRoutineDialog && routineWithExercises != null) {
        FitnessInputDialog(
            title = "Editar rutina",
            onDismiss = { showEditRoutineDialog = false },
            onConfirm = {
                val updated = routineWithExercises!!.routine.copy(
                    name = editRoutineName.trim(),
                    description = editRoutineDesc.trim()
                )
                viewModel.updateRoutine(updated)
                showEditRoutineDialog = false
            }
        ) {
            Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                OutlinedTextField(
                    value = editRoutineName,
                    onValueChange = { editRoutineName = it },
                    label = { Text("Nombre de la rutina") },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )
                OutlinedTextField(
                    value = editRoutineDesc,
                    onValueChange = { editRoutineDesc = it },
                    label = { Text("Descripción") },
                    modifier = Modifier.fillMaxWidth()
                )
            }
        }
    }

    // Diálogo para editar ejercicio
    if (exerciseToEdit != null) {
        FitnessInputDialog(
            title = "Editar ejercicio",
            onDismiss = { exerciseToEdit = null },
            onConfirm = {
                exerciseToEdit?.let {
                    val updated = it.copy(
                        name = editExerciseName.trim(),
                        muscleGroup = editExerciseMuscle.trim(),
                        description = editExerciseDesc.trim(),
                        exerciseType = editExerciseType.name // CORRECTO: guardar como String
                    )
                    viewModel.updateExercise(updated)
                }
                exerciseToEdit = null
            }
        ) {
            Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                OutlinedTextField(
                    value = editExerciseName,
                    onValueChange = { editExerciseName = it },
                    label = { Text("Nombre") },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )
                OutlinedTextField(
                    value = editExerciseMuscle,
                    onValueChange = { editExerciseMuscle = it },
                    label = { Text("Grupo muscular/Categoría") },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )
                OutlinedTextField(
                    value = editExerciseDesc,
                    onValueChange = { editExerciseDesc = it },
                    label = { Text("Descripción") },
                    modifier = Modifier.fillMaxWidth()
                )
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    FilterChip(
                        selected = editExerciseType == ExerciseType.STRENGTH,
                        onClick = { editExerciseType = ExerciseType.STRENGTH },
                        label = { Text("Musculación") },
                        modifier = Modifier.weight(1f)
                    )
                    FilterChip(
                        selected = editExerciseType == ExerciseType.CARDIO,
                        onClick = { editExerciseType = ExerciseType.CARDIO },
                        label = { Text("Cardio") },
                        modifier = Modifier.weight(1f)
                    )
                }
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
                    AnimatedVisibility(
                        visible = true,
                        enter = fadeIn() + slideInVertically(initialOffsetY = { it / 2 })
                    ) {
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
            }

            // Sección cardio
            if (cardioExercises.isNotEmpty()) {
                item { SectionHeader(title = "Cardio (${cardioExercises.size})") }
                items(cardioExercises, key = { it.id }) { exercise ->
                    AnimatedVisibility(
                        visible = true,
                        enter = fadeIn() + slideInVertically(initialOffsetY = { it / 2 })
                    ) {
                        FitnessCard(
                            title = exercise.name,
                            subtitle = if (exercise.description.isNotBlank()) exercise.description else null,
                            icon = Icons.AutoMirrored.Filled.DirectionsRun,
                            onDelete = { exerciseToDelete = exercise },
                            modifier = Modifier.padding(horizontal = 16.dp)
                        ) {
                            // Sin grupo muscular para ejercicios de cardio
                        }
                    }
                }
            }
        }
    }

    // Diálogo de confirmación para eliminar ejercicio
    if (exerciseToDelete != null) {
        ConfirmDeleteDialog(
            onDismiss = { exerciseToDelete = null },
            onConfirm = {
                exerciseToDelete?.let { viewModel.removeExerciseFromRoutine(routineId, it.id) }
                exerciseToDelete = null
            },
            message = "¿Eliminar este ejercicio de la rutina?"
        )
    }
}

// Definir formatRestTime si no existe
private fun formatRestTime(seconds: Int): String {
    return when {
        seconds < 60 -> "${seconds}s"
        seconds % 60 == 0 -> "${seconds / 60}min"
        else -> "${seconds / 60}min ${seconds % 60}s"
    }
}

// ── Historial de sesiones ──────────────────────────────────────────────────────
@Composable
private fun SessionHistoryTab(
    sessions: List<SessionWithSets>,
    allExercises: List<ExerciseEntity>,
    routineExercises: List<ExerciseEntity>,
    navController: NavController,
    viewModel: TrainingViewModel
) {
    var sessionToDelete by remember { mutableStateOf<TrainingSessionEntity?>(null) }
    val dateFormat = remember { SimpleDateFormat("dd/MM/yyyy · HH:mm", Locale("es", "ES")) }

    if (sessions.isEmpty()) {
        EmptyStateMessage(
            message = "No hay sesiones registradas.\nPulsa \"Iniciar sesión\" para empezar.",
            modifier = Modifier.fillMaxSize()
        )
    } else {
        LazyColumn(
            modifier = Modifier.fillMaxSize(),
            verticalArrangement = Arrangement.spacedBy(10.dp),
            contentPadding = androidx.compose.foundation.layout.PaddingValues(
                horizontal = 16.dp, vertical = 12.dp
            )
        ) {
            item {
                SectionHeader(
                    title = "${sessions.size} sesión${if (sessions.size != 1) "es" else ""} registrada${if (sessions.size != 1) "s" else ""}"
                )
            }
            items(sessions, key = { it.session.id }) { sessionWithSets ->
                val session = sessionWithSets.session
                val sets = sessionWithSets.sets
                val exercisesInSession = sets.map { it.exerciseId }.distinct()
                    .mapNotNull { id -> allExercises.find { it.id == id } }

                val subtitle = buildString {
                    if (session.durationMinutes > 0) append("${session.durationMinutes} min · ")
                    append("${sets.size} serie${if (sets.size != 1) "s" else ""}")
                    if (exercisesInSession.isNotEmpty()) {
                        append(" · ${exercisesInSession.size} ejercicio${if (exercisesInSession.size != 1) "s" else ""}")
                    }
                    if (!session.notes.isNullOrBlank()) append("\n${session.notes}")
                }

                FitnessCard(
                    title = dateFormat.format(Date(session.date)),
                    subtitle = subtitle.ifBlank { null },
                    icon = Icons.Default.History,
                    onClick = {
                        navController.navigate(Screen.SessionDetail.createRoute(session.id))
                    },
                    onDelete = { sessionToDelete = session }
                ) {
                    // Chips con los ejercicios realizados en esta sesión
                    if (exercisesInSession.isNotEmpty()) {
                        Row(
                            horizontalArrangement = Arrangement.spacedBy(4.dp),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            exercisesInSession.take(3).forEach { exercise ->
                                SuggestionChip(
                                    onClick = {},
                                    label = {
                                        Text(
                                            text = exercise.name,
                                            style = MaterialTheme.typography.labelSmall,
                                            maxLines = 1,
                                            overflow = TextOverflow.Ellipsis
                                        )
                                    }
                                )
                            }
                            if (exercisesInSession.size > 3) {
                                SuggestionChip(
                                    onClick = {},
                                    label = {
                                        Text(
                                            text = "+${exercisesInSession.size - 3} más",
                                            style = MaterialTheme.typography.labelSmall
                                        )
                                    }
                                )
                            }
                        }
                    }
                }
            }
        }
    }

    if (sessionToDelete != null) {
        ConfirmDeleteDialog(
            message = "¿Eliminar esta sesión? Se perderán todas las series registradas.",
            onDismiss = { sessionToDelete = null },
            onConfirm = {
                sessionToDelete?.let { viewModel.deleteSession(it) }
                sessionToDelete = null
            }
        )
    }
}
