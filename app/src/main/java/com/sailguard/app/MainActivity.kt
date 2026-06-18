package com.sailguard.app

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.viewModels
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.NavigationBarItemDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Snackbar
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.core.splashscreen.SplashScreen.Companion.installSplashScreen
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import com.sailguard.app.ui.navigation.CART_ROUTE
import com.sailguard.app.ui.navigation.SPLASH_ROUTE
import com.sailguard.app.ui.navigation.Screen
import com.sailguard.app.ui.navigation.bottomNavScreens
import com.sailguard.app.ui.screens.AlertsScreen
import com.sailguard.app.ui.screens.CartScreen
import com.sailguard.app.ui.screens.DashboardScreen
import com.sailguard.app.ui.screens.HistoryScreen
import com.sailguard.app.ui.screens.SmartModeScreen
import com.sailguard.app.ui.screens.SplashScreen
import com.sailguard.app.ui.screens.TripSetupScreen
import com.sailguard.app.ui.theme.AppBackground
import com.sailguard.app.ui.theme.AppSurface
import com.sailguard.app.ui.theme.CardBorder
import com.sailguard.app.ui.theme.SailGuardTheme
import com.sailguard.app.ui.theme.ConnectaOrange
import com.sailguard.app.ui.theme.TextSecondary
import com.sailguard.app.viewmodel.DashboardViewModel
import com.sailguard.app.viewmodel.HistoryViewModel
import com.sailguard.app.viewmodel.SmartModeViewModel
import com.sailguard.app.viewmodel.TripViewModel
import com.sailguard.app.viewmodel.UsageViewModel
import kotlinx.coroutines.launch

class MainActivity : ComponentActivity() {

    private val tripVm:    TripViewModel      by viewModels()
    private val usageVm:   UsageViewModel     by viewModels()
    private val dashVm:    DashboardViewModel by viewModels()
    private val smartVm:   SmartModeViewModel by viewModels()
    private val historyVm: HistoryViewModel   by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        installSplashScreen()
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            SailGuardTheme {
                SailGuardApp(tripVm, usageVm, dashVm, smartVm, historyVm)
            }
        }
    }
}

@Composable
private fun SailGuardApp(
    tripVm:    TripViewModel,
    usageVm:   UsageViewModel,
    dashVm:    DashboardViewModel,
    smartVm:   SmartModeViewModel,
    historyVm: HistoryViewModel
) {
    val navController     = rememberNavController()
    val snackbarHostState = remember { SnackbarHostState() }
    val tripState         by tripVm.state.collectAsState()

    val backStackEntry    by navController.currentBackStackEntryAsState()
    val currentRoute      = backStackEntry?.destination?.route

    Scaffold(
        containerColor = AppBackground,
        snackbarHost   = {
            SnackbarHost(hostState = snackbarHostState) { data ->
                Snackbar(
                    snackbarData   = data,
                    containerColor = Color(0xFF1A1A1A),
                    contentColor   = Color.White,
                    actionColor    = ConnectaOrange
                )
            }
        },
        bottomBar = {
            if (currentRoute != SPLASH_ROUTE && currentRoute != CART_ROUTE) {
                SailGuardBottomBar(
                    navController     = navController,
                    tripStarted       = tripState.tripStarted,
                    snackbarHostState = snackbarHostState
                )
            }
        }
    ) { innerPadding ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
        ) {
            NavHost(
                navController    = navController,
                startDestination = SPLASH_ROUTE
            ) {
                composable(SPLASH_ROUTE) {
                    SplashScreen(onSplashComplete = {
                        navController.navigate(Screen.Setup.route) {
                            popUpTo(SPLASH_ROUTE) { inclusive = true }
                        }
                    })
                }
                composable(Screen.Setup.route) {
                    TripSetupScreen(
                        vm         = tripVm,
                        usageVm    = usageVm,
                        historyVm  = historyVm,
                        onGoToCart = {
                            navController.navigate(CART_ROUTE) {
                                launchSingleTop = true
                            }
                        }
                    )
                }
                composable(CART_ROUTE) {
                    CartScreen(
                        tripVm    = tripVm,
                        onConfirm = {
                            navController.navigate(Screen.Dashboard.route) {
                                popUpTo(Screen.Setup.route) { inclusive = false }
                                launchSingleTop = true
                            }
                        },
                        onBack    = { navController.popBackStack() }
                    )
                }
                composable(Screen.Dashboard.route) {
                    DashboardScreen(
                        dashVm            = dashVm,
                        tripVm            = tripVm,
                        onEndTrip         = {
                            navController.navigate(Screen.Setup.route) {
                                popUpTo(navController.graph.startDestinationId) {
                                    inclusive = true
                                }
                                launchSingleTop = true
                            }
                        },
                        onNavigateToSetup = {
                            navController.navigate(Screen.Setup.route) {
                                launchSingleTop = true
                            }
                        }
                    )
                }
                composable(Screen.Smart.route) {
                    SmartModeScreen(vm = smartVm)
                }
                composable(Screen.Alerts.route) {
                    AlertsScreen(tripVm = tripVm, dashVm = dashVm, smartVm = smartVm)
                }
                composable(Screen.History.route) {
                    HistoryScreen(historyVm = historyVm)
                }
            }
        }
    }
}

@Composable
private fun SailGuardBottomBar(
    navController:     NavHostController,
    tripStarted:       Boolean,
    snackbarHostState: SnackbarHostState
) {
    val backStack by navController.currentBackStackEntryAsState()
    val current   = backStack?.destination?.route
    val scope     = rememberCoroutineScope()

    Column {
        HorizontalDivider(color = CardBorder, thickness = 1.dp)
        NavigationBar(
            containerColor = AppSurface,
            tonalElevation = 0.dp
        ) {
            bottomNavScreens.forEach { screen ->
                val selected = current == screen.route
                val isLocked = screen == Screen.Setup && tripStarted

                NavigationBarItem(
                    selected = selected,
                    onClick  = {
                        if (isLocked) {
                            scope.launch {
                                snackbarHostState.showSnackbar(
                                    "Trip in progress. End your current trip to set up a new one."
                                )
                            }
                        } else if (!selected) {
                            navController.navigate(screen.route) {
                                popUpTo(navController.graph.startDestinationId) {
                                    saveState = true
                                }
                                launchSingleTop = true
                                restoreState    = true
                            }
                        }
                    },
                    icon  = { Icon(screen.icon, contentDescription = screen.label) },
                    label = {
                        Text(
                            screen.label,
                            fontWeight = if (selected) FontWeight.SemiBold else FontWeight.Normal
                        )
                    },
                    colors = NavigationBarItemDefaults.colors(
                        selectedIconColor   = Color(0xFF0D0D0D),
                        selectedTextColor   = Color(0xFF0D0D0D),
                        unselectedIconColor = if (isLocked) TextSecondary.copy(alpha = 0.4f)
                                             else TextSecondary,
                        unselectedTextColor = if (isLocked) TextSecondary.copy(alpha = 0.4f)
                                             else TextSecondary,
                        indicatorColor      = Color(0x1A0D0D0D)
                    )
                )
            }
        }
    }
}
