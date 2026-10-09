package com.example.tortilleriaderek.presentation.backup

import com.example.tortilleriaderek.data.local.dao.UsuarioDao
import com.example.tortilleriaderek.data.local.entity.UsuarioEntity
import com.example.tortilleriaderek.domain.model.BackupError
import com.example.tortilleriaderek.domain.model.BackupExportInfo
import com.example.tortilleriaderek.domain.model.BatteryStatusProvider
import com.example.tortilleriaderek.domain.model.DbInspectionResult
import com.example.tortilleriaderek.domain.usecase.BackupUseCase
import io.mockk.coEvery
import io.mockk.every
import io.mockk.mockk
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.first
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

/**
 * Creado por 🏆 quality-pm-expert.
 * Pruebas unitarias para BackupViewModel verificando flujos de batería, autenticación admin,
 * advertencia destructiva, micro-lección y reinicio.
 */
@OptIn(ExperimentalCoroutinesApi::class)
class BackupViewModelTest {

    private val testDispatcher = StandardTestDispatcher()
    private lateinit var backupUseCase: BackupUseCase
    private lateinit var batteryStatusProvider: BatteryStatusProvider
    private lateinit var usuarioDao: UsuarioDao
    private lateinit var viewModel: BackupViewModel

    private val adminEntity = UsuarioEntity(
        id = "1",
        username = "admin1",
        passwordHash = "hash123",
        salt = "salt123",
        rol = "ADMIN"
    )

    @Before
    fun setUp() {
        Dispatchers.setMain(testDispatcher)
        backupUseCase = mockk()
        batteryStatusProvider = mockk()
        usuarioDao = mockk()

        every { backupUseCase.verificarBateriaSegura() } returns Result.success(Unit)
        coEvery { backupUseCase.verificarTurnoActivoLocal() } returns Result.success(Unit)
        every { batteryStatusProvider.obtenerNivelBateria() } returns 85

        viewModel = BackupViewModel(backupUseCase, batteryStatusProvider, usuarioDao)
    }

    @After
    fun tearDown() {
        Dispatchers.resetMain()
    }

    @Test
    fun `cuando bateria es insuficiente, IniciarExportacion activa mostrarDialogoBateria`() = runTest {
        every { backupUseCase.verificarBateriaSegura() } returns Result.failure(BackupError.BateriaBaja(18))
        every { batteryStatusProvider.obtenerNivelBateria() } returns 18

        viewModel.onIntent(BackupUiIntent.IniciarExportacion(esAdminLogueado = false))
        advanceUntilIdle()

        val state = viewModel.uiState.value
        assertTrue(state.mostrarDialogoBateria)
        assertEquals(18, state.nivelBateriaActual)
    }

    @Test
    fun `cuando no hay admin logueado, IniciarExportacion abre dialogo de autenticacion`() = runTest {
        viewModel.onIntent(BackupUiIntent.IniciarExportacion(esAdminLogueado = false))
        advanceUntilIdle()

        val state = viewModel.uiState.value
        assertTrue(state.mostrarDialogoAutenticacionAdmin)
        assertEquals(OperacionBackup.EXPORTAR, state.operacionPendiente)
    }

    @Test
    fun `cuando admin esta logueado, IniciarExportacion dispara selector guardar directamente`() = runTest {
        coEvery { usuarioDao.getUsuarioByUsername("admin1") } returns adminEntity

        viewModel.onIntent(BackupUiIntent.IniciarExportacion(esAdminLogueado = true, adminUsername = "admin1"))
        advanceUntilIdle()

        val effect = viewModel.uiEffect.first()
        assertTrue(effect is BackupUiEffect.AbrirSelectorGuardar)
    }

    @Test
    fun `autenticacion exitosa de admin para exportar emite AbrirSelectorGuardar`() = runTest {
        viewModel.onIntent(BackupUiIntent.IniciarExportacion(esAdminLogueado = false))
        advanceUntilIdle()

        coEvery { backupUseCase.autenticarAdmin("admin1", "12345") } returns Result.success(adminEntity)

        viewModel.onIntent(BackupUiIntent.OnAutenticarAdmin("admin1", "12345"))
        advanceUntilIdle()

        val state = viewModel.uiState.value
        assertFalse(state.mostrarDialogoAutenticacionAdmin)
        assertEquals("hash123", state.adminAutenticadoHash)

        val effect = viewModel.uiEffect.first()
        assertTrue(effect is BackupUiEffect.AbrirSelectorGuardar)
    }

    @Test
    fun `autenticacion fallida de admin actualiza errorAutenticacionAdmin en estado`() = runTest {
        coEvery {
            backupUseCase.autenticarAdmin("admin1", "wrong")
        } returns Result.failure(BackupError.CredencialesInvalidas)

        viewModel.onIntent(BackupUiIntent.OnAutenticarAdmin("admin1", "wrong"))
        advanceUntilIdle()

        val state = viewModel.uiState.value
        assertEquals("Usuario o contraseña de Administrador incorrectos.", state.errorAutenticacionAdmin)
    }

    @Test
    fun `ejecutar exportacion exitosa actualiza ultimoArchivoExportadoUri y emite toast`() = runTest {
        val mockInfo = BackupExportInfo(
            uriString = "content://saf/backup.db",
            sha256 = "sha123",
            tamanioBytes = 102400L,
            nombreArchivo = "tortilleria.db",
            totalTurnosCerrados = 5,
            totalVentas = 45
        )
        coEvery { backupUseCase.exportarBaseDeDatos("content://saf/backup.db") } returns Result.success(mockInfo)

        viewModel.onIntent(BackupUiIntent.OnUriDestinoSeleccionadaExportar("content://saf/backup.db"))
        advanceUntilIdle()

        val state = viewModel.uiState.value
        assertEquals("content://saf/backup.db", state.ultimoArchivoExportadoUri)

        val effect = viewModel.uiEffect.first()
        assertTrue(effect is BackupUiEffect.ShowToast)
    }

    @Test
    fun `inspeccionar archivo con version incompatible activa microleccion animada`() = runTest {
        coEvery {
            backupUseCase.inspeccionarArchivoImportacion(any())
        } returns Result.failure(BackupError.VersionIncompatible(versionEncontrada = 4, versionEsperada = 8))

        viewModel.onIntent(BackupUiIntent.OnUriOrigenSeleccionadaImportar("content://saf/v4.db"))
        advanceUntilIdle()

        val state = viewModel.uiState.value
        assertTrue(state.mostrarDialogoMicroLeccion)
        assertEquals(4, state.versionEncontradaMicroLeccion)
        assertEquals(8, state.versionEsperadaMicroLeccion)
    }

    @Test
    fun `inspeccionar archivo compatible activa dialogo de advertencia de reemplazo`() = runTest {
        val inspection = DbInspectionResult(userVersion = 8, esCompatible = true, totalVentas = 50)
        coEvery {
            backupUseCase.inspeccionarArchivoImportacion(any())
        } returns Result.success(inspection)

        viewModel.onIntent(BackupUiIntent.OnUriOrigenSeleccionadaImportar("content://saf/v8.db"))
        advanceUntilIdle()

        val state = viewModel.uiState.value
        assertTrue(state.mostrarDialogoAdvertenciaReemplazo)
        assertEquals("content://saf/v8.db", state.uriPendienteImportacion)
        assertEquals(inspection, state.inspectionResult)
    }

    @Test
    fun `confirmar importacion exitosa activa dialogo de reinicio de aplicacion`() = runTest {
        // Preparamos estado con archivo pendiente
        val inspection = DbInspectionResult(userVersion = 8, esCompatible = true)
        coEvery { backupUseCase.inspeccionarArchivoImportacion(any()) } returns Result.success(inspection)
        viewModel.onIntent(BackupUiIntent.OnUriOrigenSeleccionadaImportar("content://saf/v8.db"))
        advanceUntilIdle()

        coEvery { backupUseCase.restaurarBaseDeDatos(any(), any()) } returns Result.success(Unit)

        viewModel.onIntent(BackupUiIntent.OnConfirmarImportacionDestructiva)
        advanceUntilIdle()

        val state = viewModel.uiState.value
        assertFalse(state.mostrarDialogoAdvertenciaReemplazo)
        assertTrue(state.mostrarDialogoReinicioExitoso)
    }
}
