package com.example.tortilleriaderek.presentation.historial

enum class HistorialFiltroTipo(val label: String) {
    TODAS("Total de transacciones"),
    MOSTRADOR("Venta Mostrador"),
    REPARTIDOR("Venta Repartidor")
}

data class HistorialItemUi(
    val id: String,
    val ticketFolio: String,
    val tipo: String, // "MOSTRADOR" o "REPARTIDOR"
    val fechaTimestamp: Long,
    val fechaTexto: String,
    val hora: String,
    val monto: Double,
    val detalleProductos: String,
    val usuarioNombre: String,
    val nombreRepartidor: String? = null,
    val motoAsignada: String? = null,
    val nombreRuta: String? = null,
    val isHighlighted: Boolean = false
)

data class HistorialUiState(
    val isLoading: Boolean = false,
    val turnoId: String? = null,
    val fechaTurnoTexto: String = "",
    val isTurnoCerrado: Boolean = false,
    
    // Totales de recaudación
    val totalVentasTurno: Double = 0.0,
    val totalVentasMostrador: Double = 0.0,
    val totalVentasRepartidores: Double = 0.0,
    
    // Conteo de ventas de negocio (Mostrador 1 por ticket, Repartidor cuenta como 1 si hay ventas)
    val numeroVentasTotal: Int = 0,
    val numeroVentasMostrador: Int = 0,
    val numeroVentasRepartidor: Int = 0,
    
    // Conteo de transacciones físicas en base de datos
    val transaccionesTotalCount: Int = 0,
    val transaccionesMostradorCount: Int = 0,
    val transaccionesRepartidorCount: Int = 0,
    
    // Filtro activo
    val filtroSeleccionado: HistorialFiltroTipo = HistorialFiltroTipo.TODAS,
    
    // Orden dinámico de los chips según pantalla de origen
    val chipsOrden: List<HistorialFiltroTipo> = listOf(
        HistorialFiltroTipo.TODAS,
        HistorialFiltroTipo.MOSTRADOR,
        HistorialFiltroTipo.REPARTIDOR
    ),
    
    // Transacciones filtradas para la lista
    val transaccionesFiltradas: List<HistorialItemUi> = emptyList(),
    val todasLasTransacciones: List<HistorialItemUi> = emptyList(),
    
    // Transacción a eliminar / cancelar
    val transaccionAEliminar: HistorialItemUi? = null
) {
    val totalMostradoEnHeader: Double
        get() = when (filtroSeleccionado) {
            HistorialFiltroTipo.MOSTRADOR -> totalVentasMostrador
            HistorialFiltroTipo.REPARTIDOR -> totalVentasRepartidores
            HistorialFiltroTipo.TODAS -> totalVentasTurno
        }

    val numeroVentasMostradoEnHeader: Int
        get() = when (filtroSeleccionado) {
            HistorialFiltroTipo.MOSTRADOR -> numeroVentasMostrador
            HistorialFiltroTipo.REPARTIDOR -> numeroVentasRepartidor
            HistorialFiltroTipo.TODAS -> numeroVentasTotal
        }
}

sealed interface HistorialUiEvent {
    data class SetFiltroInicial(val filtro: HistorialFiltroTipo) : HistorialUiEvent
    data class SelectFiltro(val filtro: HistorialFiltroTipo) : HistorialUiEvent
    data class SolicitarEliminarVenta(val item: HistorialItemUi) : HistorialUiEvent
    data object ConfirmarEliminarVenta : HistorialUiEvent
    data object CancelarEliminarVenta : HistorialUiEvent
    data object Recargar : HistorialUiEvent
}

sealed interface HistorialUiEffect {
    data class ShowToast(val mensaje: String) : HistorialUiEffect
}
