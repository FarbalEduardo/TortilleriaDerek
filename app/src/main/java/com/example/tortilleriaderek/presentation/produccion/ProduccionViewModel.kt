package com.example.tortilleriaderek.presentation.produccion

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.tortilleriaderek.data.local.dao.ConfiguracionProduccionDao
import com.example.tortilleriaderek.data.local.dao.ProduccionDao
import com.example.tortilleriaderek.data.local.dao.RutaRepartidorDao
import com.example.tortilleriaderek.data.local.dao.TurnoDao
import com.example.tortilleriaderek.data.local.dao.UsuarioDao
import com.example.tortilleriaderek.data.local.dao.VentaDao
import com.example.tortilleriaderek.data.local.entity.ConfiguracionProduccionEntity
import com.example.tortilleriaderek.data.local.entity.MermaProduccionEntity
import com.example.tortilleriaderek.data.local.entity.RutaRepartidorEntity
import com.example.tortilleriaderek.data.local.entity.TandaProduccionEntity
import com.example.tortilleriaderek.data.local.entity.VentaEntity
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
import java.util.regex.Pattern
import javax.inject.Inject

@HiltViewModel
class ProduccionViewModel @Inject constructor(
    private val turnoDao: TurnoDao,
    private val produccionDao: ProduccionDao,
    private val configuracionProduccionDao: ConfiguracionProduccionDao,
    private val ventaDao: VentaDao,
    private val rutaRepartidorDao: RutaRepartidorDao,
    private val usuarioDao: UsuarioDao
) : ViewModel() {

    private val _uiState = MutableStateFlow(ProduccionUiState())
    val uiState: StateFlow<ProduccionUiState> = _uiState.asStateFlow()

    private val _effect = Channel<ProduccionUiEffect>()
    val effect = _effect.receiveAsFlow()

    init {
        observarConfiguracionReceta()
        observarTurnoActivo()
    }

    private fun observarConfiguracionReceta() {
        viewModelScope.launch {
            configuracionProduccionDao.getConfiguracion().collectLatest { config ->
                if (config != null) {
                    _uiState.update { current ->
                        val masa = current.bultosHarinaInput * config.kgMasaPorBulto
                        val tortilla = current.bultosHarinaInput * config.rendimientoTortillaPorBulto
                        current.copy(
                            pesoBultoHarinaKg = config.pesoBultoHarinaKg,
                            kgMasaPorBulto = config.kgMasaPorBulto,
                            rendimientoPorBulto = config.rendimientoTortillaPorBulto,
                            mermaToleradaPorBulto = config.mermaToleradaKgPorBulto,
                            pesoPaqueteKg = config.pesoPaqueteKg,
                            pesoMedioPaqueteKg = config.pesoMedioPaqueteKg,
                            masaCrudaCalculada = masa,
                            tortillaCalculada = tortilla
                        )
                    }
                } else {
                    // Sembrar configuración estándar inicial en ceros si la base de datos está limpia
                    val defaultConf = ConfiguracionProduccionEntity(id = "DEFAULT")
                    configuracionProduccionDao.insertOrUpdateConfiguracion(defaultConf)
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

                    _uiState.update { current ->
                        current.copy(
                            turnoActivo = turno,
                            usuarioActivoNombre = nombreUsuario,
                            fechaTurnoTexto = fechaTexto,
                            isTurnoCerrado = false,
                            bultosHarinaInput = 0,
                            masaCrudaCalculada = 0.0,
                            tortillaCalculada = 0.0
                        )
                    }
                    observarProduccionYVentasDelTurno(turno.id)
                } else {
                    // Sin turno activo o cerrado: stock limpio en 0.0
                    _uiState.update { current ->
                        current.copy(
                            turnoActivo = null,
                            isTurnoCerrado = true,
                            bultosHarinaInput = 0,
                            masaCrudaCalculada = 0.0,
                            tortillaCalculada = 0.0,
                            producidoNeto = 0.0,
                            totalVendidoKg = 0.0,
                            totalVendidoMostradorKg = 0.0,
                            totalVendidoRepartoKg = 0.0,
                            mermaTurnoKg = 0.0,
                            disponibleEnTienda = 0.0,
                            porcentajeVenta = 0.0
                        )
                    }
                }
            }
        }
    }

    private fun observarProduccionYVentasDelTurno(turnoId: String) {
        viewModelScope.launch {
            combine(
                produccionDao.getTandasPorTurno(turnoId),
                produccionDao.getMermasPorTurno(turnoId),
                ventaDao.getVentasPorTurno(turnoId),
                rutaRepartidorDao.getRutasPorTurno(turnoId)
            ) { tandas, mermas, ventas, rutas ->
                CuadreTurno(tandas, mermas, ventas, rutas)
            }.collectLatest { cuadre ->
                val producidoNeto = cuadre.tandas.sumOf { it.kgTortillaEstimada }
                val mermaTotal = cuadre.mermas.sumOf { it.kgMerma }

                val mostradorKg = calcularKgMostrador(cuadre.ventas)
                val repartoKg = calcularKgReparto(cuadre.rutas)
                val totalVendido = mostradorKg + repartoKg

                val disponible = (producidoNeto - totalVendido - mermaTotal).coerceAtLeast(0.0)
                val porcentaje = if (producidoNeto > 0.0) {
                    ((totalVendido / producidoNeto) * 100.0).coerceIn(0.0, 100.0)
                } else 0.0

                _uiState.update { current ->
                    current.copy(
                        producidoNeto = producidoNeto,
                        mermaTurnoKg = mermaTotal,
                        totalVendidoMostradorKg = mostradorKg,
                        totalVendidoRepartoKg = repartoKg,
                        totalVendidoKg = totalVendido,
                        disponibleEnTienda = disponible,
                        porcentajeVenta = porcentaje
                    )
                }
            }
        }
    }

    fun onEvent(event: ProduccionUiEvent) {
        when (event) {
            ProduccionUiEvent.OnIncrementarBultos -> {
                actualizarBultosInput(_uiState.value.bultosHarinaInput + 1)
            }
            ProduccionUiEvent.OnDecrementarBultos -> {
                if (_uiState.value.bultosHarinaInput > 0) {
                    actualizarBultosInput(_uiState.value.bultosHarinaInput - 1)
                }
            }
            is ProduccionUiEvent.OnBultosChanged -> {
                if (event.bultos >= 0) {
                    actualizarBultosInput(event.bultos)
                }
            }
            ProduccionUiEvent.OnRegistrarTandaClick -> {
                registrarTanda()
            }
            ProduccionUiEvent.OnOpenRegistrarMermaDialog -> {
                _uiState.update { it.copy(showRegistrarMermaDialog = true, mermaKgInput = "", errorMessage = null) }
            }
            ProduccionUiEvent.OnDismissRegistrarMermaDialog -> {
                _uiState.update { it.copy(showRegistrarMermaDialog = false, mermaKgInput = "", errorMessage = null) }
            }
            is ProduccionUiEvent.OnMermaKgInputChanged -> {
                val parsed = event.valor.replace(',', '.').toDoubleOrNull()
                val error = when {
                    parsed != null && parsed > _uiState.value.disponibleEnTienda -> {
                        "La merma (%.1f kg) supera la tortilla disponible (%.1f kg)"
                            .format(Locale.US, parsed, _uiState.value.disponibleEnTienda)
                    }
                    parsed != null && parsed <= 0.0 -> {
                        "Ingrese un valor de merma mayor a 0.0 kg"
                    }
                    else -> null
                }
                _uiState.update { it.copy(mermaKgInput = event.valor, errorMessage = error) }
            }
            is ProduccionUiEvent.OnMotivoMermaInputChanged -> {
                _uiState.update { it.copy(motivoMermaInput = event.motivo) }
            }
            ProduccionUiEvent.OnConfirmarRegistroMerma -> {
                registrarMerma()
            }
            ProduccionUiEvent.OnVerHistorialClick -> {
                viewModelScope.launch {
                    _effect.send(ProduccionUiEffect.NavigateToHistorial)
                }
            }
        }
    }

    private fun actualizarBultosInput(bultos: Int) {
        _uiState.update { current ->
            val masa = bultos * current.kgMasaPorBulto
            val tortilla = bultos * current.rendimientoPorBulto
            current.copy(
                bultosHarinaInput = bultos,
                masaCrudaCalculada = masa,
                tortillaCalculada = tortilla
            )
        }
    }

    private fun registrarTanda() {
        val state = _uiState.value
        val turno = state.turnoActivo
        if (turno == null || state.isTurnoCerrado) {
            viewModelScope.launch {
                _effect.send(ProduccionUiEffect.ShowError("No es posible registrar producción: no hay turno abierto."))
            }
            return
        }

        if (state.bultosHarinaInput <= 0) {
            viewModelScope.launch {
                _effect.send(ProduccionUiEffect.ShowError("La cantidad de bultos debe ser mayor a 0."))
            }
            return
        }

        val horaActual = LocalTime.now().format(DateTimeFormatter.ofPattern("hh:mm a", Locale("es", "MX")))
        val tanda = TandaProduccionEntity(
            id = UUID.randomUUID().toString(),
            turnoId = turno.id,
            fecha = System.currentTimeMillis(),
            hora = horaActual,
            bultosHarina = state.bultosHarinaInput,
            pesoBultoKg = state.pesoBultoHarinaKg,
            kgMasaCruda = state.masaCrudaCalculada,
            kgTortillaEstimada = state.tortillaCalculada,
            usuarioId = turno.usuarioId,
            usuarioNombre = state.usuarioActivoNombre.ifBlank { "admin1" }
        )

        viewModelScope.launch {
            produccionDao.insertTanda(tanda)
            // Reiniciar bultos a 0 tras registrar la tanda
            _uiState.update {
                it.copy(
                    bultosHarinaInput = 0,
                    masaCrudaCalculada = 0.0,
                    tortillaCalculada = 0.0
                )
            }
            _effect.send(
                ProduccionUiEffect.ShowToast(
                    "Tanda de ${state.bultosHarinaInput} bultos (%.1f kg) registrada con éxito".format(state.tortillaCalculada)
                )
            )
        }
    }

    private fun registrarMerma() {
        val state = _uiState.value
        val turno = state.turnoActivo
        if (turno == null || state.isTurnoCerrado) {
            viewModelScope.launch {
                _effect.send(ProduccionUiEffect.ShowError("No es posible registrar merma: el turno está cerrado."))
            }
            return
        }

        val kgMerma = state.mermaKgInput.replace(',', '.').toDoubleOrNull()
        if (kgMerma == null || kgMerma <= 0.0) {
            _uiState.update { it.copy(errorMessage = "Ingrese un valor de merma válido mayor a 0.0 kg") }
            return
        }

        if (kgMerma > state.disponibleEnTienda) {
            val errorMsg = "La merma (%.1f kg) no puede superar la tortilla disponible en tienda (%.1f kg)"
                .format(Locale.US, kgMerma, state.disponibleEnTienda)
            _uiState.update { it.copy(errorMessage = errorMsg) }
            return
        }

        val horaActual = LocalTime.now().format(DateTimeFormatter.ofPattern("hh:mm a", Locale("es", "MX")))
        val mermaEntity = MermaProduccionEntity(
            id = UUID.randomUUID().toString(),
            turnoId = turno.id,
            fecha = System.currentTimeMillis(),
            hora = horaActual,
            kgMerma = kgMerma,
            motivo = state.motivoMermaInput.ifBlank { "Tortilla fría / rotura" },
            usuarioId = turno.usuarioId,
            usuarioNombre = state.usuarioActivoNombre.ifBlank { "admin1" }
        )

        viewModelScope.launch {
            produccionDao.insertMerma(mermaEntity)
            _uiState.update {
                it.copy(
                    showRegistrarMermaDialog = false,
                    mermaKgInput = "",
                    errorMessage = null
                )
            }
            _effect.send(
                ProduccionUiEffect.ShowToast(
                    "Merma de %.1f kg registrada y descontada del disponible".format(kgMerma)
                )
            )
        }
    }

    private fun calcularKgMostrador(ventas: List<VentaEntity>): Double {
        var totalKg = 0.0
        val regex = Pattern.compile("(\\d+)\\s*(?:x\\s*)?([^,]+)")

        val pesoMedio = if (_uiState.value.pesoMedioPaqueteKg > 0) _uiState.value.pesoMedioPaqueteKg else 0.40
        val pesoPaq = if (_uiState.value.pesoPaqueteKg > 0) _uiState.value.pesoPaqueteKg else 0.80

        for (venta in ventas.filter { it.tipo == "MOSTRADOR" && it.estado == "ACTIVA" }) {
            var kgTicket = 0.0
            val matcher = regex.matcher(venta.detalleProductos)
            while (matcher.find()) {
                val qty = matcher.group(1)?.toIntOrNull() ?: 0
                val desc = matcher.group(2)?.trim()?.lowercase(Locale.ROOT) ?: ""
                when {
                    desc.contains("medio") -> kgTicket += qty * pesoMedio
                    desc.contains("mayoreo") -> kgTicket += qty * pesoPaq
                    desc.contains("paquete") -> kgTicket += qty * pesoPaq
                    desc.contains("kilo") -> kgTicket += qty * 1.00
                    else -> kgTicket += qty * 1.00
                }
            }
            // Si el ticket no tuvo items parseables pero tiene total monetario, se aproxima a base $24.00/kg
            if (kgTicket == 0.0 && venta.total > 0.0) {
                kgTicket = venta.total / 24.00
            }
            totalKg += kgTicket
        }
        return totalKg
    }

    private fun calcularKgReparto(rutas: List<RutaRepartidorEntity>): Double {
        // En reparto: las rutas liquidadas descontaron lo entregado; las que están en ruta tienen la carga en calle
        return rutas.sumOf { ruta ->
            when (ruta.status) {
                "LIQUIDADO" -> ruta.entregadoKg
                "EN_RUTA" -> ruta.cargaInicialKg
                else -> 0.0
            }
        }
    }

    private fun formatearFecha(timestamp: Long): String {
        return try {
            val instant = Instant.ofEpochMilli(timestamp)
            val zonedDateTime = instant.atZone(ZoneId.of("America/Mexico_City"))
            val diaSemana = zonedDateTime.dayOfWeek.getDisplayName(TextStyle.SHORT, Locale("es", "MX"))
                .replaceFirstChar { it.uppercase() }
            val diaMes = zonedDateTime.dayOfMonth
            val mes = zonedDateTime.month.getDisplayName(TextStyle.SHORT, Locale("es", "MX"))
                .replaceFirstChar { it.uppercase() }
            "Hoy, $diaSemana $diaMes $mes"
        } catch (e: Exception) {
            "Hoy"
        }
    }

    private data class CuadreTurno(
        val tandas: List<TandaProduccionEntity>,
        val mermas: List<MermaProduccionEntity>,
        val ventas: List<VentaEntity>,
        val rutas: List<RutaRepartidorEntity>
    )
}
