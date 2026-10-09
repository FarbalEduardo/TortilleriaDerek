package com.example.tortilleriaderek.presentation.login

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.tortilleriaderek.domain.repository.AuthRepository
import com.example.tortilleriaderek.domain.usecase.AbrirTurnoUseCase
import com.example.tortilleriaderek.domain.usecase.LoginPinUseCase
import com.example.tortilleriaderek.domain.usecase.LoginUseCase
import com.example.tortilleriaderek.domain.usecase.RecuperarAccesoMasterKeyUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.receiveAsFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

/**
 * Creado por 🏗️ mobile-developer.
 * ViewModel MVI reactivo para Login con soporte exclusivo de Código Numérico (PIN),
 * Patrón táctil y biometría. Sin selección ni escritura de usuarios.
 */
@HiltViewModel
class LoginViewModel @Inject constructor(
    private val loginUseCase: LoginUseCase,
    private val loginPinUseCase: LoginPinUseCase,
    private val recuperarAccesoMasterKeyUseCase: RecuperarAccesoMasterKeyUseCase,
    private val abrirTurnoUseCase: AbrirTurnoUseCase,
    private val authRepository: AuthRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow(LoginUiState())
    val uiState: StateFlow<LoginUiState> = _uiState.asStateFlow()

    private val _effect = Channel<LoginUiEffect>()
    val effect = _effect.receiveAsFlow()

    init {
        observarSeguridadConfig()
    }

    private fun observarSeguridadConfig() {
        viewModelScope.launch {
            authRepository.getSeguridadConfig().collect { config ->
                _uiState.update {
                    it.copy(
                        tieneBiometria = config.biometriaHabilitada,
                        estaBloqueado = config.estaBloqueadoTemporalmente,
                        segundosRestantesBloqueo = config.segundosRestantesBloqueo
                    )
                }
            }
        }
    }

    fun onEvent(event: LoginUiEvent) {
        when (event) {
            is LoginUiEvent.OnCambiarMetodoAcceso -> {
                _uiState.update { it.copy(metodoAcceso = event.metodo, errorMessage = null) }
            }
            is LoginUiEvent.OnDigitoPresionado -> {
                if (_uiState.value.estaBloqueado) return
                val currentPin = _uiState.value.pinInput
                if (currentPin.length < 6) {
                    val nuevoPin = currentPin + event.digito
                    _uiState.update { it.copy(pinInput = nuevoPin, errorMessage = null) }
                    if (nuevoPin.length == 4) {
                        realizarLoginConPin(nuevoPin)
                    }
                }
            }
            LoginUiEvent.OnBorrarDigito -> {
                val currentPin = _uiState.value.pinInput
                if (currentPin.isNotEmpty()) {
                    _uiState.update { it.copy(pinInput = currentPin.dropLast(1), errorMessage = null) }
                }
            }
            is LoginUiEvent.OnPatronNodoSeleccionado -> {
                if (_uiState.value.estaBloqueado) return
                val actual = _uiState.value.patronInput
                if (!actual.contains(event.nodo.toString())) {
                    val nuevo = actual + event.nodo
                    _uiState.update { it.copy(patronInput = nuevo, errorMessage = null) }
                }
            }
            LoginUiEvent.OnLimpiarPatron -> {
                _uiState.update { it.copy(patronInput = "", errorMessage = null) }
            }
            LoginUiEvent.OnConfirmarPatron -> {
                if (_uiState.value.patronInput.length >= 4) {
                    realizarLoginConPatron(_uiState.value.patronInput)
                }
            }
            LoginUiEvent.OnBiometriaClick -> {
                viewModelScope.launch {
                    _effect.send(LoginUiEffect.IniciarBiometricPrompt)
                }
            }
            is LoginUiEvent.OnModeSelected -> {
                _uiState.update { it.copy(selectedMode = event.mode) }
            }
            LoginUiEvent.OnIniciarSesionClick -> {
                if (_uiState.value.metodoAcceso == MetodoAcceso.CODIGO_PIN && _uiState.value.pinInput.isNotBlank()) {
                    realizarLoginConPin(_uiState.value.pinInput)
                } else if (_uiState.value.metodoAcceso == MetodoAcceso.PATRON && _uiState.value.patronInput.isNotBlank()) {
                    realizarLoginConPatron(_uiState.value.patronInput)
                }
            }
            LoginUiEvent.OnAbrirTurnoClick -> {
                _uiState.update { it.copy(selectedMode = LoginMode.ABRIR_TURNO) }
                onEvent(LoginUiEvent.OnIniciarSesionClick)
            }
            LoginUiEvent.OnSoloConsultaClick -> {
                _uiState.update { it.copy(selectedMode = LoginMode.SOLO_CONSULTA) }
                onEvent(LoginUiEvent.OnIniciarSesionClick)
            }
            LoginUiEvent.OnSettingsClick -> {
                viewModelScope.launch {
                    _effect.send(LoginUiEffect.NavigateToSettings)
                }
            }
            LoginUiEvent.OnBackupClick -> {
                // Manejado a nivel de UI
            }
            LoginUiEvent.OnAbrirDialogoMasterKey -> {
                _uiState.update { it.copy(showMasterKeyDialog = true, masterKeyError = null) }
            }
            LoginUiEvent.OnCerrarDialogoMasterKey -> {
                _uiState.update { it.copy(showMasterKeyDialog = false, masterKeyError = null) }
            }
            is LoginUiEvent.OnRestablecerPinMasterKey -> {
                restablecerPinConMasterKey(event.masterKey, event.nuevoPin)
            }
        }
    }

    fun onBiometriaAutenticadaExitosamente() {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true, errorMessage = null) }
            val result = loginPinUseCase.autenticarConBiometria()
            _uiState.update { it.copy(isLoading = false) }

            result.fold(
                onSuccess = { usuario ->
                    navegarSegunModo(usuario.id)
                },
                onFailure = { error ->
                    val mensaje = error.message ?: "No se pudo autenticar con huella dactilar"
                    _uiState.update { it.copy(errorMessage = mensaje) }
                    _effect.send(LoginUiEffect.ShowError(mensaje))
                }
            )
        }
    }

    private fun realizarLoginConPin(pin: String) {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true, errorMessage = null) }

            val result = loginPinUseCase.autenticarConPin(pinRaw = pin)

            _uiState.update { it.copy(isLoading = false) }

            result.fold(
                onSuccess = { usuario ->
                    _uiState.update { it.copy(pinInput = "") }
                    navegarSegunModo(usuario.id)
                },
                onFailure = { error ->
                    val mensaje = error.message ?: "PIN incorrecto"
                    _uiState.update { it.copy(errorMessage = mensaje, pinInput = "") }
                    _effect.send(LoginUiEffect.ShowError(mensaje))
                }
            )
        }
    }

    private fun realizarLoginConPatron(patron: String) {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true, errorMessage = null) }

            val result = loginPinUseCase.autenticarConPatron(patronRaw = patron)

            _uiState.update { it.copy(isLoading = false) }

            result.fold(
                onSuccess = { usuario ->
                    _uiState.update { it.copy(patronInput = "") }
                    navegarSegunModo(usuario.id)
                },
                onFailure = { error ->
                    val mensaje = error.message ?: "Patrón incorrecto"
                    _uiState.update { it.copy(errorMessage = mensaje, patronInput = "") }
                    _effect.send(LoginUiEffect.ShowError(mensaje))
                }
            )
        }
    }

    private suspend fun navegarSegunModo(
        usuarioId: String,
        abrirTurno: Boolean = _uiState.value.selectedMode == LoginMode.ABRIR_TURNO
    ) {
        if (abrirTurno) {
            abrirTurnoUseCase(usuarioId = usuarioId)
            _effect.send(LoginUiEffect.NavigateToMostrador)
        } else {
            _effect.send(LoginUiEffect.NavigateToMetricas)
        }
    }

    private fun restablecerPinConMasterKey(masterKey: String, nuevoPin: String) {
        viewModelScope.launch {
            _uiState.update { it.copy(masterKeyCargando = true, masterKeyError = null) }

            val result = recuperarAccesoMasterKeyUseCase.restablecerPinAdmin(
                claveMaestraRaw = masterKey,
                nuevoPin = nuevoPin
            )

            _uiState.update { it.copy(masterKeyCargando = false) }

            result.fold(
                onSuccess = {
                    _uiState.update {
                        it.copy(
                            showMasterKeyDialog = false,
                            masterKeyError = null,
                            pinInput = ""
                        )
                    }
                    _effect.send(LoginUiEffect.ShowToast("PIN restablecido con éxito. Ya puedes ingresar."))
                },
                onFailure = { error ->
                    _uiState.update { it.copy(masterKeyError = error.message ?: "Error al restablecer PIN") }
                }
            )
        }
    }
}
