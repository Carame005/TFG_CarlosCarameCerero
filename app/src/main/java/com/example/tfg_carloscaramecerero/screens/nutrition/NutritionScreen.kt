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
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.LocalDrink
import androidx.compose.material.icons.filled.Restaurant
import androidx.compose.material.icons.filled.Schedule
import androidx.compose.material.icons.filled.SmartToy
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
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
import com.example.tfg_carloscaramecerero.data.local.entity.FoodEntryEntity
import com.example.tfg_carloscaramecerero.viewmodel.MealItemInput
import com.example.tfg_carloscaramecerero.viewmodel.NutritionViewModel

@Composable
fun NutritionScreen(viewModel: NutritionViewModel) {
    val allEntries by viewModel.allEntries.collectAsState()
    val selectedDay by viewModel.selectedDay.collectAsState()
    val daysWithEntries by viewModel.daysWithEntries.collectAsState()

    val entriesForDay by remember(allEntries, selectedDay) {
        derivedStateOf { allEntries.filter { it.dayOfWeek == selectedDay } }
    }

    var showAddDialog by remember { mutableStateOf(false) }
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
            // ── Selector de día de la semana ──
            item {
                Column(modifier = Modifier.padding(top = 8.dp)) {
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
                        text = "Describe lo que comes. El asistente de IA analizará tu dieta y te dará recomendaciones personalizadas.",
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
                        message = "No hay comidas registradas para este día.\nPulsa + para añadir.",
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
                                    // Badge comida/bebida
                                    NutritionBadge(
                                        text = if (entry.foodType == "bebida") "Bebida" else "Comida",
                                        color = if (entry.foodType == "bebida")
                                            MaterialTheme.colorScheme.tertiary
                                        else MaterialTheme.colorScheme.primary
                                    )

                                    // Badge gramos/ml
                                    if (entry.grams != null) {
                                        NutritionBadge(
                                            text = if (entry.foodType == "bebida") "${entry.grams}ml"
                                                   else "${entry.grams}g",
                                            color = MaterialTheme.colorScheme.secondary
                                        )
                                    }

                                    // Badge hora
                                    if (entry.time.isNotBlank()) {
                                        NutritionBadge(
                                            text = entry.time,
                                            color = MaterialTheme.colorScheme.onSurfaceVariant
                                        )
                                    }
                                }

                                // Badge de estado IA
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
                // Hora (opcional)
                OutlinedTextField(
                    value = mealTime,
                    onValueChange = { mealTime = it },
                    label = { Text("Hora (opcional)") },
                    placeholder = { Text("Ej: 08:30") },
                    singleLine = true,
                    leadingIcon = {
                        Icon(Icons.Default.Schedule, contentDescription = null)
                    },
                    modifier = Modifier.fillMaxWidth()
                )

                // Selector tipo de comida
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

                // Día
                Text(
                    text = "Día: ${NutritionViewModel.DAY_NAMES.getOrElse(selectedDay - 1) { "" }}",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )

                HorizontalDivider()

                // Lista de items
                Text(
                    "Alimentos:",
                    style = MaterialTheme.typography.labelLarge,
                    color = MaterialTheme.colorScheme.onSurface
                )

                mealItems.forEachIndexed { index, item ->
                    MealItemRow(
                        item = item,
                        onUpdate = { updated -> mealItems[index] = updated },
                        onRemove = if (mealItems.size > 1) {
                            { mealItems.removeAt(index) }
                        } else null
                    )
                }

                // Botón añadir otro item
                TextButton(
                    onClick = { mealItems.add(MealItemInput()) },
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Icon(
                        Icons.Default.Add,
                        contentDescription = null,
                        modifier = Modifier.size(18.dp)
                    )
                    Spacer(modifier = Modifier.width(6.dp))
                    Text("Añadir otro alimento")
                }
            }
        }
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
private fun MealItemRow(
    item: MealItemInput,
    onUpdate: (MealItemInput) -> Unit,
    onRemove: (() -> Unit)?
) {
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

        // Descripción
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
            modifier = Modifier.fillMaxWidth()
        )

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



