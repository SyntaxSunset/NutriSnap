package com.example.caltracker.uii

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.ui.graphics.vector.ImageVector

sealed class Screen(val route: String) {
    object Home : Screen("home")
    object LogFood : Screen("log_food")
    object Diary : Screen("diary")
    object Progress : Screen("progress")
    object Profile : Screen("profile")       // ← renamed
    object Settings : Screen("settings")     // ← keep for gear icon
}

data class BottomNavItem(
    val screen: Screen,
    val label: String,
    val icon: ImageVector
)

val bottomNavItems = listOf(
    BottomNavItem(Screen.Home, "Home", Icons.Filled.Home),
    BottomNavItem(Screen.Diary, "Diary", Icons.Filled.MenuBook),
    BottomNavItem(Screen.Progress, "Progress", Icons.Filled.BarChart),
    BottomNavItem(Screen.Profile, "Profile", Icons.Filled.Person)
)