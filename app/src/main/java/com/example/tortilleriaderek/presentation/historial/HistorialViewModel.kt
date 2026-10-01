package com.example.tortilleriaderek.presentation.historial

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.tortilleriaderek.data.local.dao.TurnoDao
import com.example.tortilleriaderek.data.local.dao.VentaDao
import com.example.tortilleriaderek.data.local.entity.TurnoEntity
import com.example.tortilleriaderek.data.local.entity.VentaEntity
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import java.time.Instant
import java.time.LocalDate
import java.time.ZoneId
import java.time.format.DateTimeFormatter
import java.util.Locale
import javax.inject.Inject

@HiltViewModel
class HistorialViewModel @Inject constructor(
    private val turnoDao: TurnoDao,
    private val ventaDao: VentaDao
) : ViewModel() {

    private val _uiState = MutableStateFlow(HistorialUiState(isLoading = true))
    val uiState: StateFlow<HistorialUiState> = _uiState.asStateFlow()

    private val _effect = Channel<HistorialUiEffect>(Channel.BUFFERED)
    val effect = _effect.receiveAsFlow()

    private val _filtro = MutableStateFlow(HistorialFiltroTipo.TODAS)

    init {
        observarHistorialVentas()
    }

    fun onEvent(event: HistorialUiEvent) {
        when (event) {
            is HistorialUiEvent.SetFiltroInicial -> {
                val orden = obtenerOrdenChips(event.filtro)
                _filtro.value = event.filtro
                _uiState.update { current ->
                    val filtradas = filtrarTransacciones(current.todasLasTransacciones, event.filtro)
                    current.copy(
                        filtroSeleccionado = event.filtro,
                        chipsOrden = orden,
                        transaccionesFiltradas = filtradas
                    )
                }
            }
            is HistorialUiEvent.SelectFiltro -> {
                _filtro.value = event.filtro
                _uiState.update { current ->
                    val filtradas = filtrarTransacciones(current.todasLasTransacciones, event.filtro)
                    current.copy(
                        filtroSeleccionado = event.filtro,
                        transaccionesFiltradas = filtradas
                    )
                }
            }
            is HistorialUiEvent.SolicitarEliminarVenta -> {
                _uiState.update { it.copy(transaccionAEliminar = event.item) }
            }
            HistorialUiEvent.CancelarEliminarVenta -> {
                _uiState.update { it.copy(transaccionAEliminar = null) }
            }
            HistorialUiEvent.ConfirmarEliminarVenta -> {
                val venta = _uiState.value.transaccionAEliminar ?: return
                if (_uiState.value.isTurnoCerrado) {
                    viewModelScope.launch {
                        _effect.send(HistorialUiEffect.ShowToast("No se puede eliminar ventas de un turno cerrado."))
                    }
                    _uiState.update { it.copy(transaccionAEliminar = null) }
                    return
                }
                viewModelScope.launch {
                    ventaDao.deleteVenta(venta.id)
                    _uiState.update { it.copy(transaccionAEliminar = null) }
                    _effect.send(HistorialUiEffect.ShowToast("Ticket ${venta.ticketFolio} eliminado correctamente."))
                }
            }
            HistorialUiEvent.Recargar -> {
                observarHistorialVentas()
            }
        }
    }

    private fun obtenerOrdenChips(filtroInicial: HistorialFiltroTipo): List<HistorialFiltroTipo> {
        return when (filtroInicial) {
            HistorialFiltroTipo.MOSTRADOR -> listOf(
                HistorialFiltroTipo.MOSTRADOR,
                HistorialFiltroTipo.TODAS,
                HistorialFiltroTipo.REPARTIDOR
            )
            HistorialFiltroTipo.REPARTIDOR -> listOf(
                HistorialFiltroTipo.REPARTIDOR,
                HistorialFiltroTipo.TODAS,
                HistorialFiltroTipo.MOSTRADOR
            )
            HistorialFiltroTipo.TODAS -> listOf(
                HistorialFiltroTipo.TODAS,
                HistorialFiltroTipo.MOSTRADOR,
                HistorialFiltroTipo.REPARTIDOR
            )
        }
    }

    private fun observarHistorialVentas() {
        viewModelScope.launch {
            // 1. Observar turno activo o último turno si no hay activo
            combine(
                turnoDao.getTurnoActivo(),
                turnoDao.getUltimoTurno()
            ) { activo, ultimo ->
                activo ?: ultimo
            }.filterNotNull().collectLatest { turno ->
                val fechaTexto = formatearFechaTurno(turno.fechaApertura)
                val isCerrado = turno.estado == "CERRADO"

                // 2. Observar ventas del turno seleccionado en tiempo real desde Room
                ventaDao.getVentasPorTurno(turno.id).collectLatest { listaVentas ->
                    procesarVentas(turno, listaVentas, fechaTexto, isCerrado)
                }
            }
        }
    }

    private fun procesarVentas(
        turno: TurnoEntity,
        ventas: List<VentaEntity>,
        fechaTurnoTexto: String,
        isTurnoCerrado: Boolean
    ) {
        val ventasActivas = ventas.filter { it.estado == "ACTIVA" }

        val mostradorVentas = ventasActivas.filter { it.tipo == "MOSTRADOR" }
        val repartidorVentas = ventasActivas.filter { it.tipo == "REPARTIDOR" }

        val totalMostrador = mostradorVentas.sumOf { it.total }
        val totalRepartidor = repartidorVentas.sumOf { it.total }
        val totalTurno = totalMostrador + totalRepartidor

        val transaccionesMostradorCount = mostradorVentas.size
        val transaccionesRepartidorCount = repartidorVentas.size
        val transaccionesTotalCount = transaccionesMostradorCount + transaccionesRepartidorCount

        // Regla de Negocio: Cada liquidación de repartidor cuenta como una venta
        val numeroVentasMostrador = transaccionesMostradorCount
        val numeroVentasRepartidor = transaccionesRepartidorCount
        val numeroVentasTotal = numeroVentasMostrador + numeroVentasRepartidor

        val itemsUi = ventasActivas.map { v ->
            val fechaTxt = formatearFechaVenta(v.fecha)
            HistorialItemUi(
                id = v.id,
                ticketFolio = v.folioTicket.ifBlank { "—" },
                tipo = v.tipo,
                fechaTimestamp = v.fecha,
                fechaTexto = fechaTxt,
                hora = v.hora.ifBlank { "—" },
                monto = v.total,
                detalleProductos = v.detalleProductos,
                usuarioNombre = v.usuarioNombre,
                nombreRepartidor = v.nombreRepartidor,
                motoAsignada = v.motoAsignada,
                nombreRuta = v.nombreRuta
            )
        }

        val filtroActual = _filtro.value
        val filtradas = filtrarTransacciones(itemsUi, filtroActual)

        _uiState.update {
            it.copy(
                isLoading = false,
                turnoId = turno.id,
                fechaTurnoTexto = fechaTurnoTexto,
                isTurnoCerrado = isTurnoCerrado,
                totalVentasTurno = totalTurno,
                totalVentasMostrador = totalMostrador,
                totalVentasRepartidores = totalRepartidor,
                numeroVentasTotal = numeroVentasTotal,
                numeroVentasMostrador = numeroVentasMostrador,
                numeroVentasRepartidor = numeroVentasRepartidor,
                transaccionesTotalCount = transaccionesTotalCount,
                transaccionesMostradorCount = transaccionesMostradorCount,
                transaccionesRepartidorCount = transaccionesRepartidorCount,
                filtroSeleccionado = filtroActual,
                todasLasTransacciones = itemsUi,
                transaccionesFiltradas = filtradas
            )
        }
    }

    private fun filtrarTransacciones(
        items: List<HistorialItemUi>,
        filtro: HistorialFiltroTipo
    ): List<HistorialItemUi> {
        return when (filtro) {
            HistorialFiltroTipo.TODAS -> items
            HistorialFiltroTipo.MOSTRADOR -> items.filter { it.tipo == "MOSTRADOR" }
            HistorialFiltroTipo.REPARTIDOR -> items.filter { it.tipo == "REPARTIDOR" }
        }
    }

    private fun formatearFechaTurno(timestamp: Long): String {
        return try {
            val fecha = Instant.ofEpochMilli(timestamp).atZone(ZoneId.systemDefault()).toLocalDate()
            val hoy = LocalDate.now()
            val diaSemana = fecha.format(DateTimeFormatter.ofPattern("EEEE", Locale("es", "MX")))
                .replaceFirstChar { it.uppercase() }
            val dia = fecha.dayOfMonth
            val mes = fecha.format(DateTimeFormatter.ofPattern("MMMM", Locale("es", "MX")))
                .replaceFirstChar { it.uppercase() }

            if (fecha == hoy) {
                "Hoy, $diaSemana $dia de $mes"
            } else {
                "$diaSemana $dia de $mes"
            }
        } catch (e: Exception) {
            "Hoy"
        }
    }

    private fun formatearFechaVenta(timestamp: Long): String {
        return try {
            val fecha = Instant.ofEpochMilli(timestamp).atZone(ZoneId.systemDefault()).toLocalDate()
            val hoy = LocalDate.now()
            if (fecha == hoy) {
                "Hoy"
            } else {
                fecha.format(DateTimeFormatter.ofPattern("dd/MM/yyyy", Locale("es", "MX")))
            }
        } catch (e: Exception) {
            "Hoy"
        }
    }
}
