package com.example.tfg_carloscaramecerero.screens.nutrition

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import com.example.tfg_carloscaramecerero.components.ConfirmDeleteDialog
import com.example.tfg_carloscaramecerero.components.EmptyStateMessage
import com.example.tfg_carloscaramecerero.components.FitnessCard
import com.example.tfg_carloscaramecerero.components.FitnessFAB
import com.example.tfg_carloscaramecerero.components.FitnessInputDialog
import com.example.tfg_carloscaramecerero.components.FitnessTopBar
import com.example.tfg_carloscaramecerero.data.local.entity.FoodCatalogEntity
import com.example.tfg_carloscaramecerero.viewmodel.NutritionViewModel

@Composable
fun FoodCatalogScreen(
    viewModel: NutritionViewModel,
    onBackClick: () -> Unit
) {
    val catalogItems by viewModel.catalogItems.collectAsState()

    var showAddDialog by remember { mutableStateOf(false) }
    var itemToDelete by remember { mutableStateOf<FoodCatalogEntity?>(null) }

    // Estado del formulario de añadir
    var newName by remember { mutableStateOf("") }
    var newFoodType by remember { mutableStateOf("comida") }
    var newGramsText by remember { mutableStateOf("") }

    fun resetForm() {
        newName = ""
        newFoodType = "comida"
        newGramsText = ""
    }

    Scaffold(
        topBar = {
            FitnessTopBar(
                title = "Catálogo de alimentos",
                onBackClick = onBackClick
            )
        },
        floatingActionButton = {
            FitnessFAB(
                onClick = { showAddDialog = true },
                icon = Icons.Default.Add,
                contentDescription = "Añadir al catálogo"
            )
        }
    ) { padding ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding),
            verticalArrangement = Arrangement.spacedBy(8.dp),
            contentPadding = PaddingValues(horizontal = 16.dp, vertical = 8.dp)
        ) {
            if (catalogItems.isEmpty()) {
                item {
                    EmptyStateMessage(
                        message = "El catálogo está vacío",
                        subtitle = "Pulsa + para guardar tu primer alimento o bebida",
                        icon = Icons.Default.MenuBook
                    )
                }
            } else {
                // Separar por tipo
                val comidas = catalogItems.filter { it.foodType == "comida" }
                val bebidas = catalogItems.filter { it.foodType == "bebida" }

                if (comidas.isNotEmpty()) {
                    item {
                        Text(
                            "Comidas",
                            style = MaterialTheme.typography.titleSmall,
                            color = MaterialTheme.colorScheme.primary,
                            modifier = Modifier.padding(top = 4.dp, bottom = 2.dp)
                        )
                    }
                    items(comidas, key = { it.id }) { item ->
                        CatalogItemCard(
                            item = item,
                            onDelete = { itemToDelete = item }
                        )
                    }
                }

                if (bebidas.isNotEmpty()) {
                    item {
                        Text(
                            "Bebidas",
                            style = MaterialTheme.typography.titleSmall,
                            color = MaterialTheme.colorScheme.tertiary,
                            modifier = Modifier.padding(top = 8.dp, bottom = 2.dp)
                        )
                    }
                    items(bebidas, key = { it.id }) { item ->
                        CatalogItemCard(
                            item = item,
                            onDelete = { itemToDelete = item }
                        )
                    }
                }
            }

            item { Spacer(Modifier.height(80.dp)) }
        }
    }

    // ── Diálogo añadir ──
    if (showAddDialog) {
        FitnessInputDialog(
            title = "Añadir al catálogo",
            onDismiss = { showAddDialog = false; resetForm() },
            onConfirm = {
                if (newName.isNotBlank()) {
                    viewModel.addToCatalog(
                        name = newName,
                        foodType = newFoodType,
                        defaultGrams = newGramsText.toIntOrNull()
                    )
                    showAddDialog = false
                    resetForm()
                }
            }
        ) {
            Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                OutlinedTextField(
                    value = newName,
                    onValueChange = { newName = it },
                    label = { Text("Nombre") },
                    placeholder = { Text("Ej: Tostada con aguacate") },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )

                Text(
                    "Tipo:",
                    style = MaterialTheme.typography.labelLarge,
                    color = MaterialTheme.colorScheme.onSurface
                )
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    FilterChip(
                        selected = newFoodType == "comida",
                        onClick = { newFoodType = "comida" },
                        label = { Text("Comida") },
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
                        selected = newFoodType == "bebida",
                        onClick = { newFoodType = "bebida" },
                        label = { Text("Bebida") },
                        leadingIcon = {
                            Icon(
                                Icons.Default.LocalDrink,
                                contentDescription = null,
                                modifier = Modifier.size(16.dp)
                            )
                        },
                        colors = FilterChipDefaults.filterChipColors(
                            selectedContainerColor = MaterialTheme.colorScheme.tertiary,
                            selectedLabelColor = MaterialTheme.colorScheme.onTertiary
                        )
                    )
                }

                OutlinedTextField(
                    value = newGramsText,
                    onValueChange = { newGramsText = it.filter { c -> c.isDigit() } },
                    label = {
                        Text(if (newFoodType == "bebida") "ml por defecto (opcional)" else "Gramos por defecto (opcional)")
                    },
                    placeholder = { Text("Ej: 150") },
                    singleLine = true,
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                    modifier = Modifier.fillMaxWidth()
                )
            }
        }
    }

    // ── Confirmación eliminar ──
    itemToDelete?.let { item ->
        ConfirmDeleteDialog(
            title = "Eliminar del catálogo",
            message = "¿Eliminar \"${item.name}\" del catálogo?",
            onConfirm = {
                viewModel.deleteCatalogItem(item)
                itemToDelete = null
            },
            onDismiss = { itemToDelete = null }
        )
    }
}

@Composable
private fun CatalogItemCard(
    item: FoodCatalogEntity,
    onDelete: () -> Unit
) {
    val isBebida = item.foodType == "bebida"
    val accentColor = if (isBebida) MaterialTheme.colorScheme.tertiary
    else MaterialTheme.colorScheme.primary

    FitnessCard(
        title = item.name,
        icon = if (isBebida) Icons.Default.LocalDrink else Icons.Default.Restaurant,
        accentColor = accentColor,
        onDelete = onDelete
    ) {
        Row(
            horizontalArrangement = Arrangement.spacedBy(6.dp),
            modifier = Modifier.fillMaxWidth()
        ) {
            if (item.defaultGrams != null) {
                Surface(
                    shape = RoundedCornerShape(8.dp),
                    color = accentColor.copy(alpha = 0.1f)
                ) {
                    Text(
                        text = if (isBebida) "${item.defaultGrams}ml" else "${item.defaultGrams}g",
                        style = MaterialTheme.typography.labelSmall,
                        color = accentColor,
                        modifier = Modifier.padding(horizontal = 10.dp, vertical = 3.dp)
                    )
                }
            }
            if (item.calories != null) {
                Surface(
                    shape = RoundedCornerShape(8.dp),
                    color = MaterialTheme.colorScheme.secondary.copy(alpha = 0.1f)
                ) {
                    Text(
                        text = "%.0f kcal".format(item.calories),
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.secondary,
                        fontWeight = FontWeight.Medium,
                        modifier = Modifier.padding(horizontal = 10.dp, vertical = 3.dp)
                    )
                }
            }
        }
    }
}

