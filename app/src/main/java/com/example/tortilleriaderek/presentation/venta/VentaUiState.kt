package com.example.tortilleriaderek.presentation.venta

import com.example.tortilleriaderek.data.local.entity.RutaRepartidorEntity
import com.example.tortilleriaderek.data.local.entity.TurnoEntity
import com.example.tortilleriaderek.data.local.entity.VentaEntity
import com.example.tortilleriaderek.model.ProductItem

data class VentaUiState(
    val turnoActivo: TurnoEntity? = null,
    val usuarioActivoNombre: String = "admin1",
    val fechaTurnoTexto: String = "",
    val isTurnoCerrado: Boolean = false,
    
    // Header & Totales
    val selectedScope: String = "Todos", // "Todos", "Mostrador", "Repartidores"
    val totalVentasDia: Double = 0.0,
    val totalVentasMostrador: Double = 0.0,
    val totalVentasRepartidores: Double = 0.0,
    
    // Tab Mostrador (Solo los 3 productos principales de mostrador)
    val products: List<ProductItem> = listOf(
        ProductItem(1, "Kilogramo", 0.00, "kg", quantity = 0, pesoGramos = 1000),
        ProductItem(2, "Medio Paquete", 0.00, "paq", quantity = 0, pesoGramos = 0),
        ProductItem(3, "Paquete Mostrador", 0.00, "paq", quantity = 0, pesoGramos = 0)
    ),
    val proximoTicketMostrador: String = "M-0001",
    val showConfirmarVentaDialog: Boolean = false,
    val metodoPagoSeleccionado: String = "Efectivo",
    
    // Tab Repartidores / Mayoreo
    val rutasRepartidores: List<RutaRepartidorEntity> = emptyList(),
    val rutasActivasCount: Int = 0,
    val repartidorSeleccionadoParaSalida: RutaRepartidorEntity? = null,
    val repartidorSeleccionadoParaLiquidar: RutaRepartidorEntity? = null,
    val precioPaqueteRepartidor: Double = 0.00,
    val pesoPaqueteRepartidorKg: Double = 0.0,
    val pesoPaqueteMostradorKg: Double = 0.0,
    val pesoMedioPaqueteKg: Double = 0.0,
    
    // Diálogo Cierre de Turno
    val showCerrarTurnoDialog: Boolean = false,
    
    // Monto última venta
    val ultimoMontoVenta: Double = 0.0,

    // Stock y Producción para validación
    val disponibleEnTiendaKg: Double = 0.0,
    val producidoNetoKg: Double = 0.0
) {
    val subtotalOrdenActual: Double
        get() = products.sumOf { it.subtotal }

    val kgOrdenActual: Double
        get() = products.sumOf { p ->
            val pesoKg = when (p.id) {
                1 -> 1.00 // Kilogramo (1,000 g)
                2 -> pesoMedioPaqueteKg // Medio Paquete (400 g)
                3 -> pesoPaqueteMostradorKg // Paquete Mostrador (800 g)
                else -> 1.00
            }
            p.quantity * pesoKg
        }

    val totalMostradoEnHeader: Double
        get() = when (selectedScope) {
            "Mostrador" -> totalVentasMostrador
            "Repartidores" -> totalVentasRepartidores
            else -> totalVentasDia
        }
}

sealed interface VentaUiEffect {
    object NavigateToLogin : VentaUiEffect
    object NavigateToHistorial : VentaUiEffect
    data class ShowToast(val mensaje: String) : VentaUiEffect
}
