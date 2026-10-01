package com.example.tortilleriaderek.presentation.metricas

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.tortilleriaderek.data.local.dao.ProduccionDao
import com.example.tortilleriaderek.data.local.dao.RutaRepartidorDao
import com.example.tortilleriaderek.data.local.dao.TurnoDao
import com.example.tortilleriaderek.data.local.dao.VentaDao
import com.example.tortilleriaderek.data.local.entity.MermaProduccionEntity
import com.example.tortilleriaderek.data.local.entity.RutaRepartidorEntity
import com.example.tortilleriaderek.data.local.entity.TandaProduccionEntity
import com.example.tortilleriaderek.data.local.entity.VentaEntity
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import java.time.Instant
import java.time.LocalDate
import java.time.ZoneId
import java.time.format.TextStyle
import java.util.Locale
import java.util.regex.Pattern
import javax.inject.Inject

@HiltViewModel
class MetricasViewModel @Inject constructor(
    private val turnoDao: TurnoDao,
    private val ventaDao: VentaDao,
    private val produccionDao: ProduccionDao,
    private val rutaRepartidorDao: RutaRepartidorDao
) : ViewModel() {

    private val _uiState = MutableStateFlow(MetricasUiState(isLoading = true))
    val uiState: StateFlow<MetricasUiState> = _uiState.asStateFlow()

    private val _effect = Channel<MetricasUiEffect>(Channel.BUFFERED)
    val effect = _effect.receiveAsFlow()

    // Parámetros reactivos de filtro
    private val _periodo = MutableStateFlow(PeriodoFiltro.SEMANAL)
    private val _metrica = MutableStateFlow(MetricaTipo.PRODUCCION_TOTAL)
    private val _customRange = MutableStateFlow<Pair<Long, Long>?>(null)

    init {
        observarDatosAnaliticos()
    }

    fun onEvent(event: MetricasUiEvent) {
        when (event) {
            is MetricasUiEvent.SelectPeriodo -> {
                _periodo.value = event.periodo
                if (event.periodo != PeriodoFiltro.PERSONALIZADO) {
                    _customRange.value = null
                }
            }
            is MetricasUiEvent.SelectMetrica -> {
                _metrica.value = event.metrica
            }
            is MetricasUiEvent.SetCustomDateRange -> {
                _customRange.value = Pair(event.inicioMillis, event.finMillis)
                _periodo.value = PeriodoFiltro.PERSONALIZADO
                _uiState.update {
                    it.copy(
                        showCustomDatePicker = false,
                        customFechaInicio = event.inicioMillis,
                        customFechaFin = event.finMillis
                    )
                }
            }
            MetricasUiEvent.ShowCustomDatePicker -> {
                _uiState.update { it.copy(showCustomDatePicker = true) }
            }
            MetricasUiEvent.DismissCustomDatePicker -> {
                _uiState.update { it.copy(showCustomDatePicker = false) }
            }
        }
    }

    private fun observarDatosAnaliticos() {
        viewModelScope.launch {
            val roomDataFlow = combine(
                ventaDao.getTodasVentasTurnosCerrados(),
                produccionDao.getTodasTandasTurnosCerrados(),
                produccionDao.getTodasMermasTurnosCerrados(),
                rutaRepartidorDao.getRutasTurnosCerrados()
            ) { v, t, m, r ->
                RoomData(v, t, m, r)
            }

            val filtrosFlow = combine(
                _periodo,
                _metrica,
                _customRange
            ) { p, m, c ->
                Filtros(p, m, c)
            }

            combine(roomDataFlow, filtrosFlow) { data, filtros ->
                calcularEstadoMetricas(
                    ventas = data.ventas,
                    tandas = data.tandas,
                    mermas = data.mermas,
                    rutas = data.rutas,
                    periodo = filtros.periodo,
                    metrica = filtros.metrica,
                    customRange = filtros.customRange
                )
            }.collectLatest { nuevoEstado ->
                _uiState.value = nuevoEstado
            }
        }
    }

    private data class RoomData(
        val ventas: List<VentaEntity>,
        val tandas: List<TandaProduccionEntity>,
        val mermas: List<MermaProduccionEntity>,
        val rutas: List<RutaRepartidorEntity>
    )

    private data class Filtros(
        val periodo: PeriodoFiltro,
        val metrica: MetricaTipo,
        val customRange: Pair<Long, Long>?
    )

    fun calcularEstadoMetricas(
        ventas: List<VentaEntity>,
        tandas: List<TandaProduccionEntity>,
        mermas: List<MermaProduccionEntity>,
        rutas: List<RutaRepartidorEntity>,
        periodo: PeriodoFiltro,
        metrica: MetricaTipo,
        customRange: Pair<Long, Long>?
    ): MetricasUiState {
        val hoy = LocalDate.now()
        val zone = ZoneId.systemDefault()

        // 1. Determinar intervalo de fechas [fechaInicio, fechaFin]
        val (fechaInicio, fechaFin) = when (periodo) {
            PeriodoFiltro.SEMANAL -> Pair(hoy.minusDays(6), hoy)
            PeriodoFiltro.QUINCENAL -> Pair(hoy.minusDays(14), hoy)
            PeriodoFiltro.MENSUAL -> Pair(hoy.minusDays(29), hoy)
            PeriodoFiltro.PERSONALIZADO -> {
                if (customRange != null) {
                    val rawStart = Instant.ofEpochMilli(customRange.first).atZone(zone).toLocalDate()
                    val rawEnd = Instant.ofEpochMilli(customRange.second).atZone(zone).toLocalDate()
                    // US8: Acotar fecha fin a hoy si se seleccionaron fechas futuras
                    val end = if (rawEnd.isAfter(hoy)) hoy else rawEnd
                    val startBeforeClamp = if (rawStart.isAfter(end)) end else rawStart
                    // Restricción: No permitir más de 30 días en período personalizado
                    val maxDays = 30L
                    val start = if (java.time.temporal.ChronoUnit.DAYS.between(startBeforeClamp, end) + 1 > maxDays) {
                        end.minusDays(maxDays - 1)
                    } else {
                        startBeforeClamp
                    }
                    Pair(start, end)
                } else {
                    Pair(hoy.minusDays(6), hoy)
                }
            }
        }

        val rangoTexto = formatearRangoTexto(fechaInicio, fechaFin)

        // 2. Generar lista exhaustiva de días del período para asegurar continuidad del gráfico
        val diasPeriodo = mutableListOf<LocalDate>()
        var curr = fechaInicio
        while (!curr.isAfter(fechaFin)) {
            diasPeriodo.add(curr)
            curr = curr.plusDays(1)
        }

        val diasCount = diasPeriodo.size.coerceAtLeast(1)

        // Período anterior para comparativa porcentual
        val fechaInicioAnterior = fechaInicio.minusDays(diasCount.toLong())
        val fechaFinAnterior = fechaInicio.minusDays(1)

        // Mapear timestamp de registros a LocalDate
        fun timestampToLocalDate(ts: Long): LocalDate {
            return Instant.ofEpochMilli(ts).atZone(zone).toLocalDate()
        }

        // Filtrar datos para período actual
        val tandasPeriodo = tandas.filter {
            val d = timestampToLocalDate(it.fecha)
            !d.isBefore(fechaInicio) && !d.isAfter(fechaFin)
        }
        val ventasPeriodo = ventas.filter {
            val d = timestampToLocalDate(it.fecha)
            !d.isBefore(fechaInicio) && !d.isAfter(fechaFin)
        }
        val mermasPeriodo = mermas.filter {
            val d = timestampToLocalDate(it.fecha)
            !d.isBefore(fechaInicio) && !d.isAfter(fechaFin)
        }

        // Filtrar datos para período anterior
        val tandasPeriodoAnt = tandas.filter {
            val d = timestampToLocalDate(it.fecha)
            !d.isBefore(fechaInicioAnterior) && !d.isAfter(fechaFinAnterior)
        }
        val ventasPeriodoAnt = ventas.filter {
            val d = timestampToLocalDate(it.fecha)
            !d.isBefore(fechaInicioAnterior) && !d.isAfter(fechaFinAnterior)
        }

        // 3. Consolidación diaria multiturno según la métrica seleccionada
        val valoresPorDia = mutableMapOf<LocalDate, Double>()
        diasPeriodo.forEach { valoresPorDia[it] = 0.0 }

        when (metrica) {
            MetricaTipo.PRODUCCION_TOTAL -> {
                tandasPeriodo.forEach { tanda ->
                    val d = timestampToLocalDate(tanda.fecha)
                    valoresPorDia[d] = (valoresPorDia[d] ?: 0.0) + tanda.kgTortillaEstimada
                }
            }
            MetricaTipo.VENTA_TOTAL -> {
                ventasPeriodo.filter { it.estado == "ACTIVA" }.forEach { venta ->
                    val d = timestampToLocalDate(venta.fecha)
                    valoresPorDia[d] = (valoresPorDia[d] ?: 0.0) + venta.total
                }
            }
            MetricaTipo.VENTA_MOSTRADOR -> {
                ventasPeriodo.filter { it.tipo == "MOSTRADOR" && it.estado == "ACTIVA" }.forEach { venta ->
                    val d = timestampToLocalDate(venta.fecha)
                    valoresPorDia[d] = (valoresPorDia[d] ?: 0.0) + venta.total
                }
            }
            MetricaTipo.VENTA_REPARTIDOR -> {
                ventasPeriodo.filter { it.tipo == "REPARTIDOR" && it.estado == "ACTIVA" }.forEach { venta ->
                    val d = timestampToLocalDate(venta.fecha)
                    valoresPorDia[d] = (valoresPorDia[d] ?: 0.0) + venta.total
                }
            }
        }

        val totalPeriodoActual = valoresPorDia.values.sum()

        // 4. Calcular total del período anterior para variacionPorcentual
        val totalPeriodoAnterior = when (metrica) {
            MetricaTipo.PRODUCCION_TOTAL -> tandasPeriodoAnt.sumOf { it.kgTortillaEstimada }
            MetricaTipo.VENTA_TOTAL -> ventasPeriodoAnt.filter { it.estado == "ACTIVA" }.sumOf { it.total }
            MetricaTipo.VENTA_MOSTRADOR -> ventasPeriodoAnt.filter { it.tipo == "MOSTRADOR" && it.estado == "ACTIVA" }.sumOf { it.total }
            MetricaTipo.VENTA_REPARTIDOR -> ventasPeriodoAnt.filter { it.tipo == "REPARTIDOR" && it.estado == "ACTIVA" }.sumOf { it.total }
        }

        val variacionPorcentual = if (totalPeriodoAnterior > 0.0) {
            ((totalPeriodoActual - totalPeriodoAnterior) / totalPeriodoAnterior) * 100.0
        } else if (totalPeriodoActual > 0.0) {
            100.0
        } else {
            0.0
        }

        // 5. Encontrar punto pico (máximo valor)
        var maxValor = -1.0
        var diaPico: LocalDate? = null
        for ((d, v) in valoresPorDia) {
            if (v > maxValor && v > 0.0) {
                maxValor = v
                diaPico = d
            }
        }

        // Si todos los valores son 0, tomamos el día intermedio como referencia neutral
        if (diaPico == null && diasPeriodo.isNotEmpty()) {
            diaPico = diasPeriodo[diasPeriodo.size / 2]
        }

        val puntosGrafica = diasPeriodo.map { dia ->
            val v = valoresPorDia[dia] ?: 0.0
            val etiqueta = if (diasPeriodo.size <= 7) {
                dia.dayOfWeek.getDisplayName(TextStyle.SHORT, Locale("es", "MX"))
                    .replaceFirstChar { it.uppercase() }
                    .removeSuffix(".")
            } else {
                "${dia.dayOfMonth}"
            }
            val formatted = if (metrica.unidad == "kg") "%.1f kg".format(v) else "$%,.0f".format(v)
            val esPico = (dia == diaPico && (maxValor > 0.0 || diasPeriodo.size <= 7))
            PuntoGraficaDia(
                fecha = dia,
                diaEtiqueta = etiqueta,
                valor = v,
                valorFormateado = formatted,
                esPico = esPico
            )
        }

        val puntoPico = puntosGrafica.firstOrNull { it.esPico }
        val tooltipPicoTexto = if (puntoPico != null) {
            val diaSemana = puntoPico.fecha.dayOfWeek.getDisplayName(TextStyle.FULL, Locale("es", "MX"))
                .replaceFirstChar { it.uppercase() }
            if (metrica.unidad == "kg") {
                "%.1f kg • %s".format(puntoPico.valor, diaSemana)
            } else {
                "$%,.0f MXN • %s".format(puntoPico.valor, diaSemana)
            }
        } else ""

        // 6. Distribución de Canales (Mostrador vs Repartidor)
        val ventasActivas = ventasPeriodo.filter { it.estado == "ACTIVA" }
        val montoMostrador = ventasActivas.filter { it.tipo == "MOSTRADOR" }.sumOf { it.total }
        val montoReparto = ventasActivas.filter { it.tipo == "REPARTIDOR" }.sumOf { it.total }
        val montoTotalVentas = montoMostrador + montoReparto

        val porcentajeMostrador = if (montoTotalVentas > 0.0) {
            ((montoMostrador / montoTotalVentas) * 100.0).toFloat()
        } else 50f

        val porcentajeReparto = if (montoTotalVentas > 0.0) {
            ((montoReparto / montoTotalVentas) * 100.0).toFloat()
        } else 50f

        val kgMostrador = calcularKgMostrador(ventasActivas)
        // Kilos repartidor: sum of rutas liquidadas asociadas a turnos cerrados
        val turnosCerradosIds = ventasActivas.map { it.turnoId }.toSet()
        val kgReparto = rutas.filter { it.turnoId in turnosCerradosIds && it.status == "LIQUIDADO" }.sumOf { it.entregadoKg }

        // 7. Promedio Diario y Ratio de Merma
        val promedioDiario = totalPeriodoActual / diasCount
        val promedioDiarioTexto = if (metrica.unidad == "kg") {
            "%.1f kg/día".format(promedioDiario)
        } else {
            "$%,.0f MXN/día".format(promedioDiario)
        }

        val totalProduccionKg = tandasPeriodo.sumOf { it.kgTortillaEstimada }
        val totalMermaKg = mermasPeriodo.sumOf { it.kgMerma }
        val porcentajeMerma = if (totalProduccionKg > 0.0) {
            (totalMermaKg / totalProduccionKg) * 100.0
        } else 0.0
        val esMermaOptima = porcentajeMerma <= 5.0

        val hayDatos = totalPeriodoActual > 0.0 || totalProduccionKg > 0.0 || montoTotalVentas > 0.0

        return MetricasUiState(
            isLoading = false,
            rangoFechasTexto = rangoTexto,
            periodoSeleccionado = periodo,
            metricaSeleccionada = metrica,
            totalPeriodoActual = totalPeriodoActual,
            unidadMetrica = metrica.unidad,
            variacionPorcentual = variacionPorcentual,
            puntosGrafica = puntosGrafica,
            puntoPico = puntoPico,
            tooltipPicoTexto = tooltipPicoTexto,
            montoMostrador = montoMostrador,
            montoReparto = montoReparto,
            porcentajeMostrador = porcentajeMostrador,
            porcentajeReparto = porcentajeReparto,
            kgMostrador = kgMostrador,
            kgReparto = kgReparto,
            promedioDiario = promedioDiario,
            promedioDiarioTexto = promedioDiarioTexto,
            porcentajeMerma = porcentajeMerma,
            esMermaOptima = esMermaOptima,
            hayDatos = hayDatos,
            showCustomDatePicker = false,
            customFechaInicio = customRange?.first,
            customFechaFin = customRange?.second
        )
    }

    private fun calcularKgMostrador(ventas: List<VentaEntity>): Double {
        var totalKg = 0.0
        val regex = Pattern.compile("(\\d+)\\s*(?:x\\s*)?([^,]+)")

        for (venta in ventas.filter { it.tipo == "MOSTRADOR" && it.estado == "ACTIVA" }) {
            var kgTicket = 0.0
            val matcher = regex.matcher(venta.detalleProductos)
            while (matcher.find()) {
                val qty = matcher.group(1)?.toIntOrNull() ?: 0
                val desc = matcher.group(2)?.trim()?.lowercase(Locale.ROOT) ?: ""
                when {
                    desc.contains("medio") -> kgTicket += qty * 0.40
                    desc.contains("mayoreo") -> kgTicket += qty * 0.80
                    desc.contains("paquete") -> kgTicket += qty * 0.80
                    desc.contains("kilo") -> kgTicket += qty * 1.00
                    else -> kgTicket += qty * 1.00
                }
            }
            if (kgTicket == 0.0 && venta.total > 0.0) {
                kgTicket = venta.total / 24.00
            }
            totalKg += kgTicket
        }
        return totalKg
    }

    private fun formatearRangoTexto(inicio: LocalDate, fin: LocalDate): String {
        val mesInicio = inicio.month.getDisplayName(TextStyle.SHORT, Locale("es", "MX"))
            .replaceFirstChar { it.uppercase() }.removeSuffix(".")
        val mesFin = fin.month.getDisplayName(TextStyle.SHORT, Locale("es", "MX"))
            .replaceFirstChar { it.uppercase() }.removeSuffix(".")

        return if (inicio.month == fin.month && inicio.year == fin.year) {
            "${inicio.dayOfMonth} – ${fin.dayOfMonth} $mesInicio ${inicio.year}"
        } else {
            "${inicio.dayOfMonth} $mesInicio – ${fin.dayOfMonth} $mesFin ${fin.year}"
        }
    }
}
