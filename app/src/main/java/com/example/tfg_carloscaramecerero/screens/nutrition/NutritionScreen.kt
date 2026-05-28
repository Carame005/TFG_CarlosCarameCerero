package com.example.tfg_carloscaramecerero.screens.nutrition

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Bookmark
import androidx.compose.material.icons.filled.BookmarkBorder
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.LocalDrink
import androidx.compose.material.icons.filled.MenuBook
import androidx.compose.material.icons.filled.Restaurant
import androidx.compose.material.icons.filled.Schedule
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.SmartToy
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SuggestionChip
import androidx.compose.material3.SuggestionChipDefaults
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.example.tfg_carloscaramecerero.components.ConfirmDeleteDialog
import com.example.tfg_carloscaramecerero.components.EmptyStateMessage
import com.example.tfg_carloscaramecerero.components.FitnessCard
import com.example.tfg_carloscaramecerero.components.FitnessFAB
import com.example.tfg_carloscaramecerero.components.FitnessInputDialog
import com.example.tfg_carloscaramecerero.components.FitnessTopBar
import com.example.tfg_carloscaramecerero.components.SectionHeader
import com.example.tfg_carloscaramecerero.data.local.entity.FoodCatalogEntity
import com.example.tfg_carloscaramecerero.data.local.entity.FoodEntryEntity
import com.example.tfg_carloscaramecerero.viewmodel.MealItemInput
import com.example.tfg_carloscaramecerero.viewmodel.NutritionViewModel

@Composable
fun NutritionScreen(
    viewModel: NutritionViewModel,
    onNavigateToSchedules: () -> Unit,
    onNavigateToCatalog: () -> Unit = {}
) {
    val schedules by viewModel.schedules.collectAsState()
    val currentScheduleId by viewModel.currentScheduleId.collectAsState()
    val allEntries by viewModel.allEntries.collectAsState()
    val selectedDay by viewModel.selectedDay.collectAsState()
    val daysWithEntries by viewModel.daysWithEntries.collectAsState()
    val catalogItems by viewModel.catalogItems.collectAsState()

    val entriesForDay by remember(allEntries, selectedDay) {
        derivedStateOf { allEntries.filter { it.dayOfWeek == selectedDay } }
    }

    var showAddDialog by remember { mutableStateOf(false) }
    var showCatalogPicker by remember { mutableStateOf(false) }
    var selectedMealType by remember { mutableStateOf("almuerzo") }
    var mealTime by remember { mutableStateOf("") }
    val mealItems = remember { mutableStateListOf(MealItemInput()) }
    var entryToDelete by remember { mutableStateOf<FoodEntryEntity?>(null) }

    Scaffold(
        topBar = { FitnessTopBar(title = "Mi alimentación") },
        floatingActionButton = {
            FitnessFAB(
                onClick = { showAddDialog = true },
                icon = Icons.Default.Add,
                contentDescription = "Registrar comida"
            )
        }
    ) { padding ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            // ── Horario activo ──
            item {
                val activeSchedule = schedules.find { it.id == currentScheduleId }
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp, vertical = 8.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(
                            Icons.Default.Schedule,
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.primary,
                            modifier = Modifier.size(20.dp)
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Column {
                            Text(
                                text = activeSchedule?.name ?: "Sin horario",
                                style = MaterialTheme.typography.titleSmall,
                                fontWeight = androidx.compose.ui.text.font.FontWeight.SemiBold,
                                color = MaterialTheme.colorScheme.onSurface
                            )
                            Text(
                                text = "Horario activo",
                                style = MaterialTheme.typography.labelSmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }
                    TextButton(onClick = onNavigateToSchedules) {
                        Text("Gestionar", style = MaterialTheme.typography.labelMedium)
                    }
                    TextButton(onClick = onNavigateToCatalog) {
                        Icon(
                            Icons.Default.MenuBook,
                            contentDescription = null,
                            modifier = Modifier.size(16.dp)
                        )
                        Spacer(Modifier.width(4.dp))
                        Text("Catálogo", style = MaterialTheme.typography.labelMedium)
                    }
                }
            }

            // ── Selector de día de la semana ──
            item {
                Column(modifier = Modifier.padding(top = 4.dp)) {
                    SectionHeader(title = "Horario semanal")
                    DaySelector(
                        selectedDay = selectedDay,
                        daysWithEntries = daysWithEntries,
                        onDaySelected = { viewModel.selectDay(it) }
                    )
                }
            }

            // ── Info para la IA ──
            item {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp)
                        .clip(RoundedCornerShape(12.dp))
                        .background(MaterialTheme.colorScheme.surfaceVariant)
                        .padding(12.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(
                        Icons.Default.SmartToy,
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.primary,
                        modifier = Modifier.size(24.dp)
                    )
                    Spacer(modifier = Modifier.width(12.dp))
                    Text(
                        text = "El asistente de IA crea horarios nuevos sin sobreescribir los que ya tienes.",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }

            // ── Resumen del día ──
            item {
                val dayName = NutritionViewModel.DAY_NAMES.getOrElse(selectedDay - 1) { "" }
                val mealCount = entriesForDay.size
                SectionHeader(
                    title = "$dayName — $mealCount ${if (mealCount == 1) "comida" else "comidas"}"
                )
            }

            // ── Comidas del día agrupadas por mealType ──
            if (entriesForDay.isEmpty()) {
                item {
                    EmptyStateMessage(
                        message = "No hay comidas registradas",
                        subtitle = "Pulsa + para añadir la primera comida del día",
                        icon = Icons.Default.Restaurant
                    )
                }
            } else {
                NutritionViewModel.MEAL_TYPES.forEach { mealType ->
                    val mealEntries = entriesForDay.filter { it.mealType == mealType }
                    if (mealEntries.isNotEmpty()) {
                        item {
                            Text(
                                text = NutritionViewModel.MEAL_LABELS[mealType] ?: mealType,
                                style = MaterialTheme.typography.titleSmall,
                                color = MaterialTheme.colorScheme.primary,
                                modifier = Modifier.padding(horizontal = 16.dp, vertical = 4.dp)
                            )
                        }
                        items(mealEntries, key = { it.id }) { entry ->
                            FitnessCard(
                                title = entry.description,
                                icon = if (entry.foodType == "bebida") Icons.Default.LocalDrink
                                       else Icons.Default.Restaurant,
                                accentColor = if (entry.foodType == "bebida")
                                    MaterialTheme.colorScheme.tertiary
                                else MaterialTheme.colorScheme.primary,
                                onDelete = { entryToDelete = entry },
                                modifier = Modifier.padding(horizontal = 16.dp)
                            ) {
                                Row(
                                    horizontalArrangement = Arrangement.spacedBy(6.dp),
                                    modifier = Modifier.fillMaxWidth()
                                ) {
                                    NutritionBadge(
                                        text = if (entry.foodType == "bebida") "Bebida" else "Comida",
                                        color = if (entry.foodType == "bebida")
                                            MaterialTheme.colorScheme.tertiary
                                        else MaterialTheme.colorScheme.primary
                                    )
                                    if (entry.grams != null) {
                                        NutritionBadge(
                                            text = if (entry.foodType == "bebida") "${entry.grams}ml"
                                                   else "${entry.grams}g",
                                            color = MaterialTheme.colorScheme.secondary
                                        )
                                    }
                                    if (entry.time.isNotBlank()) {
                                        NutritionBadge(
                                            text = entry.time,
                                            color = MaterialTheme.colorScheme.onSurfaceVariant
                                        )
                                    }
                                }
                                if (entry.aiAnalyzed && entry.calories != null) {
                                    Spacer(modifier = Modifier.height(6.dp))
                                    Row(
                                        verticalAlignment = Alignment.CenterVertically,
                                        modifier = Modifier
                                            .clip(RoundedCornerShape(8.dp))
                                            .background(MaterialTheme.colorScheme.primary.copy(alpha = 0.1f))
                                            .padding(horizontal = 10.dp, vertical = 4.dp)
                                    ) {
                                        Icon(
                                            Icons.Default.SmartToy,
                                            contentDescription = null,
                                            tint = MaterialTheme.colorScheme.primary,
                                            modifier = Modifier.size(14.dp)
                                        )
                                        Spacer(modifier = Modifier.width(4.dp))
                                        Text(
                                            text = "%.0f kcal".format(entry.calories),
                                            style = MaterialTheme.typography.labelSmall,
                                            color = MaterialTheme.colorScheme.primary
                                        )
                                        if (entry.protein != null) {
                                            Text(
                                                text = " · P:%.0fg · C:%.0fg · G:%.0fg".format(
                                                    entry.protein, entry.carbs, entry.fat
                                                ),
                                                style = MaterialTheme.typography.labelSmall,
                                                color = MaterialTheme.colorScheme.primary
                                            )
                                        }
                                    }
                                }
                            }
                        }
                    }
                }
            }

            item { Spacer(modifier = Modifier.height(80.dp)) }
        }
    }

    // ── Dialog para añadir comida ──
    if (showAddDialog) {
        FitnessInputDialog(
            title = "Registrar comida",
            onDismiss = {
                showAddDialog = false
                mealItems.clear()
                mealItems.add(MealItemInput())
                mealTime = ""
            },
            onConfirm = {
                val validItems = mealItems.filter { it.description.isNotBlank() }
                if (validItems.isNotEmpty()) {
                    viewModel.addMultipleMealEntries(
                        entries = validItems,
                        mealType = selectedMealType,
                        dayOfWeek = selectedDay,
                        time = mealTime.trim()
                    )
                    showAddDialog = false
                    mealItems.clear()
                    mealItems.add(MealItemInput())
                    mealTime = ""
                }
            }
        ) {
            Column(
                modifier = Modifier.verticalScroll(rememberScrollState()),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                OutlinedTextField(
                    value = mealTime,
                    onValueChange = { mealTime = it },
                    label = { Text("Hora (opcional)") },
                    placeholder = { Text("Ej: 08:30") },
                    singleLine = true,
                    leadingIcon = { Icon(Icons.Default.Schedule, contentDescription = null) },
                    modifier = Modifier.fillMaxWidth()
                )
                Text(
                    "Tipo de comida:",
                    style = MaterialTheme.typography.labelLarge,
                    color = MaterialTheme.colorScheme.onSurface
                )
                FlowRow(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(6.dp),
                    verticalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    NutritionViewModel.MEAL_TYPES.forEach { type ->
                        FilterChip(
                            selected = selectedMealType == type,
                            onClick = { selectedMealType = type },
                            label = {
                                Text(
                                    type.replaceFirstChar { it.uppercase() },
                                    style = MaterialTheme.typography.labelSmall
                                )
                            },
                            colors = FilterChipDefaults.filterChipColors(
                                selectedContainerColor = MaterialTheme.colorScheme.primary,
                                selectedLabelColor = MaterialTheme.colorScheme.onPrimary
                            )
                        )
                    }
                }
                Text(
                    text = "Día: ${NutritionViewModel.DAY_NAMES.getOrElse(selectedDay - 1) { "" }}",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                HorizontalDivider()
                Text(
                    "Alimentos:",
                    style = MaterialTheme.typography.labelLarge,
                    color = MaterialTheme.colorScheme.onSurface
                )
                mealItems.forEachIndexed { index, item ->
                    MealItemRow(
                        item = item,
                        catalogItems = catalogItems,
                        onUpdate = { updated -> mealItems[index] = updated },
                        onRemove = if (mealItems.size > 1) {
                            { mealItems.removeAt(index) }
                        } else null,
                        onSaveToCatalog = { name, foodType, grams ->
                            viewModel.addToCatalog(name, foodType, grams)
                        }
                    )
                }
                TextButton(
                    onClick = { mealItems.add(MealItemInput()) },
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Icon(Icons.Default.Add, contentDescription = null, modifier = Modifier.size(18.dp))
                    Spacer(modifier = Modifier.width(6.dp))
                    Text("Añadir otro alimento")
                }
                // Botón para seleccionar desde el catálogo
                if (catalogItems.isNotEmpty()) {
                    OutlinedButton(
                        onClick = { showCatalogPicker = true },
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(10.dp)
                    ) {
                        Icon(Icons.Default.MenuBook, contentDescription = null, modifier = Modifier.size(18.dp))
                        Spacer(modifier = Modifier.width(6.dp))
                        Text("Añadir desde mi catálogo")
                    }
                }
            }
        }
    }

    // ── Selector del catálogo ──
    if (showCatalogPicker) {
        CatalogPickerDialog(
            catalogItems = catalogItems,
            onSelect = { selected ->
                // Si el último row está vacío, úsalo; si no, añade uno nuevo
                val emptyIdx = mealItems.indexOfFirst { it.description.isBlank() }
                val newItem = MealItemInput(
                    description = selected.name,
                    foodType = selected.foodType,
                    grams = selected.defaultGrams
                )
                if (emptyIdx >= 0) mealItems[emptyIdx] = newItem
                else mealItems.add(newItem)
                showCatalogPicker = false
            },
            onDismiss = { showCatalogPicker = false }
        )
    }

    // Confirmación eliminar comida
    entryToDelete?.let { entry ->
        ConfirmDeleteDialog(
            title = "Eliminar comida",
            message = "¿Eliminar \"${entry.description}\"?",
            onConfirm = { viewModel.deleteEntry(entry) },
            onDismiss = { entryToDelete = null }
        )
    }
}

// ── Composables auxiliares ──


@Composable
private fun DaySelector(
    selectedDay: Int,
    daysWithEntries: List<Int>,
    onDaySelected: (Int) -> Unit
) {
    val shortNames = listOf("L", "M", "X", "J", "V", "S", "D")

    LazyRow(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 12.dp, vertical = 8.dp),
        horizontalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        itemsIndexed(shortNames) { index, shortName ->
            val day = index + 1
            val isSelected = day == selectedDay
            val hasEntries = day in daysWithEntries

            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                modifier = Modifier
                    .clip(RoundedCornerShape(12.dp))
                    .background(
                        if (isSelected) MaterialTheme.colorScheme.primary
                        else MaterialTheme.colorScheme.surfaceVariant
                    )
                    .clickable { onDaySelected(day) }
                    .padding(horizontal = 14.dp, vertical = 10.dp)
            ) {
                Text(
                    text = shortName,
                    style = MaterialTheme.typography.titleSmall,
                    fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal,
                    color = if (isSelected) MaterialTheme.colorScheme.onPrimary
                    else MaterialTheme.colorScheme.onSurfaceVariant,
                    textAlign = TextAlign.Center
                )
                Spacer(modifier = Modifier.height(4.dp))
                // Indicador de que hay comidas registradas
                Box(
                    modifier = Modifier
                        .size(6.dp)
                        .clip(CircleShape)
                        .background(
                            when {
                                isSelected -> MaterialTheme.colorScheme.onPrimary.copy(alpha = if (hasEntries) 1f else 0.3f)
                                hasEntries -> MaterialTheme.colorScheme.primary
                                else -> MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.2f)
                            }
                        )
                )
            }
        }
    }
}

@Composable
private fun NutritionBadge(
    text: String,
    color: androidx.compose.ui.graphics.Color
) {
    Box(
        modifier = Modifier
            .clip(RoundedCornerShape(8.dp))
            .background(color.copy(alpha = 0.1f))
            .padding(horizontal = 10.dp, vertical = 3.dp)
    ) {
        Text(
            text = text,
            style = MaterialTheme.typography.labelSmall,
            color = color
        )
    }
}

@Composable
private fun CatalogPickerDialog(
    catalogItems: List<FoodCatalogEntity>,
    onSelect: (FoodCatalogEntity) -> Unit,
    onDismiss: () -> Unit
) {
    var query by remember { mutableStateOf("") }
    val filtered = remember(query, catalogItems) {
        if (query.isBlank()) catalogItems
        else catalogItems.filter { it.name.contains(query, ignoreCase = true) }
    }
    val comidas = filtered.filter { it.foodType == "comida" }
    val bebidas = filtered.filter { it.foodType == "bebida" }

    AlertDialog(
        onDismissRequest = onDismiss,
        icon = {
            Icon(Icons.Default.MenuBook, contentDescription = null,
                tint = MaterialTheme.colorScheme.primary)
        },
        title = { Text("Mi catálogo") },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                OutlinedTextField(
                    value = query,
                    onValueChange = { query = it },
                    placeholder = { Text("Buscar…") },
                    leadingIcon = {
                        Icon(Icons.Default.Search, contentDescription = null,
                            modifier = Modifier.size(18.dp))
                    },
                    trailingIcon = {
                        if (query.isNotEmpty()) {
                            IconButton(onClick = { query = "" }, modifier = Modifier.size(32.dp)) {
                                Icon(Icons.Default.Close, contentDescription = "Borrar",
                                    modifier = Modifier.size(16.dp))
                            }
                        }
                    },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )
                if (filtered.isEmpty()) {
                    Text(
                        "No hay resultados",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        modifier = Modifier.fillMaxWidth().padding(vertical = 12.dp),
                        textAlign = TextAlign.Center
                    )
                } else {
                    Column(
                        modifier = Modifier.fillMaxWidth().verticalScroll(rememberScrollState()),
                        verticalArrangement = Arrangement.spacedBy(4.dp)
                    ) {
                        if (comidas.isNotEmpty()) {
                            Text(
                                "Comidas",
                                style = MaterialTheme.typography.labelMedium,
                                color = MaterialTheme.colorScheme.primary,
                                modifier = Modifier.padding(top = 4.dp)
                            )
                            comidas.forEach { item ->
                                CatalogPickerItem(item = item, onClick = { onSelect(item) })
                            }
                        }
                        if (bebidas.isNotEmpty()) {
                            Text(
                                "Bebidas",
                                style = MaterialTheme.typography.labelMedium,
                                color = MaterialTheme.colorScheme.tertiary,
                                modifier = Modifier.padding(top = 8.dp)
                            )
                            bebidas.forEach { item ->
                                CatalogPickerItem(item = item, onClick = { onSelect(item) })
                            }
                        }
                    }
                }
            }
        },
        confirmButton = {},
        dismissButton = { TextButton(onClick = onDismiss) { Text("Cancelar") } }
    )
}

@Composable
private fun CatalogPickerItem(
    item: FoodCatalogEntity,
    onClick: () -> Unit
) {
    val isBebida = item.foodType == "bebida"
    val accentColor = if (isBebida) MaterialTheme.colorScheme.tertiary
    else MaterialTheme.colorScheme.primary
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(10.dp))
            .background(MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f))
            .clickable { onClick() }
            .padding(horizontal = 12.dp, vertical = 10.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(10.dp)
    ) {
        Icon(
            imageVector = if (isBebida) Icons.Default.LocalDrink else Icons.Default.Restaurant,
            contentDescription = null,
            tint = accentColor,
            modifier = Modifier.size(20.dp)
        )
        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = item.name,
                style = MaterialTheme.typography.bodyMedium,
                fontWeight = FontWeight.Medium,
                color = MaterialTheme.colorScheme.onSurface
            )
            if (item.defaultGrams != null || item.calories != null) {
                Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                    if (item.defaultGrams != null) {
                        Text(
                            text = if (isBebida) "${item.defaultGrams}ml" else "${item.defaultGrams}g",
                            style = MaterialTheme.typography.labelSmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                    if (item.calories != null) {
                        Text(
                            text = "· %.0f kcal".format(item.calories),
                            style = MaterialTheme.typography.labelSmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
            }
        }
        Icon(
            Icons.Default.Add, contentDescription = "Añadir",
            tint = accentColor, modifier = Modifier.size(18.dp)
        )
    }
}

@Composable
private fun MealItemRow(
    item: MealItemInput,
    catalogItems: List<FoodCatalogEntity>,
    onUpdate: (MealItemInput) -> Unit,
    onRemove: (() -> Unit)?,
    onSaveToCatalog: (name: String, foodType: String, grams: Int?) -> Unit
) {
    // Sugerencias del catálogo filtradas por texto escrito
    val suggestions = remember(item.description, catalogItems) {
        if (item.description.length < 2) emptyList()
        else catalogItems.filter { cat ->
            cat.name.contains(item.description, ignoreCase = true) &&
                    cat.name.lowercase() != item.description.lowercase()
        }.take(5)
    }

    // ¿Ya está guardado en catálogo?
    val alreadyInCatalog = remember(item.description, catalogItems) {
        catalogItems.any { it.name.equals(item.description.trim(), ignoreCase = true) }
    }

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(12.dp))
            .background(MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.4f))
            .padding(12.dp),
        verticalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        // Cabecera con botón eliminar
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Selector comida/bebida
            Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                FilterChip(
                    selected = item.foodType == "comida",
                    onClick = { onUpdate(item.copy(foodType = "comida")) },
                    label = { Text("Comida", style = MaterialTheme.typography.labelSmall) },
                    leadingIcon = {
                        Icon(
                            Icons.Default.Restaurant,
                            contentDescription = null,
                            modifier = Modifier.size(16.dp)
                        )
                    },
                    colors = FilterChipDefaults.filterChipColors(
                        selectedContainerColor = MaterialTheme.colorScheme.primary,
                        selectedLabelColor = MaterialTheme.colorScheme.onPrimary
                    )
                )
                FilterChip(
                    selected = item.foodType == "bebida",
                    onClick = { onUpdate(item.copy(foodType = "bebida")) },
                    label = { Text("Bebida", style = MaterialTheme.typography.labelSmall) },
                    leadingIcon = {
                        Icon(
                            Icons.Default.LocalDrink,
                            contentDescription = null,
                            modifier = Modifier.size(16.dp)
                        )
                    },
                    colors = FilterChipDefaults.filterChipColors(
                        selectedContainerColor = MaterialTheme.colorScheme.primary,
                        selectedLabelColor = MaterialTheme.colorScheme.onPrimary
                    )
                )
            }

            if (onRemove != null) {
                IconButton(
                    onClick = onRemove,
                    modifier = Modifier.size(32.dp)
                ) {
                    Icon(
                        Icons.Default.Close,
                        contentDescription = "Quitar",
                        tint = MaterialTheme.colorScheme.error,
                        modifier = Modifier.size(18.dp)
                    )
                }
            }
        }

        // Descripción + botón guardar en catálogo
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically
        ) {
            OutlinedTextField(
                value = item.description,
                onValueChange = { onUpdate(item.copy(description = it)) },
                label = {
                    Text(
                        if (item.foodType == "bebida") "¿Qué has bebido?" else "¿Qué has comido?"
                    )
                },
                placeholder = {
                    Text(
                        if (item.foodType == "bebida") "Ej: Zumo de naranja" else "Ej: Tostada con aguacate"
                    )
                },
                singleLine = true,
                modifier = Modifier.weight(1f)
            )
            // Botón guardar en catálogo
            if (item.description.isNotBlank()) {
                IconButton(
                    onClick = {
                        if (!alreadyInCatalog) {
                            onSaveToCatalog(item.description.trim(), item.foodType, item.grams)
                        }
                    },
                    modifier = Modifier.size(40.dp)
                ) {
                    Icon(
                        imageVector = if (alreadyInCatalog) Icons.Default.Bookmark else Icons.Default.BookmarkBorder,
                        contentDescription = if (alreadyInCatalog) "Ya en catálogo" else "Guardar en catálogo",
                        tint = if (alreadyInCatalog) MaterialTheme.colorScheme.primary
                               else MaterialTheme.colorScheme.onSurfaceVariant,
                        modifier = Modifier.size(20.dp)
                    )
                }
            }
        }

        // Sugerencias del catálogo
        if (suggestions.isNotEmpty()) {
            LazyRow(
                horizontalArrangement = Arrangement.spacedBy(6.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                items(suggestions, key = { it.id }) { suggestion ->
                    SuggestionChip(
                        onClick = {
                            onUpdate(
                                item.copy(
                                    description = suggestion.name,
                                    foodType = suggestion.foodType,
                                    grams = suggestion.defaultGrams ?: item.grams
                                )
                            )
                        },
                        label = {
                            Text(
                                suggestion.name,
                                style = MaterialTheme.typography.labelSmall,
                                maxLines = 1
                            )
                        },
                        icon = {
                            Icon(
                                imageVector = if (suggestion.foodType == "bebida")
                                    Icons.Default.LocalDrink else Icons.Default.Restaurant,
                                contentDescription = null,
                                modifier = Modifier.size(14.dp)
                            )
                        },
                        colors = SuggestionChipDefaults.suggestionChipColors(
                            containerColor = MaterialTheme.colorScheme.surfaceVariant
                        )
                    )
                }
            }
        }

        // Gramos (opcional)
        OutlinedTextField(
            value = item.grams?.toString() ?: "",
            onValueChange = { text ->
                val grams = text.filter { it.isDigit() }.toIntOrNull()
                onUpdate(item.copy(grams = grams))
            },
            label = {
                Text(if (item.foodType == "bebida") "ml (opcional)" else "Gramos (opcional)")
            },
            placeholder = {
                Text(if (item.foodType == "bebida") "Ej: 250" else "Ej: 150")
            },
            singleLine = true,
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
            modifier = Modifier.fillMaxWidth()
        )
    }
}



