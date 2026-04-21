package com.example.tfg_carloscaramecerero.screens.home

import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.EmojiEvents
import androidx.compose.material.icons.filled.FileDownload
import androidx.compose.material.icons.filled.FitnessCenter
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.Lightbulb
import androidx.compose.material.icons.filled.MonitorWeight
import androidx.compose.material.icons.filled.Restaurant
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.tfg_carloscaramecerero.components.FitnessTopBar
import com.example.tfg_carloscaramecerero.viewmodel.DashboardViewModel
import java.text.SimpleDateFormat
import java.util.*

@Composable
fun DashboardScreen(
    viewModel: DashboardViewModel,
    onNavigateToTraining: () -> Unit,
    onNavigateToNutrition: () -> Unit,
    onNavigateToBody: () -> Unit,
    onNavigateToSettings: () -> Unit,
    onNavigateToRecommendations: () -> Unit = {}
) {
    val latestWeight by viewModel.latestWeight.collectAsState()
    val userProfile by viewModel.userProfile.collectAsState()
    val allSessions by viewModel.allSessions.collectAsState()
    val routines by viewModel.routines.collectAsState()

    // Stats calculadas
    val weekSessions = remember(allSessions) {
        val weekAgo = System.currentTimeMillis() - 7L * 24 * 60 * 60 * 1000
        allSessions.count { it.date >= weekAgo }
    }

    val imc = remember(latestWeight, userProfile) {
        val weight = latestWeight?.weight
        val heightM = userProfile?.height?.let { it / 100.0 }
        if (weight != null && heightM != null && heightM > 0) weight / (heightM * heightM) else null
    }

    val imcCategory = remember(imc) {
        when {
            imc == null -> null
            imc < 18.5 -> "Bajo peso" to Color(0xFF2196F3)
            imc < 25.0 -> "Peso normal" to Color(0xFF4CAF50)
            imc < 30.0 -> "Sobrepeso" to Color(0xFFFF9800)
            else -> "Obesidad" to Color(0xFFF44336)
        }
    }

    val totalSessions = allSessions.size

    val dateStr = remember {
        SimpleDateFormat("EEEE, d MMMM", Locale.forLanguageTag("es-ES")).format(Date()).replaceFirstChar { it.uppercase() }
    }

    Scaffold(
        topBar = {
            FitnessTopBar(
                title = "Inicio",
                actions = {
                    IconButton(onClick = onNavigateToSettings) {
                        Icon(Icons.Default.Settings, contentDescription = "Ajustes")
                    }
                }
            )
        },
        floatingActionButton = {
            FloatingActionButton(
                onClick = onNavigateToRecommendations,
                containerColor = MaterialTheme.colorScheme.secondaryContainer,
                contentColor = MaterialTheme.colorScheme.onSecondaryContainer
            ) {
                Icon(Icons.Default.Lightbulb, contentDescription = "Consejos IA")
            }
        }
    ) { padding ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .padding(horizontal = 16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            item { Spacer(Modifier.height(4.dp)) }

            // Saludo
            item {
                Column {
                    Text(
                        text = dateStr,
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Text(
                        text = "¡Bienvenido!",
                        style = MaterialTheme.typography.headlineSmall.copy(fontWeight = FontWeight.Bold)
                    )
                }
            }

            // Tarjetas de stats rápidas
            item {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    QuickStatCard(
                        modifier = Modifier.weight(1f),
                        icon = Icons.Default.FitnessCenter,
                        value = "$weekSessions",
                        label = "Sesiones\nesta semana",
                        color = MaterialTheme.colorScheme.primary
                    )
                    QuickStatCard(
                        modifier = Modifier.weight(1f),
                        icon = Icons.Default.Home,
                        value = "${routines.size}",
                        label = "Rutinas\nactivas",
                        color = MaterialTheme.colorScheme.secondary
                    )
                    QuickStatCard(
                        modifier = Modifier.weight(1f),
                        icon = Icons.Default.EmojiEvents,
                        value = "$totalSessions",
                        label = "Total\nsesiones",
                        color = MaterialTheme.colorScheme.tertiary
                    )
                }
            }

            // Peso + IMC card
            item {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(16.dp),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(20.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        // Peso
                        Column {
                            Text(
                                "Peso actual",
                                style = MaterialTheme.typography.labelMedium,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                            Text(
                                latestWeight?.let { "${"%.1f".format(it.weight)} kg" } ?: "—",
                                style = MaterialTheme.typography.headlineMedium.copy(fontWeight = FontWeight.Bold),
                                color = MaterialTheme.colorScheme.primary
                            )
                            if (userProfile?.height != null) {
                                Text(
                                    "Altura: ${"%.0f".format(userProfile!!.height)} cm",
                                    style = MaterialTheme.typography.bodySmall,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }
                        }

                        // IMC circular
                        if (imc != null && imcCategory != null) {
                            ImcGauge(imc = imc, color = imcCategory.second)
                        } else {
                            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                Icon(
                                    Icons.Default.MonitorWeight,
                                    contentDescription = null,
                                    modifier = Modifier.size(40.dp),
                                    tint = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.4f)
                                )
                                Text(
                                    "IMC no disponible",
                                    style = MaterialTheme.typography.labelSmall,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.6f),
                                    textAlign = TextAlign.Center
                                )
                            }
                        }
                    }
                }
            }

            // Rutinas activas
            if (routines.isNotEmpty()) {
                item {
                    Text(
                        "Tus rutinas",
                        style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.SemiBold)
                    )
                }
                item {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        routines.take(3).forEach { rwe ->
                            Card(
                                modifier = Modifier
                                    .weight(1f)
                                    .clickable { onNavigateToTraining() },
                                shape = RoundedCornerShape(12.dp),
                                colors = CardDefaults.cardColors(
                                    containerColor = MaterialTheme.colorScheme.primaryContainer
                                )
                            ) {
                                Column(modifier = Modifier.padding(12.dp)) {
                                    Icon(
                                        Icons.Default.FitnessCenter,
                                        contentDescription = null,
                                        tint = MaterialTheme.colorScheme.primary,
                                        modifier = Modifier.size(20.dp)
                                    )
                                    Spacer(Modifier.height(6.dp))
                                    Text(
                                        rwe.routine.name,
                                        style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.SemiBold),
                                        color = MaterialTheme.colorScheme.onPrimaryContainer,
                                        maxLines = 2
                                    )
                                    Text(
                                        "${rwe.exercises.size} ejercicios",
                                        style = MaterialTheme.typography.labelSmall,
                                        color = MaterialTheme.colorScheme.onPrimaryContainer.copy(alpha = 0.7f)
                                    )
                                }
                            }
                        }
                        // Relleno si hay menos de 3
                        repeat(maxOf(0, 3 - routines.size)) {
                            Spacer(Modifier.weight(1f))
                        }
                    }
                }
            }

            // Accesos rápidos
            item {
                Text(
                    "Acceso rápido",
                    style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.SemiBold)
                )
            }
            item {
                Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        QuickAccessButton(
                            modifier = Modifier.weight(1f),
                            icon = Icons.Default.FitnessCenter,
                            label = "Entrenar",
                            color = MaterialTheme.colorScheme.primary,
                            onClick = onNavigateToTraining
                        )
                        QuickAccessButton(
                            modifier = Modifier.weight(1f),
                            icon = Icons.Default.Restaurant,
                            label = "Nutrición",
                            color = MaterialTheme.colorScheme.secondary,
                            onClick = onNavigateToNutrition
                        )
                    }
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        QuickAccessButton(
                            modifier = Modifier.weight(1f),
                            icon = Icons.Default.MonitorWeight,
                            label = "Peso",
                            color = MaterialTheme.colorScheme.tertiary,
                            onClick = onNavigateToBody
                        )
                        QuickAccessButton(
                            modifier = Modifier.weight(1f),
                            icon = Icons.Default.FileDownload,
                            label = "Exportar",
                            color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 1f),
                            contentColor = MaterialTheme.colorScheme.onSurfaceVariant,
                            onClick = onNavigateToSettings
                        )
                    }
                }
            }

            item { Spacer(Modifier.height(16.dp)) }
        }
    }
}

@Composable
private fun QuickStatCard(
    modifier: Modifier = Modifier,
    icon: ImageVector,
    value: String,
    label: String,
    color: Color
) {
    Card(
        modifier = modifier,
        shape = RoundedCornerShape(14.dp),
        colors = CardDefaults.cardColors(containerColor = color.copy(alpha = 0.1f))
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(12.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(4.dp)
        ) {
            Icon(icon, contentDescription = null, tint = color, modifier = Modifier.size(22.dp))
            Text(
                value,
                style = MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.Bold),
                color = color
            )
            Text(
                label,
                style = MaterialTheme.typography.labelSmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                textAlign = TextAlign.Center
            )
        }
    }
}

@Composable
private fun QuickAccessButton(
    modifier: Modifier = Modifier,
    icon: ImageVector,
    label: String,
    color: Color,
    contentColor: Color = Color.White,
    onClick: () -> Unit
) {
    Button(
        onClick = onClick,
        modifier = modifier.height(56.dp),
        shape = RoundedCornerShape(14.dp),
        colors = ButtonDefaults.buttonColors(containerColor = color, contentColor = contentColor)
    ) {
        Icon(icon, contentDescription = null, modifier = Modifier.size(18.dp))
        Spacer(Modifier.width(6.dp))
        Text(label, style = MaterialTheme.typography.labelLarge)
    }
}

@Composable
private fun ImcGauge(
    imc: Double,
    color: Color
) {
    val animatedProgress by animateFloatAsState(
        targetValue = ((imc.coerceIn(10.0, 40.0) - 10.0) / 30.0).toFloat(),
        animationSpec = tween(1000),
        label = "imc_progress"
    )
    val surfaceColor = MaterialTheme.colorScheme.surfaceVariant

    Box(contentAlignment = Alignment.Center) {
        Canvas(modifier = Modifier.size(80.dp)) {
            val strokeWidth = 10.dp.toPx()
            val radius = (size.minDimension - strokeWidth) / 2
            val center = Offset(size.width / 2, size.height / 2)
            // Track gris
            drawArc(
                color = surfaceColor,
                startAngle = 135f,
                sweepAngle = 270f,
                useCenter = false,
                topLeft = Offset(center.x - radius, center.y - radius),
                size = Size(radius * 2, radius * 2),
                style = Stroke(strokeWidth, cap = StrokeCap.Round)
            )
            // Progreso
            drawArc(
                color = color,
                startAngle = 135f,
                sweepAngle = 270f * animatedProgress,
                useCenter = false,
                topLeft = Offset(center.x - radius, center.y - radius),
                size = Size(radius * 2, radius * 2),
                style = Stroke(strokeWidth, cap = StrokeCap.Round)
            )
        }
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            Text(
                "${"%.1f".format(imc)}",
                style = MaterialTheme.typography.labelLarge.copy(fontWeight = FontWeight.Bold),
                color = color
            )
            Text(
                "IMC",
                style = MaterialTheme.typography.labelSmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                fontSize = 9.sp
            )
        }
    }
}

