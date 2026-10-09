package com.example.tortilleriaderek.presentation.login

import androidx.compose.runtime.Immutable

/**
 * Creado por 🏗️ mobile-developer y 🎨 design-ui-expert.
 * Define el contrato MVI (State, Event, Effect) para la pantalla de Login.
 */

enum class LoginMode {
    ABRIR_TURNO,
    SOLO_CONSULTA
}

@Immutable
data class LoginUiState(
    val isLoading: Boolean = false,
    val usernameInput: String = "",
    val passwordInput: String = "",
    val selectedMode: LoginMode = LoginMode.ABRIR_TURNO,
    val isPasswordVisible: Boolean = false,
    val isTurnoCerrado: Boolean = true,
    val errorMessage: String? = null
)

sealed interface LoginUiEvent {
    data class OnUsernameChanged(val username: String) : LoginUiEvent
    data class OnPasswordChanged(val password: String) : LoginUiEvent
    data class OnModeSelected(val mode: LoginMode) : LoginUiEvent
    object OnTogglePasswordVisibility : LoginUiEvent
    object OnIniciarSesionClick : LoginUiEvent
    object OnAbrirTurnoClick : LoginUiEvent
    object OnSoloConsultaClick : LoginUiEvent
    object OnSettingsClick : LoginUiEvent
    object OnBackupClick : LoginUiEvent
}

sealed interface LoginUiEffect {
    data class ShowError(val message: String) : LoginUiEffect
    object NavigateToMostrador : LoginUiEffect
    object NavigateToMetricas : LoginUiEffect
    object NavigateToSettings : LoginUiEffect
}
