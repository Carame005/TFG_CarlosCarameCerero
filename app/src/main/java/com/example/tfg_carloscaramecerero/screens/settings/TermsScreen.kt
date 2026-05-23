package com.example.tfg_carloscaramecerero.screens.settings

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Gavel
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp

/**
 * Pantalla de Términos y Condiciones.
 *
 * @param readOnly  true = vista desde Ajustes (solo botón Atrás).
 *                  false = primera vez (botones Aceptar / Salir).
 * @param onAccept  Callback al pulsar "Acepto" (solo en modo no readOnly).
 * @param onDecline Callback al pulsar "Salir" (solo en modo no readOnly).
 * @param onBackClick Callback del botón Atrás (solo en readOnly).
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TermsScreen(
    readOnly: Boolean = false,
    onAccept: () -> Unit = {},
    onDecline: () -> Unit = {},
    onBackClick: () -> Unit = {}
) {
    val scrollState = rememberScrollState()

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Términos y condiciones") },
                navigationIcon = {
                    if (readOnly) {
                        IconButton(onClick = onBackClick) {
                            Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Atrás")
                        }
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.surface
                )
            )
        }
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
        ) {
            // ── Contenido scrollable ──────────────────────────────────────────────
            Column(
                modifier = Modifier
                    .weight(1f)
                    .verticalScroll(scrollState)
                    .padding(horizontal = 16.dp, vertical = 8.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                // Banner de cabecera
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(16.dp),
                    colors = CardDefaults.cardColors(
                        containerColor = MaterialTheme.colorScheme.primaryContainer
                    )
                ) {
                    Row(
                        modifier = Modifier.padding(16.dp),
                        horizontalArrangement = Arrangement.spacedBy(12.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(
                            Icons.Default.Gavel,
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.primary,
                            modifier = Modifier.padding(4.dp)
                        )
                        Column {
                            Text(
                                "Lee antes de continuar",
                                style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.Bold),
                                color = MaterialTheme.colorScheme.onPrimaryContainer
                            )
                            Text(
                                "Última actualización: mayo de 2026 · Versión 1.0",
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onPrimaryContainer.copy(alpha = 0.7f)
                            )
                        }
                    }
                }

                TermsSection(
                    number = "1",
                    title = "Aceptación de los términos",
                    body = "Al utilizar FitAI («la Aplicación»), aceptas quedar vinculado por los presentes Términos y Condiciones de uso. Si no estás de acuerdo con alguno de los puntos, debes abstenerte de usar la Aplicación.\n\nFitAI es un proyecto académico desarrollado como Trabajo de Fin de Grado (TFG) del Ciclo Formativo de Grado Superior en Desarrollo de Aplicaciones Multiplataforma (DAM). No está destinado a uso comercial."
                )

                TermsSection(
                    number = "2",
                    title = "Descripción del servicio",
                    body = "FitAI es una aplicación de seguimiento de fitness que permite:\n\n• Registrar y gestionar rutinas de entrenamiento y sesiones.\n• Llevar un diario nutricional por días y tipos de comida.\n• Registrar medidas corporales, peso e historial de salud.\n• Interactuar con un asistente de inteligencia artificial (Google Gemini) para obtener consejos personalizados.\n• Exportar e importar datos en formato CSV.\n\nLas funcionalidades pueden cambiar o eliminarse en futuras versiones."
                )

                TermsSection(
                    number = "3",
                    title = "Almacenamiento de datos y privacidad",
                    body = "Todos tus datos personales (peso, medidas, rutinas, historial de nutrición, documentos PDF) se almacenan exclusivamente en el dispositivo local mediante la base de datos Room. La Aplicación no envía estos datos a servidores externos.\n\nÚnicamente los mensajes que envíes al asistente IA se transmiten a la API de Google Gemini para generar respuestas. Al usar el asistente, aceptas también la Política de Privacidad de Google AI (https://ai.google.dev/terms).\n\nNo recopilamos ni almacenamos datos en la nube. La responsabilidad de hacer copias de seguridad recae exclusivamente en el usuario."
                )

                TermsSection(
                    number = "4",
                    title = "Advertencia médica",
                    body = "FitAI no es un dispositivo médico ni un servicio de salud profesional. Los consejos generados por la inteligencia artificial son orientativos y no sustituyen la opinión de un médico, nutricionista o profesional del deporte certificado.\n\nAntes de iniciar cualquier programa de ejercicio o cambio en tu alimentación, consulta con un profesional de la salud. El uso de la información proporcionada por la Aplicación es responsabilidad exclusiva del usuario."
                )

                TermsSection(
                    number = "5",
                    title = "Uso aceptable",
                    body = "Te comprometes a utilizar la Aplicación únicamente para fines personales y lícitos. Queda expresamente prohibido:\n\n• Realizar ingeniería inversa o modificar el código fuente con fines distintos al estudio académico.\n• Utilizar la Aplicación para almacenar o procesar información de terceros sin su consentimiento.\n• Intentar vulnerar los mecanismos de seguridad de la Aplicación."
                )

                TermsSection(
                    number = "6",
                    title = "Propiedad intelectual",
                    body = "El código fuente, el diseño y la documentación de FitAI son propiedad del autor del TFG y están protegidos por las leyes de propiedad intelectual vigentes en España.\n\nLa Aplicación hace uso de las siguientes tecnologías de terceros bajo sus respectivas licencias: Android Jetpack (Apache 2.0), Kotlin (Apache 2.0), Material Design 3, Hilt, Room, Google Gemini API."
                )

                TermsSection(
                    number = "7",
                    title = "Limitación de responsabilidad",
                    body = "La Aplicación se proporciona «tal cual», sin garantías de ningún tipo. El autor no se hace responsable de:\n\n• Pérdida de datos debida a desinstalación, fallo del dispositivo o actualizaciones del sistema operativo.\n• Decisiones de salud o entrenamiento tomadas en base a los consejos de la IA.\n• Interrupciones del servicio de la API de Google Gemini."
                )

                TermsSection(
                    number = "8",
                    title = "Modificaciones",
                    body = "El autor se reserva el derecho de modificar estos Términos en cualquier momento. Los cambios entrarán en vigor en el momento de su publicación dentro de la Aplicación. El uso continuado de la Aplicación tras la publicación de cambios implica la aceptación de los nuevos términos."
                )

                TermsSection(
                    number = "9",
                    title = "Legislación aplicable",
                    body = "Estos Términos se rigen por la legislación española. Cualquier controversia derivada de su interpretación o aplicación se someterá a los juzgados y tribunales competentes del domicilio del usuario, salvo que la normativa aplicable establezca otro fuero."
                )

                Spacer(Modifier.height(8.dp))
            }

            // ── Botones de acción (solo en modo primera vez) ───────────────────
            if (!readOnly) {
                HorizontalDivider()
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp, vertical = 12.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Button(
                        onClick = onAccept,
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(12.dp)
                    ) {
                        Text(
                            "He leído y acepto los términos",
                            style = MaterialTheme.typography.labelLarge
                        )
                    }
                    OutlinedButton(
                        onClick = onDecline,
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(12.dp)
                    ) {
                        Text("Salir de la aplicación")
                    }
                }
            }
        }
    }
}

@Composable
private fun TermsSection(number: String, title: String, body: String) {
    Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
        Row(
            horizontalArrangement = Arrangement.spacedBy(8.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                number,
                style = MaterialTheme.typography.labelLarge.copy(fontWeight = FontWeight.ExtraBold),
                color = MaterialTheme.colorScheme.primary,
                modifier = Modifier
                    .padding(end = 2.dp)
            )
            Text(
                title,
                style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.Bold),
                color = MaterialTheme.colorScheme.onSurface
            )
        }
        Text(
            text = body,
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            lineHeight = MaterialTheme.typography.bodyMedium.lineHeight
        )
        HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f))
    }
}

