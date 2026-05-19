package com.example.tfg_carloscaramecerero.screens.auth

import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Fingerprint
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp

/**
 * Pantalla de bloqueo biométrico mostrada al abrir la app cuando el usuario
 * ha activado la opción en Ajustes.
 *
 * @param onAuthenticate Lambda que lanza el [androidx.biometric.BiometricPrompt].
 *                       Se llama automáticamente al entrar y al pulsar el botón.
 */
@Composable
fun BiometricLockScreen(onAuthenticate: () -> Unit) {

    // Lanza el prompt automáticamente al mostrarse la pantalla
    LaunchedEffect(Unit) {
        onAuthenticate()
    }

    Surface(
        modifier = Modifier.fillMaxSize(),
        color = MaterialTheme.colorScheme.background
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(32.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            // Icono decorativo
            Icon(
                imageVector = Icons.Default.Fingerprint,
                contentDescription = null,
                modifier = Modifier.size(96.dp),
                tint = MaterialTheme.colorScheme.primary
            )

            Spacer(Modifier.height(24.dp))

            Text(
                text = "FitAI",
                style = MaterialTheme.typography.headlineMedium.copy(fontWeight = FontWeight.Bold),
                color = MaterialTheme.colorScheme.onBackground
            )

            Spacer(Modifier.height(8.dp))

            Text(
                text = "Verifica tu identidad para acceder",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                textAlign = TextAlign.Center
            )

            Spacer(Modifier.height(40.dp))

            FilledTonalButton(
                onClick = onAuthenticate,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(52.dp)
            ) {
                Icon(
                    Icons.Default.Fingerprint,
                    contentDescription = null,
                    modifier = Modifier.size(20.dp)
                )
                Spacer(Modifier.width(8.dp))
                Text(
                    "Desbloquear",
                    style = MaterialTheme.typography.labelLarge
                )
            }
        }
    }
}

