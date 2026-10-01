package com.example.tortilleriaderek.presentation.metricas

import app.cash.turbine.test
import com.example.tortilleriaderek.data.local.dao.ProduccionDao
import com.example.tortilleriaderek.data.local.dao.RutaRepartidorDao
import com.example.tortilleriaderek.data.local.dao.TurnoDao
import com.example.tortilleriaderek.data.local.dao.VentaDao
import com.example.tortilleriaderek.data.local.entity.MermaProduccionEntity
import com.example.tortilleriaderek.data.local.entity.RutaRepartidorEntity
import com.example.tortilleriaderek.data.local.entity.TandaProduccionEntity
import com.example.tortilleriaderek.data.local.entity.TurnoEntity
import com.example.tortilleriaderek.data.local.entity.VentaEntity
import io.mockk.every
import io.mockk.mockk
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.test.StandardTestDispatcher
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.test.setMain
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test
import java.time.LocalDate
import java.time.ZoneId

@OptIn(ExperimentalCoroutinesApi::class)
class MetricasViewModelTest {

    private val testDispatcher = StandardTestDispatcher()

    private lateinit var turnoDao: TurnoDao
    private lateinit var ventaDao: VentaDao
    private lateinit var produccionDao: ProduccionDao
    private lateinit var rutaRepartidorDao: RutaRepartidorDao

    private val ventasFlow = MutableStateFlow<List<VentaEntity>>(emptyList())
    private val tandasFlow = MutableStateFlow<List<TandaProduccionEntity>>(emptyList())
    private val mermasFlow = MutableStateFlow<List<MermaProduccionEntity>>(emptyList())
    private val rutasFlow = MutableStateFlow<List<RutaRepartidorEntity>>(emptyList())
    private val turnosFlow = MutableStateFlow<List<TurnoEntity>>(emptyList())

    private lateinit var viewModel: MetricasViewModel

    @Before
    fun setUp() {
        Dispatchers.setMain(testDispatcher)

        turnoDao = mockk(relaxed = true)
        ventaDao = mockk(relaxed = true)
        produccionDao = mockk(relaxed = true)
        rutaRepartidorDao = mockk(relaxed = true)

        every { ventaDao.getTodasVentasTurnosCerrados() } returns ventasFlow
        every { produccionDao.getTodasTandasTurnosCerrados() } returns tandasFlow
        every { produccionDao.getTodasMermasTurnosCerrados() } returns mermasFlow
        every { rutaRepartidorDao.getRutasTurnosCerrados() } returns rutasFlow
        every { turnoDao.getTodosLosTurnosCerrados() } returns turnosFlow

        viewModel = MetricasViewModel(turnoDao, ventaDao, produccionDao, rutaRepartidorDao)
    }

    @After
    fun tearDown() {
        Dispatchers.resetMain()
    }

    @Test
    fun `US2 - Solo turnos cerrados son considerados en los calculos analiticos`() = runTest {
        val now = System.currentTimeMillis()

        // Simulamos ventas que vienen estrictamente de la consulta de turnos cerrados en Room
        val ventaTurnoCerrado1 = VentaEntity(
            id = "v1",
            turnoId = "turno_cerrado_1",
            folioTicket = "M-0001",
            tipo = "MOSTRADOR",
            fecha = now,
            hora = "10:00 AM",
            detalleProductos = "2 x Kilo",
            total = 1000.0,
            metodoPago = "Efectivo",
            usuarioId = "u1",
            usuarioNombre = "admin",
            estado = "ACTIVA"
        )
        val ventaTurnoCerrado2 = VentaEntity(
            id = "v2",
            turnoId = "turno_cerrado_2",
            folioTicket = "M-0002",
            tipo = "MOSTRADOR",
            fecha = now,
            hora = "04:00 PM",
            detalleProductos = "1 x Kilo",
            total = 1500.0,
            metodoPago = "Efectivo",
            usuarioId = "u1",
            usuarioNombre = "admin",
            estado = "ACTIVA"
        )

        ventasFlow.value = listOf(ventaTurnoCerrado1, ventaTurnoCerrado2)
        viewModel.onEvent(MetricasUiEvent.SelectMetrica(MetricaTipo.VENTA_TOTAL))
        testDispatcher.scheduler.advanceUntilIdle()

        viewModel.uiState.test {
            val state = awaitItem()
            assertEquals(2500.0, state.totalPeriodoActual, 0.01)
            assertEquals("MXN", state.unidadMetrica)
        }
    }

    @Test
    fun `US3 - Multiples turnos cerrados en un mismo dia se consolidan en un unico valor diario`() = runTest {
        val zone = ZoneId.systemDefault()
        val hoy = LocalDate.now()
        val hoyMillis = hoy.atStartOfDay(zone).toInstant().toEpochMilli() + 3600000 // 01:00 AM hoy

        val tandaTurno1 = TandaProduccionEntity(
            id = "t1",
            turnoId = "turno_matutino",
            fecha = hoyMillis,
            hora = "08:00 AM",
            bultosHarina = 2,
            pesoBultoKg = 20.0,
            kgMasaCruda = 80.0,
            kgTortillaEstimada = 76.0,
            usuarioId = "u1",
            usuarioNombre = "operador"
        )

        val tandaTurno2 = TandaProduccionEntity(
            id = "t2",
            turnoId = "turno_vespertino",
            fecha = hoyMillis + 28800000, // 8 horas después, mismo día
            hora = "04:00 PM",
            bultosHarina = 3,
            pesoBultoKg = 20.0,
            kgMasaCruda = 120.0,
            kgTortillaEstimada = 114.0,
            usuarioId = "u1",
            usuarioNombre = "operador"
        )

        tandasFlow.value = listOf(tandaTurno1, tandaTurno2)
        viewModel.onEvent(MetricasUiEvent.SelectMetrica(MetricaTipo.PRODUCCION_TOTAL))
        testDispatcher.scheduler.advanceUntilIdle()

        viewModel.uiState.test {
            val state = awaitItem()
            assertEquals(190.0, state.totalPeriodoActual, 0.01)
            // Punto de hoy en la gráfica
            val puntoHoy = state.puntosGrafica.firstOrNull { it.fecha == hoy }
            assertNotNull(puntoHoy)
            assertEquals(190.0, puntoHoy!!.valor, 0.01)
        }
    }

    @Test
    fun `US1 - Selector de 4 metricas con scroll horizontal conmuta correctamente`() = runTest {
        val now = System.currentTimeMillis()

        val tanda = TandaProduccionEntity(
            id = "t1",
            turnoId = "turno_1",
            fecha = now,
            hora = "09:00 AM",
            bultosHarina = 1,
            pesoBultoKg = 20.0,
            kgMasaCruda = 40.0,
            kgTortillaEstimada = 38.0,
            usuarioId = "u1",
            usuarioNombre = "admin"
        )
        val ventaMostrador = VentaEntity(
            id = "v1",
            turnoId = "turno_1",
            folioTicket = "M-0001",
            tipo = "MOSTRADOR",
            fecha = now,
            hora = "10:00 AM",
            detalleProductos = "1 x Kilo",
            total = 500.0,
            metodoPago = "Efectivo",
            usuarioId = "u1",
            usuarioNombre = "admin",
            estado = "ACTIVA"
        )
        val ventaRepartidor = VentaEntity(
            id = "v2",
            turnoId = "turno_1",
            folioTicket = "R-0001",
            tipo = "REPARTIDOR",
            fecha = now,
            hora = "11:00 AM",
            detalleProductos = "Moto 1 - Liquidación",
            total = 350.0,
            metodoPago = "Efectivo",
            usuarioId = "u1",
            usuarioNombre = "admin",
            estado = "ACTIVA"
        )

        tandasFlow.value = listOf(tanda)
        ventasFlow.value = listOf(ventaMostrador, ventaRepartidor)
        testDispatcher.scheduler.advanceUntilIdle()

        // 1. PRODUCCION_TOTAL
        viewModel.onEvent(MetricasUiEvent.SelectMetrica(MetricaTipo.PRODUCCION_TOTAL))
        testDispatcher.scheduler.advanceUntilIdle()
        assertEquals(38.0, viewModel.uiState.value.totalPeriodoActual, 0.01)
        assertEquals("kg", viewModel.uiState.value.unidadMetrica)

        // 2. VENTA_TOTAL
        viewModel.onEvent(MetricasUiEvent.SelectMetrica(MetricaTipo.VENTA_TOTAL))
        testDispatcher.scheduler.advanceUntilIdle()
        assertEquals(850.0, viewModel.uiState.value.totalPeriodoActual, 0.01)
        assertEquals("MXN", viewModel.uiState.value.unidadMetrica)

        // 3. VENTA_MOSTRADOR
        viewModel.onEvent(MetricasUiEvent.SelectMetrica(MetricaTipo.VENTA_MOSTRADOR))
        testDispatcher.scheduler.advanceUntilIdle()
        assertEquals(500.0, viewModel.uiState.value.totalPeriodoActual, 0.01)

        // 4. VENTA_REPARTIDOR
        viewModel.onEvent(MetricasUiEvent.SelectMetrica(MetricaTipo.VENTA_REPARTIDOR))
        testDispatcher.scheduler.advanceUntilIdle()
        assertEquals(350.0, viewModel.uiState.value.totalPeriodoActual, 0.01)
    }

    @Test
    fun `US4 - Deteccion automatica del punto pico y tooltip`() = runTest {
        val zone = ZoneId.systemDefault()
        val hoy = LocalDate.now()
        val anteayer = hoy.minusDays(2)

        val anteayerMillis = anteayer.atStartOfDay(zone).toInstant().toEpochMilli() + 3600000
        val hoyMillis = hoy.atStartOfDay(zone).toInstant().toEpochMilli() + 3600000

        val tandaAnteayer = TandaProduccionEntity(
            id = "t1",
            turnoId = "turno_1",
            fecha = anteayerMillis,
            hora = "09:00 AM",
            bultosHarina = 10,
            pesoBultoKg = 20.0,
            kgMasaCruda = 400.0,
            kgTortillaEstimada = 380.0,
            usuarioId = "u1",
            usuarioNombre = "admin"
        )
        val tandaHoy = TandaProduccionEntity(
            id = "t2",
            turnoId = "turno_2",
            fecha = hoyMillis,
            hora = "09:00 AM",
            bultosHarina = 2,
            pesoBultoKg = 20.0,
            kgMasaCruda = 80.0,
            kgTortillaEstimada = 76.0,
            usuarioId = "u1",
            usuarioNombre = "admin"
        )

        tandasFlow.value = listOf(tandaAnteayer, tandaHoy)
        viewModel.onEvent(MetricasUiEvent.SelectMetrica(MetricaTipo.PRODUCCION_TOTAL))
        testDispatcher.scheduler.advanceUntilIdle()

        val state = viewModel.uiState.value
        val puntoPico = state.puntoPico
        assertNotNull(puntoPico)
        assertEquals(anteayer, puntoPico!!.fecha)
        assertEquals(380.0, puntoPico.valor, 0.01)
        assertTrue(puntoPico.esPico)
        assertTrue(state.tooltipPicoTexto.contains("380.0 kg"))
    }

    @Test
    fun `US6 - Desglose y distribucion de canales Mostrador vs Repartidores`() = runTest {
        val now = System.currentTimeMillis()

        val ventaMostrador = VentaEntity(
            id = "v1",
            turnoId = "t1",
            folioTicket = "M-0001",
            tipo = "MOSTRADOR",
            fecha = now,
            hora = "10:00 AM",
            detalleProductos = "10 x Kilo",
            total = 600.0,
            metodoPago = "Efectivo",
            usuarioId = "u1",
            usuarioNombre = "admin",
            estado = "ACTIVA"
        )
        val ventaReparto = VentaEntity(
            id = "v2",
            turnoId = "t1",
            folioTicket = "R-0001",
            tipo = "REPARTIDOR",
            fecha = now,
            hora = "11:00 AM",
            detalleProductos = "Moto 1 - Liquidación",
            total = 400.0,
            metodoPago = "Efectivo",
            usuarioId = "u1",
            usuarioNombre = "admin",
            estado = "ACTIVA"
        )

        ventasFlow.value = listOf(ventaMostrador, ventaReparto)
        testDispatcher.scheduler.advanceUntilIdle()

        val state = viewModel.uiState.value
        assertEquals(600.0, state.montoMostrador, 0.01)
        assertEquals(400.0, state.montoReparto, 0.01)
        assertEquals(60f, state.porcentajeMostrador, 0.1f)
        assertEquals(40f, state.porcentajeReparto, 0.1f)
    }

    @Test
    fun `US7 - Calculo de promedio diario y merma optima menor al 5 por ciento`() = runTest {
        val now = System.currentTimeMillis()

        val tanda = TandaProduccionEntity(
            id = "t1",
            turnoId = "t1",
            fecha = now,
            hora = "09:00 AM",
            bultosHarina = 5,
            pesoBultoKg = 20.0,
            kgMasaCruda = 200.0,
            kgTortillaEstimada = 200.0,
            usuarioId = "u1",
            usuarioNombre = "admin"
        )
        val merma = MermaProduccionEntity(
            id = "m1",
            turnoId = "t1",
            fecha = now,
            hora = "10:00 AM",
            kgMerma = 6.0,
            motivo = "Rotura",
            usuarioId = "u1",
            usuarioNombre = "admin"
        )

        tandasFlow.value = listOf(tanda)
        mermasFlow.value = listOf(merma)
        viewModel.onEvent(MetricasUiEvent.SelectMetrica(MetricaTipo.PRODUCCION_TOTAL))
        testDispatcher.scheduler.advanceUntilIdle()

        val state = viewModel.uiState.value
        // 6 kg de merma sobre 200 kg producidos = 3.0%
        assertEquals(3.0, state.porcentajeMerma, 0.01)
        assertTrue(state.esMermaOptima)
    }

    @Test
    fun `US8 - Rango personalizado con fecha fin futura se acota automaticamente a hoy`() = runTest {
        val zone = ZoneId.systemDefault()
        val hoy = LocalDate.now()
        val haceTresDias = hoy.minusDays(3)
        val dentroDeCincoDias = hoy.plusDays(5)

        val startMillis = haceTresDias.atStartOfDay(zone).toInstant().toEpochMilli()
        val endFutureMillis = dentroDeCincoDias.atStartOfDay(zone).toInstant().toEpochMilli()

        viewModel.onEvent(MetricasUiEvent.SetCustomDateRange(startMillis, endFutureMillis))
        testDispatcher.scheduler.advanceUntilIdle()

        val state = viewModel.uiState.value
        assertEquals(PeriodoFiltro.PERSONALIZADO, state.periodoSeleccionado)

        // El último punto de la gráfica debe ser exactamente hoy, no dentro de 5 días
        val ultimoPunto = state.puntosGrafica.lastOrNull()
        assertNotNull(ultimoPunto)
        assertEquals(hoy, ultimoPunto!!.fecha)
        assertTrue(state.puntosGrafica.none { it.fecha.isAfter(hoy) })
    }

    @Test
    fun `Rango personalizado no permite mas de 30 dias y se acota a maximo 30 dias`() = runTest {
        val zone = ZoneId.systemDefault()
        val hoy = LocalDate.now()
        val haceCuarentaDias = hoy.minusDays(40)

        val startMillis = haceCuarentaDias.atStartOfDay(zone).toInstant().toEpochMilli()
        val endMillis = hoy.atStartOfDay(zone).toInstant().toEpochMilli()

        viewModel.onEvent(MetricasUiEvent.SetCustomDateRange(startMillis, endMillis))
        testDispatcher.scheduler.advanceUntilIdle()

        val state = viewModel.uiState.value
        assertEquals(PeriodoFiltro.PERSONALIZADO, state.periodoSeleccionado)
        // La gráfica debe tener exactamente 30 días como máximo
        assertEquals(30, state.puntosGrafica.size)
        assertEquals(hoy.minusDays(29), state.puntosGrafica.first().fecha)
        assertEquals(hoy, state.puntosGrafica.last().fecha)
    }
}

