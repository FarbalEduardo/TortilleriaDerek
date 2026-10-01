package com.example.tortilleriaderek.presentation.produccion

import app.cash.turbine.test
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
import com.example.tortilleriaderek.data.local.entity.TurnoEntity
import com.example.tortilleriaderek.data.local.entity.UsuarioEntity
import com.example.tortilleriaderek.data.local.entity.VentaEntity
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.mockk
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.test.StandardTestDispatcher
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.test.setMain
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test

/**
 * Pruebas unitarias para ProduccionViewModel con Turbine y MockK.
 * Valida el Contrato de Orquestación conforme a las historias de usuario:
 * - Caso 1: Contador inicial en 0.0 kg al abrir turno.
 * - Caso 2: Registro de tanda con cálculo de receta (20 harina + 20 agua = 40 masa).
 * - Casos 3, 4 y 5: Parámetros configurables en Settings (merma, peso bulto, masa por bulto).
 * - Caso 6: Descuento reactivo de ventas (Mostrador + Repartidores) y captura directa de merma en Producción.
 * - Caso 7: Persistencia en Room de tandas y mermas con auditoría.
 */
@OptIn(ExperimentalCoroutinesApi::class)
class ProduccionViewModelTest {

    private val testDispatcher = StandardTestDispatcher()

    private lateinit var turnoDao: TurnoDao
    private lateinit var produccionDao: ProduccionDao
    private lateinit var configuracionProduccionDao: ConfiguracionProduccionDao
    private lateinit var ventaDao: VentaDao
    private lateinit var rutaRepartidorDao: RutaRepartidorDao
    private lateinit var usuarioDao: UsuarioDao

    private lateinit var viewModel: ProduccionViewModel

    private val turnoActivoFlow = MutableStateFlow<TurnoEntity?>(null)
    private val configuracionFlow = MutableStateFlow<ConfiguracionProduccionEntity?>(null)
    private val tandasFlow = MutableStateFlow<List<TandaProduccionEntity>>(emptyList())
    private val mermasFlow = MutableStateFlow<List<MermaProduccionEntity>>(emptyList())
    private val ventasFlow = MutableStateFlow<List<VentaEntity>>(emptyList())
    private val rutasFlow = MutableStateFlow<List<RutaRepartidorEntity>>(emptyList())

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

    private val configDefault = ConfiguracionProduccionEntity(
        id = "DEFAULT",
        pesoBultoHarinaKg = 20.0,
        kgMasaPorBulto = 40.0,
        rendimientoTortillaPorBulto = 38.5,
        mermaToleradaKgPorBulto = 1.5,
        porcentajeMermaTolerada = 3.8
    )

    @Before
    fun setUp() {
        Dispatchers.setMain(testDispatcher)

        turnoDao = mockk(relaxed = true)
        produccionDao = mockk(relaxed = true)
        configuracionProduccionDao = mockk(relaxed = true)
        ventaDao = mockk(relaxed = true)
        rutaRepartidorDao = mockk(relaxed = true)
        usuarioDao = mockk(relaxed = true)

        coEvery { turnoDao.getTurnoActivo() } returns turnoActivoFlow
        coEvery { usuarioDao.getUsuarioById("user-1") } returns dummyUsuario
        coEvery { configuracionProduccionDao.getConfiguracion() } returns configuracionFlow
        coEvery { produccionDao.getTandasPorTurno("turno-123") } returns tandasFlow
        coEvery { produccionDao.getMermasPorTurno("turno-123") } returns mermasFlow
        coEvery { ventaDao.getVentasPorTurno("turno-123") } returns ventasFlow
        coEvery { rutaRepartidorDao.getRutasPorTurno("turno-123") } returns rutasFlow

        configuracionFlow.value = configDefault
        turnoActivoFlow.value = dummyTurno

        viewModel = ProduccionViewModel(
            turnoDao,
            produccionDao,
            configuracionProduccionDao,
            ventaDao,
            rutaRepartidorDao,
            usuarioDao
        )
    }

    @After
    fun tearDown() {
        Dispatchers.resetMain()
    }

    @Test
    fun `Caso 1 - al iniciar sesion y abrir turno el contador de tortillas disponibles inicia en cero`() = runTest {
        testDispatcher.scheduler.advanceUntilIdle()

        val state = viewModel.uiState.value
        assertEquals("turno-123", state.turnoActivo?.id)
        assertEquals("admin1", state.usuarioActivoNombre)
        assertFalse(state.isTurnoCerrado)
        // Verificación estricta del Caso 1: sin tandas registradas, el stock es exactamente 0.0 kg
        assertEquals(0.0, state.producidoNeto, 0.001)
        assertEquals(0.0, state.totalVendidoKg, 0.001)
        assertEquals(0.0, state.mermaTurnoKg, 0.001)
        assertEquals(0.0, state.disponibleEnTienda, 0.001)
        assertEquals(0.0, state.porcentajeVenta, 0.001)
    }

    @Test
    fun `Caso 2 - registro de tanda calcula masa cruda y tortilla conforme a receta y persiste en Room`() = runTest {
        testDispatcher.scheduler.advanceUntilIdle()

        // Seleccionar 4 bultos (Base: 20 kg harina + 20 kg agua = 40 kg masa por bulto)
        viewModel.onEvent(ProduccionUiEvent.OnBultosChanged(4))
        testDispatcher.scheduler.advanceUntilIdle()

        var state = viewModel.uiState.value
        assertEquals(4, state.bultosHarinaInput)
        assertEquals(160.0, state.masaCrudaCalculada, 0.001) // 4 * 40 kg masa
        assertEquals(154.0, state.tortillaCalculada, 0.001)  // 4 * 38.5 kg rendimiento

        viewModel.effect.test {
            // Acción: Registrar Tanda
            viewModel.onEvent(ProduccionUiEvent.OnRegistrarTandaClick)
            testDispatcher.scheduler.advanceUntilIdle()

            // Efecto: Toast de confirmación
            val efecto = awaitItem()
            assertTrue(efecto is ProduccionUiEffect.ShowToast)

            // Verificación Room: Inserción en tandas_produccion con usuario y auditoría
            coVerify(atLeast = 1) {
                produccionDao.insertTanda(match {
                    it.turnoId == "turno-123" &&
                            it.bultosHarina == 4 &&
                            it.kgMasaCruda == 160.0 &&
                            it.kgTortillaEstimada == 154.0 &&
                            it.usuarioNombre == "admin1"
                })
            }

            // Simulamos que Room emite la nueva tanda
            tandasFlow.value = listOf(
                TandaProduccionEntity(
                    id = "tanda-1",
                    turnoId = "turno-123",
                    fecha = System.currentTimeMillis(),
                    hora = "10:00 AM",
                    bultosHarina = 4,
                    pesoBultoKg = 20.0,
                    kgMasaCruda = 160.0,
                    kgTortillaEstimada = 154.0,
                    usuarioId = "user-1",
                    usuarioNombre = "admin1"
                )
            )
            testDispatcher.scheduler.advanceUntilIdle()

            // State actualizado con stock en tienda y contador de bultos reiniciado a cero
            state = viewModel.uiState.value
            assertEquals(154.0, state.producidoNeto, 0.001)
            assertEquals(154.0, state.disponibleEnTienda, 0.001)
            assertEquals(0, state.bultosHarinaInput)
            assertEquals(0.0, state.masaCrudaCalculada, 0.001)
            assertEquals(0.0, state.tortillaCalculada, 0.001)

            cancelAndIgnoreRemainingEvents()
        }
    }

    @Test
    fun `Casos 3, 4 y 5 - actualizacion de parametros en Settings se refleja reactivamente en calculos de Produccion`() = runTest {
        testDispatcher.scheduler.advanceUntilIdle()

        // Simulamos actualización en Room desde pantalla de Ajustes:
        // Harina de 25 kg, 50 kg de masa por bulto, rendimiento de 48 kg y merma de 2.0 kg
        val nuevaConfig = ConfiguracionProduccionEntity(
            id = "DEFAULT",
            pesoBultoHarinaKg = 25.0,
            kgMasaPorBulto = 50.0,
            rendimientoTortillaPorBulto = 48.0,
            mermaToleradaKgPorBulto = 2.0,
            porcentajeMermaTolerada = 4.0
        )
        configuracionFlow.value = nuevaConfig
        testDispatcher.scheduler.advanceUntilIdle()

        val state = viewModel.uiState.value
        assertEquals(25.0, state.pesoBultoHarinaKg, 0.001)
        assertEquals(50.0, state.kgMasaPorBulto, 0.001)
        assertEquals(48.0, state.rendimientoPorBulto, 0.001)
        assertEquals(2.0, state.mermaToleradaPorBulto, 0.001)

        // Con 2 bultos, masa = 100 kg y tortilla = 96 kg
        viewModel.onEvent(ProduccionUiEvent.OnBultosChanged(2))
        testDispatcher.scheduler.advanceUntilIdle()

        val stateCalculado = viewModel.uiState.value
        assertEquals(100.0, stateCalculado.masaCrudaCalculada, 0.001)
        assertEquals(96.0, stateCalculado.tortillaCalculada, 0.001)
    }

    @Test
    fun `Caso 6 - registrar merma directamente en pantalla de Produccion inserta en Room y descuenta el disponible`() = runTest {
        testDispatcher.scheduler.advanceUntilIdle()

        // Dado: 100 kg producidos
        tandasFlow.value = listOf(
            TandaProduccionEntity(
                id = "t-1",
                turnoId = "turno-123",
                fecha = System.currentTimeMillis(),
                hora = "10:00 AM",
                bultosHarina = 2,
                pesoBultoKg = 20.0,
                kgMasaCruda = 80.0,
                kgTortillaEstimada = 100.0,
                usuarioId = "user-1",
                usuarioNombre = "admin1"
            )
        )
        testDispatcher.scheduler.advanceUntilIdle()
        assertEquals(100.0, viewModel.uiState.value.disponibleEnTienda, 0.001)

        // Abrir diálogo de merma
        viewModel.onEvent(ProduccionUiEvent.OnOpenRegistrarMermaDialog)
        assertTrue(viewModel.uiState.value.showRegistrarMermaDialog)

        // Ingresar 5.5 kg de merma por comal
        viewModel.onEvent(ProduccionUiEvent.OnMermaKgInputChanged("5.5"))
        viewModel.onEvent(ProduccionUiEvent.OnMotivoMermaInputChanged("Tortilla rota en comal"))

        viewModel.effect.test {
            viewModel.onEvent(ProduccionUiEvent.OnConfirmarRegistroMerma)
            testDispatcher.scheduler.advanceUntilIdle()

            val toast = awaitItem()
            assertTrue(toast is ProduccionUiEffect.ShowToast)

            // Verifica inserción en mermas_produccion
            coVerify(atLeast = 1) {
                produccionDao.insertMerma(match {
                    it.turnoId == "turno-123" &&
                            it.kgMerma == 5.5 &&
                            it.motivo == "Tortilla rota en comal" &&
                            it.usuarioNombre == "admin1"
                })
            }

            // Simulamos emisión de Room con la nueva merma
            mermasFlow.value = listOf(
                MermaProduccionEntity(
                    id = "m-1",
                    turnoId = "turno-123",
                    fecha = System.currentTimeMillis(),
                    hora = "10:30 AM",
                    kgMerma = 5.5,
                    motivo = "Tortilla rota en comal",
                    usuarioId = "user-1",
                    usuarioNombre = "admin1"
                )
            )
            testDispatcher.scheduler.advanceUntilIdle()

            // Stock disponible debe ser 100.0 - 5.5 = 94.5 kg
            val stateFinal = viewModel.uiState.value
            assertFalse(stateFinal.showRegistrarMermaDialog)
            assertEquals(5.5, stateFinal.mermaTurnoKg, 0.001)
            assertEquals(94.5, stateFinal.disponibleEnTienda, 0.001)

            cancelAndIgnoreRemainingEvents()
        }
    }

    @Test
    fun `Caso 6 - ventas de mostrador y reparto descuentan el total vendido y stock disponible en tiempo real`() = runTest {
        testDispatcher.scheduler.advanceUntilIdle()

        // 100 kg producidos
        tandasFlow.value = listOf(
            TandaProduccionEntity(
                id = "t-1",
                turnoId = "turno-123",
                fecha = System.currentTimeMillis(),
                hora = "10:00 AM",
                bultosHarina = 2,
                pesoBultoKg = 20.0,
                kgMasaCruda = 80.0,
                kgTortillaEstimada = 100.0,
                usuarioId = "user-1",
                usuarioNombre = "admin1"
            )
        )

        // Venta de mostrador: 10 kg
        ventasFlow.value = listOf(
            VentaEntity(
                id = "v-1",
                turnoId = "turno-123",
                folioTicket = "M-0143",
                tipo = "MOSTRADOR",
                fecha = System.currentTimeMillis(),
                hora = "10:15 AM",
                detalleProductos = "10x Kilo Completo",
                total = 240.0,
                metodoPago = "Efectivo",
                usuarioId = "user-1",
                usuarioNombre = "admin1"
            )
        )

        // Repartidor en ruta liquidado con 25 kg entregados
        rutasFlow.value = listOf(
            RutaRepartidorEntity(
                id = "r-1",
                turnoId = "turno-123",
                moto = "Moto 01",
                repartidorNombre = "Carlos Ruiz",
                nombreRuta = "Ruta San Juan",
                status = "LIQUIDADO",
                cargaInicialKg = 30.0,
                entregadoKg = 25.0,
                devolucionKg = 5.0,
                cobrado = 550.0
            )
        )

        testDispatcher.scheduler.advanceUntilIdle()

        val state = viewModel.uiState.value
        assertEquals(10.0, state.totalVendidoMostradorKg, 0.001)
        assertEquals(25.0, state.totalVendidoRepartoKg, 0.001)
        assertEquals(35.0, state.totalVendidoKg, 0.001)
        // Disponible: 100 - 35 = 65.0 kg
        assertEquals(65.0, state.disponibleEnTienda, 0.001)
        // Porcentaje: 35 / 100 = 35%
        assertEquals(35.0, state.porcentajeVenta, 0.001)
    }

    @Test
    fun `stock disponible nunca es negativo si las ventas exceden provisionalmente la produccion`() = runTest {
        testDispatcher.scheduler.advanceUntilIdle()

        // 20 kg producidos
        tandasFlow.value = listOf(
            TandaProduccionEntity(
                id = "t-1",
                turnoId = "turno-123",
                fecha = System.currentTimeMillis(),
                hora = "10:00 AM",
                bultosHarina = 1,
                pesoBultoKg = 20.0,
                kgMasaCruda = 40.0,
                kgTortillaEstimada = 20.0,
                usuarioId = "user-1",
                usuarioNombre = "admin1"
            )
        )

        // 30 kg vendidos
        ventasFlow.value = listOf(
            VentaEntity(
                id = "v-1",
                turnoId = "turno-123",
                folioTicket = "M-0143",
                tipo = "MOSTRADOR",
                fecha = System.currentTimeMillis(),
                hora = "10:15 AM",
                detalleProductos = "30x Kilo Completo",
                total = 720.0,
                metodoPago = "Efectivo",
                usuarioId = "user-1",
                usuarioNombre = "admin1"
            )
        )

        testDispatcher.scheduler.advanceUntilIdle()

        val state = viewModel.uiState.value
        assertEquals(30.0, state.totalVendidoKg, 0.001)
        // Coerción a 0.0 kg
        assertEquals(0.0, state.disponibleEnTienda, 0.001)
    }

    @Test
    fun `stepper de bultos inicia en cero, incrementa y decrementa correctamente sin permitir negativos`() = runTest {
        testDispatcher.scheduler.advanceUntilIdle()

        // Inicia en cero conforme a requerimiento
        assertEquals(0, viewModel.uiState.value.bultosHarinaInput)

        viewModel.onEvent(ProduccionUiEvent.OnIncrementarBultos)
        assertEquals(1, viewModel.uiState.value.bultosHarinaInput)

        viewModel.onEvent(ProduccionUiEvent.OnIncrementarBultos)
        assertEquals(2, viewModel.uiState.value.bultosHarinaInput)

        viewModel.onEvent(ProduccionUiEvent.OnDecrementarBultos)
        assertEquals(1, viewModel.uiState.value.bultosHarinaInput)

        viewModel.onEvent(ProduccionUiEvent.OnDecrementarBultos)
        assertEquals(0, viewModel.uiState.value.bultosHarinaInput)

        // No debe decrementar de 0 (no permite negativos)
        viewModel.onEvent(ProduccionUiEvent.OnDecrementarBultos)
        assertEquals(0, viewModel.uiState.value.bultosHarinaInput)
    }

    @Test
    fun `intentar registrar tanda con cero bultos emite mensaje de error y no inserta en Room`() = runTest {
        testDispatcher.scheduler.advanceUntilIdle()

        assertEquals(0, viewModel.uiState.value.bultosHarinaInput)

        viewModel.effect.test {
            viewModel.onEvent(ProduccionUiEvent.OnRegistrarTandaClick)
            testDispatcher.scheduler.advanceUntilIdle()

            val errorEffect = awaitItem()
            assertTrue(errorEffect is ProduccionUiEffect.ShowError)
            assertEquals("La cantidad de bultos debe ser mayor a 0.", (errorEffect as ProduccionUiEffect.ShowError).error)

            coVerify(exactly = 0) { produccionDao.insertTanda(any()) }
            cancelAndIgnoreRemainingEvents()
        }
    }

    @Test
    fun `intentar registrar merma con valor invalido muestra mensaje de error y no inserta en Room`() = runTest {
        testDispatcher.scheduler.advanceUntilIdle()

        viewModel.onEvent(ProduccionUiEvent.OnOpenRegistrarMermaDialog)
        viewModel.onEvent(ProduccionUiEvent.OnMermaKgInputChanged("abc"))
        viewModel.onEvent(ProduccionUiEvent.OnConfirmarRegistroMerma)

        testDispatcher.scheduler.advanceUntilIdle()

        assertTrue(viewModel.uiState.value.errorMessage != null)
        coVerify(exactly = 0) { produccionDao.insertMerma(any()) }
    }

    @Test
    fun `calcularKgMostrador descuenta con precision 1kg por kilo, 800g por paquete y 400g por medio paquete`() = runTest {
        testDispatcher.scheduler.advanceUntilIdle()

        // 100 kg producidos
        tandasFlow.value = listOf(
            TandaProduccionEntity(
                id = "t-100",
                turnoId = "turno-123",
                fecha = System.currentTimeMillis(),
                hora = "08:00 AM",
                bultosHarina = 2,
                pesoBultoKg = 20.0,
                kgMasaCruda = 80.0,
                kgTortillaEstimada = 100.0,
                usuarioId = "user-1",
                usuarioNombre = "admin1"
            )
        )

        // Ventas: 10 Kilo Completo (10kg), 5 Paquete Mostrador (5 * 0.8kg = 4kg), 4 Medio Paquete (4 * 0.4kg = 1.6kg)
        // Total = 10 + 4 + 1.6 = 15.6 kg
        ventasFlow.value = listOf(
            VentaEntity(
                id = "v-combo",
                turnoId = "turno-123",
                folioTicket = "M-0144",
                tipo = "MOSTRADOR",
                fecha = System.currentTimeMillis(),
                hora = "10:30 AM",
                detalleProductos = "10 Kilogramo, 5 Paquete Mostrador, 4 Medio Paquete",
                total = 380.0,
                metodoPago = "Efectivo",
                usuarioId = "user-1",
                usuarioNombre = "admin1"
            )
        )

        testDispatcher.scheduler.advanceUntilIdle()

        val state = viewModel.uiState.value
        assertEquals(15.6, state.totalVendidoMostradorKg, 0.01)
        assertEquals(15.6, state.totalVendidoKg, 0.01)
        assertEquals(84.4, state.disponibleEnTienda, 0.01)
    }

    @Test
    fun `US-PROD-09 - Registro de merma rechaza cantidad mayor a la tortilla disponible en tienda`() = runTest {
        // 1 tanda de 2 bultos (~77.0 kg)
        tandasFlow.value = listOf(
            TandaProduccionEntity(
                id = "t-1",
                turnoId = "turno-123",
                fecha = System.currentTimeMillis(),
                hora = "08:00 AM",
                bultosHarina = 2,
                pesoBultoKg = 20.0,
                kgMasaCruda = 80.0,
                kgTortillaEstimada = 77.0,
                usuarioId = "user-1",
                usuarioNombre = "admin1"
            )
        )
        testDispatcher.scheduler.advanceUntilIdle()
        assertEquals(77.0, viewModel.uiState.value.disponibleEnTienda, 0.01)

        // Intenta ingresar merma de 90 kg (mayor a 77.0 kg disponible)
        viewModel.onEvent(ProduccionUiEvent.OnOpenRegistrarMermaDialog)
        viewModel.onEvent(ProduccionUiEvent.OnMermaKgInputChanged("90.0"))
        assertEquals("La merma (90.0 kg) supera la tortilla disponible (77.0 kg)", viewModel.uiState.value.errorMessage)

        viewModel.onEvent(ProduccionUiEvent.OnConfirmarRegistroMerma)
        testDispatcher.scheduler.advanceUntilIdle()

        // Verificar que NO se insertó la merma
        coVerify(exactly = 0) {
            produccionDao.insertMerma(any())
        }
    }
}

