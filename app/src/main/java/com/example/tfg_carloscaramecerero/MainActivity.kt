package com.example.tfg_carloscaramecerero

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
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
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import com.example.tfg_carloscaramecerero.components.BottomNavItem
import com.example.tfg_carloscaramecerero.components.FitnessBottomNavBar
import com.example.tfg_carloscaramecerero.navigation.FitnessNavGraph
import com.example.tfg_carloscaramecerero.navigation.Screen
import com.example.tfg_carloscaramecerero.ui.theme.TFG_CarlosCarameCereroTheme
import com.example.tfg_carloscaramecerero.viewmodel.SettingsViewModel
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class MainActivity : ComponentActivity() {

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

            TFG_CarlosCarameCereroTheme(darkTheme = isDark) {
                val navController = rememberNavController()
                val navBackStackEntry by navController.currentBackStackEntryAsState()
                val currentRoute = navBackStackEntry?.destination?.route

                val bottomBarRoutes = Screen.bottomNavItems.map { it.route }
                val showNav = currentRoute in bottomBarRoutes

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
                                    selected = currentRoute == item.route,
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
                                    currentRoute = currentRoute,
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
    }
}

