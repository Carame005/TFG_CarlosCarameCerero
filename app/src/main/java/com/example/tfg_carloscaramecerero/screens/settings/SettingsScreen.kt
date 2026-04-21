package com.example.tfg_carloscaramecerero.screens.settings

import android.Manifest
import android.os.Build
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.example.tfg_carloscaramecerero.components.FitnessTopBar
import com.example.tfg_carloscaramecerero.data.local.entity.BodyWeightEntity
import com.example.tfg_carloscaramecerero.data.local.entity.FoodEntryEntity
import com.example.tfg_carloscaramecerero.data.local.relation.SessionWithSets
import com.example.tfg_carloscaramecerero.data.util.ExportManager
import com.example.tfg_carloscaramecerero.viewmodel.SettingsViewModel

@Composable
fun SettingsScreen(
    viewModel: SettingsViewModel,
    onBackClick: () -> Unit,
    sessions: List<SessionWithSets> = emptyList(),
    weights: List<BodyWeightEntity> = emptyList(),
    foodEntries: List<FoodEntryEntity> = emptyList()
) {
    val darkMode by viewModel.darkMode.collectAsState()
    val notificationsEnabled by viewModel.notificationsEnabled.collectAsState()
    val aiCanCreateRoutines by viewModel.aiCanCreateRoutines.collectAsState()
    val aiCanCreateExercises by viewModel.aiCanCreateExercises.collectAsState()
    val aiCanCreateFoodSchedule by viewModel.aiCanCreateFoodSchedule.collectAsState()
    val context = LocalContext.current

    // Permiso notificaciones (Android 13+)
    val notificationPermissionLauncher = rememberLauncherForActivityResult(
        ActivityResultContracts.RequestPermission()
    ) { granted ->
        if (granted) viewModel.setNotificationsEnabled(true)
    }

    Scaffold(
        topBar = {
            FitnessTopBar(title = "Ajustes", onBackClick = onBackClick)
        }
    ) { padding ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .padding(horizontal = 16.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            item { Spacer(Modifier.height(8.dp)) }

            // ── Apariencia ──
            item {
                SettingsSectionHeader("Apariencia")
            }
            item {
                SettingsCard {
                    Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                        Text(
                            "Tema de la aplicación",
                            style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.SemiBold)
                        )
                        Spacer(Modifier.height(8.dp))
                        SingleChoiceSegmentedButtonRow(modifier = Modifier.fillMaxWidth()) {
                            SegmentedButton(
                                selected = darkMode == null,
                                onClick = { viewModel.setDarkMode(null) },
                                shape = SegmentedButtonDefaults.itemShape(0, 3)
                            ) { Text("Auto") }
                            SegmentedButton(
                                selected = darkMode == false,
                                onClick = { viewModel.setDarkMode(false) },
                                shape = SegmentedButtonDefaults.itemShape(1, 3)
                            ) { Text("Claro") }
                            SegmentedButton(
                                selected = darkMode == true,
                                onClick = { viewModel.setDarkMode(true) },
                                shape = SegmentedButtonDefaults.itemShape(2, 3)
                            ) { Text("Oscuro") }
                        }
                    }
                }
            }

            // ── Notificaciones ──
            item { SettingsSectionHeader("Notificaciones") }
            item {
                SettingsCard {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(12.dp),
                            modifier = Modifier.weight(1f)
                        ) {
                            Icon(
                                Icons.Default.NotificationsActive,
                                contentDescription = null,
                                tint = MaterialTheme.colorScheme.primary,
                                modifier = Modifier.size(24.dp)
                            )
                            Column {
                                Text(
                                    "Recordatorio diario",
                                    style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.SemiBold)
                                )
                                Text(
                                    "Recibe un aviso para entrenar cada día",
                                    style = MaterialTheme.typography.bodySmall,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }
                        }
                        Switch(
                            checked = notificationsEnabled,
                            onCheckedChange = { enabled ->
                                if (enabled && Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                                    notificationPermissionLauncher.launch(Manifest.permission.POST_NOTIFICATIONS)
                                } else {
                                    viewModel.setNotificationsEnabled(enabled)
                                }
                            }
                        )
                    }
                }
            }

            // ── Asistente IA ──
            item { SettingsSectionHeader("Asistente IA - Permisos de creación") }
            item {
                SettingsCard {
                    Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                        Text(
                            "Permite al asistente crear contenido en tu app cuando se lo pidas.",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                        Spacer(Modifier.height(8.dp))
                        AiPermissionToggle(
                            icon = Icons.Default.FitnessCenter,
                            title = "Crear rutinas",
                            subtitle = "El asistente puede añadir nuevas rutinas de entrenamiento",
                            checked = aiCanCreateRoutines,
                            onCheckedChange = { viewModel.setAiCanCreateRoutines(it) }
                        )
                        HorizontalDivider(modifier = Modifier.padding(vertical = 4.dp))
                        AiPermissionToggle(
                            icon = Icons.Default.SportsGymnastics,
                            title = "Crear ejercicios",
                            subtitle = "El asistente puede añadir ejercicios a tu biblioteca",
                            checked = aiCanCreateExercises,
                            onCheckedChange = { viewModel.setAiCanCreateExercises(it) }
                        )
                        HorizontalDivider(modifier = Modifier.padding(vertical = 4.dp))
                        AiPermissionToggle(
                            icon = Icons.Default.Restaurant,
                            title = "Crear horario de comidas",
                            subtitle = "El asistente puede añadir entradas al horario nutricional",
                            checked = aiCanCreateFoodSchedule,
                            onCheckedChange = { viewModel.setAiCanCreateFoodSchedule(it) }
                        )
                    }
                }
            }

            // ── Exportar datos ──
            item { SettingsSectionHeader("Exportar datos") }
            item {
                SettingsCard {
                    Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                        Text(
                            "Exporta tus datos como CSV para abrirlos en Excel u otra app.",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                        Spacer(Modifier.height(4.dp))
                        ExportButton(
                            icon = Icons.Default.FitnessCenter,
                            label = "Exportar sesiones de entrenamiento"
                        ) {
                            ExportManager.exportSessions(context, sessions)
                        }
                        ExportButton(
                            icon = Icons.Default.MonitorWeight,
                            label = "Exportar historial de peso"
                        ) {
                            ExportManager.exportWeights(context, weights)
                        }
                        ExportButton(
                            icon = Icons.Default.Restaurant,
                            label = "Exportar registro nutricional"
                        ) {
                            ExportManager.exportNutrition(context, foodEntries)
                        }
                    }
                }
            }

            item { Spacer(Modifier.height(16.dp)) }
        }
    }
}

@Composable
private fun SettingsSectionHeader(title: String) {
    Text(
        title,
        style = MaterialTheme.typography.labelLarge.copy(fontWeight = FontWeight.Bold),
        color = MaterialTheme.colorScheme.primary,
        modifier = Modifier.padding(vertical = 4.dp)
    )
}

@Composable
private fun SettingsCard(content: @Composable () -> Unit) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(14.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
    ) {
        Box(modifier = Modifier.padding(16.dp)) {
            content()
        }
    }
}

@Composable
private fun AiPermissionToggle(
    icon: ImageVector,
    title: String,
    subtitle: String,
    checked: Boolean,
    onCheckedChange: (Boolean) -> Unit
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(12.dp),
            modifier = Modifier.weight(1f)
        ) {
            Icon(icon, contentDescription = null, tint = MaterialTheme.colorScheme.primary, modifier = Modifier.size(22.dp))
            Column {
                Text(title, style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.SemiBold))
                Text(subtitle, style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
            }
        }
        Switch(checked = checked, onCheckedChange = onCheckedChange)
    }
}

@Composable
private fun ExportButton(icon: ImageVector, label: String, onClick: () -> Unit) {
    OutlinedButton(
        onClick = onClick,
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(10.dp)
    ) {
        Icon(icon, contentDescription = null, modifier = Modifier.size(16.dp))
        Spacer(Modifier.width(8.dp))
        Text(label, style = MaterialTheme.typography.labelMedium)
    }
}

