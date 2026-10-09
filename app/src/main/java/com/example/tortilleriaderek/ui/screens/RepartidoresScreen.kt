package com.example.tortilleriaderek.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.example.tortilleriaderek.data.local.entity.RutaRepartidorEntity
import com.example.tortilleriaderek.presentation.venta.VentaUiState
import com.example.tortilleriaderek.ui.components.DialogLiquidarRepartidor
import com.example.tortilleriaderek.ui.components.DialogSalidaRepartidor
import com.example.tortilleriaderek.ui.components.HeroSalesCard
import com.example.tortilleriaderek.ui.components.MostradorRepartidoresTabBar
import com.example.tortilleriaderek.ui.components.repartidores.DialogDetalleLiquidacion
import com.example.tortilleriaderek.ui.components.repartidores.RepartidoresSubContent
import com.example.tortilleriaderek.ui.theme.*

/**
 * Pantalla de Control de Repartidores modularizada conforme a la Constitución (Artículo III).
 * Descompuesta en subcomponentes stateless en `ui/components/repartidores/`.
 */
@Composable
fun RepartidoresScreen(
    uiState: VentaUiState = VentaUiState(),
    onSwitchToMostrador: () -> Unit = {},
    onScopeSelected: (String) -> Unit = {},
    onCerrarTurnoClick: () -> Unit = {},
    onOpenSalida: (RutaRepartidorEntity) -> Unit = {},
    onOpenLiquidar: (RutaRepartidorEntity) -> Unit = {},
    onConfirmarSalida: (Double) -> Unit = { _ -> },
    onConfirmarLiquidar: (Double, Double) -> Unit = { _, _ -> },
    onConfirmarSalidaConPaquetes: ((Double, Int) -> Unit)? = null,
    onConfirmarLiquidarConPaquetes: ((Double, Double, Int) -> Unit)? = null,
    onDismissSalida: () -> Unit = {},
    onDismissLiquidar: () -> Unit = {},
    onNavigateToHistorial: () -> Unit = {},
    modifier: Modifier = Modifier
) {
    var detalleLiquidadoModal by remember { mutableStateOf<RutaRepartidorEntity?>(null) }

    Column(
        modifier = modifier
            .fillMaxSize()
            .background(SurfaceWarm)
            .padding(horizontal = 16.dp)
            .padding(top = 12.dp),
        verticalArrangement = Arrangement.spacedBy(14.dp)
    ) {
        // ── 1. Hero Card Naranja ─────────────────────────────────────────────
        HeroSalesCard(
            fechaTexto = uiState.fechaTurnoTexto,
            totalAmount = uiState.totalMostradoEnHeader,
            currentScope = uiState.selectedScope,
            onScopeSelected = onScopeSelected,
            onCerrarTurnoClick = onCerrarTurnoClick
        )

        // ── 2. Selector Segmentado: Mostrador / Repartidores ─────────────────
        MostradorRepartidoresTabBar(
            selectedTab = 1,
            onMostradorSelected = onSwitchToMostrador,
            onRepartidoresSelected = { /* ya en esta pantalla */ }
        )

        // ── 3. Contenido de Repartidores ─────────────────────────────────────
        RepartidoresSubContent(
            uiState = uiState,
            onOpenSalida = onOpenSalida,
            onOpenLiquidar = onOpenLiquidar,
            onShowDetalleLiquidado = { detalleLiquidadoModal = it },
            onNavigateToHistorial = onNavigateToHistorial,
            modifier = Modifier.weight(1f)
        )
    }

    // ── Diálogo: Ingresar Datos de Salida ────────────────────────────────────
    uiState.repartidorSeleccionadoParaSalida?.let { driver ->
        DialogSalidaRepartidor(
            driver = driver,
            onConfirmarSalida = onConfirmarSalida,
            onDismiss = onDismissSalida,
            precioPaqueteRepartidor = uiState.precioPaqueteRepartidor,
            pesoPaqueteRepartidorKg = uiState.pesoPaqueteRepartidorKg,
            onConfirmarSalidaConPaquetes = onConfirmarSalidaConPaquetes
        )
    }

    // ── Diálogo: Registrar Devolución y Liquidar ─────────────────────────────
    uiState.repartidorSeleccionadoParaLiquidar?.let { driver ->
        DialogLiquidarRepartidor(
            driver = driver,
            onConfirmarLiquidar = onConfirmarLiquidar,
            onDismiss = onDismissLiquidar,
            precioPaqueteRepartidor = uiState.precioPaqueteRepartidor,
            pesoPaqueteRepartidorKg = uiState.pesoPaqueteRepartidorKg,
            onConfirmarLiquidarConPaquetes = onConfirmarLiquidarConPaquetes
        )
    }

    // ── Diálogo: Detalle de Liquidación ──────────────────────────────────────
    detalleLiquidadoModal?.let { driver ->
        DialogDetalleLiquidacion(
            driver = driver,
            onDismiss = { detalleLiquidadoModal = null }
        )
    }
}

@Preview(showBackground = true, showSystemUi = true)
@Composable
fun PreviewRepartidoresScreen() {
    TortilleriaDerekTheme {
        RepartidoresScreen()
    }
}
