package com.example.tfg_carloscaramecerero.screens.training

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.animation.expandVertically
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.shrinkVertically
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
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.DirectionsRun
import androidx.compose.material.icons.filled.FitnessCenter
import androidx.compose.material.icons.filled.Repeat
import androidx.compose.material.icons.filled.Route
import androidx.compose.material.icons.filled.Scale
import androidx.compose.material.icons.filled.Timer
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExposedDropdownMenuBox
import androidx.compose.material3.ExposedDropdownMenuDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.MenuAnchorType
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
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
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import com.example.tfg_carloscaramecerero.components.ConfirmDeleteDialog
import com.example.tfg_carloscaramecerero.components.EmptyStateMessage
import com.example.tfg_carloscaramecerero.components.FitnessCard
import com.example.tfg_carloscaramecerero.components.FitnessFAB
import com.example.tfg_carloscaramecerero.components.FitnessInputDialog
import com.example.tfg_carloscaramecerero.components.FitnessTopBar
import com.example.tfg_carloscaramecerero.components.SectionHeader
import com.example.tfg_carloscaramecerero.components.StatCard
import com.example.tfg_carloscaramecerero.data.local.entity.ExerciseEntity
import com.example.tfg_carloscaramecerero.data.local.entity.TrainingSetEntity
import com.example.tfg_carloscaramecerero.viewmodel.TrainingViewModel
import kotlinx.coroutines.delay

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SessionDetailScreen(
    sessionId: Long,
    navController: NavController,
    viewModel: TrainingViewModel
) {
    val sessionWithSets by viewModel.currentSession.collectAsState()
    val allExercises by viewModel.allExercises.collectAsState()
    val routineExercises by viewModel.sessionRoutineExercises.collectAsState()
    val routineName by viewModel.sessionRoutineName.collectAsState()
    var showAddSetDialog by remember { mutableStateOf(false) }
    var setToDelete by remember { mutableStateOf<TrainingSetEntity?>(null) }

    var repsText by remember { mutableStateOf("") }
    var weightText by remember { mutableStateOf("") }
    var durationMinText by remember { mutableStateOf("") }
    var durationSecText by remember { mutableStateOf("") }
    var distanceText by remember { mutableStateOf("") }
    var selectedExercise by remember { mutableStateOf<ExerciseEntity?>(null) }
    var exerciseDropdownExpanded by remember { mutableStateOf(false) }

    // ── Rest Timer ──
    val restSeconds = sessionWithSets?.session?.restSeconds ?: 60
    var timerRunning by remember { mutableStateOf(false) }
    var timerSecondsLeft by remember { mutableIntStateOf(restSeconds) }
    var totalTimerSeconds by remember { mutableIntStateOf(restSeconds) }

    LaunchedEffect(timerRunning) {
        if (timerRunning) {
            while (timerSecondsLeft > 0 && timerRunning) {
                delay(1000L)
                timerSecondsLeft--
            }
            if (timerSecondsLeft == 0) timerRunning = false
        }
    }

    fun startRestTimer() {
        totalTimerSeconds = restSeconds
        timerSecondsLeft = restSeconds
        timerRunning = true
    }

    LaunchedEffect(sessionId) {
        viewModel.loadSession(sessionId)
    }

    val sets = sessionWithSets?.sets ?: emptyList()
    val exerciseMap = (routineExercises + allExercises).distinctBy { it.id }.associateBy { it.id }
    val selectableExercises = routineExercises.ifEmpty { allExercises }

    Scaffold(
        topBar = {
            FitnessTopBar(
                title = routineName ?: "Sesión de entrenamiento",
                onBackClick = { navController.popBackStack() }
            )
        },
        floatingActionButton = {
            FitnessFAB(
                onClick = { showAddSetDialog = true },
                icon = Icons.Default.Add,
                contentDescription = "Añadir set"
            )
        }
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
        ) {
            // Stats
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 12.dp, vertical = 8.dp),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                StatCard(
                    label = "Sets",
                    value = "${sets.size}",
                    modifier = Modifier.weight(1f),
                    icon = Icons.Default.Repeat
                )
                StatCard(
                    label = "Ejercicios",
                    value = "${sets.map { it.exerciseId }.distinct().size}",
                    modifier = Modifier.weight(1f),
                    icon = Icons.Default.FitnessCenter
                )
                val restSecs = sessionWithSets?.session?.restSeconds ?: 0
                StatCard(
                    label = "Descanso",
                    value = if (restSecs < 60) "${restSecs}s"
                            else if (restSecs % 60 == 0) "${restSecs / 60}min"
                            else "${restSecs / 60}m${restSecs % 60}s",
                    modifier = Modifier.weight(1f),
                    icon = Icons.Default.Timer
                )
            }

            // ── Rest Timer Banner ──
            AnimatedVisibility(
                visible = timerRunning || timerSecondsLeft < (totalTimerSeconds.takeIf { it > 0 } ?: 1),
                enter = expandVertically() + fadeIn(),
                exit = shrinkVertically() + fadeOut()
            ) {
                val progress = if (totalTimerSeconds > 0) timerSecondsLeft.toFloat() / totalTimerSeconds else 0f
                val animatedProgress by animateFloatAsState(
                    targetValue = progress,
                    animationSpec = tween(500),
                    label = "rest_timer"
                )
                val timerColor = when {
                    progress > 0.5f -> MaterialTheme.colorScheme.primary
                    progress > 0.25f -> MaterialTheme.colorScheme.secondary
                    else -> MaterialTheme.colorScheme.error
                }
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .background(timerColor.copy(alpha = 0.08f))
                        .padding(horizontal = 16.dp, vertical = 10.dp)
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            Box(
                                modifier = Modifier
                                    .size(32.dp)
                                    .clip(CircleShape)
                                    .background(timerColor.copy(alpha = 0.15f)),
                                contentAlignment = Alignment.Center
                            ) {
                                Icon(
                                    Icons.Default.Timer,
                                    contentDescription = null,
                                    tint = timerColor,
                                    modifier = Modifier.size(18.dp)
                                )
                            }
                            Column {
                                Text(
                                    if (timerRunning) "Descansando..." else "¡Listo para el siguiente set!",
                                    style = MaterialTheme.typography.labelMedium.copy(
                                        fontWeight = FontWeight.SemiBold
                                    ),
                                    color = timerColor
                                )
                                Text(
                                    formatDuration(timerSecondsLeft),
                                    style = MaterialTheme.typography.headlineSmall.copy(
                                        fontWeight = FontWeight.Bold
                                    ),
                                    color = timerColor
                                )
                            }
                        }
                        Row(horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                            TextButton(
                                onClick = { startRestTimer() }
                            ) { Text("↺ Reset") }
                            IconButton(
                                onClick = {
                                    timerRunning = false
                                    timerSecondsLeft = 0
                                }
                            ) {
                                Icon(Icons.Default.Close, contentDescription = "Cerrar timer", tint = timerColor)
                            }
                        }
                    }
                    Spacer(Modifier.height(6.dp))
                    LinearProgressIndicator(
                        progress = { animatedProgress },
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(4.dp)),
                        color = timerColor,
                        trackColor = timerColor.copy(alpha = 0.2f),
                        strokeCap = StrokeCap.Round
                    )
                }
            }

            if (sets.isEmpty()) {
                EmptyStateMessage(
                    message = "No hay sets registrados.\nPulsa + para añadir uno.",
                    icon = Icons.Default.FitnessCenter,
                    modifier = Modifier.weight(1f)
                )
            } else {
                LazyColumn(
                    modifier = Modifier.weight(1f),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    // Agrupar por ejercicio
                    val grouped = sets.groupBy { it.exerciseId }
                    grouped.forEach { (exerciseId, exerciseSets) ->
                        val exercise = exerciseMap[exerciseId]
                        val exerciseName = exercise?.name ?: "Ejercicio #$exerciseId"
                        val isCardioGroup = exerciseSets.firstOrNull()?.isCardio == true
                        item {
                            SectionHeader(
                                title = if (isCardioGroup) "Cardio · $exerciseName" else exerciseName
                            )
                        }
                        items(exerciseSets, key = { it.id }) { set ->
                            if (set.isCardio) {
                                CardioSetCard(
                                    setNumber = set.setNumber,
                                    durationSeconds = set.durationSeconds,
                                    distanceKm = set.distanceKm,
                                    onDelete = { setToDelete = set },
                                    modifier = Modifier.padding(horizontal = 16.dp)
                                )
                            } else {
                                SetCard(
                                    setNumber = set.setNumber,
                                    reps = set.reps,
                                    weight = set.weight,
                                    onDelete = { setToDelete = set },
                                    modifier = Modifier.padding(horizontal = 16.dp)
                                )
                            }
                        }
                    }
                    item { Spacer(modifier = Modifier.height(80.dp)) }
                }
            }
        }
    }

    if (showAddSetDialog) {
        val isCardioExercise = selectedExercise?.isCardio == true

        FitnessInputDialog(
            title = if (isCardioExercise) "Añadir set de cardio" else "Añadir set",
            onDismiss = {
                showAddSetDialog = false
                repsText = ""
                weightText = ""
                durationMinText = ""
                durationSecText = ""
                distanceText = ""
                selectedExercise = null
            },
            onConfirm = {
                if (selectedExercise != null) {
                    val nextSetNumber = sets.count { it.exerciseId == selectedExercise!!.id } + 1
                    if (isCardioExercise) {
                        val mins = durationMinText.toIntOrNull() ?: 0
                        val secs = durationSecText.toIntOrNull() ?: 0
                        val totalSeconds = mins * 60 + secs
                        val distance = distanceText.toDoubleOrNull() ?: 0.0
                        if (totalSeconds > 0) {
                            viewModel.addCardioSet(
                                sessionId = sessionId,
                                exerciseId = selectedExercise!!.id,
                                setNumber = nextSetNumber,
                                durationSeconds = totalSeconds,
                                distanceKm = distance
                            )
                            startRestTimer()
                            showAddSetDialog = false
                            durationMinText = ""
                            durationSecText = ""
                            distanceText = ""
                            selectedExercise = null
                        }
                    } else {
                        val reps = repsText.toIntOrNull()
                        val weight = weightText.toDoubleOrNull()
                        if (reps != null && weight != null) {
                            viewModel.addSet(
                                sessionId = sessionId,
                                exerciseId = selectedExercise!!.id,
                                setNumber = nextSetNumber,
                                reps = reps,
                                weight = weight
                            )
                            startRestTimer()
                            showAddSetDialog = false
                            repsText = ""
                            weightText = ""
                            selectedExercise = null
                        }
                    }
                }
            }
        ) {
            Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                // Selector de ejercicio
                if (selectableExercises.isEmpty()) {
                    Text(
                        "No hay ejercicios en esta rutina. Añade ejercicios a la rutina primero.",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.error
                    )
                } else {
                    ExposedDropdownMenuBox(
                        expanded = exerciseDropdownExpanded,
                        onExpandedChange = { exerciseDropdownExpanded = it }
                    ) {
                        OutlinedTextField(
                            value = selectedExercise?.let { "${it.name} (${if (it.isCardio) "Cardio" else it.muscleGroup})" } ?: "",
                            onValueChange = {},
                            readOnly = true,
                            label = { Text("Ejercicio") },
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
                            selectableExercises.forEach { exercise ->
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
                                                modifier = Modifier.size(16.dp),
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

                // Campos según tipo de ejercicio
                if (isCardioExercise) {
                    // Campos para cardio: duración y distancia
                    Text(
                        "Duración",
                        style = MaterialTheme.typography.labelMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        OutlinedTextField(
                            value = durationMinText,
                            onValueChange = { durationMinText = it },
                            label = { Text("Min") },
                            leadingIcon = {
                                Icon(
                                    Icons.Default.Timer,
                                    contentDescription = null,
                                    modifier = Modifier.size(18.dp)
                                )
                            },
                            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                            singleLine = true,
                            modifier = Modifier.weight(1f)
                        )
                        OutlinedTextField(
                            value = durationSecText,
                            onValueChange = { durationSecText = it },
                            label = { Text("Seg") },
                            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                            singleLine = true,
                            modifier = Modifier.weight(1f)
                        )
                    }
                    OutlinedTextField(
                        value = distanceText,
                        onValueChange = { distanceText = it },
                        label = { Text("Distancia (km) — opcional") },
                        leadingIcon = {
                            Icon(
                                Icons.Default.Route,
                                contentDescription = null,
                                modifier = Modifier.size(18.dp)
                            )
                        },
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                        singleLine = true,
                        modifier = Modifier.fillMaxWidth()
                    )
                } else {
                    // Campos para musculación: reps y peso
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        OutlinedTextField(
                            value = repsText,
                            onValueChange = { repsText = it },
                            label = { Text("Reps") },
                            leadingIcon = {
                                Icon(
                                    Icons.Default.Repeat,
                                    contentDescription = null,
                                    modifier = Modifier.size(18.dp)
                                )
                            },
                            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                            singleLine = true,
                            modifier = Modifier.weight(1f)
                        )
                        OutlinedTextField(
                            value = weightText,
                            onValueChange = { weightText = it },
                            label = { Text("Peso (kg)") },
                            leadingIcon = {
                                Icon(
                                    Icons.Default.Scale,
                                    contentDescription = null,
                                    modifier = Modifier.size(18.dp)
                                )
                            },
                            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                            singleLine = true,
                            modifier = Modifier.weight(1f)
                        )
                    }
                }
            }
        }
    }

    setToDelete?.let { set ->
        val deleteMsg = if (set.isCardio) {
            val mins = set.durationSeconds / 60
            val secs = set.durationSeconds % 60
            val timeStr = if (secs > 0) "${mins}min ${secs}s" else "${mins}min"
            val distStr = if (set.distanceKm > 0) " · ${set.distanceKm} km" else ""
            "¿Eliminar el Set ${set.setNumber} ($timeStr$distStr)?"
        } else {
            "¿Eliminar el Set ${set.setNumber} (${set.reps} reps × ${set.weight} kg)?"
        }
        ConfirmDeleteDialog(
            title = "Eliminar set",
            message = deleteMsg,
            onConfirm = { viewModel.deleteSet(set) },
            onDismiss = { setToDelete = null }
        )
    }
}

@Composable
private fun SetCard(
    setNumber: Int,
    reps: Int,
    weight: Double,
    onDelete: () -> Unit,
    modifier: Modifier = Modifier
) {
    FitnessCard(
        title = "Set $setNumber",
        onDelete = onDelete,
        modifier = modifier
    ) {
        Row(
            horizontalArrangement = Arrangement.spacedBy(12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Badge reps
            Row(
                modifier = Modifier
                    .clip(RoundedCornerShape(8.dp))
                    .background(MaterialTheme.colorScheme.primary.copy(alpha = 0.1f))
                    .padding(horizontal = 10.dp, vertical = 5.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(4.dp)
            ) {
                Icon(
                    Icons.Default.Repeat,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.primary,
                    modifier = Modifier.size(14.dp)
                )
                Text(
                    text = "$reps reps",
                    style = MaterialTheme.typography.labelMedium.copy(
                        fontWeight = FontWeight.SemiBold
                    ),
                    color = MaterialTheme.colorScheme.primary
                )
            }

            // Badge peso
            Row(
                modifier = Modifier
                    .clip(RoundedCornerShape(8.dp))
                    .background(MaterialTheme.colorScheme.tertiary.copy(alpha = 0.1f))
                    .padding(horizontal = 10.dp, vertical = 5.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(4.dp)
            ) {
                Icon(
                    Icons.Default.Scale,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.tertiary,
                    modifier = Modifier.size(14.dp)
                )
                Text(
                    text = "$weight kg",
                    style = MaterialTheme.typography.labelMedium.copy(
                        fontWeight = FontWeight.SemiBold
                    ),
                    color = MaterialTheme.colorScheme.tertiary
                )
            }
        }
    }
}

@Composable
private fun CardioSetCard(
    setNumber: Int,
    durationSeconds: Int,
    distanceKm: Double,
    onDelete: () -> Unit,
    modifier: Modifier = Modifier
) {
    FitnessCard(
        title = "Set $setNumber",
        icon = Icons.Default.DirectionsRun,
        accentColor = MaterialTheme.colorScheme.secondary,
        onDelete = onDelete,
        modifier = modifier
    ) {
        Row(
            horizontalArrangement = Arrangement.spacedBy(12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Badge duración
            Row(
                modifier = Modifier
                    .clip(RoundedCornerShape(8.dp))
                    .background(MaterialTheme.colorScheme.secondary.copy(alpha = 0.1f))
                    .padding(horizontal = 10.dp, vertical = 5.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(4.dp)
            ) {
                Icon(
                    Icons.Default.Timer,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.secondary,
                    modifier = Modifier.size(14.dp)
                )
                Text(
                    text = formatDuration(durationSeconds),
                    style = MaterialTheme.typography.labelMedium.copy(
                        fontWeight = FontWeight.SemiBold
                    ),
                    color = MaterialTheme.colorScheme.secondary
                )
            }

            // Badge distancia (si hay)
            if (distanceKm > 0) {
                Row(
                    modifier = Modifier
                        .clip(RoundedCornerShape(8.dp))
                        .background(MaterialTheme.colorScheme.tertiary.copy(alpha = 0.1f))
                        .padding(horizontal = 10.dp, vertical = 5.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(4.dp)
                ) {
                    Icon(
                        Icons.Default.Route,
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.tertiary,
                        modifier = Modifier.size(14.dp)
                    )
                    Text(
                        text = "$distanceKm km",
                        style = MaterialTheme.typography.labelMedium.copy(
                            fontWeight = FontWeight.SemiBold
                        ),
                        color = MaterialTheme.colorScheme.tertiary
                    )
                }
            }
        }
    }
}

private fun formatDuration(totalSeconds: Int): String {
    val hours = totalSeconds / 3600
    val mins = (totalSeconds % 3600) / 60
    val secs = totalSeconds % 60
    return when {
        hours > 0 -> "${hours}h ${mins}min"
        mins > 0 && secs > 0 -> "${mins}min ${secs}s"
        mins > 0 -> "${mins}min"
        else -> "${secs}s"
    }
}

