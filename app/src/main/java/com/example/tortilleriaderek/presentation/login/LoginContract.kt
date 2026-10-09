package com.example.tortilleriaderek.presentation.login

import androidx.compose.runtime.Immutable

/**
 * Creado por 🏗️ mobile-developer y 🎨 design-ui-expert.
 * Define el contrato MVI (State, Event, Effect) para la pantalla de Login con soporte
 * exclusivo de Código Numérico (PIN), Patrón táctil (3x3), biometría y Clave Maestra.
 * Sin campo ni selección de usuario.
 */

enum class LoginMode {
    ABRIR_TURNO,
    SOLO_CONSULTA
}

enum class MetodoAcceso {
    CODIGO_PIN,
    PATRON
}

@Immutable
data class LoginUiState(
    val isLoading: Boolean = false,
    val pinInput: String = "",
    val patronInput: String = "",
    val metodoAcceso: MetodoAcceso = MetodoAcceso.CODIGO_PIN,
    val selectedMode: LoginMode = LoginMode.ABRIR_TURNO,
    val isTurnoCerrado: Boolean = true,
    val errorMessage: String? = null,
    val tieneBiometria: Boolean = true,
    val estaBloqueado: Boolean = false,
    val segundosRestantesBloqueo: Long = 0L,
    val showMasterKeyDialog: Boolean = false,
    val masterKeyError: String? = null,
    val masterKeyCargando: Boolean = false
)

sealed interface LoginUiEvent {
    data class OnDigitoPresionado(val digito: String) : LoginUiEvent
    object OnBorrarDigito : LoginUiEvent
    data class OnPatronNodoSeleccionado(val nodo: Int) : LoginUiEvent
    object OnLimpiarPatron : LoginUiEvent
    object OnConfirmarPatron : LoginUiEvent
    data class OnCambiarMetodoAcceso(val metodo: MetodoAcceso) : LoginUiEvent
    object OnBiometriaClick : LoginUiEvent
    data class OnModeSelected(val mode: LoginMode) : LoginUiEvent
    object OnIniciarSesionClick : LoginUiEvent
    object OnAbrirTurnoClick : LoginUiEvent
    object OnSoloConsultaClick : LoginUiEvent
    object OnSettingsClick : LoginUiEvent
    object OnBackupClick : LoginUiEvent
    object OnAbrirDialogoMasterKey : LoginUiEvent
    object OnCerrarDialogoMasterKey : LoginUiEvent
    data class OnRestablecerPinMasterKey(val masterKey: String, val nuevoPin: String) : LoginUiEvent
}

sealed interface LoginUiEffect {
    data class ShowError(val message: String) : LoginUiEffect
    data class ShowToast(val message: String) : LoginUiEffect
    object IniciarBiometricPrompt : LoginUiEffect
    object NavigateToMostrador : LoginUiEffect
    object NavigateToMetricas : LoginUiEffect
    object NavigateToSettings : LoginUiEffect
}
