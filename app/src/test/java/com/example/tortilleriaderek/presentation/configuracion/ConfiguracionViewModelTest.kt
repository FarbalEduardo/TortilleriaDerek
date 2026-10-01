package com.example.tortilleriaderek.presentation.configuracion

import com.example.tortilleriaderek.data.local.dao.ConfiguracionProduccionDao
import com.example.tortilleriaderek.data.local.dao.RepartidorDao
import com.example.tortilleriaderek.data.local.dao.RutaRepartidorDao
import com.example.tortilleriaderek.data.local.dao.TurnoDao
import com.example.tortilleriaderek.data.local.dao.UsuarioDao
import com.example.tortilleriaderek.data.local.entity.ConfiguracionProduccionEntity
import com.example.tortilleriaderek.data.local.entity.RepartidorEntity
import com.example.tortilleriaderek.data.local.entity.TurnoEntity
import com.example.tortilleriaderek.data.local.entity.UsuarioEntity
import com.example.tortilleriaderek.data.security.CryptoManager
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.mockk
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.test.StandardTestDispatcher
import kotlinx.coroutines.test.advanceUntilIdle
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.test.setMain
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test

@OptIn(ExperimentalCoroutinesApi::class)
class ConfiguracionViewModelTest {

    private val testDispatcher = StandardTestDispatcher()

    private lateinit var configuracionProduccionDao: ConfiguracionProduccionDao
    private lateinit var repartidorDao: RepartidorDao
    private lateinit var rutaRepartidorDao: RutaRepartidorDao
    private lateinit var turnoDao: TurnoDao
    private lateinit var usuarioDao: UsuarioDao
    private lateinit var viewModel: ConfiguracionViewModel

    private val repartidoresFlow = MutableStateFlow<List<RepartidorEntity>>(emptyList())
    private val usuariosFlow = MutableStateFlow<List<UsuarioEntity>>(emptyList())

    @Before
    fun setUp() {
        Dispatchers.setMain(testDispatcher)
        configuracionProduccionDao = mockk(relaxed = true)
        repartidorDao = mockk(relaxed = true)
        rutaRepartidorDao = mockk(relaxed = true)
        turnoDao = mockk(relaxed = true)
        usuarioDao = mockk(relaxed = true)

        coEvery { configuracionProduccionDao.getConfiguracion() } returns flowOf(ConfiguracionProduccionEntity())
        coEvery { repartidorDao.getAllRepartidores() } returns repartidoresFlow
        coEvery { usuarioDao.getAllUsuarios() } returns usuariosFlow
        coEvery { usuarioDao.countUsuarios() } returns 1

        viewModel = ConfiguracionViewModel(
            configuracionProduccionDao,
            repartidorDao,
            rutaRepartidorDao,
            turnoDao,
            usuarioDao
        )
    }

    @After
    fun tearDown() {
        Dispatchers.resetMain()
    }

    @Test
    fun `agregarRepartidor sin turno activo inserta repartidor en Room con badge autocalculado`() = runTest {
        coEvery { turnoDao.getTurnoActivoSync() } returns null

        viewModel.agregarRepartidor(
            nombre = "Roberto Díaz",
            motoRuta = "Moto 01 • Ruta Centro"
        )
        testDispatcher.scheduler.advanceUntilIdle()

        coVerify(exactly = 1) {
            repartidorDao.insertRepartidor(match {
                it.nombre == "Roberto Díaz" &&
                it.numeroBadge == "#01" &&
                it.moto == "Moto 01" &&
                it.detalleRuta == "Moto 01 • Ruta Centro"
            })
        }
        coVerify(exactly = 0) { rutaRepartidorDao.insertRuta(any()) }
    }

    @Test
    fun `agregarRepartidor con turno activo inserta repartidor y crea ruta en PENDIENTE_SALIDA para turno`() = runTest {
        val dummyTurno = TurnoEntity(
            id = "turno-act-1",
            fechaApertura = 1726500000000L,
            estado = "ABIERTO",
            usuarioId = "user-1"
        )
        coEvery { turnoDao.getTurnoActivoSync() } returns dummyTurno

        viewModel.agregarRepartidor(
            nombre = "Carlos Santana",
            motoRuta = "Moto Italika • Ruta Poniente"
        )
        testDispatcher.scheduler.advanceUntilIdle()

        coVerify(exactly = 1) {
            repartidorDao.insertRepartidor(match {
                it.nombre == "Carlos Santana"
            })
        }
        coVerify(exactly = 1) {
            rutaRepartidorDao.insertRuta(match {
                it.turnoId == "turno-act-1" &&
                it.repartidorNombre == "Carlos Santana" &&
                it.status == "PENDIENTE_SALIDA" &&
                it.cargaInicialKg == 0.0
            })
        }
    }

    @Test
    fun `eliminarRepartidor borra de Room y retira la ruta del turno activo si existe`() = runTest {
        val dummyTurno = TurnoEntity(
            id = "turno-act-1",
            fechaApertura = 1726500000000L,
            estado = "ABIERTO",
            usuarioId = "user-1"
        )
        val rep = RepartidorEntity("rep-99", "#01", "Chofer A Eliminar", "Moto 01", "Ruta A")
        coEvery { repartidorDao.getRepartidorById("rep-99") } returns rep
        coEvery { turnoDao.getTurnoActivoSync() } returns dummyTurno

        viewModel.eliminarRepartidor("rep-99")
        testDispatcher.scheduler.advanceUntilIdle()

        coVerify(exactly = 1) { repartidorDao.deleteRepartidorById("rep-99") }
        coVerify(exactly = 1) { rutaRepartidorDao.deleteRutaById("turno-act-1_rep-99") }
        coVerify(exactly = 1) { rutaRepartidorDao.deleteRutasByRepartidorNombre("turno-act-1", "Chofer A Eliminar") }
    }

    @Test
    fun `guardarPrecioProducto para kilo completo actualiza precioKilo en Room`() = runTest {
        viewModel.guardarPrecioProducto("1", 26.50)
        testDispatcher.scheduler.advanceUntilIdle()

        coVerify(exactly = 1) {
            configuracionProduccionDao.insertOrUpdateConfiguracion(match {
                it.precioKilo == 26.50
            })
        }
    }

    @Test
    fun `guardarPrecioProducto para medio paquete actualiza precioMedioPaquete en Room`() = runTest {
        viewModel.guardarPrecioYPesoProducto("2", 12.00, 400)
        testDispatcher.scheduler.advanceUntilIdle()

        coVerify(exactly = 1) {
            configuracionProduccionDao.insertOrUpdateConfiguracion(match {
                it.precioMedioPaquete == 12.00 && it.pesoMedioPaqueteGramos == 400
            })
        }
    }

    @Test
    fun `guardarPrecioProducto para paquete mostrador actualiza precioPaquete en Room`() = runTest {
        viewModel.guardarPrecioYPesoProducto("3", 22.00, 800)
        testDispatcher.scheduler.advanceUntilIdle()

        coVerify(exactly = 1) {
            configuracionProduccionDao.insertOrUpdateConfiguracion(match {
                it.precioPaquete == 22.00 && it.pesoPaqueteGramos == 800
            })
        }
    }

    @Test
    fun `guardarPrecioRepartidor actualiza precioPaqueteRepartidor mayoreo en Room`() = runTest {
        viewModel.guardarPrecioRepartidor(17.50)
        testDispatcher.scheduler.advanceUntilIdle()

        coVerify(exactly = 1) {
            configuracionProduccionDao.insertOrUpdateConfiguracion(match {
                it.precioPaqueteRepartidor == 17.50
            })
        }
    }

    @Test
    fun `guardarPrecioProducto con id 4 actualiza precioPaqueteRepartidor mayoreo en Room`() = runTest {
        viewModel.guardarPrecioProducto("4", 18.50)
        testDispatcher.scheduler.advanceUntilIdle()

        coVerify(exactly = 1) {
            configuracionProduccionDao.insertOrUpdateConfiguracion(match {
                it.precioPaqueteRepartidor == 18.50
            })
        }
    }

    @Test
    fun `guardarPrecioYPesoProducto con id 4 actualiza precio y tamano de paquete en gramos en Room`() = runTest {
        viewModel.guardarPrecioYPesoProducto("4", 17.00, 850)
        testDispatcher.scheduler.advanceUntilIdle()

        coVerify(exactly = 1) {
            configuracionProduccionDao.insertOrUpdateConfiguracion(match {
                it.precioPaqueteRepartidor == 17.00 && it.pesoPaqueteGramos == 850
            })
        }
    }

    @Test
    fun `crearUsuario exitoso inserta en Room con hash y salt seguros`() = runTest {
        coEvery { usuarioDao.getUsuarioByUsername("nuevoUser") } returns null

        var fueExitoso = false
        viewModel.crearUsuario("nuevoUser", "pass1234", "EMPLEADO") { ok, _ ->
            fueExitoso = ok
        }
        testDispatcher.scheduler.advanceUntilIdle()

        assertTrue(fueExitoso)
        coVerify(exactly = 1) {
            usuarioDao.insertUsuario(match {
                it.username == "nuevoUser" && it.rol == "EMPLEADO" && it.salt.isNotBlank() && it.passwordHash.isNotBlank()
            })
        }
    }

    @Test
    fun `crearUsuario falla si username ya existe`() = runTest {
        val existing = UsuarioEntity("u1", "admin1", "hash", "salt", "ADMIN")
        coEvery { usuarioDao.getUsuarioByUsername("admin1") } returns existing

        var fueExitoso = true
        var errorMensaje: String? = null
        viewModel.crearUsuario("admin1", "pass1234", "ADMIN") { ok, err ->
            fueExitoso = ok
            errorMensaje = err
        }
        testDispatcher.scheduler.advanceUntilIdle()

        assertFalse(fueExitoso)
        assertTrue(errorMensaje?.contains("ya existe") == true)
        coVerify(exactly = 0) { usuarioDao.insertUsuario(any()) }
    }

    @Test
    fun `eliminarUsuario impide borrar la cuenta principal admin1`() = runTest {
        val adminUser = UsuarioEntity("1", "admin1", "hash", "salt", "ADMIN")
        coEvery { usuarioDao.getUsuarioById("1") } returns adminUser
        coEvery { usuarioDao.countAdmins() } returns 2

        var fueExitoso = true
        var errorMensaje: String? = null
        viewModel.eliminarUsuario("1") { ok, err ->
            fueExitoso = ok
            errorMensaje = err
        }
        testDispatcher.scheduler.advanceUntilIdle()

        assertFalse(fueExitoso)
        assertTrue(errorMensaje?.contains("admin1") == true && errorMensaje?.contains("protegida") == true)
        coVerify(exactly = 0) { usuarioDao.deleteUsuarioById("1") }
    }

    @Test
    fun `eliminarUsuario de cuenta admin secundaria impide borrar si es el unico ADMIN`() = runTest {
        val adminSecundario = UsuarioEntity("u2", "admin2", "hash", "salt", "ADMIN")
        coEvery { usuarioDao.getUsuarioById("u2") } returns adminSecundario
        coEvery { usuarioDao.countAdmins() } returns 1

        var fueExitoso = true
        var errorMensaje: String? = null
        viewModel.eliminarUsuario("u2") { ok, err ->
            fueExitoso = ok
            errorMensaje = err
        }
        testDispatcher.scheduler.advanceUntilIdle()

        assertFalse(fueExitoso)
        assertTrue(errorMensaje?.contains("No se puede eliminar al único Administrador") == true)
        coVerify(exactly = 0) { usuarioDao.deleteUsuarioById("u2") }
    }

    @Test
    fun `eliminarUsuario permite borrar usuario EMPLEADO`() = runTest {
        val empUser = UsuarioEntity("u2", "cajero1", "hash", "salt", "EMPLEADO")
        coEvery { usuarioDao.getUsuarioById("u2") } returns empUser

        var fueExitoso = false
        viewModel.eliminarUsuario("u2") { ok, _ ->
            fueExitoso = ok
        }
        testDispatcher.scheduler.advanceUntilIdle()

        assertTrue(fueExitoso)
        coVerify(exactly = 1) { usuarioDao.deleteUsuarioById("u2") }
    }

    @Test
    fun `editarUsuario en admin1 no permite renombrar la cuenta`() = runTest {
        val adminUser = UsuarioEntity("1", "admin1", "hash", "salt", "ADMIN")
        coEvery { usuarioDao.getUsuarioById("1") } returns adminUser

        var fueExitoso = true
        var errorMensaje: String? = null
        viewModel.editarUsuario("1", "nuevoAdmin", "ADMIN", null) { ok, err ->
            fueExitoso = ok
            errorMensaje = err
        }
        testDispatcher.scheduler.advanceUntilIdle()

        assertFalse(fueExitoso)
        assertTrue(errorMensaje?.contains("no puede ser renombrada") == true)
        coVerify(exactly = 0) { usuarioDao.insertUsuario(match { it.username == "nuevoAdmin" }) }
    }

    @Test
    fun `editarUsuario en admin1 no permite cambiar rol de ADMIN a EMPLEADO`() = runTest {
        val adminUser = UsuarioEntity("1", "admin1", "hash", "salt", "ADMIN")
        coEvery { usuarioDao.getUsuarioById("1") } returns adminUser

        var fueExitoso = true
        var errorMensaje: String? = null
        viewModel.editarUsuario("1", "admin1", "EMPLEADO", null) { ok, err ->
            fueExitoso = ok
            errorMensaje = err
        }
        testDispatcher.scheduler.advanceUntilIdle()

        assertFalse(fueExitoso)
        assertTrue(errorMensaje?.contains("Administrador") == true)
        coVerify(exactly = 0) { usuarioDao.insertUsuario(match { it.rol == "EMPLEADO" }) }
    }

    @Test
    fun `verificarCredencialesAdmin1 valida contrasena correcta con PBKDF2`() = runTest {
        val salt = CryptoManager.generateSalt()
        val hash = CryptoManager.hashPassword("admin123", salt)
        val adminUser = UsuarioEntity("1", "admin1", hash, salt, "ADMIN")
        coEvery { usuarioDao.getUsuarioByUsername("admin1") } returns adminUser

        val esValido = viewModel.verificarCredencialesAdmin1("admin1", "admin123")
        assertTrue(esValido)

        val esInvalidoPass = viewModel.verificarCredencialesAdmin1("admin1", "wrongPass")
        assertFalse(esInvalidoPass)

        val esInvalidoUser = viewModel.verificarCredencialesAdmin1("otroUser", "admin123")
        assertFalse(esInvalidoUser)
    }
}

