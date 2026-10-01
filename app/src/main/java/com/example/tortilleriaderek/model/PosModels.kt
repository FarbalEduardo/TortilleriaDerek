package com.example.tortilleriaderek.model

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Analytics
import androidx.compose.material.icons.filled.Factory
import androidx.compose.material.icons.filled.History
import androidx.compose.material.icons.filled.PointOfSale
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.filled.TwoWheeler
import androidx.compose.ui.graphics.vector.ImageVector

/**
 * Screen destinations for the POS application.
 */
sealed class Screen(val route: String, val title: String, val icon: ImageVector? = null) {
    object Login : Screen("login", "Acceso al Sistema")
    object Mostrador : Screen("mostrador", "Ventas Mostrador", Icons.Default.PointOfSale)
    object Repartidores : Screen("repartidores", "Repartidores", Icons.Default.TwoWheeler)
    object Produccion : Screen("produccion", "Producción", Icons.Default.Factory)
    object Historial : Screen("historial", "Historial", Icons.Default.History)
    object Metricas : Screen("metricas", "Métricas", Icons.Default.Analytics)
    object Configuracion : Screen("configuracion", "Ajustes", Icons.Default.Settings)
}

/**
 * Products available at the counter.
 */
data class ProductItem(
    val id: Int,
    val name: String,
    val price: Double,
    val unitLabel: String = "kg",
    val quantity: Int = 0,
    val pesoGramos: Int = 1000
) {
    val subtotal: Double
        get() = price * quantity
}

/**
 * Status of delivery route.
 */
enum class RepartidorStatus(val label: String) {
    EN_RUTA("En Ruta"),
    PENDIENTE_SALIDA("Pendiente de Salida"),
    LIQUIDADO("Liquidado")
}

/**
 * Delivery driver item.
 */
data class RepartidorItem(
    val id: String,
    val moto: String,
    val repartidorName: String,
    val cargaInicialKg: Double,
    val pendienteCobro: Double,
    val status: RepartidorStatus,
    val entregadoKg: Double = 0.0,
    val cobrado: Double = 0.0
)

/**
 * Production and raw materials log.
 */
data class ProduccionState(
    val bultosMaiz: Int = 18,
    val bultosHarina: Int = 12,
    val calKg: Double = 15.0,
    val gasLitros: Double = 180.0,
    val kgMasaAmasada: Double = 920.0,
    val kgTortillasProducidas: Double = 890.0,
    val mermaKg: Double = 8.5
)

/**
 * Sales ticket entry for history.
 */
data class VentaTicket(
    val id: String,
    val hora: String,
    val tipo: String, // "Mostrador" o "Repartidor"
    val detalle: String,
    val total: Double,
    val metodoPago: String = "Efectivo"
)
