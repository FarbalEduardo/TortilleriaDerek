package com.example.tortilleriaderek.presentation.venta

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.tortilleriaderek.data.local.dao.ConfiguracionProduccionDao
import com.example.tortilleriaderek.data.local.dao.ProduccionDao
import com.example.tortilleriaderek.data.local.dao.RepartidorDao
import com.example.tortilleriaderek.data.local.dao.RutaRepartidorDao
import com.example.tortilleriaderek.data.local.dao.TurnoDao
import com.example.tortilleriaderek.data.local.dao.UsuarioDao
import com.example.tortilleriaderek.data.local.dao.VentaDao
import com.example.tortilleriaderek.data.local.entity.ConfiguracionProduccionEntity
import com.example.tortilleriaderek.data.local.entity.RutaRepartidorEntity
import com.example.tortilleriaderek.data.local.entity.TurnoEntity
import com.example.tortilleriaderek.data.local.entity.VentaEntity
import com.example.tortilleriaderek.model.ProductItem
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.receiveAsFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import java.time.Instant
import java.time.LocalTime
import java.time.ZoneId
import java.time.format.DateTimeFormatter
import java.time.format.TextStyle
import java.util.Locale
import java.util.UUID
import javax.inject.Inject

@HiltViewModel
class VentaViewModel @Inject constructor(
    private val turnoDao: TurnoDao,
    private val ventaDao: VentaDao,
    private val rutaRepartidorDao: RutaRepartidorDao,
    private val usuarioDao: UsuarioDao,
    private val produccionDao: ProduccionDao,
    private val repartidorDao: RepartidorDao,
    private val configuracionProduccionDao: ConfiguracionProduccionDao
) : ViewModel() {

    private val _uiState = MutableStateFlow(VentaUiState())
    val uiState: StateFlow<VentaUiState> = _uiState.asStateFlow()

    private val _effect = Channel<VentaUiEffect>()
    val effect = _effect.receiveAsFlow()

    init {
        observarPreciosConfiguracion()
        observarTurnoActivo()
    }

    private fun observarPreciosConfiguracion() {
        viewModelScope.launch {
            configuracionProduccionDao.getConfiguracion().collectLatest { config ->
                val cfg = if (config == null) {
                    val def = ConfiguracionProduccionEntity()
                    configuracionProduccionDao.insertOrUpdateConfiguracion(def)
                    def
                } else {
                    config
                }
                _uiState.update { current ->
                    val updatedProducts = current.products.map { p ->
                        when (p.id) {
                            1 -> p.copy(price = cfg.precioKilo, pesoGramos = 1000)
                            2 -> p.copy(price = cfg.precioMedioPaquete, pesoGramos = cfg.pesoMedioPaqueteGramos)
                            3 -> p.copy(price = cfg.precioPaquete, pesoGramos = cfg.pesoPaqueteGramos)
                            else -> p
                        }
                    }
                    current.copy(
                        products = updatedProducts,
                        precioPaqueteRepartidor = cfg.precioPaqueteRepartidor,
                        pesoPaqueteRepartidorKg = cfg.pesoPaqueteKg,
                        pesoPaqueteMostradorKg = cfg.pesoPaqueteKg,
                        pesoMedioPaqueteKg = cfg.pesoMedioPaqueteKg
                    )
                }
            }
        }
    }

    private fun observarTurnoActivo() {
        viewModelScope.launch {
            turnoDao.getTurnoActivo().collectLatest { turno ->
                if (turno != null) {
                    val fechaTexto = formatearFecha(turno.fechaApertura)
                    val usuario = usuarioDao.getUsuarioById(turno.usuarioId)
                    val nombreUsuario = usuario?.username ?: "admin1"
                    _uiState.update {
                        it.copy(
                            turnoActivo = turno,
                            usuarioActivoNombre = nombreUsuario,
                            fechaTurnoTexto = fechaTexto,
                            isTurnoCerrado = false,
                            products = it.products.map { p -> p.copy(quantity = 0) }
                        )
                    }
                    observarVentasYRepartidores(turno.id)
                } else {
                    _uiState.update {
                        it.copy(
                            turnoActivo = null,
                            isTurnoCerrado = true,
                            products = it.products.map { p -> p.copy(quantity = 0) }
                        )
                    }
                }
            }
        }
    }

    private fun observarVentasYRepartidores(turnoId: String) {
        // Observar ventas del turno y cuadre de stock
        viewModelScope.launch {
            combine(
                produccionDao.getTandasPorTurno(turnoId),
                produccionDao.getMermasPorTurno(turnoId),
                ventaDao.getVentasPorTurno(turnoId),
                rutaRepartidorDao.getRutasPorTurno(turnoId)
            ) { tandas, mermas, ventas, rutas ->
                val producidoNeto = tandas.sumOf { it.kgTortillaEstimada }
                val mermaTotal = mermas.sumOf { it.kgMerma }
                val mostradorKg = calcularKgMostrador(ventas)
                val repartoKg = calcularKgReparto(rutas)
                val totalVendidoKg = mostradorKg + repartoKg
                val disponibleKg = (producidoNeto - totalVendidoKg - mermaTotal).coerceAtLeast(0.0)

                val mostradorTotal = ventas.filter { it.tipo == "MOSTRADOR" && it.estado == "ACTIVA" }.sumOf { it.total }
                val repartidorTotal = ventas.filter { it.tipo == "REPARTIDOR" && it.estado == "ACTIVA" }.sumOf { it.total }
                val ventasCount = ventas.count { it.tipo == "MOSTRADOR" }
                val nextFolioNumber = 143 + ventasCount
                val nextFolio = "M-%04d".format(nextFolioNumber)

                _uiState.update {
                    it.copy(
                        producidoNetoKg = producidoNeto,
                        disponibleEnTiendaKg = disponibleKg,
                        totalVentasDia = mostradorTotal + repartidorTotal,
                        totalVentasMostrador = mostradorTotal,
                        totalVentasRepartidores = repartidorTotal,
                        proximoTicketMostrador = nextFolio
                    )
                }
            }.collectLatest { }
        }

        // Observar rutas de repartidores del turno
        viewModelScope.launch {
            rutaRepartidorDao.getRutasPorTurno(turnoId).collectLatest { rutas ->
                if (rutas.isEmpty()) {
                    // Si no hay rutas en el turno, consultar si existen repartidores en el catálogo de Room
                    val repartidores = repartidorDao.getRepartidoresList()
                    if (repartidores.isNotEmpty()) {
                        val rutasCatalogadas = repartidores.mapIndexed { index, rep ->
                            val motoNombre = if (rep.moto.isNotBlank()) {
                                rep.moto
                            } else if (rep.detalleRuta.contains("•")) {
                                rep.detalleRuta.substringBefore("•").trim()
                            } else {
                                "Moto %02d".format(index + 1)
                            }

                            val rutaNombre = if (rep.detalleRuta.contains("•")) {
                                rep.detalleRuta.substringAfter("•").trim()
                            } else {
                                rep.detalleRuta.ifBlank { "Ruta General" }
                            }

                            RutaRepartidorEntity(
                                id = "${turnoId}_${rep.id}",
                                turnoId = turnoId,
                                moto = motoNombre,
                                repartidorNombre = rep.nombre,
                                nombreRuta = rutaNombre,
                                status = "PENDIENTE_SALIDA",
                                cargaInicialKg = 0.0,
                                pendienteCobro = 0.00
                            )
                        }
                        rutaRepartidorDao.insertRutas(rutasCatalogadas)
                    } else {
                        _uiState.update {
                            it.copy(
                                rutasRepartidores = emptyList(),
                                rutasActivasCount = 0
                            )
                        }
                    }
                } else {
                    val activas = rutas.count { it.status == "EN_RUTA" }
                    _uiState.update {
                        it.copy(
                            rutasRepartidores = rutas,
                            rutasActivasCount = activas
                        )
                    }
                }
            }
        }
    }

    // ── Steppers y Cantidades ───────────────────────────────────────────────
    fun onQuantityChanged(productId: Int, newQty: Int) {
        if (_uiState.value.isTurnoCerrado) return
        if (newQty < 0) return
        _uiState.update { state ->
            val updated = state.products.map { p ->
                if (p.id == productId) p.copy(quantity = newQty) else p
            }
            state.copy(products = updated)
        }
    }

    fun onScopeSelected(scope: String) {
        _uiState.update { it.copy(selectedScope = scope) }
    }

    // ── Diálogo e Inserción de Venta Mostrador ────────────────────────────────
    fun onOpenConfirmarVentaDialog() {
        if (_uiState.value.isTurnoCerrado) {
            viewModelScope.launch { _effect.send(VentaUiEffect.ShowToast("El turno está cerrado. No se pueden ingresar ventas.")) }
            return
        }
        if (_uiState.value.subtotalOrdenActual <= 0) return
        _uiState.update { it.copy(showConfirmarVentaDialog = true) }
    }

    fun onDismissConfirmarVentaDialog() {
        _uiState.update { it.copy(showConfirmarVentaDialog = false) }
    }

    fun onMetodoPagoSelected(metodo: String) {
        _uiState.update { it.copy(metodoPagoSeleccionado = metodo) }
    }

    fun onConfirmarVenta() {
        val state = _uiState.value
        val turnoId = state.turnoActivo?.id ?: return
        if (state.isTurnoCerrado || state.subtotalOrdenActual <= 0) return

        // Validación de Stock: no permitir vender más de lo disponible en tienda
        val kilosOrden = state.kgOrdenActual
        if (kilosOrden > state.disponibleEnTiendaKg) {
            viewModelScope.launch {
                _effect.send(
                    VentaUiEffect.ShowToast(
                        "Stock insuficiente: Solo hay %.1f kg disponibles en tienda (orden: %.1f kg)".format(
                            Locale.US,
                            state.disponibleEnTiendaKg,
                            kilosOrden
                        )
                    )
                )
            }
            return
        }

        val articulosVendidos = state.products
            .filter { it.quantity > 0 }
            .joinToString(", ") { "${it.quantity} ${it.name}" }

        val horaActual = LocalTime.now().format(DateTimeFormatter.ofPattern("hh:mm a", Locale("es", "MX")))
        val venta = VentaEntity(
            id = UUID.randomUUID().toString(),
            turnoId = turnoId,
            folioTicket = state.proximoTicketMostrador,
            tipo = "MOSTRADOR",
            fecha = System.currentTimeMillis(),
            hora = horaActual,
            detalleProductos = articulosVendidos,
            total = state.subtotalOrdenActual,
            metodoPago = "Efectivo",
            usuarioId = state.turnoActivo.usuarioId,
            usuarioNombre = state.usuarioActivoNombre.ifBlank { "admin1" }
        )

        viewModelScope.launch {
            ventaDao.insertVenta(venta)
            _uiState.update {
                it.copy(
                    showConfirmarVentaDialog = false,
                    ultimoMontoVenta = state.subtotalOrdenActual,
                    products = state.products.map { p -> p.copy(quantity = 0) }
                )
            }
            _effect.send(VentaUiEffect.ShowToast("Venta guardada ($%,.2f MXN)".format(venta.total)))
        }
    }

    fun onEliminarVenta(ventaId: String) {
        if (_uiState.value.isTurnoCerrado) {
            viewModelScope.launch {
                _effect.send(VentaUiEffect.ShowToast("No se puede eliminar ventas con turno cerrado."))
            }
            return
        }
        viewModelScope.launch {
            ventaDao.deleteVenta(ventaId)
            _effect.send(VentaUiEffect.ShowToast("Venta eliminada correctamente."))
        }
    }

    // ── Repartidores: Salida y Liquidación ────────────────────────────────────
    fun onOpenSalidaRepartidor(ruta: RutaRepartidorEntity) {
        _uiState.update { it.copy(repartidorSeleccionadoParaSalida = ruta) }
    }

    fun onDismissSalidaRepartidor() {
        _uiState.update { it.copy(repartidorSeleccionadoParaSalida = null) }
    }

    fun onConfirmarSalidaRepartidor(cargaKg: Double, paquetes: Int? = null) {
        val ruta = _uiState.value.repartidorSeleccionadoParaSalida ?: return
        val state = _uiState.value
        val pesoPaq = if (state.pesoPaqueteRepartidorKg > 0) state.pesoPaqueteRepartidorKg else 0.80

        val paquetesCalculados = paquetes ?: if (pesoPaq > 0) kotlin.math.round(cargaKg / pesoPaq).toInt() else 0
        val kilosEfectivos = if (paquetes != null) paquetes * pesoPaq else cargaKg

        if (kilosEfectivos > state.disponibleEnTiendaKg) {
            viewModelScope.launch {
                _effect.send(
                    VentaUiEffect.ShowToast(
                        String.format(
                            Locale.US,
                            "Stock insuficiente: Disponible %.1f kg, intentas asignar %.1f kg",
                            state.disponibleEnTiendaKg,
                            kilosEfectivos
                        )
                    )
                )
            }
            return
        }

        val horaActual = LocalTime.now().format(DateTimeFormatter.ofPattern("hh:mm a", Locale("es", "MX")))
        val pendienteCobro = paquetesCalculados * state.precioPaqueteRepartidor

        val actualizada = ruta.copy(
            status = "EN_RUTA",
            cargaInicialKg = kilosEfectivos,
            pendienteCobro = pendienteCobro,
            horaSalida = horaActual,
            paquetesCargados = paquetesCalculados
        )

        viewModelScope.launch {
            rutaRepartidorDao.updateRuta(actualizada)
            _uiState.update { it.copy(repartidorSeleccionadoParaSalida = null) }
            _effect.send(VentaUiEffect.ShowToast("${ruta.moto} salió a ruta con $paquetesCalculados paq (${String.format(Locale.US, "%.1f", kilosEfectivos)} kg)"))
        }
    }

    fun onOpenLiquidarRepartidor(ruta: RutaRepartidorEntity) {
        _uiState.update { it.copy(repartidorSeleccionadoParaLiquidar = ruta) }
    }

    fun onDismissLiquidarRepartidor() {
        _uiState.update { it.copy(repartidorSeleccionadoParaLiquidar = null) }
    }

    fun onConfirmarLiquidarRepartidor(devolucionKg: Double, cobrado: Double, paquetesDevueltos: Int? = null) {
        val ruta = _uiState.value.repartidorSeleccionadoParaLiquidar ?: return
        val turnoId = _uiState.value.turnoActivo?.id ?: return

        if (devolucionKg > ruta.cargaInicialKg) {
            viewModelScope.launch {
                _effect.send(
                    VentaUiEffect.ShowToast(
                        "Error: La devolución (%.1f kg) no puede superar la carga inicial (%.1f kg)"
                            .format(Locale.US, devolucionKg, ruta.cargaInicialKg)
                    )
                )
            }
            return
        }

        val entregado = (ruta.cargaInicialKg - devolucionKg).coerceAtLeast(0.0)
        val horaActual = LocalTime.now().format(DateTimeFormatter.ofPattern("hh:mm a", Locale("es", "MX")))
        val pesoPaq = if (_uiState.value.pesoPaqueteRepartidorKg > 0) _uiState.value.pesoPaqueteRepartidorKg else 0.80
        val paqDevueltos = paquetesDevueltos ?: if (pesoPaq > 0) kotlin.math.round(devolucionKg / pesoPaq).toInt() else 0

        viewModelScope.launch {
            val ultimoFolio = ventaDao.getUltimoFolioRepartidor()
            val nextNum = (ultimoFolio?.removePrefix("R-")?.toIntOrNull() ?: 38) + 1
            val folioTicket = "R-%04d".format(nextNum)

            val rutaLiquidada = ruta.copy(
                status = "LIQUIDADO",
                devolucionKg = devolucionKg,
                entregadoKg = entregado,
                cobrado = cobrado,
                pendienteCobro = 0.0,
                folioTicket = folioTicket,
                horaLiquidacion = horaActual,
                paquetesDevueltos = paqDevueltos
            )
            rutaRepartidorDao.updateRuta(rutaLiquidada)

            // Registrar venta del repartidor en Room con trazabilidad del usuario que cobra/liquida
            val state = _uiState.value
            val usuarioId = state.turnoActivo?.usuarioId ?: "1"
            val usuarioNombre = state.usuarioActivoNombre.ifBlank { "admin1" }

            val ventaRepartidor = VentaEntity(
                id = UUID.randomUUID().toString(),
                turnoId = turnoId,
                folioTicket = folioTicket,
                tipo = "REPARTIDOR",
                fecha = System.currentTimeMillis(),
                hora = horaActual,
                detalleProductos = "${ruta.moto} (${ruta.repartidorNombre}) - Liquidación $entregado kg",
                total = cobrado,
                metodoPago = "Efectivo",
                usuarioId = usuarioId,
                usuarioNombre = usuarioNombre,
                nombreRepartidor = ruta.repartidorNombre,
                motoAsignada = ruta.moto,
                nombreRuta = ruta.nombreRuta
            )
            ventaDao.insertVenta(ventaRepartidor)

            _uiState.update { it.copy(repartidorSeleccionadoParaLiquidar = null) }
            _effect.send(VentaUiEffect.ShowToast("${ruta.moto} liquidada con éxito (Ticket: $folioTicket)"))
        }
    }

    fun onCancelarSalidaRepartidor(ruta: RutaRepartidorEntity) {
        if (ruta.status != "EN_RUTA") return
        viewModelScope.launch {
            val revertida = ruta.copy(
                status = "PENDIENTE_SALIDA",
                cargaInicialKg = 0.0,
                pendienteCobro = 0.0,
                horaSalida = null,
                paquetesCargados = 0
            )
            rutaRepartidorDao.updateRuta(revertida)
            _effect.send(VentaUiEffect.ShowToast("Salida de ${ruta.moto} cancelada. Stock restituido a tienda."))
        }
    }

    private fun calcularKgMostrador(ventas: List<VentaEntity>): Double {
        var totalKg = 0.0
        val regex = java.util.regex.Pattern.compile("(\\d+)\\s*x\\s*([^,]+)")

        for (venta in ventas.filter { it.tipo == "MOSTRADOR" && it.estado == "ACTIVA" }) {
            var kgTicket = 0.0
            val matcher = regex.matcher(venta.detalleProductos)
            while (matcher.find()) {
                val qty = matcher.group(1)?.toIntOrNull() ?: 0
                val desc = matcher.group(2)?.trim()?.lowercase(Locale.ROOT) ?: ""
                when {
                    desc.contains("medio") -> kgTicket += qty * _uiState.value.pesoMedioPaqueteKg
                    desc.contains("mayoreo") -> kgTicket += qty * _uiState.value.pesoPaqueteRepartidorKg
                    desc.contains("paquete") -> kgTicket += qty * _uiState.value.pesoPaqueteMostradorKg
                    desc.contains("kilo") -> kgTicket += qty * 1.00
                    else -> kgTicket += qty * 1.00
                }
            }
            if (kgTicket == 0.0 && venta.total > 0.0) {
                val precioKiloActual = _uiState.value.products.firstOrNull { it.id == 1 }?.price ?: 24.00
                kgTicket = if (precioKiloActual > 0) venta.total / precioKiloActual else venta.total / 24.00
            }
            totalKg += kgTicket
        }
        return totalKg
    }

    private fun calcularKgReparto(rutas: List<RutaRepartidorEntity>): Double {
        return rutas.sumOf { ruta ->
            when (ruta.status) {
                "LIQUIDADO" -> ruta.entregadoKg
                "EN_RUTA" -> ruta.cargaInicialKg
                else -> 0.0
            }
        }
    }

    // ── Cerrar Turno ────────────────────────────────────────────────────────
    fun onOpenCerrarTurnoDialog() {
        _uiState.update { it.copy(showCerrarTurnoDialog = true) }
    }

    fun onDismissCerrarTurnoDialog() {
        _uiState.update { it.copy(showCerrarTurnoDialog = false) }
    }

    fun onConfirmarCerrarTurno() {
        if (_uiState.value.rutasActivasCount > 0) {
            viewModelScope.launch {
                _effect.send(VentaUiEffect.ShowToast("No es posible cerrar turno: hay rutas activas sin liquidar."))
            }
            return
        }
        viewModelScope.launch {
            val totalAcumulado = _uiState.value.totalVentasDia
            val ventasMostrador = _uiState.value.totalVentasMostrador
            val ventasRepartidores = _uiState.value.totalVentasRepartidores
            val fechaCierre = System.currentTimeMillis()
            val turno = _uiState.value.turnoActivo
            val turnoId = turno?.id
            val fondo = turno?.fondoInicial ?: 0.0
            val efectivoEsperado = fondo + totalAcumulado

            if (turnoId != null) {
                turnoDao.cerrarTurno(
                    turnoId = turnoId,
                    fechaCierre = fechaCierre,
                    totalVentas = totalAcumulado
                )
                turnoDao.cerrarTurnoConArqueo(
                    turnoId = turnoId,
                    fechaCierre = fechaCierre,
                    usuarioCierreId = _uiState.value.usuarioActivoNombre,
                    totalVentasMostrador = ventasMostrador,
                    totalCobradoReparto = ventasRepartidores,
                    totalVentas = totalAcumulado,
                    efectivoEsperado = efectivoEsperado,
                    efectivoContado = efectivoEsperado,
                    diferenciaArqueo = 0.0,
                    notasCierre = "Corte cerrado correctamente."
                )
            }
            // Garantizar que no quede absolutamente ningún turno en estado ABIERTO en Room
            turnoDao.cerrarTodosLosTurnosActivos(fechaCierre)

            _uiState.update {
                it.copy(
                    turnoActivo = null,
                    isTurnoCerrado = true,
                    showCerrarTurnoDialog = false
                )
            }
            _effect.send(VentaUiEffect.NavigateToLogin)
        }
    }

    private fun formatearFecha(timestamp: Long): String {
        return try {
            val localDate = Instant.ofEpochMilli(timestamp).atZone(ZoneId.systemDefault()).toLocalDate()
            val dayName = localDate.dayOfWeek.getDisplayName(TextStyle.SHORT, Locale("es", "MX"))
                .replaceFirstChar { it.uppercase() }.take(3)
            val monthName = localDate.month.getDisplayName(TextStyle.SHORT, Locale("es", "MX"))
                .replaceFirstChar { it.uppercase() }.take(4)
            "Hoy, $dayName ${localDate.dayOfMonth} $monthName"
        } catch (e: Exception) {
            "Hoy, Lun 7 Sept"
        }
    }
}
