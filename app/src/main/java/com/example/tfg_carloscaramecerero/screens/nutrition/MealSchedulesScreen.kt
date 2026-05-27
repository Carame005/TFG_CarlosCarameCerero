package com.example.tfg_carloscaramecerero.screens.nutrition

import androidx.compose.foundation.layout.Arrangement
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
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardCapitalization
import androidx.compose.ui.unit.dp
import com.example.tfg_carloscaramecerero.components.ConfirmDeleteDialog
import com.example.tfg_carloscaramecerero.components.EmptyStateMessage
import com.example.tfg_carloscaramecerero.components.FitnessFAB
import com.example.tfg_carloscaramecerero.components.FitnessTopBar
import com.example.tfg_carloscaramecerero.data.local.entity.MealScheduleEntity
import com.example.tfg_carloscaramecerero.viewmodel.NutritionViewModel
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

@Composable
fun MealSchedulesScreen(
    viewModel: NutritionViewModel,
    onBackClick: () -> Unit
) {
    val schedules by viewModel.schedules.collectAsState()
    val currentScheduleId by viewModel.currentScheduleId.collectAsState()

    var showNewScheduleDialog by remember { mutableStateOf(false) }
    var scheduleToDelete by remember { mutableStateOf<MealScheduleEntity?>(null) }

    Scaffold(
        topBar = {
            FitnessTopBar(
                title = "Mis horarios",
                onBackClick = onBackClick
            )
        },
        floatingActionButton = {
            FitnessFAB(
                onClick = { showNewScheduleDialog = true },
                icon = Icons.Default.Add,
                contentDescription = "Nuevo horario"
            )
        }
    ) { padding ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .padding(horizontal = 16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            item { Spacer(modifier = Modifier.height(8.dp)) }

            if (schedules.isEmpty()) {
                item {
                    EmptyStateMessage(
                        message = "No hay horarios creados",
                        subtitle = "Pulsa + para crear tu primer horario de comidas",
                        icon = Icons.Default.Add
                    )
                }
            } else {
                items(schedules, key = { it.id }) { schedule ->
                    ScheduleCard(
                        schedule = schedule,
                        isActive = schedule.id == currentScheduleId,
                        canDelete = schedules.size > 1,
                        onActivate = { viewModel.selectSchedule(schedule.id) },
                        onDelete = { scheduleToDelete = schedule }
                    )
                }
            }

            item { Spacer(modifier = Modifier.height(80.dp)) }
        }
    }

    // Dialog: crear nuevo horario
    if (showNewScheduleDialog) {
        var newName by remember { mutableStateOf("") }
        AlertDialog(
            onDismissRequest = { showNewScheduleDialog = false; newName = "" },
            title = { Text("Nuevo horario") },
            text = {
                OutlinedTextField(
                    value = newName,
                    onValueChange = { newName = it },
                    label = { Text("Nombre del horario") },
                    placeholder = { Text("Ej: Volumen, Cutting, Verano...") },
                    singleLine = true,
                    keyboardOptions = KeyboardOptions(capitalization = KeyboardCapitalization.Sentences),
                    modifier = Modifier.fillMaxWidth()
                )
            },
            confirmButton = {
                Button(
                    onClick = {
                        if (newName.isNotBlank()) {
                            viewModel.createSchedule(newName.trim())
                            showNewScheduleDialog = false
                            newName = ""
                        }
                    },
                    enabled = newName.isNotBlank()
                ) { Text("Crear") }
            },
            dismissButton = {
                TextButton(onClick = { showNewScheduleDialog = false; newName = "" }) {
                    Text("Cancelar")
                }
            }
        )
    }

    // Confirm: eliminar horario
    scheduleToDelete?.let { schedule ->
        ConfirmDeleteDialog(
            title = "Eliminar horario",
            message = "¿Seguro que quieres eliminar \"${schedule.name}\" y todas sus comidas registradas? Esta acción no se puede deshacer.",
            onConfirm = {
                viewModel.deleteSchedule(schedule)
                scheduleToDelete = null
            },
            onDismiss = { scheduleToDelete = null }
        )
    }
}

@Composable
private fun ScheduleCard(
    schedule: MealScheduleEntity,
    isActive: Boolean,
    canDelete: Boolean,
    onActivate: () -> Unit,
    onDelete: () -> Unit
) {
    val dateFormat = remember { SimpleDateFormat("dd/MM/yyyy", Locale("es", "ES")) }
    val createdDate = remember(schedule.createdAt) { dateFormat.format(Date(schedule.createdAt)) }

    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(
            containerColor = if (isActive)
                MaterialTheme.colorScheme.primaryContainer
            else
                MaterialTheme.colorScheme.surfaceVariant
        ),
        elevation = CardDefaults.cardElevation(if (isActive) 4.dp else 1.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Icono de estado
            Icon(
                imageVector = if (isActive) Icons.Default.CheckCircle else Icons.Default.PlayArrow,
                contentDescription = null,
                tint = if (isActive) MaterialTheme.colorScheme.primary
                       else MaterialTheme.colorScheme.onSurfaceVariant,
                modifier = Modifier.size(28.dp)
            )

            Spacer(modifier = Modifier.width(12.dp))

            // Nombre y fecha
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = schedule.name,
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = if (isActive) FontWeight.Bold else FontWeight.Normal,
                    color = if (isActive) MaterialTheme.colorScheme.onPrimaryContainer
                            else MaterialTheme.colorScheme.onSurfaceVariant
                )
                Spacer(modifier = Modifier.height(2.dp))
                if (isActive) {
                    Text(
                        text = "Activo",
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.primary,
                        fontWeight = FontWeight.SemiBold
                    )
                } else {
                    Text(
                        text = "Creado el $createdDate",
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }

            // Botón Activar (solo si no está activo)
            if (!isActive) {
                Button(
                    onClick = onActivate,
                    shape = RoundedCornerShape(8.dp),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = MaterialTheme.colorScheme.primary
                    ),
                    modifier = Modifier.height(36.dp)
                ) {
                    Text("Activar", style = MaterialTheme.typography.labelMedium)
                }
                Spacer(modifier = Modifier.width(4.dp))
            }

            // Botón eliminar
            if (canDelete) {
                IconButton(
                    onClick = onDelete,
                    modifier = Modifier.size(36.dp)
                ) {
                    Icon(
                        Icons.Default.Delete,
                        contentDescription = "Eliminar horario",
                        tint = MaterialTheme.colorScheme.error,
                        modifier = Modifier.size(20.dp)
                    )
                }
            }
        }
    }
}

