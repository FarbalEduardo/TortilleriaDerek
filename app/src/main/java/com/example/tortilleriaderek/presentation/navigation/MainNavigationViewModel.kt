package com.example.tortilleriaderek.presentation.navigation

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.tortilleriaderek.domain.usecase.VerificarTurnoActivoUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch
import javax.inject.Inject

sealed interface NavigationStartState {
    object Loading : NavigationStartState
    data class Ready(val startDestination: Screen) : NavigationStartState
}

/**
 * Creado por 🏗️ mobile-developer.
 * Router de decisión de inicio para determinar la ruta inicial
 * evaluando de forma reactiva y asíncrona si hay un turno abierto en Room (US8).
 */
@HiltViewModel
class MainNavigationViewModel @Inject constructor(
    private val verificarTurnoActivoUseCase: VerificarTurnoActivoUseCase
) : ViewModel() {

    private val _navigationState = MutableStateFlow<NavigationStartState>(NavigationStartState.Loading)
    val navigationState: StateFlow<NavigationStartState> = _navigationState.asStateFlow()

    init {
        observarRutaInicial()
    }

    private fun observarRutaInicial() {
        viewModelScope.launch {
            verificarTurnoActivoUseCase.observar().collectLatest { hayTurnoActivo ->
                val destinoInicial = if (hayTurnoActivo) {
                    Screen.Mostrador
                } else {
                    Screen.Login
                }
                _navigationState.value = NavigationStartState.Ready(destinoInicial)
            }
        }
    }
}
