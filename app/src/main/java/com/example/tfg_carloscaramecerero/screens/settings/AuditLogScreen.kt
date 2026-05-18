package com.example.tfg_carloscaramecerero.screens.settings

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.example.tfg_carloscaramecerero.components.FitnessTopBar
import com.example.tfg_carloscaramecerero.data.local.entity.AuditLogEntity
import com.example.tfg_carloscaramecerero.viewmodel.AuditLogViewModel
import java.text.SimpleDateFormat
import java.util.*

@Composable
fun AuditLogScreen(
    viewModel: AuditLogViewModel,
    onBackClick: () -> Unit
) {
    val entries by viewModel.filteredEntries.collectAsState()
    val selectedCategory by viewModel.selectedCategory.collectAsState()
    var showConfirmDialog by remember { mutableStateOf(false) }

    if (showConfirmDialog) {
        AlertDialog(
            onDismissRequest = { showConfirmDialog = false },
            icon = { Icon(Icons.Default.DeleteForever, contentDescription = null) },
            title = { Text("Limpiar registro") },
            text = { Text("Se eliminarán todas las entradas del registro de auditoría. Esta acción no se puede deshacer.") },
            confirmButton = {
                TextButton(onClick = {
                    viewModel.clearLog()
                    showConfirmDialog = false
                }) { Text("Limpiar", color = MaterialTheme.colorScheme.error) }
            },
            dismissButton = {
                TextButton(onClick = { showConfirmDialog = false }) { Text("Cancelar") }
            }
        )
    }

    Scaffold(
        topBar = {
            FitnessTopBar(
                title = "Registro de acciones",
                onBackClick = onBackClick,
                actions = {
                    IconButton(onClick = { showConfirmDialog = true }) {
                        Icon(
                            Icons.Default.DeleteForever,
                            contentDescription = "Limpiar registro",
                            tint = MaterialTheme.colorScheme.error
                        )
                    }
                }
            )
        }
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
        ) {
            // Filtros de categoría
            LazyRow(
                contentPadding = PaddingValues(horizontal = 16.dp, vertical = 8.dp),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                item {
                    FilterChip(
                        selected = selectedCategory == null,
                        onClick = { viewModel.selectCategory(null) },
                        label = { Text("Todos") },
                        leadingIcon = if (selectedCategory == null) {
                            { Icon(Icons.Default.CheckCircle, contentDescription = null, modifier = Modifier.size(16.dp)) }
                        } else null
                    )
                }
                items(AuditLogViewModel.CATEGORIES) { cat ->
                    FilterChip(
                        selected = selectedCategory == cat,
                        onClick = { viewModel.selectCategory(if (selectedCategory == cat) null else cat) },
                        label = { Text(cat) },
                        leadingIcon = if (selectedCategory == cat) {
                            { Icon(Icons.Default.CheckCircle, contentDescription = null, modifier = Modifier.size(16.dp)) }
                        } else null
                    )
                }
            }

            HorizontalDivider()

            if (entries.isEmpty()) {
                Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Icon(
                            Icons.Default.History,
                            contentDescription = null,
                            modifier = Modifier.size(56.dp),
                            tint = MaterialTheme.colorScheme.outlineVariant
                        )
                        Text(
                            "No hay acciones registradas",
                            style = MaterialTheme.typography.bodyLarge,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                        Text(
                            "Las acciones que realices aparecerán aquí",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.outlineVariant
                        )
                    }
                }
            } else {
                LazyColumn(
                    contentPadding = PaddingValues(horizontal = 16.dp, vertical = 8.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    items(entries) { entry ->
                        AuditLogEntryCard(entry = entry)
                    }
                    item { Spacer(Modifier.height(8.dp)) }
                }
            }
        }
    }
}

@Composable
private fun AuditLogEntryCard(entry: AuditLogEntity) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
    ) {
        Row(
            modifier = Modifier.padding(12.dp),
            horizontalArrangement = Arrangement.spacedBy(12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Icono de categoría
            Surface(
                shape = RoundedCornerShape(8.dp),
                color = categoryColor(entry.category).copy(alpha = 0.15f),
                modifier = Modifier.size(40.dp)
            ) {
                Box(contentAlignment = Alignment.Center) {
                    Icon(
                        imageVector = categoryIcon(entry.category),
                        contentDescription = null,
                        tint = categoryColor(entry.category),
                        modifier = Modifier.size(20.dp)
                    )
                }
            }

            // Texto principal
            Column(modifier = Modifier.weight(1f)) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    Text(
                        entry.action,
                        style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.SemiBold),
                        modifier = Modifier.weight(1f, fill = false)
                    )
                    Surface(
                        shape = RoundedCornerShape(4.dp),
                        color = categoryColor(entry.category).copy(alpha = 0.12f)
                    ) {
                        Text(
                            entry.category,
                            style = MaterialTheme.typography.labelSmall,
                            color = categoryColor(entry.category),
                            modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                        )
                    }
                }
                if (entry.detail.isNotBlank()) {
                    Text(
                        entry.detail,
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        maxLines = 2
                    )
                }
                Text(
                    formatAuditTimestamp(entry.timestamp),
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.outline
                )
            }
        }
    }
}

@Composable
private fun categoryColor(category: String) = when (category) {
    "Entrenamiento" -> MaterialTheme.colorScheme.primary
    "Nutrición"     -> MaterialTheme.colorScheme.tertiary
    "Cuerpo"        -> MaterialTheme.colorScheme.secondary
    "Sistema"       -> MaterialTheme.colorScheme.outline
    else            -> MaterialTheme.colorScheme.primary
}

private fun categoryIcon(category: String): ImageVector = when (category) {
    "Entrenamiento" -> Icons.Default.FitnessCenter
    "Nutrición"     -> Icons.Default.Restaurant
    "Cuerpo"        -> Icons.Default.MonitorWeight
    "Sistema"       -> Icons.Default.Settings
    else            -> Icons.Default.Circle
}

private fun formatAuditTimestamp(timestamp: Long): String {
    val cal = Calendar.getInstance()
    val today = (cal.clone() as Calendar).apply {
        set(Calendar.HOUR_OF_DAY, 0); set(Calendar.MINUTE, 0)
        set(Calendar.SECOND, 0); set(Calendar.MILLISECOND, 0)
    }
    val yesterday = (today.clone() as Calendar).apply { add(Calendar.DAY_OF_YEAR, -1) }

    cal.timeInMillis = timestamp
    val timeFmt = SimpleDateFormat("HH:mm", Locale.getDefault())
    val time = timeFmt.format(Date(timestamp))

    return when {
        timestamp >= today.timeInMillis     -> "Hoy a las $time"
        timestamp >= yesterday.timeInMillis -> "Ayer a las $time"
        else -> {
            val dateFmt = SimpleDateFormat("dd/MM", Locale.getDefault())
            "${dateFmt.format(Date(timestamp))} a las $time"
        }
    }
}

