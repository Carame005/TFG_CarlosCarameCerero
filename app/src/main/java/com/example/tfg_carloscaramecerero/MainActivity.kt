package com.example.tfg_carloscaramecerero

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.biometric.BiometricPrompt
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.Icon
import androidx.compose.material3.NavigationRail
import androidx.compose.material3.NavigationRailItem
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.windowsizeclass.ExperimentalMaterial3WindowSizeClassApi
import androidx.compose.material3.windowsizeclass.WindowWidthSizeClass
import androidx.compose.material3.windowsizeclass.calculateWindowSizeClass
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.core.content.ContextCompat
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import com.example.tfg_carloscaramecerero.components.BottomNavItem
import com.example.tfg_carloscaramecerero.components.FitnessBottomNavBar
import com.example.tfg_carloscaramecerero.navigation.FitnessNavGraph
import com.example.tfg_carloscaramecerero.navigation.Screen
import com.example.tfg_carloscaramecerero.screens.auth.BiometricLockScreen
import com.example.tfg_carloscaramecerero.screens.settings.TermsScreen
import com.example.tfg_carloscaramecerero.service.SessionTimerService
import com.example.tfg_carloscaramecerero.ui.theme.TFG_CarlosCarameCereroTheme
import com.example.tfg_carloscaramecerero.viewmodel.SettingsViewModel
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class MainActivity : AppCompatActivity() {

    companion object {
        /** Tiempo mínimo fuera de la app (sin timer corriendo) para pedir biometría */
        private const val FIXED_GRACE_MS = 2 * 60 * 1000L // 2 minutos
    }

    /** true = el usuario ya pasó la verificación biométrica en esta sesión */
    private var isAuthenticated by mutableStateOf(false)

    /** Instante en que la app fue enviada a segundo plano */
    private var lastStopTime: Long = 0L

    /** Registra cuándo se minimizó la app */
    override fun onStop() {
        super.onStop()
        lastStopTime = System.currentTimeMillis()
    }

    /**
     * Al volver:
     *  - Si el temporizador de descanso sigue corriendo → gracia indefinida (no se interrumpe
     *    el entrenamiento bajo ningún concepto).
     *  - Si el temporizador está parado → se exige re-autenticación tras [FIXED_GRACE_MS].
     */
    override fun onResume() {
        super.onResume()
        if (isAuthenticated) {
            val timerRunning = SessionTimerService.timerState.value.isRunning
            val timeAway = System.currentTimeMillis() - lastStopTime
            if (!timerRunning && timeAway > FIXED_GRACE_MS) {
                isAuthenticated = false
            }
        }
    }

    /** Muestra el diálogo de BiometricPrompt del sistema */
    private fun showBiometricPrompt() {
        val executor = ContextCompat.getMainExecutor(this)
        val callback = object : BiometricPrompt.AuthenticationCallback() {
            override fun onAuthenticationSucceeded(result: BiometricPrompt.AuthenticationResult) {
                isAuthenticated = true
            }
            override fun onAuthenticationError(errorCode: Int, errString: CharSequence) {
                // Si el usuario cancela, cierra la app
                if (errorCode == BiometricPrompt.ERROR_NEGATIVE_BUTTON ||
                    errorCode == BiometricPrompt.ERROR_USER_CANCELED
                ) finish()
            }
            override fun onAuthenticationFailed() { /* huella no reconocida – el prompt se muestra solo */ }
        }
        val promptInfo = BiometricPrompt.PromptInfo.Builder()
            .setTitle("FitAI – Acceso protegido")
            .setSubtitle("Verifica tu identidad para continuar")
            .setNegativeButtonText("Cancelar")
            .build()
        BiometricPrompt(this, executor, callback).authenticate(promptInfo)
    }

    @OptIn(ExperimentalMaterial3WindowSizeClassApi::class)
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            val settingsViewModel: SettingsViewModel = hiltViewModel()
            val darkModePreference by settingsViewModel.darkMode.collectAsState()
            val systemDark = isSystemInDarkTheme()
            val isDark = darkModePreference ?: systemDark

            // Calcula el tamaño de ventana actual (teléfonos → Compact, tablets → Medium/Expanded)
            val windowSizeClass = calculateWindowSizeClass(this)
            val useNavigationRail =
                windowSizeClass.widthSizeClass != WindowWidthSizeClass.Compact

            // Bloqueo biométrico ──────────────────────────────────────────────
            val biometricLock by settingsViewModel.biometricLock.collectAsState()
            val termsAccepted by settingsViewModel.termsAccepted.collectAsState()

            TFG_CarlosCarameCereroTheme(darkTheme = isDark) {
                // Surface cubre toda la ventana con el color de fondo del tema Compose,
                // evitando que el fondo gris de AppCompat se filtre durante la carga.
                androidx.compose.material3.Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = androidx.compose.material3.MaterialTheme.colorScheme.background
                ) {
                when {
                    // T&C no aceptados → mostrar pantalla de términos antes que nada
                    !termsAccepted -> {
                        TermsScreen(
                            readOnly = false,
                            onAccept = { settingsViewModel.acceptTerms() },
                            onDecline = { finish() }
                        )
                    }

                    // Bloqueo activo y usuario no autenticado → lock screen
                    biometricLock && !isAuthenticated -> {
                        BiometricLockScreen(onAuthenticate = { showBiometricPrompt() })
                    }

                    // Sin bloqueo o ya autenticado → contenido normal
                    else -> {
                val navController = rememberNavController()
                val navBackStackEntry by navController.currentBackStackEntryAsState()
                val currentRoute = navBackStackEntry?.destination?.route

                val bottomBarRoutes = Screen.bottomNavItems.map { it.route }
                val currentBaseRoute = currentRoute?.substringBefore("?")
                val showNav = currentBaseRoute in bottomBarRoutes

                val bottomNavItems = Screen.bottomNavItems.map { screen ->
                    BottomNavItem(
                        route = screen.route,
                        label = screen.label,
                        icon = screen.icon!!,
                        highlighted = screen == Screen.Assistant
                    )
                }

                fun navigateTo(route: String) {
                    if (route == Screen.Dashboard.route) {
                        navController.navigate(Screen.Dashboard.route) {
                            popUpTo(navController.graph.startDestinationId) {
                                inclusive = false; saveState = false
                            }
                            launchSingleTop = true; restoreState = false
                        }
                    } else {
                        navController.navigate(route) {
                            popUpTo(Screen.Dashboard.route) { saveState = true }
                            launchSingleTop = true; restoreState = true
                        }
                    }
                }

                if (useNavigationRail && showNav) {
                    // ── Layout adaptativo para tablets / pantallas anchas ──────────────
                    Row(modifier = Modifier.fillMaxSize()) {
                        NavigationRail {
                            bottomNavItems.forEach { item ->
                                NavigationRailItem(
                                    selected = currentBaseRoute == item.route,
                                    onClick = { navigateTo(item.route) },
                                    icon = { Icon(item.icon, contentDescription = item.label) },
                                    label = { Text(item.label) }
                                )
                            }
                        }
                        Box(modifier = Modifier.weight(1f)) {
                            FitnessNavGraph(
                                navController = navController,
                                innerPadding = PaddingValues(0.dp)
                            )
                        }
                    }
                } else {
                    // ── Layout estándar para teléfonos (BottomNavigationBar) ──────────
                    Scaffold(
                        modifier = Modifier.fillMaxSize(),
                        contentWindowInsets = WindowInsets(0, 0, 0, 0),
                        bottomBar = {
                            if (showNav) {
                                FitnessBottomNavBar(
                                    items = bottomNavItems,
                                    currentRoute = currentBaseRoute,
                                    onItemClick = { item -> navigateTo(item.route) }
                                )
                            }
                        }
                    ) { innerPadding ->
                        FitnessNavGraph(
                            navController = navController,
                            innerPadding = innerPadding
                        )
                    }
                }
                }
            }
        } // cierra Surface
        }
    }
}
}

