package com.owengarden.plugsmart

import android.graphics.Color
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.SystemBarStyle
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.owengarden.plugsmart.ui.history.HistoryScreen
import com.owengarden.plugsmart.ui.theme.PlugSmartTheme
import com.owengarden.plugsmart.ui.today.TodayScreen

/**
 * The app's single Activity. It owns the dark-mode setting (because it affects the
 * whole app) and sets up navigation between the screens. All the screen logic lives
 * in the ui package.
 */
class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            // Always starts in dark mode, which suits the app's colours best.
            // rememberSaveable keeps the user's choice when the screen rotates.
            // (It resets when the app is closed - saving it permanently is a later improvement.)
            var darkTheme by rememberSaveable { mutableStateOf(true) }

            // Keep the status bar icons readable when the app theme differs from the phone's.
            LaunchedEffect(darkTheme) {
                val barStyle = if (darkTheme) {
                    SystemBarStyle.dark(Color.TRANSPARENT)
                } else {
                    SystemBarStyle.light(Color.TRANSPARENT, Color.TRANSPARENT)
                }
                enableEdgeToEdge(statusBarStyle = barStyle, navigationBarStyle = barStyle)
            }

            PlugSmartTheme(darkTheme = darkTheme) {
                // Two screens: "today" (the start screen) and "history".
                // The back button/gesture on History returns to Today automatically.
                val navController = rememberNavController()
                NavHost(navController = navController, startDestination = "today") {
                    composable("today") {
                        TodayScreen(
                            isDarkTheme = darkTheme,
                            onToggleTheme = { darkTheme = !darkTheme },
                            onOpenHistory = { navController.navigate("history") }
                        )
                    }
                    composable("history") {
                        HistoryScreen(onBack = { navController.popBackStack() })
                    }
                }
            }
        }
    }
}
