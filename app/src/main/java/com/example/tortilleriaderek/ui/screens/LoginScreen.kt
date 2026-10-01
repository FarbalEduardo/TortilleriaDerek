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
                usernameInput = "Operador Derek",
                passwordInput = "1234",
                selectedMode = LoginMode.ABRIR_TURNO
            )
        )
    }

    LoginScreenContent(
        uiState = state,
        onEvent = { event ->
            when (event) {
                is LoginUiEvent.OnUsernameChanged -> state = state.copy(usernameInput = event.username)
                is LoginUiEvent.OnPasswordChanged -> state = state.copy(passwordInput = event.password)
                is LoginUiEvent.OnModeSelected -> state = state.copy(selectedMode = event.mode)
                LoginUiEvent.OnTogglePasswordVisibility -> state = state.copy(isPasswordVisible = !state.isPasswordVisible)
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
                usernameInput = "Operador Derek",
                passwordInput = "1234",
                selectedMode = LoginMode.ABRIR_TURNO
            ),
            onEvent = {}
        )
    }
}
