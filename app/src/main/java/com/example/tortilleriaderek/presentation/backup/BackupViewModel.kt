package com.example.tortilleriaderek.presentation.backup

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.tortilleriaderek.data.local.dao.UsuarioDao
import com.example.tortilleriaderek.domain.model.BackupError
import com.example.tortilleriaderek.domain.model.BatteryStatusProvider
import com.example.tortilleriaderek.domain.model.PoliticaPasswordAdmin
import com.example.tortilleriaderek.domain.usecase.BackupUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.receiveAsFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import javax.inject.Inject

/**
 * Creado por 🏗️ mobile-developer y 🛡️ security-expert.
 * ViewModel MVI para la orquestación visual y de negocio del Respaldo SQLite.
 */
@HiltViewModel
class BackupViewModel @Inject constructor(
    private val backupUseCase: BackupUseCase,
    private val batteryStatusProvider: BatteryStatusProvider,
    private val usuarioDao: UsuarioDao
) : ViewModel() {

    private val _uiState = MutableStateFlow(BackupUiState())
    val uiState: StateFlow<BackupUiState> = _uiState.asStateFlow()

    private val _uiEffect = Channel<BackupUiEffect>(Channel.BUFFERED)
    val uiEffect = _uiEffect.receiveAsFlow()

    fun onIntent(intent: BackupUiIntent) {
        when (intent) {
            is BackupUiIntent.IniciarExportacion -> handleIniciarExportacion(intent.esAdminLogueado, intent.adminUsername)
            is BackupUiIntent.IniciarImportacion -> handleIniciarImportacion(intent.esAdminLogueado, intent.adminUsername)
            is BackupUiIntent.OnAutenticarAdmin -> handleAutenticarAdmin(intent.username, intent.pin)
            is BackupUiIntent.OnCerrarDialogoAutenticacion -> {
                _uiState.update { it.copy(mostrarDialogoAutenticacionAdmin = false, errorAutenticacionAdmin = null) }
            }
            is BackupUiIntent.OnCerrarDialogoBateria -> {
                _uiState.update { it.copy(mostrarDialogoBateria = false) }
            }
            is BackupUiIntent.OnUriDestinoSeleccionadaExportar -> ejecutarExportacion(intent.uriString)
            is BackupUiIntent.OnUriOrigenSeleccionadaImportar -> inspeccionarArchivoParaImportar(intent.uriString)
            is BackupUiIntent.OnCambiarTipoPoliticaPassword -> {
                _uiState.update { it.copy(tipoPoliticaPassword = intent.tipo) }
            }
            is BackupUiIntent.OnCambiarNuevaPasswordInput -> {
                _uiState.update { it.copy(nuevaPasswordAdminInput = intent.pass) }
            }
            is BackupUiIntent.OnConfirmarImportacionDestructiva -> ejecutarImportacion()
            is BackupUiIntent.OnCancelarImportacionDestructiva -> {
                _uiState.update { it.copy(mostrarDialogoAdvertenciaReemplazo = false, uriPendienteImportacion = null) }
            }
            is BackupUiIntent.OnCambiarPasoMicroLeccion -> {
                _uiState.update { it.copy(pasoMicroLeccionActual = intent.paso) }
            }
            is BackupUiIntent.OnCerrarMicroLeccion -> {
                _uiState.update { it.copy(mostrarDialogoMicroLeccion = false, pasoMicroLeccionActual = 1) }
            }
            is BackupUiIntent.OnConfirmarReinicioApp -> {
                viewModelScope.launch {
                    _uiEffect.send(BackupUiEffect.ReiniciarApp)
                }
            }
            is BackupUiIntent.OnCompartirUltimoArchivo -> {
                _uiState.value.ultimoArchivoExportadoUri?.let { uri ->
                    viewModelScope.launch {
                        _uiEffect.send(BackupUiEffect.CompartirArchivo(uri))
                    }
                }
            }
        }
    }

    private fun handleIniciarExportacion(esAdminLogueado: Boolean, adminUsername: String?) {
        viewModelScope.launch {
            if (!validarBateriaYTurnos()) return@launch

            if (esAdminLogueado) {
                val admin = if (adminUsername != null) {
                    usuarioDao.getUsuarioByUsername(adminUsername)
                } else {
                    usuarioDao.getAllUsuariosSync().firstOrNull { it.rol == "ADMIN" }
                }
                if (admin != null && admin.rol == "ADMIN") {
                    _uiState.update {
                        it.copy(
                            adminAutenticadoHash = admin.passwordHash,
                            adminAutenticadoSalt = admin.salt,
                            operacionPendiente = OperacionBackup.EXPORTAR
                        )
                    }
                    dispararSelectorGuardar()
                    return@launch
                }
            }

            _uiState.update {
                it.copy(
                    mostrarDialogoAutenticacionAdmin = true,
                    operacionPendiente = OperacionBackup.EXPORTAR,
                    errorAutenticacionAdmin = null
                )
            }
        }
    }

    private fun handleIniciarImportacion(esAdminLogueado: Boolean, adminUsername: String?) {
        viewModelScope.launch {
            if (!validarBateriaYTurnos()) return@launch

            if (esAdminLogueado) {
                val admin = if (adminUsername != null) {
                    usuarioDao.getUsuarioByUsername(adminUsername)
                } else {
                    usuarioDao.getAllUsuariosSync().firstOrNull { it.rol == "ADMIN" }
                }
                if (admin != null && admin.rol == "ADMIN") {
                    _uiState.update {
                        it.copy(
                            adminAutenticadoHash = admin.passwordHash,
                            adminAutenticadoSalt = admin.salt,
                            operacionPendiente = OperacionBackup.IMPORTAR
                        )
                    }
                    _uiEffect.send(BackupUiEffect.AbrirSelectorLeer)
                    return@launch
                }
            }

            _uiState.update {
                it.copy(
                    mostrarDialogoAutenticacionAdmin = true,
                    operacionPendiente = OperacionBackup.IMPORTAR,
                    errorAutenticacionAdmin = null
                )
            }
        }
    }

    private suspend fun validarBateriaYTurnos(): Boolean {
        val batResult = backupUseCase.verificarBateriaSegura()
        if (batResult.isFailure) {
            val nivel = batteryStatusProvider.obtenerNivelBateria()
            _uiState.update { it.copy(mostrarDialogoBateria = true, nivelBateriaActual = nivel) }
            return false
        }

        val turnoResult = backupUseCase.verificarTurnoActivoLocal()
        if (turnoResult.isFailure) {
            _uiEffect.send(BackupUiEffect.ShowToast(turnoResult.exceptionOrNull()?.message ?: "Turno activo"))
            return false
        }

        return true
    }

    private fun handleAutenticarAdmin(username: String, pin: String) {
        viewModelScope.launch {
            val result = backupUseCase.autenticarAdmin(username, pin)
            result.onSuccess { admin ->
                _uiState.update {
                    it.copy(
                        mostrarDialogoAutenticacionAdmin = false,
                        adminAutenticadoHash = admin.passwordHash,
                        adminAutenticadoSalt = admin.salt,
                        errorAutenticacionAdmin = null
                    )
                }
                when (_uiState.value.operacionPendiente) {
                    OperacionBackup.EXPORTAR -> dispararSelectorGuardar()
                    OperacionBackup.IMPORTAR -> _uiEffect.send(BackupUiEffect.AbrirSelectorLeer)
                    null -> {}
                }
            }.onFailure { error ->
                _uiState.update { it.copy(errorAutenticacionAdmin = error.message) }
            }
        }
    }

    private suspend fun dispararSelectorGuardar() {
        val timestamp = SimpleDateFormat("yyyyMMdd_HHmm", Locale.getDefault()).format(Date())
        val nombreSugerido = "tortilleria_derek_$timestamp.db"
        _uiEffect.send(BackupUiEffect.AbrirSelectorGuardar(nombreSugerido))
    }

    private fun ejecutarExportacion(uriString: String) {
        viewModelScope.launch {
            _uiState.update {
                it.copy(mostrarOverlayCarga = true, faseCargaActual = FaseCargaDb.VOLCANDO_WAL)
            }
            val result = backupUseCase.exportarBaseDeDatos(uriString)
            _uiState.update { it.copy(mostrarOverlayCarga = false) }

            result.onSuccess { info ->
                val kb = info.tamanioBytes / 1024
                _uiState.update { it.copy(ultimoArchivoExportadoUri = info.uriString) }
                _uiEffect.send(
                    BackupUiEffect.ShowToast(
                        "Respaldo generado con éxito ($kb KB) • ${info.totalTurnosCerrados} turnos y ${info.totalVentas} ventas"
                    )
                )
            }.onFailure { error ->
                _uiEffect.send(BackupUiEffect.ShowToast(error.message ?: "Error al exportar base de datos"))
            }
        }
    }

    private fun inspeccionarArchivoParaImportar(uriString: String) {
        viewModelScope.launch {
            _uiState.update {
                it.copy(mostrarOverlayCarga = true, faseCargaActual = FaseCargaDb.VERIFICANDO_INTEGRIDAD)
            }
            val inspectionResult = backupUseCase.inspeccionarArchivoImportacion(uriString = uriString)
            _uiState.update { it.copy(mostrarOverlayCarga = false) }

            inspectionResult.onSuccess { inspection ->
                _uiState.update {
                    it.copy(
                        mostrarDialogoAdvertenciaReemplazo = true,
                        uriPendienteImportacion = uriString,
                        inspectionResult = inspection
                    )
                }
            }.onFailure { error ->
                if (error is BackupError.VersionIncompatible) {
                    _uiState.update {
                        it.copy(
                            mostrarDialogoMicroLeccion = true,
                            versionEncontradaMicroLeccion = error.versionEncontrada,
                            versionEsperadaMicroLeccion = error.versionEsperada,
                            pasoMicroLeccionActual = 1
                        )
                    }
                } else {
                    _uiEffect.send(BackupUiEffect.ShowToast(error.message ?: "Archivo no compatible"))
                }
            }
        }
    }

    private fun ejecutarImportacion() {
        val uri = _uiState.value.uriPendienteImportacion ?: return
        val tipo = _uiState.value.tipoPoliticaPassword

        val politica: PoliticaPasswordAdmin = when (tipo) {
            TipoPoliticaPassword.MANTENER_ACTUAL -> {
                val hash = _uiState.value.adminAutenticadoHash
                val salt = _uiState.value.adminAutenticadoSalt
                if (hash != null && salt != null) {
                    PoliticaPasswordAdmin.MantenerActual(hash, salt)
                } else {
                    PoliticaPasswordAdmin.UsarDelRespaldo
                }
            }
            TipoPoliticaPassword.USAR_RESPALDO -> PoliticaPasswordAdmin.UsarDelRespaldo
            TipoPoliticaPassword.ESTABLECER_NUEVA -> {
                val nuevaPass = _uiState.value.nuevaPasswordAdminInput.trim()
                if (nuevaPass.length < 4) {
                    viewModelScope.launch {
                        _uiEffect.send(BackupUiEffect.ShowToast("La contraseña debe tener al menos 4 caracteres"))
                    }
                    return
                }
                PoliticaPasswordAdmin.EstablecerNueva(nuevaPass)
            }
        }

        viewModelScope.launch {
            _uiState.update {
                it.copy(
                    mostrarDialogoAdvertenciaReemplazo = false,
                    mostrarOverlayCarga = true,
                    faseCargaActual = FaseCargaDb.ESCRIBIENDO_DB
                )
            }

            val result = backupUseCase.restaurarBaseDeDatos(uri, politica)
            _uiState.update { it.copy(mostrarOverlayCarga = false) }

            result.onSuccess {
                _uiState.update {
                    it.copy(
                        mostrarDialogoReinicioExitoso = true,
                        mensajeExitoReinicio = "La base de datos se transfirió correctamente. Se actualizaron turnos, ventas y producción. La aplicación debe reiniciarse para aplicar los cambios de sesión."
                    )
                }
            }.onFailure { error ->
                _uiEffect.send(BackupUiEffect.ShowToast("Error al restaurar: ${error.message}"))
            }
        }
    }
}
