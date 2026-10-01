package com.example.tortilleriaderek.presentation.navigation

import androidx.compose.animation.EnterTransition
import androidx.compose.animation.ExitTransition
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.navigation.NavController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import androidx.navigation.toRoute
import com.example.tortilleriaderek.presentation.login.LoginScreenContent
import com.example.tortilleriaderek.presentation.login.LoginUiEffect
import com.example.tortilleriaderek.presentation.login.LoginViewModel
import com.example.tortilleriaderek.ui.screens.ConfiguracionScreen
import com.example.tortilleriaderek.ui.screens.HistorialScreen
import com.example.tortilleriaderek.ui.screens.MetricasScreen
import com.example.tortilleriaderek.ui.screens.MostradorRootScreen
import com.example.tortilleriaderek.ui.screens.ProduccionScreen

/**
 * Función de utilidad para navegar de forma limpia entre las pestañas
 * del Bottom Navigation Bar, evitando duplicar destinos en el backstack.
 */
private fun NavController.navigateToBottomBarTab(destination: Screen) {
    navigate(destination) {
        popUpTo(Screen.Mostrador) {
            saveState = true
        }
        launchSingleTop = true
        restoreState = true
    }
}

/**
 * El grafo de navegación de la aplicación Tortillería Derek.
 * Soporta arranque dinámico: Si existe un turno abierto en Room, inicia en Mostrador;
 * en caso contrario, inicia en Login (US8).
 */
@Composable
fun AppNavigation(
    mainViewModel: MainNavigationViewModel = hiltViewModel()
) {
    val navigationState by mainViewModel.navigationState.collectAsStateWithLifecycle()

    when (val state = navigationState) {
        NavigationStartState.Loading -> {
            // Fondo neutro sin parpadeos mientras se lee el estado del turno en Room
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(MaterialTheme.colorScheme.background)
            )
        }
        is NavigationStartState.Ready -> {
            val navController = rememberNavController()

            NavHost(
                navController = navController,
                startDestination = state.startDestination,
                enterTransition = { EnterTransition.None },
                exitTransition = { ExitTransition.None },
                popEnterTransition = { EnterTransition.None },
                popExitTransition = { ExitTransition.None }
            ) {
                composable<Screen.Login> {
                    val viewModel = hiltViewModel<LoginViewModel>()
                    val uiState by viewModel.uiState.collectAsStateWithLifecycle()

                    LaunchedEffect(Unit) {
                        viewModel.effect.collect { effect ->
                            when (effect) {
                                LoginUiEffect.NavigateToMostrador -> {
                                    navController.navigate(Screen.Mostrador) {
                                        popUpTo(Screen.Login) { inclusive = true }
                                    }
                                }
                                LoginUiEffect.NavigateToMetricas -> {
                                    navController.navigate(Screen.MetricasSoloConsulta)
                                }
                                LoginUiEffect.NavigateToSettings -> {
                                    navController.navigate(Screen.ConfiguracionDesdeLogin)
                                }
                                is LoginUiEffect.ShowError -> {
                                    // Mostrar mensaje de error
                                }
                            }
                        }
                    }

                    LoginScreenContent(
                        uiState = uiState,
                        onEvent = viewModel::onEvent
                    )
                }

                // Pantalla Principal de Venta (Mostrador y Repartidores con Turno y Room)
                composable<Screen.Mostrador> {
                    MostradorRootScreen(
                        onNavigateToHistorial = { origen ->
                            navController.navigate(Screen.Historial(filtroInicial = origen))
                        },
                        onNavigateToLogin = {
                            navController.navigate(Screen.Login) {
                                popUpTo(0) { inclusive = true }
                            }
                        },
                        onNavigateToProduccion = {
                            navController.navigateToBottomBarTab(Screen.Produccion)
                        },
                        onNavigateToMetricas = {
                            navController.navigateToBottomBarTab(Screen.Metricas)
                        },
                        onNavigateToConfiguracion = {
                            navController.navigateToBottomBarTab(Screen.Ajustes)
                        }
                    )
                }

                // Pantalla de Historial de Ventas
                composable<Screen.Historial> { backStackEntry ->
                    val historialRoute = backStackEntry.toRoute<Screen.Historial>()
                    HistorialScreen(
                        filtroInicial = historialRoute.filtroInicial,
                        onBack = {
                            navController.popBackStack()
                        },
                        onNavigateToProduccion = {
                            navController.navigateToBottomBarTab(Screen.Produccion)
                        },
                        onNavigateToMetricas = {
                            navController.navigateToBottomBarTab(Screen.Metricas)
                        },
                        onNavigateToConfiguracion = {
                            navController.navigateToBottomBarTab(Screen.Ajustes)
                        }
                    )
                }

                // Pantalla de Producción (Fiel a Imagen 1)
                composable<Screen.Produccion> {
                    ProduccionScreen(
                        onNavigateToVenta = {
                            navController.navigateToBottomBarTab(Screen.Mostrador)
                        },
                        onNavigateToProduccion = { /* Ya estamos en Producción */ },
                        onNavigateToMetricas = {
                            navController.navigateToBottomBarTab(Screen.Metricas)
                        },
                        onNavigateToConfiguracion = {
                            navController.navigateToBottomBarTab(Screen.Ajustes)
                        },
                        onVerHistorialProduccion = {
                            navController.navigate(Screen.Historial(filtroInicial = "TODAS"))
                        }
                    )
                }

                // Pantalla de Métricas (Fiel a Imagen 2)
                composable<Screen.Metricas> {
                    MetricasScreen(
                        onNavigateToVenta = {
                            navController.navigateToBottomBarTab(Screen.Mostrador)
                        },
                        onNavigateToProduccion = {
                            navController.navigateToBottomBarTab(Screen.Produccion)
                        },
                        onNavigateToMetricas = { /* Ya estamos en Métricas */ },
                        onNavigateToConfiguracion = {
                            navController.navigateToBottomBarTab(Screen.Ajustes)
                        }
                    )
                }

                // Pantalla de Métricas en Modo Solo Consulta (Sin BottomBar y con salida a Login)
                composable<Screen.MetricasSoloConsulta> {
                    MetricasScreen(
                        mostrarBottomBar = false,
                        onCerrarConsulta = {
                            navController.navigate(Screen.Login) {
                                popUpTo(0) { inclusive = true }
                            }
                        }
                    )
                }

                // Pantalla de Ajustes / Configuración desde Login (Sin BottomBar y con retorno a Login)
                composable<Screen.ConfiguracionDesdeLogin> {
                    ConfiguracionScreen(
                        mostrarBottomBar = false,
                        onBackToLogin = {
                            navController.popBackStack()
                        }
                    )
                }

                // Pantalla de Ajustes / Configuración en Flujo Operativo Normal (Con BottomBar)
                composable<Screen.Ajustes> {
                    ConfiguracionScreen(
                        mostrarBottomBar = true,
                        onNavigateToVenta = {
                            navController.navigateToBottomBarTab(Screen.Mostrador)
                        },
                        onNavigateToProduccion = {
                            navController.navigateToBottomBarTab(Screen.Produccion)
                        },
                        onNavigateToMetricas = {
                            navController.navigateToBottomBarTab(Screen.Metricas)
                        },
                        onNavigateToConfiguracion = { /* Ya estamos en Ajustes */ }
                    )
                }
            }
        }
    }
}
