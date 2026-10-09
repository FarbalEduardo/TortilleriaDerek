package com.example.tortilleriaderek.ui.screens

import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import com.example.tortilleriaderek.presentation.login.*
import com.example.tortilleriaderek.ui.theme.TortilleriaDerekTheme

/**
 * Pantalla de Login conectada al componente oficial LoginScreenContent.
 * Garantiza que la renderización en Android Studio (Preview) coincida 100%
 * con la aplicación compilada en el dispositivo.
 */
@Composable
fun LoginScreen(
    onLoginSuccess: () -> Unit = {},
    onNavigateToSettings: () -> Unit = {},
    modifier: Modifier = Modifier
) {
    var state by remember {
        mutableStateOf(
            LoginUiState(
                pinInput = "1234",
                selectedMode = LoginMode.ABRIR_TURNO
            )
        )
    }

    LoginScreenContent(
        uiState = state,
        onEvent = { event ->
            when (event) {
                is LoginUiEvent.OnModeSelected -> state = state.copy(selectedMode = event.mode)
                is LoginUiEvent.OnCambiarMetodoAcceso -> state = state.copy(metodoAcceso = event.metodo)
                is LoginUiEvent.OnDigitoPresionado -> state = state.copy(pinInput = state.pinInput + event.digito)
                LoginUiEvent.OnBorrarDigito -> state = state.copy(pinInput = state.pinInput.dropLast(1))
                is LoginUiEvent.OnPatronNodoSeleccionado -> state = state.copy(patronInput = state.patronInput + event.nodo)
                LoginUiEvent.OnLimpiarPatron -> state = state.copy(patronInput = "")
                LoginUiEvent.OnIniciarSesionClick -> onLoginSuccess()
                LoginUiEvent.OnSettingsClick -> onNavigateToSettings()
                else -> Unit
            }
        },
        modifier = modifier
    )
}

@Preview(showBackground = true, showSystemUi = true, name = "Login - Móvil Oficial")
@Composable
fun LoginScreenPreview() {
    TortilleriaDerekTheme {
        LoginScreenContent(
            uiState = LoginUiState(
                pinInput = "1234",
                selectedMode = LoginMode.ABRIR_TURNO
            ),
            onEvent = {}
        )
    }
}
