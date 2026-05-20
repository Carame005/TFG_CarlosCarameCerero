package com.example.tfg_carloscaramecerero.screens.settings

import androidx.compose.animation.animateContentSize
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.HelpOutline
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.example.tfg_carloscaramecerero.components.FitnessTopBar

// ─── Modelo de datos ────────────────────────────────────────────────────────

private data class HelpSection(
    val icon: ImageVector,
    val title: String,
    val subtitle: String,
    val steps: List<String>
)

private val helpSections = listOf(
    HelpSection(
        icon = Icons.Default.Home,
        title = "Panel de inicio",
        subtitle = "Resumen rápido de tu actividad",
        steps = listOf(
            "En el panel de inicio verás un resumen de tus últimas sesiones de entrenamiento, peso corporal y calorías del día.",
            "Pulsa en cualquiera de las tarjetas para ir directamente a esa sección.",
            "Usa la barra de navegación inferior para moverte entre las secciones principales de la app."
        )
    ),
    HelpSection(
        icon = Icons.Default.FitnessCenter,
        title = "Entrenamiento",
        subtitle = "Rutinas, ejercicios y sesiones",
        steps = listOf(
            "Ve a la pestaña 'Entreno' para gestionar tus rutinas de entrenamiento.",
            "Pulsa el botón '+' para crear una nueva rutina. Ponle nombre y añade los ejercicios que quieras.",
            "Para iniciar una sesión, abre la rutina y pulsa 'Iniciar sesión'. Registra series, repeticiones y peso.",
            "En 'Ejercicios' encontrarás tu biblioteca personal de ejercicios. Puedes añadir los tuyos propios.",
            "Consulta el historial de sesiones pasadas entrando en cualquier rutina existente."
        )
    ),
    HelpSection(
        icon = Icons.Default.MonitorWeight,
        title = "Cuerpo",
        subtitle = "Seguimiento de peso y medidas",
        steps = listOf(
            "La sección 'Cuerpo' tiene tres pestañas: Peso, Medidas y Salud.",
            "En la pestaña 'Peso' registra tu peso diariamente pulsando el botón '+'. El listado muestra todas tus entradas ordenadas por fecha.",
            "En 'Medidas' puedes apuntar tus medidas corporales (pecho, cintura, cadera, bíceps y muslos). También se calcula el IMC si has introducido tu altura.",
            "En 'Salud' elige tus objetivos de fitness, indica condiciones de salud relevantes (enfermedades, alergias, intolerancias) y adjunta documentos médicos en PDF."
        )
    ),
    HelpSection(
        icon = Icons.Default.Restaurant,
        title = "Nutrición",
        subtitle = "Registro de comidas y calorías",
        steps = listOf(
            "En 'Nutrición' registra cada comida indicando nombre, calorías, proteínas, carbohidratos y grasas.",
            "Pulsa '+' para añadir una nueva entrada nutricional.",
            "Verás el resumen diario de macronutrientes en la parte superior.",
            "Puedes filtrar las entradas por fecha usando el selector disponible."
        )
    ),
    HelpSection(
        icon = Icons.Default.SmartToy,
        title = "Asistente IA",
        subtitle = "Tu entrenador personal inteligente",
        steps = listOf(
            "El asistente IA puede responder preguntas sobre entrenamiento, nutrición y salud.",
            "Escribe tu consulta en el campo de texto y pulsa enviar.",
            "Si lo permites en Ajustes, el asistente puede crear rutinas, ejercicios y entradas nutricionales por ti.",
            "Accede al historial de conversaciones pulsando el icono de historial en la esquina superior derecha.",
            "Puedes retomar cualquier conversación anterior desde el historial de chats."
        )
    ),
    HelpSection(
        icon = Icons.Default.Lightbulb,
        title = "Consejos",
        subtitle = "Recomendaciones personalizadas",
        steps = listOf(
            "La sección 'Consejos' muestra recomendaciones adaptadas a tu actividad reciente.",
            "Los consejos se actualizan automáticamente según tus registros de entrenamiento y nutrición.",
            "Pulsa un consejo para ver más detalles sobre él."
        )
    ),
    HelpSection(
        icon = Icons.Default.Settings,
        title = "Ajustes",
        subtitle = "Personalización y privacidad",
        steps = listOf(
            "En Ajustes puedes cambiar el tema de la app entre Claro, Oscuro o Automático.",
            "Activa el recordatorio diario para recibir una notificación que te anime a entrenar.",
            "Gestiona qué acciones puede realizar el asistente IA en la sección 'Permisos de creación'.",
            "Exporta tus datos (sesiones, peso, nutrición) en formato CSV desde la sección 'Exportar datos'.",
            "Activa el bloqueo biométrico para proteger la app con huella dactilar o reconocimiento facial.",
            "Consulta el registro de auditoría para ver el historial de acciones realizadas en la app."
        )
    )
)

// ─── Pantalla principal ──────────────────────────────────────────────────────

@Composable
fun HelpScreen(onBackClick: () -> Unit) {
    Scaffold(
        topBar = {
            FitnessTopBar(title = "Ayuda", onBackClick = onBackClick)
        }
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .padding(horizontal = 16.dp)
                .verticalScroll(rememberScrollState()),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Spacer(Modifier.height(8.dp))

            WelcomeBanner()

            Text(
                text = "Secciones de la aplicación",
                style = MaterialTheme.typography.labelLarge.copy(fontWeight = FontWeight.Bold),
                color = MaterialTheme.colorScheme.primary,
                modifier = Modifier.padding(vertical = 4.dp)
            )

            helpSections.forEach { section ->
                HelpSectionCard(section = section)
            }

            Spacer(Modifier.height(16.dp))
        }
    }
}

// ─── Componentes privados ────────────────────────────────────────────────────

@Composable
private fun WelcomeBanner() {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.primaryContainer
        )
    ) {
        Row(
            modifier = Modifier.padding(16.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Icon(
                imageVector = Icons.AutoMirrored.Filled.HelpOutline,
                contentDescription = null,
                tint = MaterialTheme.colorScheme.onPrimaryContainer,
                modifier = Modifier.size(40.dp)
            )
            Column {
                Text(
                    text = "¿Cómo usar FitTrack?",
                    style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                    color = MaterialTheme.colorScheme.onPrimaryContainer
                )
                Text(
                    text = "Pulsa en cada sección para ver una guía paso a paso.",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onPrimaryContainer.copy(alpha = 0.8f)
                )
            }
        }
    }
}

@Composable
private fun HelpSectionCard(section: HelpSection) {
    var expanded by remember { mutableStateOf(false) }

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .animateContentSize(animationSpec = tween(250))
            .clickable { expanded = !expanded },
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surface
        ),
        elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            // Cabecera
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween,
                modifier = Modifier.fillMaxWidth()
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(12.dp),
                    modifier = Modifier.weight(1f)
                ) {
                    Box(
                        modifier = Modifier
                            .size(40.dp)
                            .clip(CircleShape)
                            .background(MaterialTheme.colorScheme.primaryContainer),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = section.icon,
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.onPrimaryContainer,
                            modifier = Modifier.size(22.dp)
                        )
                    }
                    Column {
                        Text(
                            text = section.title,
                            style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.SemiBold)
                        )
                        Text(
                            text = section.subtitle,
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
                Icon(
                    imageVector = if (expanded) Icons.Default.ExpandLess else Icons.Default.ExpandMore,
                    contentDescription = if (expanded) "Colapsar" else "Expandir",
                    tint = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }

            // Pasos (expandibles)
            if (expanded) {
                Column(
                    modifier = Modifier.padding(top = 12.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    HorizontalDivider()
                    Spacer(Modifier.height(4.dp))
                    section.steps.forEachIndexed { index, step ->
                        StepItem(number = index + 1, text = step)
                    }
                }
            }
        }
    }
}

@Composable
private fun StepItem(number: Int, text: String) {
    Row(
        horizontalArrangement = Arrangement.spacedBy(10.dp),
        modifier = Modifier.fillMaxWidth()
    ) {
        Box(
            modifier = Modifier
                .size(22.dp)
                .clip(CircleShape)
                .background(MaterialTheme.colorScheme.primary),
            contentAlignment = Alignment.Center
        ) {
            Text(
                text = number.toString(),
                style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold),
                color = MaterialTheme.colorScheme.onPrimary
            )
        }
        Text(
            text = text,
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onSurface,
            modifier = Modifier.weight(1f)
        )
    }
}

