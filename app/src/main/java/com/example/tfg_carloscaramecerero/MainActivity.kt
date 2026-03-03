package com.example.tfg_carloscaramecerero

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.Scaffold
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import com.example.tfg_carloscaramecerero.components.BottomNavItem
import com.example.tfg_carloscaramecerero.components.FitnessBottomNavBar
import com.example.tfg_carloscaramecerero.navigation.FitnessNavGraph
import com.example.tfg_carloscaramecerero.navigation.Screen
import com.example.tfg_carloscaramecerero.ui.theme.TFG_CarlosCarameCereroTheme
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            TFG_CarlosCarameCereroTheme {
                val navController = rememberNavController()
                val navBackStackEntry by navController.currentBackStackEntryAsState()
                val currentRoute = navBackStackEntry?.destination?.route

                val bottomBarRoutes = Screen.bottomNavItems.map { it.route }
                val showBottomBar = currentRoute in bottomBarRoutes

                val bottomNavItems = Screen.bottomNavItems.map { screen ->
                    BottomNavItem(
                        route = screen.route,
                        label = screen.label,
                        icon = screen.icon!!,
                        highlighted = screen == Screen.Assistant
                    )
                }

                Scaffold(
                    modifier = Modifier.fillMaxSize(),
                    contentWindowInsets = WindowInsets(0, 0, 0, 0),
                    bottomBar = {
                        if (showBottomBar) {
                            FitnessBottomNavBar(
                                items = bottomNavItems,
                                currentRoute = currentRoute,
                                onItemClick = { item ->
                                    navController.navigate(item.route) {
                                        popUpTo(navController.graph.startDestinationId) {
                                            saveState = true
                                        }
                                        launchSingleTop = true
                                        restoreState = true
                                    }
                                }
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

