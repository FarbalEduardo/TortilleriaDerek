package com.example.tortilleriaderek.ui.screens

import android.widget.Toast
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.tortilleriaderek.presentation.produccion.ProduccionUiEffect
import com.example.tortilleriaderek.presentation.produccion.ProduccionUiEvent
import com.example.tortilleriaderek.presentation.produccion.ProduccionUiState
import com.example.tortilleriaderek.presentation.produccion.ProduccionViewModel
import com.example.tortilleriaderek.ui.components.TortilleriaNavBar
import com.example.tortilleriaderek.ui.components.produccion.*
import com.example.tortilleriaderek.ui.theme.*

/**
 * Pantalla de Producción modularizada conforme a la Constitución (Artículo III).
 * Descompuesta en subcomponentes stateless en `ui/components/produccion/`.
 */
@Composable
fun ProduccionScreen(
    viewModel: ProduccionViewModel = hiltViewModel(),
    onNavigateToVenta: () -> Unit = {},
    onNavigateToProduccion: () -> Unit = {},
    onNavigateToMetricas: () -> Unit = {},
    onNavigateToConfiguracion: () -> Unit = {},
    onVerHistorialProduccion: () -> Unit = {},
    modifier: Modifier = Modifier
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    val context = LocalContext.current

    LaunchedEffect(Unit) {
        viewModel.effect.collect { effect ->
            when (effect) {
                is ProduccionUiEffect.ShowToast -> {
                    Toast.makeText(context, effect.mensaje, Toast.LENGTH_SHORT).show()
                }
                is ProduccionUiEffect.ShowError -> {
                    Toast.makeText(context, effect.error, Toast.LENGTH_LONG).show()
                }
                ProduccionUiEffect.NavigateToHistorial -> {
                    onVerHistorialProduccion()
                }
            }
        }
    }

    ProduccionScreenContent(
        uiState = uiState,
        onEvent = viewModel::onEvent,
        onNavigateToVenta = onNavigateToVenta,
        onNavigateToProduccion = onNavigateToProduccion,
        onNavigateToMetricas = onNavigateToMetricas,
        onNavigateToConfiguracion = onNavigateToConfiguracion,
        onVerHistorialProduccion = onVerHistorialProduccion,
        modifier = modifier
    )
}

@Composable
fun ProduccionScreenContent(
    uiState: ProduccionUiState,
    onEvent: (ProduccionUiEvent) -> Unit,
    onNavigateToVenta: () -> Unit = {},
    onNavigateToProduccion: () -> Unit = {},
    onNavigateToMetricas: () -> Unit = {},
    onNavigateToConfiguracion: () -> Unit = {},
    onVerHistorialProduccion: () -> Unit = {},
    modifier: Modifier = Modifier
) {
    Scaffold(
        modifier = modifier.fillMaxSize(),
        containerColor = SurfaceWarm,
        contentWindowInsets = WindowInsets.navigationBars,
        bottomBar = {
            TortilleriaNavBar(
                activeItem = "Producción",
                onNavigateToVenta = onNavigateToVenta,
                onNavigateToProduccion = onNavigateToProduccion,
                onNavigateToMetricas = onNavigateToMetricas,
                onNavigateToConfiguracion = onNavigateToConfiguracion
            )
        }
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .background(SurfaceWarm)
                .verticalScroll(rememberScrollState())
                .padding(horizontal = 16.dp, vertical = 12.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            // ── 1. HERO CARD NARANJA SUPERIOR ────────────────────────────────
            ProduccionHeroCard(
                fechaTurnoTexto = uiState.fechaTurnoTexto,
                isTurnoCerrado = uiState.isTurnoCerrado,
                estadoTurnoTexto = uiState.estadoTurnoTexto,
                disponibleEnTienda = uiState.disponibleEnTienda,
                porcentajeVenta = uiState.porcentajeVenta,
                producidoNeto = uiState.producidoNeto,
                totalVendidoKg = uiState.totalVendidoKg,
                mermaTurnoKg = uiState.mermaTurnoKg
            )

            // ── 2. SECCIÓN: REGISTRO DE NUEVA TANDA (STEPPER) ────────────────
            CardRegistroTandaStepper(
                pesoBultoHarinaKg = uiState.pesoBultoHarinaKg,
                rendimientoPorBulto = uiState.rendimientoPorBulto,
                bultosHarinaInput = uiState.bultosHarinaInput,
                masaCrudaCalculada = uiState.masaCrudaCalculada,
                tortillaCalculada = uiState.tortillaCalculada,
                onIncrementar = { onEvent(ProduccionUiEvent.OnIncrementarBultos) },
                onDecrementar = { onEvent(ProduccionUiEvent.OnDecrementarBultos) }
            )

            // ── 3. BOTONES DE ACCIÓN ──────────────────────────────────────────
            ProduccionActionButtons(
                onRegistrarTandaClick = { onEvent(ProduccionUiEvent.OnRegistrarTandaClick) },
                onRegistrarMermaClick = { onEvent(ProduccionUiEvent.OnOpenRegistrarMermaDialog) },
                onVerHistorialClick = onVerHistorialProduccion
            )

            Spacer(modifier = Modifier.height(10.dp))
        }

        // ── 4. DIÁLOGO PARA REGISTRAR MERMA DIRECTA (Caso 6) ──────────────────
        if (uiState.showRegistrarMermaDialog) {
            DialogRegistrarMerma(
                mermaKgInput = uiState.mermaKgInput,
                motivoMermaInput = uiState.motivoMermaInput,
                disponibleEnTienda = uiState.disponibleEnTienda,
                errorMessage = uiState.errorMessage,
                onMermaKgChange = { onEvent(ProduccionUiEvent.OnMermaKgInputChanged(it)) },
                onMotivoChange = { onEvent(ProduccionUiEvent.OnMotivoMermaInputChanged(it)) },
                onConfirmar = { onEvent(ProduccionUiEvent.OnConfirmarRegistroMerma) },
                onDismiss = { onEvent(ProduccionUiEvent.OnDismissRegistrarMermaDialog) }
            )
        }
    }
}

@Preview(showBackground = true, name = "Teléfono Compacto")
@Preview(showBackground = true, name = "Tablet Landscape (Producción)", widthDp = 840, heightDp = 480)
@Composable
fun ProduccionScreenPreview() {
    TortilleriaDerekTheme {
        ProduccionScreenContent(
            uiState = ProduccionUiState(
                producidoNeto = 154.0,
                totalVendidoKg = 139.5,
                disponibleEnTienda = 14.5,
                porcentajeVenta = 90.5
            ),
            onEvent = {}
        )
    }
}
