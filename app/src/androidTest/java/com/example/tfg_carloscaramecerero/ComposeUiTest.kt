package com.example.tfg_carloscaramecerero

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.FitnessCenter
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.Restaurant
import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onNodeWithContentDescription
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.example.tfg_carloscaramecerero.components.BottomNavItem
import com.example.tfg_carloscaramecerero.components.ConfirmDeleteDialog
import com.example.tfg_carloscaramecerero.components.EmptyStateMessage
import com.example.tfg_carloscaramecerero.components.FitnessBottomNavBar
import com.example.tfg_carloscaramecerero.components.FitnessCard
import com.example.tfg_carloscaramecerero.components.StatCard
import com.example.tfg_carloscaramecerero.screens.auth.BiometricLockScreen
import com.example.tfg_carloscaramecerero.ui.theme.TFG_CarlosCarameCereroTheme
import org.junit.Assert.assertTrue
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith

/**
 * Tests de UI con Compose Testing (createComposeRule).
 *
 * Cubren los componentes reutilizables y las pantallas clave de la app
 * sin requerir inyección de dependencias (Hilt), ya que se prueban
 * los Composables de forma aislada dentro del tema de la app.
 *
 * Ejecución: ./gradlew connectedDebugAndroidTest
 */
@RunWith(AndroidJUnit4::class)
class ComposeUiTest {

    @get:Rule
    val composeRule = createComposeRule()

    // ──────────────────────────────────────────────────────────────────
    // BiometricLockScreen
    // ──────────────────────────────────────────────────────────────────

    @Test
    fun biometricLockScreen_titulo_visible() {
        composeRule.setContent {
            TFG_CarlosCarameCereroTheme {
                BiometricLockScreen(onAuthenticate = {})
            }
        }
        composeRule.onNodeWithText("FitAI").assertIsDisplayed()
    }

    @Test
    fun biometricLockScreen_subtitulo_visible() {
        composeRule.setContent {
            TFG_CarlosCarameCereroTheme {
                BiometricLockScreen(onAuthenticate = {})
            }
        }
        composeRule.onNodeWithText("Verifica tu identidad para acceder").assertIsDisplayed()
    }

    @Test
    fun biometricLockScreen_botonDesbloquear_visible() {
        composeRule.setContent {
            TFG_CarlosCarameCereroTheme {
                BiometricLockScreen(onAuthenticate = {})
            }
        }
        composeRule.onNodeWithText("Desbloquear").assertIsDisplayed()
    }

    @Test
    fun biometricLockScreen_botonDesbloquear_llamaCallback() {
        var called = false
        composeRule.setContent {
            TFG_CarlosCarameCereroTheme {
                BiometricLockScreen(onAuthenticate = { called = true })
            }
        }
        // El LaunchedEffect ya lo llama una vez al aparecer la pantalla
        composeRule.waitForIdle()
        assertTrue("onAuthenticate debe haberse llamado al aparecer", called)
    }

    @Test
    fun biometricLockScreen_botonDesbloquear_llamaCallbackAlPulsar() {
        var callCount = 0
        composeRule.setContent {
            TFG_CarlosCarameCereroTheme {
                BiometricLockScreen(onAuthenticate = { callCount++ })
            }
        }
        composeRule.waitForIdle()
        val prevCount = callCount
        composeRule.onNodeWithText("Desbloquear").performClick()
        composeRule.waitForIdle()
        assertTrue("El botón debe incrementar el contador", callCount > prevCount)
    }

    // ──────────────────────────────────────────────────────────────────
    // FitnessCard
    // ──────────────────────────────────────────────────────────────────

    @Test
    fun fitnessCard_titulo_visible() {
        composeRule.setContent {
            TFG_CarlosCarameCereroTheme {
                FitnessCard(title = "Sentadillas")
            }
        }
        composeRule.onNodeWithText("Sentadillas").assertIsDisplayed()
    }

    @Test
    fun fitnessCard_subtitulo_visible_cuandoSeProvee() {
        composeRule.setContent {
            TFG_CarlosCarameCereroTheme {
                FitnessCard(title = "Sentadillas", subtitle = "Piernas y glúteos")
            }
        }
        composeRule.onNodeWithText("Piernas y glúteos").assertIsDisplayed()
    }

    @Test
    fun fitnessCard_botonEliminar_visible_cuandoSeProveeOnDelete() {
        composeRule.setContent {
            TFG_CarlosCarameCereroTheme {
                FitnessCard(title = "Sentadillas", onDelete = {})
            }
        }
        composeRule.onNodeWithContentDescription("Eliminar").assertIsDisplayed()
    }

    @Test
    fun fitnessCard_botonEliminar_ausente_cuandoNoHayOnDelete() {
        composeRule.setContent {
            TFG_CarlosCarameCereroTheme {
                FitnessCard(title = "Sentadillas")
            }
        }
        composeRule.onNodeWithContentDescription("Eliminar").assertDoesNotExist()
    }

    @Test
    fun fitnessCard_onDelete_llamadoAlPulsar() {
        var deleted = false
        composeRule.setContent {
            TFG_CarlosCarameCereroTheme {
                FitnessCard(title = "Sentadillas", onDelete = { deleted = true })
            }
        }
        composeRule.onNodeWithContentDescription("Eliminar").performClick()
        assertTrue("onDelete debe ejecutarse al pulsar", deleted)
    }

    @Test
    fun fitnessCard_onClick_llamadoAlPulsar() {
        var clicked = false
        composeRule.setContent {
            TFG_CarlosCarameCereroTheme {
                FitnessCard(title = "Sentadillas", onClick = { clicked = true })
            }
        }
        composeRule.onNodeWithText("Sentadillas").performClick()
        assertTrue("onClick debe ejecutarse al pulsar la tarjeta", clicked)
    }

    // ──────────────────────────────────────────────────────────────────
    // ConfirmDeleteDialog
    // ──────────────────────────────────────────────────────────────────

    @Test
    fun confirmDeleteDialog_titulo_visible() {
        composeRule.setContent {
            TFG_CarlosCarameCereroTheme {
                ConfirmDeleteDialog(
                    title = "Eliminar rutina",
                    message = "¿Eliminar 'Pecho y tríceps'?",
                    onConfirm = {},
                    onDismiss = {}
                )
            }
        }
        composeRule.onNodeWithText("Eliminar rutina").assertIsDisplayed()
    }

    @Test
    fun confirmDeleteDialog_mensaje_visible() {
        composeRule.setContent {
            TFG_CarlosCarameCereroTheme {
                ConfirmDeleteDialog(
                    message = "Esta acción es irreversible",
                    onConfirm = {},
                    onDismiss = {}
                )
            }
        }
        composeRule.onNodeWithText("Esta acción es irreversible").assertIsDisplayed()
    }

    @Test
    fun confirmDeleteDialog_botonCancelar_visible() {
        composeRule.setContent {
            TFG_CarlosCarameCereroTheme {
                ConfirmDeleteDialog(onConfirm = {}, onDismiss = {})
            }
        }
        composeRule.onNodeWithText("Cancelar").assertIsDisplayed()
    }

    @Test
    fun confirmDeleteDialog_botonEliminar_llamaOnConfirm() {
        var confirmed = false
        composeRule.setContent {
            TFG_CarlosCarameCereroTheme {
                ConfirmDeleteDialog(
                    onConfirm = { confirmed = true },
                    onDismiss = {}
                )
            }
        }
        composeRule.onNodeWithText("Eliminar").performClick()
        assertTrue("onConfirm debe ejecutarse al pulsar Eliminar", confirmed)
    }

    @Test
    fun confirmDeleteDialog_botonCancelar_llamaOnDismiss() {
        var dismissed = false
        composeRule.setContent {
            TFG_CarlosCarameCereroTheme {
                ConfirmDeleteDialog(
                    onConfirm = {},
                    onDismiss = { dismissed = true }
                )
            }
        }
        composeRule.onNodeWithText("Cancelar").performClick()
        assertTrue("onDismiss debe ejecutarse al pulsar Cancelar", dismissed)
    }

    // ──────────────────────────────────────────────────────────────────
    // EmptyStateMessage
    // ──────────────────────────────────────────────────────────────────

    @Test
    fun emptyStateMessage_mensaje_visible() {
        composeRule.setContent {
            TFG_CarlosCarameCereroTheme {
                EmptyStateMessage(message = "No hay rutinas todavía")
            }
        }
        composeRule.onNodeWithText("No hay rutinas todavía").assertIsDisplayed()
    }

    @Test
    fun emptyStateMessage_mensajeMultilinea_visible() {
        composeRule.setContent {
            TFG_CarlosCarameCereroTheme {
                EmptyStateMessage(message = "No hay sets registrados.\nPulsa + para añadir uno.")
            }
        }
        composeRule.onNodeWithText("No hay sets registrados.\nPulsa + para añadir uno.")
            .assertIsDisplayed()
    }

    // ──────────────────────────────────────────────────────────────────
    // StatCard
    // ──────────────────────────────────────────────────────────────────

    @Test
    fun statCard_valor_visible() {
        composeRule.setContent {
            TFG_CarlosCarameCereroTheme {
                StatCard(label = "Sets", value = "12")
            }
        }
        composeRule.onNodeWithText("12").assertIsDisplayed()
    }

    @Test
    fun statCard_etiqueta_visible() {
        composeRule.setContent {
            TFG_CarlosCarameCereroTheme {
                StatCard(label = "Sets", value = "12")
            }
        }
        composeRule.onNodeWithText("Sets").assertIsDisplayed()
    }

    @Test
    fun statCard_conIcono_valorYEtiqueta_visibles() {
        composeRule.setContent {
            TFG_CarlosCarameCereroTheme {
                StatCard(
                    label = "Ejercicios",
                    value = "5",
                    icon = Icons.Default.FitnessCenter
                )
            }
        }
        composeRule.onNodeWithText("Ejercicios").assertIsDisplayed()
        composeRule.onNodeWithText("5").assertIsDisplayed()
    }

    // ──────────────────────────────────────────────────────────────────
    // FitnessBottomNavBar
    // ──────────────────────────────────────────────────────────────────

    @Test
    fun bottomNavBar_etiquetasItems_visibles() {
        val items = listOf(
            BottomNavItem("inicio", "Inicio", Icons.Default.Home),
            BottomNavItem("nutricion", "Nutrición", Icons.Default.Restaurant),
            BottomNavItem("entreno", "Entreno", Icons.Default.FitnessCenter)
        )
        composeRule.setContent {
            TFG_CarlosCarameCereroTheme {
                FitnessBottomNavBar(
                    items = items,
                    currentRoute = "inicio",
                    onItemClick = {}
                )
            }
        }
        composeRule.onNodeWithText("Inicio").assertIsDisplayed()
        composeRule.onNodeWithText("Nutrición").assertIsDisplayed()
        composeRule.onNodeWithText("Entreno").assertIsDisplayed()
    }

    @Test
    fun bottomNavBar_click_llamaCallbackConItemCorrecto() {
        val items = listOf(
            BottomNavItem("inicio", "Inicio", Icons.Default.Home),
            BottomNavItem("nutricion", "Nutrición", Icons.Default.Restaurant)
        )
        var clickedRoute = ""
        composeRule.setContent {
            TFG_CarlosCarameCereroTheme {
                FitnessBottomNavBar(
                    items = items,
                    currentRoute = "inicio",
                    onItemClick = { clickedRoute = it.route }
                )
            }
        }
        composeRule.onNodeWithText("Nutrición").performClick()
        assertTrue("El route del item pulsado debe ser 'nutricion'", clickedRoute == "nutricion")
    }

    @Test
    fun bottomNavBar_itemDestacado_visible() {
        val items = listOf(
            BottomNavItem("asistente", "Asistente", Icons.Default.FitnessCenter, highlighted = true)
        )
        composeRule.setContent {
            TFG_CarlosCarameCereroTheme {
                FitnessBottomNavBar(
                    items = items,
                    currentRoute = null,
                    onItemClick = {}
                )
            }
        }
        composeRule.onNodeWithText("Asistente").assertIsDisplayed()
    }
}

