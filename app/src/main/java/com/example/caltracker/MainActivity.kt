package com.example.caltracker

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.compose.ui.Alignment
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import com.example.caltracker.uii.*
import com.example.caltracker.uii.bottomNavItems
import com.example.caltracker.uii.Screen
import com.example.caltracker.ui.theme.*
import com.example.caltracker.viewmodel.MainViewModel
import dagger.hilt.android.AndroidEntryPoint
import androidx.compose.material.icons.filled.*
import androidx.navigation.NavGraph.Companion.findStartDestination
import androidx.compose.material.icons.*
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.unit.sp
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.FloatingActionButtonDefaults
@AndroidEntryPoint
class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            CalTrackerTheme {
                CalTrackerMainApp()
            }
        }
    }
}

@Composable
fun CalTrackerMainApp() {
    val navController = rememberNavController()
    val viewModel: MainViewModel = hiltViewModel()
    val snackbarHostState = remember { SnackbarHostState() }
    val currentRoute by navController.currentBackStackEntryAsState()
    val isFirstLaunch by viewModel.isFirstLaunch.collectAsState()

    // ── Splash state ──────────────────────────────────────────────────────────
    var showSplash by remember { mutableStateOf(true) }

    LaunchedEffect(Unit) {
        viewModel.snackbarMessage.collect { message ->
            snackbarHostState.showSnackbar(message)
        }
    }

    // ── Show Splash ───────────────────────────────────────────────────────────
    if (showSplash) {
        SplashScreen(onSplashComplete = { showSplash = false })
        return
    }

    // ── Show Registration on first launch ─────────────────────────────────────
    if (isFirstLaunch) {
        RegisterScreen(viewModel = viewModel)
        return
    }

    // ── Main App ──────────────────────────────────────────────────────────────
    Scaffold(
        modifier = Modifier.fillMaxSize(),
        containerColor = DarkBg,
        snackbarHost = {
            SnackbarHost(snackbarHostState) { data ->
                Snackbar(
                    snackbarData = data,
                    containerColor = CardBg,
                    contentColor = TextPrimary,
                    actionColor = GreenAccent
                )
            }
        },
        bottomBar = {
            Box {
                NavigationBar(
                    containerColor = CardBg,
                    tonalElevation = 0.dp,
                    modifier = Modifier.height(90.dp)
                ) {
                    // Home
                    val homeSelected = currentRoute?.destination?.route == Screen.Home.route
                    NavigationBarItem(
                        selected = homeSelected,
                        onClick = {
                            if (!homeSelected) {
                                navController.navigate(Screen.Home.route) {
                                    popUpTo(navController.graph.startDestinationId) {
                                        saveState = true
                                    }
                                    launchSingleTop = true
                                    restoreState = true
                                }
                            }
                        },
                        icon = {
                            Icon(
                                Icons.Filled.Home,
                                contentDescription = "Home",
                                tint = if (homeSelected) GreenAccent else TextMuted
                            )
                        },
                        label = {
                            Text(
                                "Home",
                                color = if (homeSelected) GreenAccent else TextMuted,
                                fontSize = 10.sp
                            )
                        },
                        colors = NavigationBarItemDefaults.colors(
                            indicatorColor = GreenAccent.copy(alpha = 0.15f)
                        )
                    )

                    // Diary
                    val diarySelected = currentRoute?.destination?.route == Screen.Diary.route
                    NavigationBarItem(
                        selected = diarySelected,
                        onClick = {
                            if (!diarySelected) {
                                navController.navigate(Screen.Diary.route) {
                                    popUpTo(navController.graph.startDestinationId) {
                                        saveState = true
                                    }
                                    launchSingleTop = true
                                    restoreState = true
                                }
                            }
                        },
                        icon = {
                            Icon(
                                Icons.Filled.MenuBook,
                                contentDescription = "Diary",
                                tint = if (diarySelected) GreenAccent else TextMuted
                            )
                        },
                        label = {
                            Text(
                                "Diary",
                                color = if (diarySelected) GreenAccent else TextMuted,
                                fontSize = 10.sp
                            )
                        },
                        colors = NavigationBarItemDefaults.colors(
                            indicatorColor = GreenAccent.copy(alpha = 0.15f)
                        )
                    )

                    // Empty center space for FAB
                    NavigationBarItem(
                        selected = false,
                        onClick = {},
                        icon = { Spacer(Modifier.size(48.dp)) },
                        label = { Text("") },
                        enabled = false,
                        colors = NavigationBarItemDefaults.colors(
                            indicatorColor = Color.Transparent
                        )
                    )

                    // Progress
                    val progressSelected = currentRoute?.destination?.route == Screen.Progress.route
                    NavigationBarItem(
                        selected = progressSelected,
                        onClick = {
                            if (!progressSelected) {
                                navController.navigate(Screen.Progress.route) {
                                    popUpTo(navController.graph.startDestinationId) {
                                        saveState = true
                                    }
                                    launchSingleTop = true
                                    restoreState = true
                                }
                            }
                        },
                        icon = {
                            Icon(
                                Icons.Filled.BarChart,
                                contentDescription = "Progress",
                                tint = if (progressSelected) GreenAccent else TextMuted
                            )
                        },
                        label = {
                            Text(
                                "Progress",
                                color = if (progressSelected) GreenAccent else TextMuted,
                                fontSize = 10.sp
                            )
                        },
                        colors = NavigationBarItemDefaults.colors(
                            indicatorColor = GreenAccent.copy(alpha = 0.15f)
                        )
                    )

                    // In bottomBar — change last nav item:
                    val profileSelected = currentRoute?.destination?.route == Screen.Profile.route
                    NavigationBarItem(
                        selected = profileSelected,
                        onClick = {
                            if (!profileSelected) {
                                navController.navigate(Screen.Profile.route) {
                                    popUpTo(navController.graph.startDestinationId) {
                                        saveState = true
                                    }
                                    launchSingleTop = true
                                    restoreState = true
                                }
                            }
                        },
                        icon = {
                            Icon(
                                Icons.Filled.Person,
                                contentDescription = "Profile",
                                tint = if (profileSelected) GreenAccent else TextMuted
                            )
                        },
                        label = {
                            Text(
                                "Profile",
                                color = if (profileSelected) GreenAccent else TextMuted
                            )
                        },
                        colors = NavigationBarItemDefaults.colors(
                            indicatorColor = GreenAccent.copy(alpha = 0.15f)
                        )
                    )
                }

                // ── Elevated Center FAB ───────────────────────────────────────────
                val fabPulse = rememberInfiniteTransition(label = "navFab")
                val fabGlowAlpha by fabPulse.animateFloat(
                    initialValue = 0.3f,
                    targetValue = 0.7f,
                    animationSpec = infiniteRepeatable(
                        animation = tween(1000),
                        repeatMode = RepeatMode.Reverse
                    ),
                    label = "navFabGlow"
                )
                val fabScale by fabPulse.animateFloat(
                    initialValue = 1f,
                    targetValue = 1.06f,
                    animationSpec = infiniteRepeatable(
                        animation = tween(1000),
                        repeatMode = RepeatMode.Reverse
                    ),
                    label = "navFabScale"
                )

                Box(
                    modifier = Modifier
                        .align(Alignment.TopCenter)
                        .offset(y = (-20).dp)
                        .size(72.dp),
                    contentAlignment = Alignment.Center
                ) {
                    // Outer glow ring
                    Box(
                        modifier = Modifier
                            .size(72.dp)
                            .clip(CircleShape)
                            .background(GreenAccent.copy(alpha = fabGlowAlpha * 0.2f))
                    )
                    // Inner glow ring
                    Box(
                        modifier = Modifier
                            .size(62.dp)
                            .clip(CircleShape)
                            .background(GreenAccent.copy(alpha = fabGlowAlpha * 0.15f))
                    )
                    // FAB
                    FloatingActionButton(
                        onClick = {
                            navController.navigate(Screen.LogFood.route) {
                                popUpTo(navController.graph.startDestinationId) {
                                    saveState = true
                                }
                                launchSingleTop = true
                                restoreState = true
                            }
                        },
                        modifier = Modifier
                            .size(54.dp)
                            .scale(fabScale),
                        containerColor = GreenAccent,
                        contentColor = Color.Black,
                        shape = CircleShape,
                        elevation = FloatingActionButtonDefaults.elevation(
                            defaultElevation = 8.dp,
                            pressedElevation = 12.dp
                        )
                    ) {
                        Icon(
                            Icons.Filled.CameraAlt,
                            contentDescription = "Log Food",
                            modifier = Modifier.size(24.dp),
                            tint = Color.Black
                        )
                    }
                }
            }
        }
    ) { innerPadding ->
        NavHost(
            navController = navController,
            startDestination = Screen.Home.route,
            modifier = Modifier.padding(innerPadding)
        ) {
            composable(Screen.Home.route) {
                HomeScreen(
                    viewModel = viewModel,
                    onLogFood = {
                        navController.navigate(Screen.LogFood.route) {
                            popUpTo(navController.graph.startDestinationId) {
                                saveState = true
                            }
                            launchSingleTop = true
                            restoreState = true
                        }
                    }
                )
            }
            composable(Screen.LogFood.route) {
                LogFoodScreen(viewModel = viewModel)
            }
            composable(Screen.Diary.route) {
                DiaryScreen(viewModel = viewModel)
            }
            composable(Screen.Progress.route) {
                ProgressScreen(viewModel = viewModel)
            }
            composable(Screen.Profile.route) {
                ProfileScreen(
                    viewModel = viewModel,
                    onNavigateToSettings = {
                        navController.navigate(Screen.Settings.route)
                    },
                    onLogOut = {
                        // Clear profile and go back to registration
                        viewModel.logOut()
                    }
                )
            }
            composable(Screen.Settings.route) {
                SettingsScreen(viewModel = viewModel)
            }
            }
        }
    }