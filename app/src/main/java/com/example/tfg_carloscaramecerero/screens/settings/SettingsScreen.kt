package com.example.tfg_carloscaramecerero.screens.settings

import android.Manifest
import android.os.Build
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.biometric.BiometricManager
import androidx.biometric.BiometricManager.Authenticators.BIOMETRIC_STRONG
import androidx.biometric.BiometricManager.Authenticators.DEVICE_CREDENTIAL
import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.automirrored.filled.Assignment
import androidx.compose.material.icons.automirrored.filled.HelpOutline
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.rememberLazyListState
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
import com.example.tfg_carloscaramecerero.data.local.entity.BodyMeasurementEntity
import com.example.tfg_carloscaramecerero.data.local.entity.BodyWeightEntity
import com.example.tfg_carloscaramecerero.data.local.entity.ExerciseEntity
import com.example.tfg_carloscaramecerero.data.local.entity.FoodCatalogEntity
import com.example.tfg_carloscaramecerero.data.local.entity.FoodEntryEntity
import com.example.tfg_carloscaramecerero.data.local.entity.RoutineEntity
import com.example.tfg_carloscaramecerero.data.local.relation.SessionWithSets
import com.example.tfg_carloscaramecerero.data.util.ExportManager
import com.example.tfg_carloscaramecerero.data.util.ImportManager
import com.example.tfg_carloscaramecerero.viewmodel.SettingsViewModel

@Composable
fun SettingsScreen(
    viewModel: SettingsViewModel,
    onBackClick: () -> Unit,
    onNavigateToAuditLog: () -> Unit = {},
    onNavigateToHelp: () -> Unit = {},
    onNavigateToTerms: () -> Unit = {},
    sessions: List<SessionWithSets> = emptyList(),
    weights: List<BodyWeightEntity> = emptyList(),
    measurements: List<BodyMeasurementEntity> = emptyList(),
    foodEntries: List<FoodEntryEntity> = emptyList(),
    allRoutines: List<RoutineEntity> = emptyList(),
    allExercises: List<ExerciseEntity> = emptyList(),
    foodCatalog: List<FoodCatalogEntity> = emptyList(),
    scrollToSection: String? = null
) {
    val darkMode by viewModel.darkMode.collectAsState()
    val notificationsEnabled by viewModel.notificationsEnabled.collectAsState()
    val aiCanCreateRoutines by viewModel.aiCanCreateRoutines.collectAsState()
    val aiCanCreateExercises by viewModel.aiCanCreateExercises.collectAsState()
    val aiCanCreateFoodSchedule by viewModel.aiCanCreateFoodSchedule.collectAsState()
    val biometricLock by viewModel.biometricLock.collectAsState()
    val dbRestoreSuccess by viewModel.dbRestoreSuccess.collectAsState()
    val dbExportError by viewModel.dbExportError.collectAsState()
    val importResult by viewModel.importResult.collectAsState()
    val context = LocalContext.current
    val snackbarHostState = remember { SnackbarHostState() }

    var showRestartDialog by remember { mutableStateOf(false) }

    val listState = rememberLazyListState()

    // Índice de la sección "Copia de seguridad" en el LazyColumn
    // (0: Spacer, 1: Apariencia header, 2: Apariencia card, 3: Notificaciones header,
    //  4: Notificaciones card, 5: IA header, 6: IA card, 7: Backup header)
    LaunchedEffect(scrollToSection) {
        if (scrollToSection == "backup") {
            listState.animateScrollToItem(index = 7)
        }
    }

    LaunchedEffect(dbRestoreSuccess) {
        when (dbRestoreSuccess) {
            true  -> showRestartDialog = true
            else  -> Unit
        }
    }

    LaunchedEffect(dbExportError) {
        if (dbExportError != null) {
            snackbarHostState.showSnackbar("Error al exportar: $dbExportError")
            viewModel.clearExportError()
        }
    }

    LaunchedEffect(importResult) {
        if (importResult != null) {
            snackbarHostState.showSnackbar(importResult!!)
            viewModel.clearImportResult()
        }
    }

    // Diálogo de reinicio tras restaurar la BD
    if (showRestartDialog) {
        AlertDialog(
            onDismissRequest = {},
            icon = { Icon(Icons.Default.CheckCircle, contentDescription = null, tint = MaterialTheme.colorScheme.primary) },
            title = { Text("Base de datos restaurada") },
            text = { Text("La copia de seguridad se ha restaurado correctamente. La app necesita reiniciarse para aplicar los cambios.") },
            confirmButton = {
                Button(onClick = {
                    viewModel.clearRestoreState()
                    ImportManager.restartApp(context)
                }) { Text("Reiniciar ahora") }
            }
        )
    }

    // Comprueba si el dispositivo tiene biometría disponible
    val biometricAvailable = remember(context) {
        val bm = BiometricManager.from(context)
        bm.canAuthenticate(BIOMETRIC_STRONG or DEVICE_CREDENTIAL) == BiometricManager.BIOMETRIC_SUCCESS
    }

    // Permiso notificaciones (Android 13+)
    val notificationPermissionLauncher = rememberLauncherForActivityResult(
        ActivityResultContracts.RequestPermission()
    ) { granted ->
        if (granted) viewModel.setNotificationsEnabled(true)
    }

    // Launchers de importación CSV
    val importWeightsLauncher = rememberLauncherForActivityResult(
        ActivityResultContracts.GetContent()
    ) { uri ->
        uri ?: return@rememberLauncherForActivityResult
        val csv = ImportManager.readCsvFromUri(context, uri) ?: return@rememberLauncherForActivityResult
        val data = ImportManager.parseWeightsCsv(csv)
        if (data.isNotEmpty()) viewModel.importWeights(data)
    }

    val importNutritionLauncher = rememberLauncherForActivityResult(
        ActivityResultContracts.GetContent()
    ) { uri ->
        uri ?: return@rememberLauncherForActivityResult
        val csv = ImportManager.readCsvFromUri(context, uri) ?: return@rememberLauncherForActivityResult
        val data = ImportManager.parseNutritionCsv(csv)
        if (data.isNotEmpty()) viewModel.importNutrition(data)
    }

    val importRoutinesLauncher = rememberLauncherForActivityResult(
        ActivityResultContracts.GetContent()
    ) { uri ->
        uri ?: return@rememberLauncherForActivityResult
        val csv = ImportManager.readCsvFromUri(context, uri) ?: return@rememberLauncherForActivityResult
        val data = ImportManager.parseRoutinesCsv(csv)
        if (data.isNotEmpty()) viewModel.importRoutines(data)
    }

    val importExercisesLauncher = rememberLauncherForActivityResult(
        ActivityResultContracts.GetContent()
    ) { uri ->
        uri ?: return@rememberLauncherForActivityResult
        val csv = ImportManager.readCsvFromUri(context, uri) ?: return@rememberLauncherForActivityResult
        val data = ImportManager.parseExercisesCsv(csv)
        if (data.isNotEmpty()) viewModel.importExercises(data)
    }

    // Launcher: restaurar base de datos .db completa
    val importDbLauncher = rememberLauncherForActivityResult(
        ActivityResultContracts.GetContent()
    ) { uri ->
        uri ?: return@rememberLauncherForActivityResult
        viewModel.restoreDatabase(uri)
    }

    // Launcher: importar sesiones detalladas CSV
    val importDetailedSessionsLauncher = rememberLauncherForActivityResult(
        ActivityResultContracts.GetContent()
    ) { uri ->
        uri ?: return@rememberLauncherForActivityResult
        val csv = ImportManager.readCsvFromUri(context, uri) ?: return@rememberLauncherForActivityResult
        val data = ImportManager.parseDetailedSessionsCsv(csv)
        if (data.isNotEmpty()) viewModel.importDetailedSessions(data)
    }

    // Launcher: importar catálogo de alimentos
    val importFoodCatalogLauncher = rememberLauncherForActivityResult(
        ActivityResultContracts.GetContent()
    ) { uri ->
        uri ?: return@rememberLauncherForActivityResult
        val csv = ImportManager.readCsvFromUri(context, uri) ?: return@rememberLauncherForActivityResult
        val data = ImportManager.parseFoodCatalogCsv(csv)
        if (data.isNotEmpty()) viewModel.importFoodCatalog(data)
    }

    // Launcher: importar medidas corporales
    val importMeasurementsLauncher = rememberLauncherForActivityResult(
        ActivityResultContracts.GetContent()
    ) { uri ->
        uri ?: return@rememberLauncherForActivityResult
        val csv = ImportManager.readCsvFromUri(context, uri) ?: return@rememberLauncherForActivityResult
        val data = ImportManager.parseMeasurementsCsv(csv)
        if (data.isNotEmpty()) viewModel.importMeasurements(data)
    }

    Scaffold(
        topBar = {
            FitnessTopBar(title = "Ajustes", onBackClick = onBackClick)
        },
        snackbarHost = { SnackbarHost(snackbarHostState) }
    ) { padding ->
        LazyColumn(
            state = listState,
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

            // ── Copia de seguridad completa ──
            item { SettingsSectionHeader("Copia de seguridad completa") }
            item {
                SettingsCard {
                    Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                        Text(
                            "Exporta o restaura todos tus datos en un único archivo .db de SQLite. Incluye rutinas, ejercicios, sesiones, nutrición, peso y configuración.",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                        Spacer(Modifier.height(4.dp))
                        ExportButton(
                            icon = Icons.Default.Backup,
                            label = "Exportar base de datos (.db)"
                        ) { viewModel.exportDatabase() }
                        ExportButton(
                            icon = Icons.Default.Restore,
                            label = "Restaurar base de datos (.db)"
                        ) { importDbLauncher.launch("*/*") }
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
                            label = "Exportar sesiones (resumen)"
                        ) {
                            ExportManager.exportSessions(context, sessions)
                            viewModel.logDataExport("Sesiones de entrenamiento (CSV)")
                        }
                        ExportButton(
                            icon = Icons.AutoMirrored.Filled.Assignment,
                            label = "Exportar sesiones (detallado con sets)"
                        ) {
                            ExportManager.exportDetailedSessions(context, sessions)
                            viewModel.logDataExport("Sesiones detalladas (CSV)")
                        }
                        ExportButton(
                            icon = Icons.Default.MonitorWeight,
                            label = "Exportar historial de peso"
                        ) {
                            ExportManager.exportWeights(context, weights)
                            viewModel.logDataExport("Historial de peso (CSV)")
                        }
                        ExportButton(
                            icon = Icons.Default.Straighten,
                            label = "Exportar medidas corporales"
                        ) {
                            ExportManager.exportMeasurements(context, measurements)
                            viewModel.logDataExport("Medidas corporales (CSV)")
                        }
                        ExportButton(
                            icon = Icons.Default.Restaurant,
                            label = "Exportar registro nutricional"
                        ) {
                            ExportManager.exportNutrition(context, foodEntries)
                            viewModel.logDataExport("Registro nutricional (CSV)")
                        }
                        ExportButton(
                            icon = Icons.Default.FitnessCenter,
                            label = "Exportar rutinas"
                        ) {
                            ExportManager.exportRoutines(context, allRoutines)
                            viewModel.logDataExport("Rutinas (CSV)")
                        }
                        ExportButton(
                            icon = Icons.Default.SportsGymnastics,
                            label = "Exportar biblioteca de ejercicios"
                        ) {
                            ExportManager.exportExercises(context, allExercises)
                            viewModel.logDataExport("Ejercicios (CSV)")
                        }
                        ExportButton(
                            icon = Icons.Default.MenuBook,
                            label = "Exportar catálogo de alimentos"
                        ) {
                            ExportManager.exportFoodCatalog(context, foodCatalog)
                            viewModel.logDataExport("Catálogo de alimentos (CSV)")
                        }
                    }
                }
            }

            // ── Importar datos ──
            item { SettingsSectionHeader("Importar datos") }
            item {
                SettingsCard {
                    Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                        Text(
                            "Importa datos desde un CSV generado por esta app. Se añadirán a los datos existentes.",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                        Spacer(Modifier.height(4.dp))
                        ExportButton(
                            icon = Icons.Default.MonitorWeight,
                            label = "Importar historial de peso"
                        ) { importWeightsLauncher.launch("text/*") }
                        ExportButton(
                            icon = Icons.Default.Straighten,
                            label = "Importar medidas corporales"
                        ) { importMeasurementsLauncher.launch("text/*") }
                        ExportButton(
                            icon = Icons.Default.Restaurant,
                            label = "Importar registro nutricional"
                        ) { importNutritionLauncher.launch("text/*") }
                        ExportButton(
                            icon = Icons.Default.FitnessCenter,
                            label = "Importar rutinas"
                        ) { importRoutinesLauncher.launch("text/*") }
                        ExportButton(
                            icon = Icons.Default.SportsGymnastics,
                            label = "Importar ejercicios"
                        ) { importExercisesLauncher.launch("text/*") }
                        ExportButton(
                            icon = Icons.AutoMirrored.Filled.Assignment,
                            label = "Importar sesiones (detallado con sets)"
                        ) { importDetailedSessionsLauncher.launch("text/*") }
                        ExportButton(
                            icon = Icons.Default.MenuBook,
                            label = "Importar catálogo de alimentos"
                        ) { importFoodCatalogLauncher.launch("text/*") }
                    }
                }
            }

            // ── Seguridad ──
            item { SettingsSectionHeader("Seguridad") }
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
                                Icons.Default.Fingerprint,
                                contentDescription = null,
                                tint = if (biometricAvailable) MaterialTheme.colorScheme.primary
                                       else MaterialTheme.colorScheme.onSurfaceVariant,
                                modifier = Modifier.size(24.dp)
                            )
                            Column {
                                Text(
                                    "Bloqueo biométrico",
                                    style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.SemiBold)
                                )
                                Text(
                                    if (biometricAvailable) "Requiere huella o rostro al abrir la app"
                                    else "No hay biometría disponible en este dispositivo",
                                    style = MaterialTheme.typography.bodySmall,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }
                        }
                        Switch(
                            checked = biometricLock,
                            onCheckedChange = { viewModel.setBiometricLock(it) },
                            enabled = biometricAvailable
                        )
                    }
                }
            }

            // ── Auditoría ──
            item { SettingsSectionHeader("Auditoría") }
            item {
                SettingsCard {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(12.dp),
                            modifier = Modifier.weight(1f)
                        ) {
                            Icon(
                                Icons.Default.History,
                                contentDescription = null,
                                tint = MaterialTheme.colorScheme.primary,
                                modifier = Modifier.size(24.dp)
                            )
                            Column {
                                Text(
                                    "Registro de acciones",
                                    style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.SemiBold)
                                )
                                Text(
                                    "Consulta el historial de operaciones realizadas",
                                    style = MaterialTheme.typography.bodySmall,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }
                        }
                        IconButton(onClick = onNavigateToAuditLog) {
                            Icon(Icons.Default.ChevronRight, contentDescription = "Ver registro")
                        }
                    }
                }
            }

            // ── Ayuda ──
            item { SettingsSectionHeader("Ayuda") }
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
                                Icons.AutoMirrored.Filled.HelpOutline,
                                contentDescription = null,
                                tint = MaterialTheme.colorScheme.primary,
                                modifier = Modifier.size(24.dp)
                            )
                            Column {
                                Text(
                                    "Guía de uso",
                                    style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.SemiBold)
                                )
                                Text(
                                    "Aprende a usar todas las funciones de la app",
                                    style = MaterialTheme.typography.bodySmall,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }
                        }
                        IconButton(onClick = onNavigateToHelp) {
                            Icon(Icons.Default.ChevronRight, contentDescription = "Ver ayuda")
                        }
                    }
                }
            }

            item { Spacer(Modifier.height(4.dp)) }

            // ── Términos y condiciones ──────────────────────────────────────────
            item {
                SettingsCard {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Row(
                            horizontalArrangement = Arrangement.spacedBy(12.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            modifier = Modifier.weight(1f)
                        ) {
                            Icon(
                                Icons.Default.Gavel,
                                contentDescription = null,
                                tint = MaterialTheme.colorScheme.primary,
                                modifier = Modifier.size(24.dp)
                            )
                            Column {
                                Text(
                                    "Términos y condiciones",
                                    style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.SemiBold)
                                )
                                Text(
                                    "Lee el acuerdo de uso de la aplicación",
                                    style = MaterialTheme.typography.bodySmall,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }
                        }
                        IconButton(onClick = onNavigateToTerms) {
                            Icon(Icons.Default.ChevronRight, contentDescription = "Ver términos")
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

