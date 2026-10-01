package com.example.tortilleriaderek.presentation.historial

import app.cash.turbine.test
import com.example.tortilleriaderek.data.local.dao.TurnoDao
import com.example.tortilleriaderek.data.local.dao.VentaDao
import com.example.tortilleriaderek.data.local.entity.TurnoEntity
import com.example.tortilleriaderek.data.local.entity.VentaEntity
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.every
import io.mockk.mockk
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.test.StandardTestDispatcher
import kotlinx.coroutines.test.advanceUntilIdle
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.test.setMain
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test

@OptIn(ExperimentalCoroutinesApi::class)
class HistorialViewModelTest {

    private val testDispatcher = StandardTestDispatcher()

    private lateinit var turnoDao: TurnoDao
    private lateinit var ventaDao: VentaDao

    private val turnoActivoFlow = MutableStateFlow<TurnoEntity?>(null)
    private val ultimoTurnoFlow = MutableStateFlow<TurnoEntity?>(null)
    private val ventasPorTurnoFlow = MutableStateFlow<List<VentaEntity>>(emptyList())

    @Before
    fun setUp() {
        Dispatchers.setMain(testDispatcher)

        turnoDao = mockk(relaxed = true)
        ventaDao = mockk(relaxed = true)

        every { turnoDao.getTurnoActivo() } returns turnoActivoFlow
        every { turnoDao.getUltimoTurno() } returns ultimoTurnoFlow
        every { ventaDao.getVentasPorTurno(any()) } returns ventasPorTurnoFlow
    }

    @After
    fun tearDown() {
        Dispatchers.resetMain()
    }

    private fun createViewModel(): HistorialViewModel {
        return HistorialViewModel(
            turnoDao = turnoDao,
            ventaDao = ventaDao
        )
    }

    private fun dummyTurno(id: String = "turno_1", estado: String = "ABIERTO"): TurnoEntity {
        return TurnoEntity(
            id = id,
            fechaApertura = System.currentTimeMillis(),
            estado = estado,
            usuarioId = "user_1"
        )
    }

    private fun dummyVenta(
        id: String,
        tipo: String,
        monto: Double,
        folio: String = "FOLIO-$id",
        hora: String = "10:00 AM",
        estado: String = "ACTIVA"
    ): VentaEntity {
        return VentaEntity(
            id = id,
            turnoId = "turno_1",
            folioTicket = folio,
            tipo = tipo,
            fecha = System.currentTimeMillis(),
            hora = hora,
            detalleProductos = "1 Kilo",
            total = monto,
            metodoPago = "Efectivo",
            usuarioId = "user_1",
            usuarioNombre = "admin1",
            estado = estado
        )
    }

    @Test
    fun cuandoNoHayTurno_estadoPermaneceInicial() = runTest {
        val viewModel = createViewModel()
        testDispatcher.scheduler.advanceUntilIdle()

        val state = viewModel.uiState.value
        assertNull(state.turnoId)
        assertTrue(state.todasLasTransacciones.isEmpty())
        assertTrue(state.transaccionesFiltradas.isEmpty())
    }

    @Test
    fun cuandoHayTurnoConVentas_calculaTotalesCorrectamente() = runTest {
        turnoActivoFlow.value = dummyTurno()
        ventasPorTurnoFlow.value = listOf(
            dummyVenta("1", "MOSTRADOR", 50.0),
            dummyVenta("2", "MOSTRADOR", 30.0),
            dummyVenta("3", "REPARTIDOR", 200.0)
        )

        val viewModel = createViewModel()
        testDispatcher.scheduler.advanceUntilIdle()

        val state = viewModel.uiState.value
        assertEquals("turno_1", state.turnoId)
        assertFalse(state.isTurnoCerrado)
        assertEquals(280.0, state.totalVentasTurno, 0.001)
        assertEquals(80.0, state.totalVentasMostrador, 0.001)
        assertEquals(200.0, state.totalVentasRepartidores, 0.001)
        assertEquals(3, state.todasLasTransacciones.size)
    }

    @Test
    fun reglaDeNegocio_cadaLiquidacionRepartidorCuentaComoUnaVenta() = runTest {
        turnoActivoFlow.value = dummyTurno()
        ventasPorTurnoFlow.value = listOf(
            dummyVenta("1", "MOSTRADOR", 50.0),
            dummyVenta("2", "REPARTIDOR", 100.0),
            dummyVenta("3", "REPARTIDOR", 150.0)
        )

        val viewModel = createViewModel()
        testDispatcher.scheduler.advanceUntilIdle()

        val state = viewModel.uiState.value
        assertEquals(1, state.numeroVentasMostrador)
        assertEquals(2, state.numeroVentasRepartidor)
        assertEquals(3, state.numeroVentasTotal)
        assertEquals(3, state.transaccionesTotalCount)
    }

    @Test
    fun filtroMostrador_soloMuestraVentasMostrador() = runTest {
        turnoActivoFlow.value = dummyTurno()
        ventasPorTurnoFlow.value = listOf(
            dummyVenta("1", "MOSTRADOR", 50.0),
            dummyVenta("2", "REPARTIDOR", 100.0)
        )

        val viewModel = createViewModel()
        testDispatcher.scheduler.advanceUntilIdle()

        viewModel.onEvent(HistorialUiEvent.SelectFiltro(HistorialFiltroTipo.MOSTRADOR))
        testDispatcher.scheduler.advanceUntilIdle()

        val state = viewModel.uiState.value
        assertEquals(HistorialFiltroTipo.MOSTRADOR, state.filtroSeleccionado)
        assertEquals(1, state.transaccionesFiltradas.size)
        assertEquals("MOSTRADOR", state.transaccionesFiltradas[0].tipo)
        assertEquals(50.0, state.totalMostradoEnHeader, 0.001)
        assertEquals(1, state.numeroVentasMostradoEnHeader)
    }

    @Test
    fun filtroRepartidor_soloMuestraVentasRepartidor() = runTest {
        turnoActivoFlow.value = dummyTurno()
        ventasPorTurnoFlow.value = listOf(
            dummyVenta("1", "MOSTRADOR", 50.0),
            dummyVenta("2", "REPARTIDOR", 100.0)
        )

        val viewModel = createViewModel()
        testDispatcher.scheduler.advanceUntilIdle()

        viewModel.onEvent(HistorialUiEvent.SelectFiltro(HistorialFiltroTipo.REPARTIDOR))
        testDispatcher.scheduler.advanceUntilIdle()

        val state = viewModel.uiState.value
        assertEquals(HistorialFiltroTipo.REPARTIDOR, state.filtroSeleccionado)
        assertEquals(1, state.transaccionesFiltradas.size)
        assertEquals("REPARTIDOR", state.transaccionesFiltradas[0].tipo)
        assertEquals(100.0, state.totalMostradoEnHeader, 0.001)
        assertEquals(1, state.numeroVentasMostradoEnHeader)
    }

    @Test
    fun setFiltroInicial_mostradorOrdenaChipsCorrectamente() = runTest {
        val viewModel = createViewModel()
        viewModel.onEvent(HistorialUiEvent.SetFiltroInicial(HistorialFiltroTipo.MOSTRADOR))
        testDispatcher.scheduler.advanceUntilIdle()

        val state = viewModel.uiState.value
        assertEquals(HistorialFiltroTipo.MOSTRADOR, state.filtroSeleccionado)
        assertEquals(
            listOf(HistorialFiltroTipo.MOSTRADOR, HistorialFiltroTipo.TODAS, HistorialFiltroTipo.REPARTIDOR),
            state.chipsOrden
        )
    }

    @Test
    fun setFiltroInicial_repartidorOrdenaChipsCorrectamente() = runTest {
        val viewModel = createViewModel()
        viewModel.onEvent(HistorialUiEvent.SetFiltroInicial(HistorialFiltroTipo.REPARTIDOR))
        testDispatcher.scheduler.advanceUntilIdle()

        val state = viewModel.uiState.value
        assertEquals(HistorialFiltroTipo.REPARTIDOR, state.filtroSeleccionado)
        assertEquals(
            listOf(HistorialFiltroTipo.REPARTIDOR, HistorialFiltroTipo.TODAS, HistorialFiltroTipo.MOSTRADOR),
            state.chipsOrden
        )
    }

    @Test
    fun solicitarYConfirmarEliminarVenta_eliminaEnDaoCuandoTurnoAbierto() = runTest {
        turnoActivoFlow.value = dummyTurno(estado = "ABIERTO")
        val venta = dummyVenta("venta_10", "MOSTRADOR", 40.0, folio = "M-0010")
        ventasPorTurnoFlow.value = listOf(venta)

        val viewModel = createViewModel()
        testDispatcher.scheduler.advanceUntilIdle()

        val itemUi = viewModel.uiState.value.todasLasTransacciones.first()
        viewModel.onEvent(HistorialUiEvent.SolicitarEliminarVenta(itemUi))
        assertEquals(itemUi, viewModel.uiState.value.transaccionAEliminar)

        viewModel.onEvent(HistorialUiEvent.ConfirmarEliminarVenta)
        testDispatcher.scheduler.advanceUntilIdle()

        coVerify { ventaDao.deleteVenta("venta_10") }
        assertNull(viewModel.uiState.value.transaccionAEliminar)
    }

    @Test
    fun confirmarEliminarVenta_noEliminaSiTurnoEstaCerrado() = runTest {
        ultimoTurnoFlow.value = dummyTurno(estado = "CERRADO")
        val venta = dummyVenta("venta_20", "MOSTRADOR", 60.0)
        ventasPorTurnoFlow.value = listOf(venta)

        val viewModel = createViewModel()
        testDispatcher.scheduler.advanceUntilIdle()

        assertTrue(viewModel.uiState.value.isTurnoCerrado)
        val itemUi = viewModel.uiState.value.todasLasTransacciones.first()

        viewModel.effect.test {
            viewModel.onEvent(HistorialUiEvent.SolicitarEliminarVenta(itemUi))
            viewModel.onEvent(HistorialUiEvent.ConfirmarEliminarVenta)
            testDispatcher.scheduler.advanceUntilIdle()

            val effect = awaitItem()
            assertTrue(effect is HistorialUiEffect.ShowToast)
            assertTrue((effect as HistorialUiEffect.ShowToast).mensaje.contains("turno cerrado"))
        }

        coVerify(exactly = 0) { ventaDao.deleteVenta("venta_20") }
    }

    @Test
    fun fallbackCamposVacios_noRompeUiItem() = runTest {
        turnoActivoFlow.value = dummyTurno()
        ventasPorTurnoFlow.value = listOf(
            dummyVenta("1", "MOSTRADOR", 50.0, folio = "", hora = "")
        )

        val viewModel = createViewModel()
        testDispatcher.scheduler.advanceUntilIdle()

        val item = viewModel.uiState.value.todasLasTransacciones.first()
        assertEquals("—", item.ticketFolio)
        assertEquals("—", item.hora)
    }
}
