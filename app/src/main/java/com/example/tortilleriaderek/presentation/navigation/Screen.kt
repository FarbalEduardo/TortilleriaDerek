package com.example.tortilleriaderek.presentation.navigation

import kotlinx.serialization.Serializable

/**
 * Contrato de rutas para Navigation Compose Type-Safe.
 */
sealed interface Screen {
    
    @Serializable
    object Login : Screen
    
    @Serializable
    object Mostrador : Screen
    
    @Serializable
    data class Historial(val filtroInicial: String = "TODAS") : Screen

    @Serializable
    object Produccion : Screen
    
    @Serializable
    object Metricas : Screen
    
    @Serializable
    object MetricasSoloConsulta : Screen
    
    @Serializable
    object Ajustes : Screen

    @Serializable
    object ConfiguracionDesdeLogin : Screen
}
