package com.example.tortilleriaderek.presentation.produccion

import androidx.compose.runtime.Immutable
import com.example.tortilleriaderek.data.local.entity.TurnoEntity

/**
 * Contrato de Orquestación MVI para la pantalla de Producción.
 * Define UiState inmutable, UiEvent para acciones del usuario y UiEffect para efectos secundarios.
 */

@Immutable
data class ProduccionUiState(
    val isLoading: Boolean = false,
    val turnoActivo: TurnoEntity? = null,
    val usuarioActivoNombre: String = "",
    val fechaTurnoTexto: String = "Hoy",
    val estadoTurnoTexto: String = "En Producción Activa",
    val isTurnoCerrado: Boolean = false,
    // Parámetros de receta configurables
    val pesoBultoHarinaKg: Double = 0.0,
    val kgMasaPorBulto: Double = 0.0,
    val rendimientoPorBulto: Double = 0.0,
    val mermaToleradaPorBulto: Double = 0.0,
    val pesoPaqueteKg: Double = 0.0,
    val pesoMedioPaqueteKg: Double = 0.0,
    // Stepper y cálculo de tanda (inicia en ceros)
    val bultosHarinaInput: Int = 0,
    val masaCrudaCalculada: Double = 0.0,
    val tortillaCalculada: Double = 0.0,
    // Balance del turno (Caso 1: inician en 0.0)
    val producidoNeto: Double = 0.0,
    val totalVendidoKg: Double = 0.0,
    val totalVendidoMostradorKg: Double = 0.0,
    val totalVendidoRepartoKg: Double = 0.0,
    val mermaTurnoKg: Double = 0.0,
    val disponibleEnTienda: Double = 0.0,
    val porcentajeVenta: Double = 0.0,
    // Diálogo de registro rápido de merma en Producción (Caso 6)
    val showRegistrarMermaDialog: Boolean = false,
    val mermaKgInput: String = "",
    val motivoMermaInput: String = "Tortilla fría / rotura",
    val errorMessage: String? = null
)

sealed interface ProduccionUiEvent {
    object OnIncrementarBultos : ProduccionUiEvent
    object OnDecrementarBultos : ProduccionUiEvent
    data class OnBultosChanged(val bultos: Int) : ProduccionUiEvent
    object OnRegistrarTandaClick : ProduccionUiEvent
    object OnOpenRegistrarMermaDialog : ProduccionUiEvent
    object OnDismissRegistrarMermaDialog : ProduccionUiEvent
    data class OnMermaKgInputChanged(val valor: String) : ProduccionUiEvent
    data class OnMotivoMermaInputChanged(val motivo: String) : ProduccionUiEvent
    object OnConfirmarRegistroMerma : ProduccionUiEvent
    object OnVerHistorialClick : ProduccionUiEvent
}

sealed interface ProduccionUiEffect {
    data class ShowToast(val mensaje: String) : ProduccionUiEffect
    data class ShowError(val error: String) : ProduccionUiEffect
    object NavigateToHistorial : ProduccionUiEffect
}
