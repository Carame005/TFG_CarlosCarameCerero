package com.example.tfg_carloscaramecerero.screens.body

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Height
import androidx.compose.material.icons.filled.MedicalServices
import androidx.compose.material.icons.filled.MonitorWeight
import androidx.compose.material.icons.filled.Save
import androidx.compose.material.icons.filled.Straighten
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Tab
import androidx.compose.material3.TabRow
import androidx.compose.material3.TabRowDefaults.SecondaryIndicator
import androidx.compose.material3.TabRowDefaults.tabIndicatorOffset
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import com.example.tfg_carloscaramecerero.components.ConfirmDeleteDialog
import com.example.tfg_carloscaramecerero.components.EmptyStateMessage
import com.example.tfg_carloscaramecerero.components.FitnessCard
import com.example.tfg_carloscaramecerero.components.FitnessFAB
import com.example.tfg_carloscaramecerero.components.FitnessInputDialog
import com.example.tfg_carloscaramecerero.components.FitnessTopBar
import com.example.tfg_carloscaramecerero.components.SectionHeader
import com.example.tfg_carloscaramecerero.components.StatCard
import com.example.tfg_carloscaramecerero.data.local.entity.BodyMeasurementEntity
import com.example.tfg_carloscaramecerero.data.local.entity.BodyWeightEntity
import com.example.tfg_carloscaramecerero.viewmodel.BodyViewModel
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

@Composable
fun BodyScreen(viewModel: BodyViewModel) {
    val pagerState = rememberPagerState(pageCount = { 3 })
    val coroutineScope = rememberCoroutineScope()
    val context = androidx.compose.ui.platform.LocalContext.current

    var showWeightDialog by remember { mutableStateOf(false) }
    var showMeasurementDialog by remember { mutableStateOf(false) }
    var showHeightDialog by remember { mutableStateOf(false) }
    var weightText by remember { mutableStateOf("") }

    // Campos medidas
    var chestText by remember { mutableStateOf("") }
    var waistText by remember { mutableStateOf("") }
    var hipsText by remember { mutableStateOf("") }
    var bicepsText by remember { mutableStateOf("") }
    var thighsText by remember { mutableStateOf("") }

    // Delete confirmation states
    var weightToDelete by remember { mutableStateOf<BodyWeightEntity?>(null) }
    var measurementToDelete by remember { mutableStateOf<BodyMeasurementEntity?>(null) }

    val weights by viewModel.weights.collectAsState()
    val latestWeight by viewModel.latestWeight.collectAsState()
    val measurements by viewModel.measurements.collectAsState()
    val latestMeasurement by viewModel.latestMeasurement.collectAsState()
    val userProfile by viewModel.userProfile.collectAsState()
    val healthDocuments by viewModel.healthDocuments.collectAsState()

    // Campos perfil salud
    var healthConditionsText by remember { mutableStateOf("") }
    var heightText by remember { mutableStateOf("") }

    // Sincronizar campos cuando se carguen los datos
    LaunchedEffect(userProfile) {
        userProfile?.let {
            heightText = it.height?.let { h -> "%.1f".format(h) } ?: ""
            healthConditionsText = it.healthConditions
        }
    }

    val dateFormat = remember { SimpleDateFormat("dd/MM/yyyy", Locale.getDefault()) }

    Scaffold(
        topBar = { FitnessTopBar(title = "Cuerpo") },
        floatingActionButton = {
            when (pagerState.currentPage) {
                0 -> FitnessFAB(
                    onClick = { showWeightDialog = true },
                    icon = Icons.Default.Add,
                    contentDescription = "Añadir peso"
                )
                1 -> FitnessFAB(
                    onClick = { showMeasurementDialog = true },
                    icon = Icons.Default.Add,
                    contentDescription = "Añadir medidas"
                )
            }
        }
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
        ) {
            TabRow(
                selectedTabIndex = pagerState.currentPage,
                containerColor = MaterialTheme.colorScheme.surface,
                contentColor = MaterialTheme.colorScheme.primary,
                indicator = { tabPositions ->
                    SecondaryIndicator(
                        Modifier.tabIndicatorOffset(tabPositions[pagerState.currentPage]),
                        color = MaterialTheme.colorScheme.primary
                    )
                }
            ) {
                Tab(
                    selected = pagerState.currentPage == 0,
                    onClick = { coroutineScope.launch { pagerState.animateScrollToPage(0) } },
                    text = { Text("Peso") },
                    icon = { Icon(Icons.Default.MonitorWeight, contentDescription = null) }
                )
                Tab(
                    selected = pagerState.currentPage == 1,
                    onClick = { coroutineScope.launch { pagerState.animateScrollToPage(1) } },
                    text = { Text("Medidas") },
                    icon = { Icon(Icons.Default.Straighten, contentDescription = null) }
                )
                Tab(
                    selected = pagerState.currentPage == 2,
                    onClick = { coroutineScope.launch { pagerState.animateScrollToPage(2) } },
                    text = { Text("Salud") },
                    icon = { Icon(Icons.Default.MedicalServices, contentDescription = null) }
                )
            }

            HorizontalPager(
                state = pagerState,
                modifier = Modifier.fillMaxSize()
            ) { page ->
                when (page) {
                    0 -> WeightTab(
                        latestWeight = latestWeight,
                        weights = weights,
                        dateFormat = dateFormat,
                        onDeleteWeight = { weightToDelete = it }
                    )

                    1 -> MeasurementsTab(
                        latestMeasurement = latestMeasurement,
                        measurements = measurements,
                        dateFormat = dateFormat,
                        userHeight = userProfile?.height,
                        latestWeight = latestWeight,
                        onEditHeight = { showHeightDialog = true },
                        onDeleteMeasurement = { measurementToDelete = it }
                    )

                    2 -> HealthTab(
                        healthConditionsText = healthConditionsText,
                        onHealthConditionsChange = { healthConditionsText = it },
                        onSave = {
                            viewModel.saveHealthConditions(healthConditionsText.trim())
                        },
                        healthDocuments = healthDocuments,
                        onUploadDocument = { uri ->
                            viewModel.uploadHealthDocument(context, uri)
                        },
                        onDeleteDocument = { viewModel.deleteHealthDocument(it) }
                    )
                }
            }
        }
    }

    // Dialog peso
    if (showWeightDialog) {
        FitnessInputDialog(
            title = "Registrar peso",
            onDismiss = {
                showWeightDialog = false
                weightText = ""
            },
            onConfirm = {
                weightText.replace(",", ".").toDoubleOrNull()?.let {
                    viewModel.addWeight(it)
                    showWeightDialog = false
                    weightText = ""
                }
            }
        ) {
            OutlinedTextField(
                value = weightText,
                onValueChange = { weightText = it },
                label = { Text("Peso (kg)") },
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                singleLine = true,
                modifier = Modifier.fillMaxWidth()
            )
        }
    }

    // Dialog altura
    if (showHeightDialog) {
        FitnessInputDialog(
            title = "Registrar altura",
            onDismiss = {
                showHeightDialog = false
            },
            onConfirm = {
                heightText.replace(",", ".").toDoubleOrNull()?.let {
                    viewModel.saveHeight(it)
                    showHeightDialog = false
                }
            }
        ) {
            OutlinedTextField(
                value = heightText,
                onValueChange = { heightText = it },
                label = { Text("Altura (cm)") },
                placeholder = { Text("Ej: 175") },
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                singleLine = true,
                modifier = Modifier.fillMaxWidth()
            )
        }
    }

    // Dialog medidas
    if (showMeasurementDialog) {
        FitnessInputDialog(
            title = "Registrar medidas (cm)",
            onDismiss = {
                showMeasurementDialog = false
                chestText = ""; waistText = ""; hipsText = ""; bicepsText = ""; thighsText = ""
            },
            onConfirm = {
                viewModel.addMeasurement(
                    chest = chestText.replace(",", ".").toDoubleOrNull(),
                    waist = waistText.replace(",", ".").toDoubleOrNull(),
                    hips = hipsText.replace(",", ".").toDoubleOrNull(),
                    biceps = bicepsText.replace(",", ".").toDoubleOrNull(),
                    thighs = thighsText.replace(",", ".").toDoubleOrNull()
                )
                showMeasurementDialog = false
                chestText = ""; waistText = ""; hipsText = ""; bicepsText = ""; thighsText = ""
            }
        ) {
            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                val fields = listOf(
                    "Pecho" to chestText,
                    "Cintura" to waistText,
                    "Cadera" to hipsText,
                    "Bíceps" to bicepsText,
                    "Muslos" to thighsText
                )
                val setters = listOf<(String) -> Unit>(
                    { chestText = it },
                    { waistText = it },
                    { hipsText = it },
                    { bicepsText = it },
                    { thighsText = it }
                )
                fields.forEachIndexed { index, (label, value) ->
                    OutlinedTextField(
                        value = value,
                        onValueChange = setters[index],
                        label = { Text(label) },
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                        singleLine = true,
                        modifier = Modifier.fillMaxWidth()
                    )
                }
            }
        }
    }

    // Confirmación eliminar peso
    weightToDelete?.let { weight ->
        ConfirmDeleteDialog(
            title = "Eliminar registro",
            message = "¿Eliminar el registro de %.1f kg?".format(weight.weight),
            onConfirm = { viewModel.deleteWeight(weight) },
            onDismiss = { weightToDelete = null }
        )
    }

    // Confirmación eliminar medida
    measurementToDelete?.let { measurement ->
        ConfirmDeleteDialog(
            title = "Eliminar medidas",
            message = "¿Eliminar las medidas del ${dateFormat.format(Date(measurement.date))}?",
            onConfirm = { viewModel.deleteMeasurement(measurement) },
            onDismiss = { measurementToDelete = null }
        )
    }
}

// ── Pestaña Peso ──
@Composable
private fun WeightTab(
    latestWeight: BodyWeightEntity?,
    weights: List<BodyWeightEntity>,
    dateFormat: SimpleDateFormat,
    onDeleteWeight: (BodyWeightEntity) -> Unit
) {
    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        verticalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        latestWeight?.let {
            item {
                StatCard(
                    label = "Peso actual",
                    value = "%.1f kg".format(it.weight),
                    icon = Icons.Default.MonitorWeight,
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp)
                )
            }
        }

        if (weights.isEmpty()) {
            item {
                EmptyStateMessage(
                    message = "No hay registros de peso.\nPulsa + para añadir uno.",
                    icon = Icons.Default.MonitorWeight
                )
            }
        } else {
            item { SectionHeader(title = "Historial de peso") }
            items(weights, key = { it.id }) { weight ->
                FitnessCard(
                    title = "%.1f kg".format(weight.weight),
                    subtitle = dateFormat.format(Date(weight.date)),
                    icon = Icons.Default.MonitorWeight,
                    onDelete = { onDeleteWeight(weight) },
                    modifier = Modifier.padding(horizontal = 16.dp)
                )
            }
            item { Spacer(modifier = Modifier.height(80.dp)) }
        }
    }
}

// ── Pestaña Medidas ──
@Composable
private fun MeasurementsTab(
    latestMeasurement: BodyMeasurementEntity?,
    measurements: List<BodyMeasurementEntity>,
    dateFormat: SimpleDateFormat,
    userHeight: Double?,
    latestWeight: BodyWeightEntity?,
    onEditHeight: () -> Unit,
    onDeleteMeasurement: (BodyMeasurementEntity) -> Unit
) {
    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        verticalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        // Resumen: altura + IMC
        item {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                StatCard(
                    label = "Altura",
                    value = if (userHeight != null) "%.1f cm".format(userHeight) else "Sin datos",
                    icon = Icons.Default.Height,
                    modifier = Modifier.weight(1f)
                )

                if (userHeight != null && userHeight > 0 && latestWeight != null) {
                    val heightM = userHeight / 100.0
                    val bmi = latestWeight.weight / (heightM * heightM)
                    StatCard(
                        label = "IMC",
                        value = "%.1f".format(bmi),
                        modifier = Modifier.weight(1f)
                    )
                }
            }
        }

        // Botón para editar/establecer altura
        item {
            androidx.compose.material3.OutlinedButton(
                onClick = onEditHeight,
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp),
                border = androidx.compose.foundation.BorderStroke(
                    1.dp,
                    MaterialTheme.colorScheme.primary
                )
            ) {
                Icon(
                    Icons.Default.Height,
                    contentDescription = null,
                    modifier = Modifier.padding(end = 8.dp),
                    tint = MaterialTheme.colorScheme.primary
                )
                Text(
                    text = if (userHeight != null) "Cambiar altura" else "Establecer altura",
                    color = MaterialTheme.colorScheme.primary
                )
            }
        }

        // Últimas medidas
        latestMeasurement?.let { m ->
            val row1 = listOfNotNull(
                m.chest?.let { "Pecho" to "%.1f cm".format(it) },
                m.waist?.let { "Cintura" to "%.1f cm".format(it) },
                m.hips?.let { "Cadera" to "%.1f cm".format(it) }
            )
            val row2 = listOfNotNull(
                m.biceps?.let { "Bíceps" to "%.1f cm".format(it) },
                m.thighs?.let { "Muslos" to "%.1f cm".format(it) }
            )

            if (row1.isNotEmpty()) {
                item {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 16.dp),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        row1.forEach { (label, value) ->
                            StatCard(
                                label = label,
                                value = value,
                                modifier = Modifier.weight(1f)
                            )
                        }
                    }
                }
            }

            if (row2.isNotEmpty()) {
                item {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 16.dp),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        row2.forEach { (label, value) ->
                            StatCard(
                                label = label,
                                value = value,
                                modifier = Modifier.weight(1f)
                            )
                        }
                    }
                }
            }
        }

        if (measurements.isEmpty()) {
            item {
                EmptyStateMessage(
                    message = "No hay medidas corporales.\nPulsa + para añadir.",
                    icon = Icons.Default.Straighten
                )
            }
        } else {
            item { SectionHeader(title = "Historial de medidas") }
            items(measurements, key = { it.id }) { measurement ->
                val parts = mutableListOf<String>()
                measurement.chest?.let { parts.add("Pecho: %.1f".format(it)) }
                measurement.waist?.let { parts.add("Cintura: %.1f".format(it)) }
                measurement.hips?.let { parts.add("Cadera: %.1f".format(it)) }
                measurement.biceps?.let { parts.add("Bíceps: %.1f".format(it)) }
                measurement.thighs?.let { parts.add("Muslos: %.1f".format(it)) }

                FitnessCard(
                    title = dateFormat.format(Date(measurement.date)),
                    subtitle = parts.joinToString(" · ") + " cm",
                    icon = Icons.Default.Straighten,
                    onDelete = { onDeleteMeasurement(measurement) },
                    modifier = Modifier.padding(horizontal = 16.dp)
                )
            }
            item { Spacer(modifier = Modifier.height(80.dp)) }
        }
    }
}

// ── Pestaña Salud ──
@Composable
private fun HealthTab(
    healthConditionsText: String,
    onHealthConditionsChange: (String) -> Unit,
    onSave: () -> Unit,
    healthDocuments: List<com.example.tfg_carloscaramecerero.data.local.entity.HealthDocumentEntity>,
    onUploadDocument: (android.net.Uri) -> Unit,
    onDeleteDocument: (com.example.tfg_carloscaramecerero.data.local.entity.HealthDocumentEntity) -> Unit
) {
    val focusManager = androidx.compose.ui.platform.LocalFocusManager.current
    var documentToDelete by remember {
        mutableStateOf<com.example.tfg_carloscaramecerero.data.local.entity.HealthDocumentEntity?>(null)
    }

    val pdfPickerLauncher = androidx.activity.compose.rememberLauncherForActivityResult(
        contract = androidx.activity.result.contract.ActivityResultContracts.OpenDocument()
    ) { uri ->
        uri?.let { onUploadDocument(it) }
    }

    val dateFormat = remember { SimpleDateFormat("dd/MM/yyyy HH:mm", Locale.getDefault()) }

    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        // ── Condiciones de salud ──
        item {
            SectionHeader(title = "Condiciones de salud")
        }

        item {
            Text(
                text = "Indica enfermedades crónicas, trastornos, alergias alimentarias, intolerancias, etc. El asistente de IA usará esta información para darte recomendaciones personalizadas.",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }

        item {
            OutlinedTextField(
                value = healthConditionsText,
                onValueChange = onHealthConditionsChange,
                label = { Text("Condiciones de salud") },
                placeholder = { Text("Ej: Alergia al gluten, diabetes tipo 2...") },
                leadingIcon = {
                    Icon(
                        Icons.Default.MedicalServices,
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.primary
                    )
                },
                minLines = 3,
                maxLines = 6,
                modifier = Modifier.fillMaxWidth(),
                colors = OutlinedTextFieldDefaults.colors(
                    focusedBorderColor = MaterialTheme.colorScheme.primary,
                    unfocusedBorderColor = MaterialTheme.colorScheme.outline
                )
            )
        }

        item {
            Button(
                onClick = {
                    focusManager.clearFocus()
                    onSave()
                },
                modifier = Modifier.fillMaxWidth(),
                colors = ButtonDefaults.buttonColors(
                    containerColor = MaterialTheme.colorScheme.primary
                )
            ) {
                Icon(
                    Icons.Default.Save,
                    contentDescription = null,
                    modifier = Modifier.padding(end = 8.dp)
                )
                Text("Guardar")
            }
        }

        // ── Documentos de salud ──
        item {
            Spacer(modifier = Modifier.height(8.dp))
            SectionHeader(title = "Analíticas y documentos")
        }

        item {
            Text(
                text = "Sube tus analíticas en PDF para que el asistente de IA pueda analizarlas y darte feedback más preciso.",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }

        item {
            androidx.compose.material3.OutlinedButton(
                onClick = { pdfPickerLauncher.launch(arrayOf("application/pdf")) },
                modifier = Modifier.fillMaxWidth(),
                border = androidx.compose.foundation.BorderStroke(
                    1.dp,
                    MaterialTheme.colorScheme.primary
                )
            ) {
                Icon(
                    Icons.Default.Add,
                    contentDescription = null,
                    modifier = Modifier.padding(end = 8.dp),
                    tint = MaterialTheme.colorScheme.primary
                )
                Text(
                    text = "Subir PDF",
                    color = MaterialTheme.colorScheme.primary
                )
            }
        }

        if (healthDocuments.isEmpty()) {
            item {
                EmptyStateMessage(
                    message = "No hay documentos subidos.\nPulsa \"Subir PDF\" para añadir una analítica.",
                    icon = Icons.Default.MedicalServices
                )
            }
        } else {
            items(healthDocuments, key = { it.id }) { document ->
                FitnessCard(
                    title = document.fileName,
                    subtitle = "Subido el ${dateFormat.format(Date(document.uploadDate))}",
                    icon = Icons.Default.MedicalServices,
                    onDelete = { documentToDelete = document },
                    modifier = Modifier
                )
            }
        }

        item { Spacer(modifier = Modifier.height(80.dp)) }
    }

    // Confirmación eliminar documento
    documentToDelete?.let { doc ->
        ConfirmDeleteDialog(
            title = "Eliminar documento",
            message = "¿Eliminar \"${doc.fileName}\"?",
            onConfirm = {
                onDeleteDocument(doc)
                documentToDelete = null
            },
            onDismiss = { documentToDelete = null }
        )
    }
}
