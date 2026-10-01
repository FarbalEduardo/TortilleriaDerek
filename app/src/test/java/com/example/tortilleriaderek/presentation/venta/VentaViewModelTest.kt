package com.example.tortilleriaderek.presentation.venta

import app.cash.turbine.test
import com.example.tortilleriaderek.data.local.dao.ConfiguracionProduccionDao
import com.example.tortilleriaderek.data.local.dao.ProduccionDao
import com.example.tortilleriaderek.data.local.dao.RepartidorDao
import com.example.tortilleriaderek.data.local.dao.RutaRepartidorDao
import com.example.tortilleriaderek.data.local.dao.TurnoDao
import com.example.tortilleriaderek.data.local.dao.UsuarioDao
import com.example.tortilleriaderek.data.local.dao.VentaDao
import com.example.tortilleriaderek.data.local.entity.ConfiguracionProduccionEntity
import com.example.tortilleriaderek.data.local.entity.RepartidorEntity
import com.example.tortilleriaderek.data.local.entity.RutaRepartidorEntity
import com.example.tortilleriaderek.data.local.entity.TandaProduccionEntity
import com.example.tortilleriaderek.data.local.entity.TurnoEntity
import com.example.tortilleriaderek.data.local.entity.UsuarioEntity
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.mockk
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.test.StandardTestDispatcher
import kotlinx.coroutines.test.advanceUntilIdle
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.test.setMain
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test

/**
 * Creado por 🏆 quality-pm-expert.
 * Pruebas unitarias para VentaViewModel cubriendo reglas de negocio de Mostrador,
 * Cierre de Turno con validación de rutas y registro de ventas.
 */
@OptIn(ExperimentalCoroutinesApi::class)
class VentaViewModelTest {

    private val testDispatcher = StandardTestDispatcher()

    private lateinit var turnoDao: TurnoDao
    private lateinit var ventaDao: VentaDao
    private lateinit var rutaRepartidorDao: RutaRepartidorDao
    private lateinit var usuarioDao: UsuarioDao
    private lateinit var produccionDao: ProduccionDao
    private lateinit var repartidorDao: RepartidorDao
    private lateinit var configuracionProduccionDao: ConfiguracionProduccionDao
    private lateinit var viewModel: VentaViewModel

    private val dummyTurno = TurnoEntity(
        id = "turno-123",
        fechaApertura = 1726500000000L,
        estado = "ABIERTO",
        usuarioId = "user-1"
    )

    private val dummyUsuario = UsuarioEntity(
        id = "user-1",
        username = "admin1",
        passwordHash = "hash",
        salt = "salt",
        rol = "ADMIN"
    )

    @Before
    fun setUp() {
        Dispatchers.setMain(testDispatcher)
        turnoDao = mockk(relaxed = true)
        ventaDao = mockk(relaxed = true)
        rutaRepartidorDao = mockk(relaxed = true)
        usuarioDao = mockk(relaxed = true)
        produccionDao = mockk(relaxed = true)
        repartidorDao = mockk(relaxed = true)
        configuracionProduccionDao = mockk(relaxed = true)

        val tandaStock = TandaProduccionEntity(
            id = "tanda-stock",
            turnoId = "turno-123",
            fecha = 1726500000000L,
            hora = "08:00 AM",
            bultosHarina = 2,
            pesoBultoKg = 20.0,
            kgMasaCruda = 80.0,
            kgTortillaEstimada = 100.0,
            usuarioId = "user-1",
            usuarioNombre = "admin1"
        )

        val dummyConfig = ConfiguracionProduccionEntity(
            precioKilo = 24.00,
            precioPaquete = 20.00,
            precioMedioPaquete = 10.00,
            precioPaqueteRepartidor = 18.00,
            pesoPaqueteGramos = 800,
            pesoMedioPaqueteGramos = 400
        )

        coEvery { turnoDao.getTurnoActivo() } returns flowOf(dummyTurno)
        coEvery { usuarioDao.getUsuarioById("user-1") } returns dummyUsuario
        coEvery { ventaDao.getVentasPorTurno("turno-123") } returns flowOf(emptyList())
        coEvery { rutaRepartidorDao.getRutasPorTurno("turno-123") } returns flowOf(emptyList())
        coEvery { produccionDao.getTandasPorTurno("turno-123") } returns flowOf(listOf(tandaStock))
        coEvery { produccionDao.getMermasPorTurno("turno-123") } returns flowOf(emptyList())
        coEvery { repartidorDao.getRepartidoresList() } returns emptyList()
        coEvery { configuracionProduccionDao.getConfiguracion() } returns flowOf(dummyConfig)

        viewModel = VentaViewModel(turnoDao, ventaDao, rutaRepartidorDao, usuarioDao, produccionDao, repartidorDao, configuracionProduccionDao)
    }

    @After
    fun tearDown() {
        Dispatchers.resetMain()
    }

    @Test
    fun `al inicializar con turno abierto, carga los datos del turno y usuario en UiState con contadores en cero (US16)`() = runTest {
        testDispatcher.scheduler.advanceUntilIdle()

        val state = viewModel.uiState.value
        assertEquals("turno-123", state.turnoActivo?.id)
        assertEquals("admin1", state.usuarioActivoNombre)
        assertFalse(state.isTurnoCerrado)
        assertTrue(state.products.all { it.quantity == 0 })
        assertEquals(0.0, state.subtotalOrdenActual, 0.001)
    }

    @Test
    fun `al abrir turno, no debe aparecer ningun repartidor con monto asignado o en ruta (US16)`() = runTest {
        testDispatcher.scheduler.advanceUntilIdle()

        val state = viewModel.uiState.value
        assertEquals(0, state.rutasActivasCount)
        assertTrue(state.rutasRepartidores.all {
            it.status == "PENDIENTE_SALIDA" && it.cargaInicialKg == 0.0 && it.pendienteCobro == 0.0
        })
    }

    @Test
    fun `cuando hay rutas activas mayores a 0, onConfirmarCerrarTurno emite ShowToast y NO cierra turno`() = runTest {
        // Given: simulamos rutas con 1 activa EN_RUTA
        val rutaEnCurso = RutaRepartidorEntity(
            id = "ruta-1",
            turnoId = "turno-123",
            moto = "Moto 01",
            repartidorNombre = "Carlos",
            nombreRuta = "Ruta 1",
            status = "EN_RUTA",
            cargaInicialKg = 50.0,
            pendienteCobro = 1200.0
        )
        coEvery { rutaRepartidorDao.getRutasPorTurno("turno-123") } returns flowOf(listOf(rutaEnCurso))

        // Re-creamos o dejamos que corra el observer
        val vm = VentaViewModel(turnoDao, ventaDao, rutaRepartidorDao, usuarioDao, produccionDao, repartidorDao, configuracionProduccionDao)
        testDispatcher.scheduler.advanceUntilIdle()

        assertEquals(1, vm.uiState.value.rutasActivasCount)

        vm.effect.test {
            // When
            vm.onConfirmarCerrarTurno()
            testDispatcher.scheduler.advanceUntilIdle()

            // Then: debe emitir el toast de advertencia
            val efecto = awaitItem()
            assertTrue(efecto is VentaUiEffect.ShowToast)
            assertEquals(
                "No es posible cerrar turno: hay rutas activas sin liquidar.",
                (efecto as VentaUiEffect.ShowToast).mensaje
            )

            // Verifica que NO se llamó a cerrarTurno en Room
            coVerify(exactly = 0) { turnoDao.cerrarTurno(any(), any(), any()) }
            coVerify(exactly = 0) { turnoDao.cerrarTodosLosTurnosActivos(any()) }

            cancelAndIgnoreRemainingEvents()
        }
    }

    @Test
    fun `cuando no hay rutas activas, onConfirmarCerrarTurno cierra el turno en Room y emite NavigateToLogin`() = runTest {
        testDispatcher.scheduler.advanceUntilIdle()
        assertEquals(0, viewModel.uiState.value.rutasActivasCount)

        viewModel.effect.test {
            // When
            viewModel.onConfirmarCerrarTurno()
            testDispatcher.scheduler.advanceUntilIdle()

            // Then
            val efecto = awaitItem()
            assertEquals(VentaUiEffect.NavigateToLogin, efecto)

            // Verifica que sí se llamó a cerrarTurno y cerrarTodosLosTurnosActivos
            coVerify(atLeast = 1) { turnoDao.cerrarTurno(eq("turno-123"), any(), any()) }
            coVerify(atLeast = 1) { turnoDao.cerrarTodosLosTurnosActivos(any()) }

            val stateFinal = viewModel.uiState.value
            assertTrue(stateFinal.isTurnoCerrado)
            assertNull(stateFinal.turnoActivo)
            assertFalse(stateFinal.showCerrarTurnoDialog)

            cancelAndIgnoreRemainingEvents()
        }
    }

    @Test
    fun `dialogo de cerrar turno se abre y cierra correctamente`() = runTest {
        assertFalse(viewModel.uiState.value.showCerrarTurnoDialog)

        viewModel.onOpenCerrarTurnoDialog()
        assertTrue(viewModel.uiState.value.showCerrarTurnoDialog)

        viewModel.onDismissCerrarTurnoDialog()
        assertFalse(viewModel.uiState.value.showCerrarTurnoDialog)
    }

    @Test
    fun `onQuantityChanged modifica la cantidad de un producto`() = runTest {
        testDispatcher.scheduler.advanceUntilIdle()

        // Producto 1 tiene por defecto 4
        viewModel.onQuantityChanged(productId = 1, newQty = 7)
        val prod1 = viewModel.uiState.value.products.first { it.id == 1 }
        assertEquals(7, prod1.quantity)

        // No debe permitir valores negativos
        viewModel.onQuantityChanged(productId = 1, newQty = -2)
        val prod1SinCambio = viewModel.uiState.value.products.first { it.id == 1 }
        assertEquals(7, prod1SinCambio.quantity)
    }

    @Test
    fun `onScopeSelected actualiza el filtro en UiState`() = runTest {
        viewModel.onScopeSelected("Repartidores")
        assertEquals("Repartidores", viewModel.uiState.value.selectedScope)

        viewModel.onScopeSelected("Mostrador")
        assertEquals("Mostrador", viewModel.uiState.value.selectedScope)
    }

    @Test
    fun `onConfirmarVenta inserta la venta en Room con metodo Efectivo y emite ShowToast`() = runTest {
        testDispatcher.scheduler.advanceUntilIdle()

        // Agregar productos para la orden
        viewModel.onQuantityChanged(productId = 1, newQty = 2)
        testDispatcher.scheduler.advanceUntilIdle()

        viewModel.effect.test {
            viewModel.onConfirmarVenta()
            testDispatcher.scheduler.advanceUntilIdle()

            val efecto = awaitItem()
            assertTrue(efecto is VentaUiEffect.ShowToast)

            // Verifica que se insertó la venta con metodo Efectivo
            coVerify(atLeast = 1) {
                ventaDao.insertVenta(match {
                    it.metodoPago == "Efectivo" && it.tipo == "MOSTRADOR" && it.usuarioNombre == "admin1"
                })
            }

            cancelAndIgnoreRemainingEvents()
        }
    }

    @Test
    fun `cuando la orden en mostrador excede el stock disponible, onConfirmarVenta emite ShowToast y NO inserta venta`() = runTest {
        // Mockeamos producción con solo 1.0 kg disponible
        val tandaPocoStock = TandaProduccionEntity(
            id = "t-1",
            turnoId = "turno-123",
            fecha = 1726500000000L,
            hora = "08:00 AM",
            bultosHarina = 1,
            pesoBultoKg = 20.0,
            kgMasaCruda = 40.0,
            kgTortillaEstimada = 1.0,
            usuarioId = "user-1",
            usuarioNombre = "admin1"
        )
        coEvery { produccionDao.getTandasPorTurno("turno-123") } returns flowOf(listOf(tandaPocoStock))

        val vm = VentaViewModel(turnoDao, ventaDao, rutaRepartidorDao, usuarioDao, produccionDao, repartidorDao, configuracionProduccionDao)
        testDispatcher.scheduler.advanceUntilIdle()

        // Seleccionar 2 kg (supera 1.0 kg disponible)
        vm.onQuantityChanged(productId = 1, newQty = 2)
        testDispatcher.scheduler.advanceUntilIdle()

        vm.effect.test {
            vm.onConfirmarVenta()
            testDispatcher.scheduler.advanceUntilIdle()

            val toast = awaitItem()
            assertTrue(toast is VentaUiEffect.ShowToast)
            assertTrue((toast as VentaUiEffect.ShowToast).mensaje.contains("Stock insuficiente"))

            // Verifica que NO se insertó la venta
            coVerify(exactly = 0) { ventaDao.insertVenta(any()) }
            cancelAndIgnoreRemainingEvents()
        }
    }

    @Test
    fun `cuando la carga para repartidor excede el stock disponible, onConfirmarSalidaRepartidor emite ShowToast y NO actualiza ruta`() = runTest {
        testDispatcher.scheduler.advanceUntilIdle()

        // En setUp hay 100 kg disponibles. Solicitamos 150 kg para la moto
        val ruta = RutaRepartidorEntity(
            id = "ruta-moto-1",
            turnoId = "turno-123",
            moto = "Moto 01",
            repartidorNombre = "Carlos Ruiz",
            nombreRuta = "Ruta San Juan",
            status = "PENDIENTE_SALIDA"
        )
        viewModel.onOpenSalidaRepartidor(ruta)

        viewModel.effect.test {
            viewModel.onConfirmarSalidaRepartidor(cargaKg = 150.0)
            testDispatcher.scheduler.advanceUntilIdle()

            val toast = awaitItem()
            assertTrue(toast is VentaUiEffect.ShowToast)
            assertTrue((toast as VentaUiEffect.ShowToast).mensaje.contains("Stock insuficiente"))

            // Verifica que NO se actualizó la ruta a EN_RUTA
            coVerify(exactly = 0) { rutaRepartidorDao.updateRuta(any()) }
            cancelAndIgnoreRemainingEvents()
        }
    }

    @Test
    fun `cuando no hay rutas en turno pero si repartidores en catalogo Room, inicializa rutas para ellos`() = runTest {
        val localRutaDao = mockk<RutaRepartidorDao>(relaxed = true)
        val repCatalogo = listOf(
            RepartidorEntity("r1", "#01", "Chofer Test 1", "Moto 01", "Ruta A"),
            RepartidorEntity("r2", "#02", "Chofer Test 2", "Moto 02", "Ruta B")
        )
        coEvery { repartidorDao.getRepartidoresList() } returns repCatalogo
        coEvery { localRutaDao.getRutasPorTurno("turno-123") } returns flowOf(emptyList())

        val vm = VentaViewModel(turnoDao, ventaDao, localRutaDao, usuarioDao, produccionDao, repartidorDao, configuracionProduccionDao)
        testDispatcher.scheduler.advanceUntilIdle()

        coVerify(exactly = 1) {
            localRutaDao.insertRutas(match { rutas ->
                rutas.size == 2 &&
                rutas[0].repartidorNombre == "Chofer Test 1" &&
                rutas[1].repartidorNombre == "Chofer Test 2"
            })
        }
    }

    @Test
    fun `cuando catalogo de repartidores esta vacio y turno no tiene rutas, lista de rutas permanece vacia sin nombres hardcodeados`() = runTest {
        coEvery { repartidorDao.getRepartidoresList() } returns emptyList()
        coEvery { rutaRepartidorDao.getRutasPorTurno("turno-123") } returns flowOf(emptyList())

        val vm = VentaViewModel(turnoDao, ventaDao, rutaRepartidorDao, usuarioDao, produccionDao, repartidorDao, configuracionProduccionDao)
        testDispatcher.scheduler.advanceUntilIdle()

        coVerify(exactly = 0) { rutaRepartidorDao.insertRutas(any()) }
        assertTrue(vm.uiState.value.rutasRepartidores.isEmpty())
    }

    @Test
    fun `observarPreciosConfiguracion actualiza reactivamente precios de mostrador y precio mayoreo repartidor`() = runTest {
        testDispatcher.scheduler.advanceUntilIdle()

        // Verificamos que solo se muestren los 3 productos principales de mostrador
        val state = viewModel.uiState.value
        assertEquals(3, state.products.size)
        assertTrue(state.products.none { it.name.contains("Mayoreo", ignoreCase = true) })
        assertEquals(24.00, state.products.first { it.id == 1 }.price, 0.01)
        assertEquals(10.00, state.products.first { it.id == 2 }.price, 0.01)
        assertEquals(20.00, state.products.first { it.id == 3 }.price, 0.01)
        assertEquals(18.00, state.precioPaqueteRepartidor, 0.01)

        // Verificamos cálculo de kilos reales (1kg + 2 medios de 400g = 1.80 kg)
        viewModel.onQuantityChanged(productId = 1, newQty = 1) // 1.0 kg
        viewModel.onQuantityChanged(productId = 2, newQty = 2) // 2 * 0.40 kg = 0.80 kg
        testDispatcher.scheduler.advanceUntilIdle()

        assertEquals(1.80, viewModel.uiState.value.kgOrdenActual, 0.01)
    }

    @Test
    fun `onConfirmarSalidaRepartidor calcula pendienteCobro usando paquetes de 800g y precio mayoreo configurado`() = runTest {
        testDispatcher.scheduler.advanceUntilIdle()

        val ruta = RutaRepartidorEntity(
            id = "ruta-mayoreo-1",
            turnoId = "turno-123",
            moto = "Moto 01",
            repartidorNombre = "Carlos Ruiz",
            nombreRuta = "Ruta San Juan",
            status = "PENDIENTE_SALIDA"
        )
        viewModel.onOpenSalidaRepartidor(ruta)

        // Salida con 80 kg = 100 paquetes de 800g @ $18.00 = $1800.00
        viewModel.onConfirmarSalidaRepartidor(cargaKg = 80.0)
        testDispatcher.scheduler.advanceUntilIdle()

        coVerify(exactly = 1) {
            rutaRepartidorDao.updateRuta(match {
                it.cargaInicialKg == 80.0 &&
                it.status == "EN_RUTA" &&
                Math.abs(it.pendienteCobro - 1800.00) < 0.01
            })
        }
    }

    @Test
    fun `onConfirmarSalidaRepartidor con paquetes explicitos calcula cargaKg y cobro exacto de mayoreo`() = runTest {
        testDispatcher.scheduler.advanceUntilIdle()

        val ruta = RutaRepartidorEntity(
            id = "ruta-mayoreo-paq",
            turnoId = "turno-123",
            moto = "Moto 01",
            repartidorNombre = "Carlos Ruiz",
            nombreRuta = "Ruta San Juan",
            status = "PENDIENTE_SALIDA"
        )
        viewModel.onOpenSalidaRepartidor(ruta)

        // 70 paquetes de 800g = 56.0 kg @ $18.00 = $1,260.00
        viewModel.onConfirmarSalidaRepartidor(cargaKg = 56.0, paquetes = 70)
        testDispatcher.scheduler.advanceUntilIdle()

        coVerify(exactly = 1) {
            rutaRepartidorDao.updateRuta(match {
                it.paquetesCargados == 70 &&
                it.cargaInicialKg == 56.0 &&
                it.status == "EN_RUTA" &&
                Math.abs(it.pendienteCobro - 1260.00) < 0.01
            })
        }
    }

    @Test
    fun `onConfirmarLiquidarRepartidor con paquetesDevueltos registra liquidacion exacta de mayoreo`() = runTest {
        testDispatcher.scheduler.advanceUntilIdle()

        val ruta = RutaRepartidorEntity(
            id = "ruta-liq-paq",
            turnoId = "turno-123",
            moto = "Moto 01",
            repartidorNombre = "Carlos Ruiz",
            nombreRuta = "Ruta San Juan",
            status = "EN_RUTA",
            cargaInicialKg = 56.0,
            paquetesCargados = 70,
            pendienteCobro = 1260.00
        )
        viewModel.onOpenLiquidarRepartidor(ruta)

        // Devolución de 5 paquetes (4.0 kg), vendidos 65 paquetes (52.0 kg), cobrado $1,170.00
        viewModel.onConfirmarLiquidarRepartidor(devolucionKg = 4.0, cobrado = 1170.00, paquetesDevueltos = 5)
        testDispatcher.scheduler.advanceUntilIdle()

        coVerify(exactly = 1) {
            rutaRepartidorDao.updateRuta(match {
                it.status == "LIQUIDADO" &&
                it.paquetesDevueltos == 5 &&
                it.devolucionKg == 4.0 &&
                it.entregadoKg == 52.0 &&
                Math.abs(it.cobrado - 1170.00) < 0.01
            })
        }
    }

    @Test
    fun `US17 - onConfirmarLiquidarRepartidor con devolucion mayor a la carga emite ShowToast y NO liquida`() = runTest {
        testDispatcher.scheduler.advanceUntilIdle()

        val ruta = RutaRepartidorEntity(
            id = "ruta-err",
            turnoId = "turno-123",
            moto = "Moto 01",
            repartidorNombre = "Carlos Ruiz",
            nombreRuta = "Ruta San Juan",
            status = "EN_RUTA",
            cargaInicialKg = 50.0,
            paquetesCargados = 62,
            pendienteCobro = 1116.00
        )
        viewModel.onOpenLiquidarRepartidor(ruta)

        viewModel.effect.test {
            // Intenta devolver 60 kg (mayor a 50 kg cargados)
            viewModel.onConfirmarLiquidarRepartidor(devolucionKg = 60.0, cobrado = 0.0)
            testDispatcher.scheduler.advanceUntilIdle()

            val toast = awaitItem()
            assertTrue(toast is VentaUiEffect.ShowToast)
            assertTrue((toast as VentaUiEffect.ShowToast).mensaje.contains("no puede superar la carga inicial"))

            // Verificar que NO se insertó venta ni se actualizó ruta a LIQUIDADO
            coVerify(exactly = 0) {
                rutaRepartidorDao.updateRuta(match { it.status == "LIQUIDADO" })
            }
            cancelAndIgnoreRemainingEvents()
        }
    }

    @Test
    fun `US18 - onCancelarSalidaRepartidor regresa status a PENDIENTE_SALIDA y resetea carga a cero`() = runTest {
        testDispatcher.scheduler.advanceUntilIdle()

        val rutaEnRuta = RutaRepartidorEntity(
            id = "ruta-cancel",
            turnoId = "turno-123",
            moto = "Moto 02",
            repartidorNombre = "Miguel Gómez",
            nombreRuta = "Ruta Taquerías",
            status = "EN_RUTA",
            cargaInicialKg = 70.0,
            paquetesCargados = 87,
            pendienteCobro = 1566.00,
            horaSalida = "09:15 AM"
        )

        viewModel.onCancelarSalidaRepartidor(rutaEnRuta)
        testDispatcher.scheduler.advanceUntilIdle()

        coVerify(exactly = 1) {
            rutaRepartidorDao.updateRuta(match {
                it.id == "ruta-cancel" &&
                it.status == "PENDIENTE_SALIDA" &&
                it.cargaInicialKg == 0.0 &&
                it.pendienteCobro == 0.0 &&
                it.horaSalida == null &&
                it.paquetesCargados == 0
            })
        }
    }
}

