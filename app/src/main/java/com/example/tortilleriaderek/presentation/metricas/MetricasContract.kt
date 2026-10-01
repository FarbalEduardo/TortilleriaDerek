package com.example.tortilleriaderek.presentation.metricas

import java.time.LocalDate

enum class MetricaTipo(val label: String, val unidad: String) {
    PRODUCCION_TOTAL("Producción Total (kg)", "kg"),
    VENTA_TOTAL("Venta Total ($)", "MXN"),
    VENTA_MOSTRADOR("Venta Mostrador ($)", "MXN"),
    VENTA_REPARTIDOR("Venta Repartidor ($)", "MXN")
}

enum class PeriodoFiltro(val label: String, val dias: Int) {
    SEMANAL("Semanal", 7),
    QUINCENAL("Quincenal", 15),
    MENSUAL("Mensual", 30),
    PERSONALIZADO("Personalizado", 0)
}

data class PuntoGraficaDia(
    val fecha: LocalDate,
    val diaEtiqueta: String,
    val valor: Double,
    val valorFormateado: String,
    val esPico: Boolean = false
)

data class MetricasUiState(
    val isLoading: Boolean = false,
    val rangoFechasTexto: String = "",
    val periodoSeleccionado: PeriodoFiltro = PeriodoFiltro.SEMANAL,
    val metricaSeleccionada: MetricaTipo = MetricaTipo.PRODUCCION_TOTAL,
    val totalPeriodoActual: Double = 0.0,
    val unidadMetrica: String = "kg",
    val variacionPorcentual: Double = 0.0,
    val puntosGrafica: List<PuntoGraficaDia> = emptyList(),
    val puntoPico: PuntoGraficaDia? = null,
    val tooltipPicoTexto: String = "",
    // Distribución de canales
    val montoMostrador: Double = 0.0,
    val montoReparto: Double = 0.0,
    val porcentajeMostrador: Float = 0f,
    val porcentajeReparto: Float = 0f,
    val kgMostrador: Double = 0.0,
    val kgReparto: Double = 0.0,
    // Indicadores inferiores
    val promedioDiario: Double = 0.0,
    val promedioDiarioTexto: String = "",
    val porcentajeMerma: Double = 0.0,
    val esMermaOptima: Boolean = true,
    val hayDatos: Boolean = false,
    // Selector personalizado
    val showCustomDatePicker: Boolean = false,
    val customFechaInicio: Long? = null,
    val customFechaFin: Long? = null
)

sealed interface MetricasUiEvent {
    data class SelectPeriodo(val periodo: PeriodoFiltro) : MetricasUiEvent
    data class SelectMetrica(val metrica: MetricaTipo) : MetricasUiEvent
    data class SetCustomDateRange(val inicioMillis: Long, val finMillis: Long) : MetricasUiEvent
    data object ShowCustomDatePicker : MetricasUiEvent
    data object DismissCustomDatePicker : MetricasUiEvent
}

sealed interface MetricasUiEffect {
    data class ShowToast(val message: String) : MetricasUiEffect
}
