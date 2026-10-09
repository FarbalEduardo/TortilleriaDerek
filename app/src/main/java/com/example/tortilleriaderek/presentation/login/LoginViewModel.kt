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
 * Creado por 🏗️ mobile-developer y 🛡️ security-expert.
 * ViewModel reactivo MVI para autenticación con PIN, Biometría y Master Key.
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
            is LoginUiEvent.OnUsernameChanged -> {
                _uiState.update { it.copy(usernameInput = event.username, errorMessage = null) }
            }
            is LoginUiEvent.OnPasswordChanged -> {
                _uiState.update { it.copy(passwordInput = event.password, errorMessage = null) }
            }
            is LoginUiEvent.OnDigitoPresionado -> {
                if (_uiState.value.estaBloqueado) return
                val currentPin = _uiState.value.pinInput
                if (currentPin.length < 6) {
                    val nuevoPin = currentPin + event.digito
                    _uiState.update { it.copy(pinInput = nuevoPin, errorMessage = null) }
                    // En POS si completa 4 dígitos y es la longitud típica, intentamos autenticar
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
            LoginUiEvent.OnBiometriaClick -> {
                viewModelScope.launch {
                    _effect.send(LoginUiEffect.IniciarBiometricPrompt)
                }
            }
            is LoginUiEvent.OnModeSelected -> {
                _uiState.update { it.copy(selectedMode = event.mode) }
            }
            LoginUiEvent.OnTogglePasswordVisibility -> {
                _uiState.update { it.copy(isPasswordVisible = !it.isPasswordVisible) }
            }
            LoginUiEvent.OnIniciarSesionClick -> {
                if (_uiState.value.pinInput.isNotBlank()) {
                    realizarLoginConPin(_uiState.value.pinInput)
                } else {
                    val abrirTurno = _uiState.value.selectedMode == LoginMode.ABRIR_TURNO
                    realizarLogin(abrirTurno = abrirTurno)
                }
            }
            LoginUiEvent.OnAbrirTurnoClick -> {
                realizarLogin(abrirTurno = true)
            }
            LoginUiEvent.OnSoloConsultaClick -> {
                realizarLogin(abrirTurno = false)
            }
            LoginUiEvent.OnSettingsClick -> {
                viewModelScope.launch {
                    _effect.send(LoginUiEffect.NavigateToSettings)
                }
            }
            LoginUiEvent.OnBackupClick -> {
                // Manejado a nivel de UI para desplegar selector de exportación/importación
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

            val result = loginPinUseCase.autenticarConPin(pin)

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

    private fun realizarLogin(abrirTurno: Boolean) {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true, errorMessage = null) }

            val result = loginUseCase(
                username = _uiState.value.usernameInput.trim(),
                passwordRaw = _uiState.value.passwordInput.trim()
            )

            _uiState.update { it.copy(isLoading = false) }

            result.fold(
                onSuccess = { usuario ->
                    navegarSegunModo(usuario.id, abrirTurno)
                },
                onFailure = { error ->
                    val mensaje = error.message ?: "Usuario o contraseña incorrectos"
                    _uiState.update { it.copy(errorMessage = mensaje) }
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

    private fun restablecerPinConMasterKey(claveMaestra: String, nuevoPin: String) {
        viewModelScope.launch {
            _uiState.update { it.copy(masterKeyCargando = true, masterKeyError = null) }
            val result = recuperarAccesoMasterKeyUseCase.restablecerPinAdmin(claveMaestra, nuevoPin)
            _uiState.update { it.copy(masterKeyCargando = false) }

            result.fold(
                onSuccess = {
                    _uiState.update {
                        it.copy(
                            showMasterKeyDialog = false,
                            pinInput = nuevoPin,
                            masterKeyError = null
                        )
                    }
                    _effect.send(LoginUiEffect.ShowToast("¡PIN restablecido con éxito! Ya puedes ingresar."))
                },
                onFailure = { error ->
                    _uiState.update {
                        it.copy(masterKeyError = error.message ?: "Clave Maestra de Rescate incorrecta")
                    }
                }
            )
        }
    }
}
