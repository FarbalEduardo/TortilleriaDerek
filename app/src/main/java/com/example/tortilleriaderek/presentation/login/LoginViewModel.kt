package com.example.tortilleriaderek.presentation.login

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.tortilleriaderek.domain.usecase.AbrirTurnoUseCase
import com.example.tortilleriaderek.domain.usecase.LoginUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.receiveAsFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class LoginViewModel @Inject constructor(
    private val loginUseCase: LoginUseCase,
    private val abrirTurnoUseCase: AbrirTurnoUseCase
) : ViewModel() {

    private val _uiState = MutableStateFlow(LoginUiState())
    val uiState: StateFlow<LoginUiState> = _uiState.asStateFlow()

    private val _effect = Channel<LoginUiEffect>()
    val effect = _effect.receiveAsFlow()

    fun onEvent(event: LoginUiEvent) {
        when (event) {
            is LoginUiEvent.OnUsernameChanged -> {
                _uiState.update { it.copy(usernameInput = event.username, errorMessage = null) }
            }
            is LoginUiEvent.OnPasswordChanged -> {
                _uiState.update { it.copy(passwordInput = event.password, errorMessage = null) }
            }
            is LoginUiEvent.OnModeSelected -> {
                _uiState.update { it.copy(selectedMode = event.mode) }
            }
            LoginUiEvent.OnTogglePasswordVisibility -> {
                _uiState.update { it.copy(isPasswordVisible = !it.isPasswordVisible) }
            }
            LoginUiEvent.OnIniciarSesionClick -> {
                val abrirTurno = _uiState.value.selectedMode == LoginMode.ABRIR_TURNO
                realizarLogin(abrirTurno = abrirTurno)
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
                    if (abrirTurno) {
                        abrirTurnoUseCase(usuarioId = usuario.id)
                        _effect.send(LoginUiEffect.NavigateToMostrador)
                    } else {
                        _effect.send(LoginUiEffect.NavigateToMetricas)
                    }
                },
                onFailure = { error ->
                    val mensaje = error.message ?: "Usuario o contraseña incorrectos"
                    _uiState.update { it.copy(errorMessage = mensaje) }
                    _effect.send(LoginUiEffect.ShowError(mensaje))
                }
            )
        }
    }
}
