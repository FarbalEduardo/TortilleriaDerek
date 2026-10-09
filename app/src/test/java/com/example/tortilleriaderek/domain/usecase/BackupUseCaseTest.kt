package com.example.tortilleriaderek.domain.usecase

import com.example.tortilleriaderek.data.local.dao.TurnoDao
import com.example.tortilleriaderek.data.local.dao.UsuarioDao
import com.example.tortilleriaderek.data.local.entity.TurnoEntity
import com.example.tortilleriaderek.data.local.entity.UsuarioEntity
import com.example.tortilleriaderek.data.security.CryptoManager
import com.example.tortilleriaderek.domain.model.BackupError
import com.example.tortilleriaderek.domain.model.BackupExportInfo
import com.example.tortilleriaderek.domain.model.BatteryStatusProvider
import com.example.tortilleriaderek.domain.model.DbInspectionResult
import com.example.tortilleriaderek.domain.model.PoliticaPasswordAdmin
import com.example.tortilleriaderek.domain.repository.BackupStoragePort
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.every
import io.mockk.mockk
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test

/**
 * Creado por 🏆 quality-pm-expert y 🛡️ security-expert.
 * Batería de pruebas unitarias para BackupUseCase cubriendo requerimientos de seguridad,
 * blindaje energético, turnos cerrados, roles admin y versionado SQLite.
 */
class BackupUseCaseTest {

    private lateinit var turnoDao: TurnoDao
    private lateinit var usuarioDao: UsuarioDao
    private lateinit var batteryStatusProvider: BatteryStatusProvider
    private lateinit var backupStoragePort: BackupStoragePort
    private lateinit var backupUseCase: BackupUseCase

    private val adminSalt = CryptoManager.generateSalt()
    private val adminHash = CryptoManager.hashPassword("12345", adminSalt)
    private val adminEntity = UsuarioEntity(
        id = "1",
        username = "admin1",
        passwordHash = adminHash,
        salt = adminSalt,
        rol = "ADMIN"
    )

    private val operadorSalt = CryptoManager.generateSalt()
    private val operadorHash = CryptoManager.hashPassword("12345", operadorSalt)
    private val operadorEntity = UsuarioEntity(
        id = "2",
        username = "cajero1",
        passwordHash = operadorHash,
        salt = operadorSalt,
        rol = "OPERADOR"
    )

    @Before
    fun setUp() {
        turnoDao = mockk()
        usuarioDao = mockk()
        batteryStatusProvider = mockk()
        backupStoragePort = mockk()

        backupUseCase = BackupUseCase(
            turnoDao = turnoDao,
            usuarioDao = usuarioDao,
            batteryStatusProvider = batteryStatusProvider,
            backupStoragePort = backupStoragePort
        )

        // Por defecto: Batería suficiente (80%, no cargando), sin turno activo
        every { batteryStatusProvider.obtenerNivelBateria() } returns 80
        every { batteryStatusProvider.estaCargando() } returns false
        coEvery { turnoDao.getTurnoActivoSync() } returns null
    }

    // --- REGLA: Blindaje Energético de Batería (≥ 25% o Cargador Conectado) ---

    @Test
    fun `verificarBateriaSegura cuando bateria es menor a 25 y no esta cargando, retorna error BateriaBaja`() {
        every { batteryStatusProvider.obtenerNivelBateria() } returns 24
        every { batteryStatusProvider.estaCargando() } returns false

        val result = backupUseCase.verificarBateriaSegura()

        assertTrue(result.isFailure)
        val error = result.exceptionOrNull()
        assertTrue(error is BackupError.BateriaBaja)
        assertEquals(24, (error as BackupError.BateriaBaja).nivelActual)
    }

    @Test
    fun `verificarBateriaSegura cuando bateria es menor a 25 pero esta cargando, retorna success`() {
        every { batteryStatusProvider.obtenerNivelBateria() } returns 15
        every { batteryStatusProvider.estaCargando() } returns true

        val result = backupUseCase.verificarBateriaSegura()

        assertTrue(result.isSuccess)
    }

    @Test
    fun `verificarBateriaSegura cuando bateria es exactamente 25 y no esta cargando, retorna success`() {
        every { batteryStatusProvider.obtenerNivelBateria() } returns 25
        every { batteryStatusProvider.estaCargando() } returns false

        val result = backupUseCase.verificarBateriaSegura()

        assertTrue(result.isSuccess)
    }

    // --- REGLA: Autenticación de Administrador Principal ---

    @Test
    fun `autenticarAdmin con campos vacios, retorna error CredencialesInvalidas`() = runTest {
        val result = backupUseCase.autenticarAdmin("", "12345")

        assertTrue(result.isFailure)
        assertTrue(result.exceptionOrNull() is BackupError.CredencialesInvalidas)
    }

    @Test
    fun `autenticarAdmin con usuario inexistente, retorna error CredencialesInvalidas`() = runTest {
        coEvery { usuarioDao.getUsuarioByUsername("fantasma") } returns null

        val result = backupUseCase.autenticarAdmin("fantasma", "12345")

        assertTrue(result.isFailure)
        assertTrue(result.exceptionOrNull() is BackupError.CredencialesInvalidas)
    }

    @Test
    fun `autenticarAdmin con usuario existente pero rol no es ADMIN, retorna error NoEsAdmin`() = runTest {
        coEvery { usuarioDao.getUsuarioByUsername("cajero1") } returns operadorEntity

        val result = backupUseCase.autenticarAdmin("cajero1", "12345")

        assertTrue(result.isFailure)
        assertTrue(result.exceptionOrNull() is BackupError.NoEsAdmin)
    }

    @Test
    fun `autenticarAdmin con PIN incorrecto, retorna error CredencialesInvalidas`() = runTest {
        coEvery { usuarioDao.getUsuarioByUsername("admin1") } returns adminEntity

        val result = backupUseCase.autenticarAdmin("admin1", "pinErroneo")

        assertTrue(result.isFailure)
        assertTrue(result.exceptionOrNull() is BackupError.CredencialesInvalidas)
    }

    @Test
    fun `autenticarAdmin con credenciales correctas de ADMIN, retorna UsuarioEntity`() = runTest {
        coEvery { usuarioDao.getUsuarioByUsername("admin1") } returns adminEntity

        val result = backupUseCase.autenticarAdmin("admin1", "12345")

        assertTrue(result.isSuccess)
        assertEquals(adminEntity, result.getOrNull())
    }

    // --- REGLA: Blindaje de Turnos Cerrados ---

    @Test
    fun `verificarTurnoActivoLocal cuando hay un turno ABIERTO, retorna error TurnoActivoLocal`() = runTest {
        val turnoAbierto = TurnoEntity(
            id = "t1",
            usuarioId = "1",
            fechaApertura = System.currentTimeMillis(),
            estado = "ABIERTO"
        )
        coEvery { turnoDao.getTurnoActivoSync() } returns turnoAbierto

        val result = backupUseCase.verificarTurnoActivoLocal()

        assertTrue(result.isFailure)
        assertTrue(result.exceptionOrNull() is BackupError.TurnoActivoLocal)
    }

    @Test
    fun `verificarTurnoActivoLocal cuando el turno esta CERRADO o no existe, retorna success`() = runTest {
        coEvery { turnoDao.getTurnoActivoSync() } returns null

        val result = backupUseCase.verificarTurnoActivoLocal()

        assertTrue(result.isSuccess)
    }

    // --- FLUJO: Validación en Cascada para Exportación ---

    @Test
    fun `validarPrecondicionesExportacion cuando bateria es baja, se detiene y retorna error BateriaBaja`() = runTest {
        every { batteryStatusProvider.obtenerNivelBateria() } returns 10
        every { batteryStatusProvider.estaCargando() } returns false

        val result = backupUseCase.validarPrecondicionesExportacion("admin1", "12345")

        assertTrue(result.isFailure)
        assertTrue(result.exceptionOrNull() is BackupError.BateriaBaja)
        coVerify(exactly = 0) { usuarioDao.getUsuarioByUsername(any()) }
    }

    @Test
    fun `validarPrecondicionesExportacion exitoso cuando cumple bateria, credenciales admin y sin turnos abiertos`() = runTest {
        coEvery { usuarioDao.getUsuarioByUsername("admin1") } returns adminEntity

        val result = backupUseCase.validarPrecondicionesExportacion("admin1", "12345")

        assertTrue(result.isSuccess)
        assertEquals(adminEntity, result.getOrNull())
    }

    // --- FLUJO: Exportar Base de Datos ---

    @Test
    fun `exportarBaseDeDatos ejecuta exitosamente la llamada al puerto de almacenamiento`() = runTest {
        val mockExportInfo = BackupExportInfo(
            uriString = "content://saf/backup.db",
            sha256 = "abc123sha",
            tamanioBytes = 512000L,
            nombreArchivo = "tortilleria_backup_20261003.db",
            totalTurnosCerrados = 12,
            totalVentas = 150
        )
        coEvery { backupStoragePort.exportarDbHaciaDestino(any()) } returns Result.success(mockExportInfo)

        val result = backupUseCase.exportarBaseDeDatos("content://saf/backup.db")

        assertTrue(result.isSuccess)
        assertEquals(mockExportInfo, result.getOrNull())
        coVerify(exactly = 1) { backupStoragePort.exportarDbHaciaDestino("content://saf/backup.db") }
    }

    // --- FLUJO: Inspección de Archivo Entrante y Versión Incompatible ---

    @Test
    fun `inspeccionarArchivoImportacion con version SQLite menor o mayor a la esperada, retorna VersionIncompatible`() = runTest {
        coEvery { usuarioDao.getUsuarioByUsername("admin1") } returns adminEntity
        val inspectionV5 = DbInspectionResult(
            userVersion = 5,
            versionEsperada = 8,
            esCompatible = false,
            totalVentas = 80
        )
        coEvery { backupStoragePort.inspeccionarDbDesdeOrigen(any()) } returns Result.success(inspectionV5)

        val result = backupUseCase.inspeccionarArchivoImportacion(
            uriString = "content://saf/origen.db",
            usernameAdmin = "admin1",
            pinAdmin = "12345"
        )

        assertTrue(result.isFailure)
        val error = result.exceptionOrNull()
        assertTrue(error is BackupError.VersionIncompatible)
        assertEquals(5, (error as BackupError.VersionIncompatible).versionEncontrada)
        assertEquals(8, error.versionEsperada)
    }

    @Test
    fun `inspeccionarArchivoImportacion con version 7 u 8 compatible, retorna DbInspectionResult exitoso`() = runTest {
        coEvery { usuarioDao.getUsuarioByUsername("admin1") } returns adminEntity
        val inspectionV8 = DbInspectionResult(
            userVersion = 8,
            versionEsperada = 8,
            esCompatible = true,
            totalVentas = 120,
            totalTurnosCerrados = 8,
            totalUsuarios = 2
        )
        coEvery { backupStoragePort.inspeccionarDbDesdeOrigen(any()) } returns Result.success(inspectionV8)

        val result = backupUseCase.inspeccionarArchivoImportacion(
            uriString = "content://saf/origen.db",
            usernameAdmin = "admin1",
            pinAdmin = "12345"
        )

        assertTrue(result.isSuccess)
        assertEquals(inspectionV8, result.getOrNull())
    }

    @Test
    fun `inspeccionarArchivoImportacion con version 7 proveniente de respaldo anterior, retorna DbInspectionResult compatible para migracion`() = runTest {
        coEvery { usuarioDao.getUsuarioByUsername("admin1") } returns adminEntity
        val inspectionV7 = DbInspectionResult(
            userVersion = 7,
            versionEsperada = 8,
            esCompatible = true,
            totalVentas = 50,
            totalTurnosCerrados = 4,
            totalUsuarios = 1
        )
        coEvery { backupStoragePort.inspeccionarDbDesdeOrigen(any()) } returns Result.success(inspectionV7)

        val result = backupUseCase.inspeccionarArchivoImportacion(
            uriString = "content://saf/origen_v7.db",
            usernameAdmin = "admin1",
            pinAdmin = "12345"
        )

        assertTrue(result.isSuccess)
        assertEquals(inspectionV7, result.getOrNull())
    }

    @Test
    fun `inspeccionarArchivoImportacion sobrecarga limpia sin credenciales valida bateria y turnos e inspecciona con exito`() = runTest {
        val inspectionV8 = DbInspectionResult(
            userVersion = 8,
            versionEsperada = 8,
            esCompatible = true
        )
        coEvery { backupStoragePort.inspeccionarDbDesdeOrigen(any()) } returns Result.success(inspectionV8)

        val result = backupUseCase.inspeccionarArchivoImportacion("content://saf/origen_limpio.db")

        assertTrue(result.isSuccess)
        assertEquals(inspectionV8, result.getOrNull())
    }

    // --- FLUJO: Restaurar Base de Datos con Políticas de Contraseña ---

    @Test
    fun `restaurarBaseDeDatos con Politica MantenerActual delega al puerto de almacenamiento`() = runTest {
        val politica = PoliticaPasswordAdmin.MantenerActual(hashActual = adminHash, saltActual = adminSalt)
        coEvery { backupStoragePort.restaurarDbDesdeOrigen(any(), any()) } returns Result.success(Unit)

        val result = backupUseCase.restaurarBaseDeDatos("content://saf/origen.db", politica)

        assertTrue(result.isSuccess)
        coVerify(exactly = 1) { backupStoragePort.restaurarDbDesdeOrigen("content://saf/origen.db", politica) }
    }

    @Test
    fun `restaurarBaseDeDatos con Politica UsarDelRespaldo delega al puerto de almacenamiento`() = runTest {
        val politica = PoliticaPasswordAdmin.UsarDelRespaldo
        coEvery { backupStoragePort.restaurarDbDesdeOrigen(any(), any()) } returns Result.success(Unit)

        val result = backupUseCase.restaurarBaseDeDatos("content://saf/origen.db", politica)

        assertTrue(result.isSuccess)
        coVerify(exactly = 1) { backupStoragePort.restaurarDbDesdeOrigen("content://saf/origen.db", politica) }
    }

    @Test
    fun `restaurarBaseDeDatos con Politica EstablecerNueva delega al puerto de almacenamiento`() = runTest {
        val politica = PoliticaPasswordAdmin.EstablecerNueva("9999")
        coEvery { backupStoragePort.restaurarDbDesdeOrigen(any(), any()) } returns Result.success(Unit)

        val result = backupUseCase.restaurarBaseDeDatos("content://saf/origen.db", politica)

        assertTrue(result.isSuccess)
        coVerify(exactly = 1) { backupStoragePort.restaurarDbDesdeOrigen("content://saf/origen.db", politica) }
    }

    @Test
    fun `restaurarBaseDeDatos cuando el archivo esta corrupto, propaga fallo retornado por el puerto`() = runTest {
        val politica = PoliticaPasswordAdmin.UsarDelRespaldo
        coEvery {
            backupStoragePort.restaurarDbDesdeOrigen(any(), any())
        } returns Result.failure(BackupError.ArchivoInvalido)

        val result = backupUseCase.restaurarBaseDeDatos("content://saf/corrupto.db", politica)

        assertTrue(result.isFailure)
        assertTrue(result.exceptionOrNull() is BackupError.ArchivoInvalido)
    }
}
