package com.example.tortilleriaderek.ui.screens

import android.widget.Toast
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.material3.Scaffold
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.tortilleriaderek.data.local.entity.RutaRepartidorEntity
import com.example.tortilleriaderek.presentation.venta.VentaUiEffect
import com.example.tortilleriaderek.presentation.venta.VentaUiState
import com.example.tortilleriaderek.presentation.venta.VentaViewModel
import com.example.tortilleriaderek.ui.components.DialogLiquidarRepartidor
import com.example.tortilleriaderek.ui.components.DialogSalidaRepartidor
import com.example.tortilleriaderek.ui.components.HeroSalesCard
import com.example.tortilleriaderek.ui.components.MostradorRepartidoresTabBar
import com.example.tortilleriaderek.ui.components.TortilleriaNavBar
import com.example.tortilleriaderek.ui.components.mostrador.DialogCerrarTurno
import com.example.tortilleriaderek.ui.components.mostrador.DialogConfirmarVentaMostrador
import com.example.tortilleriaderek.ui.components.mostrador.MostradorSubContent
import com.example.tortilleriaderek.ui.components.repartidores.DialogDetalleLiquidacion
import com.example.tortilleriaderek.ui.components.repartidores.RepartidoresSubContent
import com.example.tortilleriaderek.ui.theme.SurfaceWarm
import com.example.tortilleriaderek.ui.theme.TortilleriaDerekTheme
import kotlinx.coroutines.launch

/**
 * Pantalla principal de Mostrador & Punto de Venta.
 * Ensamblada mediante subcomponentes modulares siguiendo la Constitución de Agentes.
 */
@Composable
fun MostradorRootScreen(
    viewModel: VentaViewModel = hiltViewModel(),
    onNavigateToProduccion: () -> Unit = {},
    onNavigateToMetricas: () -> Unit = {},
    onNavigateToConfiguracion: () -> Unit = {},
    onNavigateToHistorial: (String) -> Unit = {},
    onNavigateToLogin: () -> Unit = {},
    modifier: Modifier = Modifier
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    val context = LocalContext.current
    val coroutineScope = rememberCoroutineScope()

    // Pager para alternar únicamente el subcontenido inferior
    val pagerState = rememberPagerState(initialPage = 0, pageCount = { 2 })

    // Estados para diálogos de repartidores
    var detalleLiquidadoModal by remember { mutableStateOf<RutaRepartidorEntity?>(null) }

    LaunchedEffect(Unit) {
        viewModel.effect.collect { effect ->
            when (effect) {
                VentaUiEffect.NavigateToLogin -> onNavigateToLogin()
                VentaUiEffect.NavigateToHistorial -> onNavigateToHistorial("TODAS")
                is VentaUiEffect.ShowToast -> {
                    Toast.makeText(context, effect.mensaje, Toast.LENGTH_SHORT).show()
                }
            }
        }
    }

    Scaffold(
        modifier = modifier,
        containerColor = SurfaceWarm,
        contentWindowInsets = WindowInsets.navigationBars,
        bottomBar = {
            TortilleriaNavBar(
                activeItem = "Venta",
                onNavigateToVenta = { /* Ya estamos en venta */ },
                onNavigateToProduccion = onNavigateToProduccion,
                onNavigateToMetricas = onNavigateToMetricas,
                onNavigateToConfiguracion = onNavigateToConfiguracion
            )
        }
    ) { innerPadding ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .background(SurfaceWarm)
        ) {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(horizontal = 16.dp)
                    .padding(top = 12.dp),
                verticalArrangement = Arrangement.spacedBy(14.dp)
            ) {
                // ── 1. CABECERA FIJA: Hero Card ──────────────────────────────
                HeroSalesCard(
                    fechaTexto = uiState.fechaTurnoTexto,
                    totalAmount = uiState.totalMostradoEnHeader,
                    currentScope = uiState.selectedScope,
                    onScopeSelected = viewModel::onScopeSelected,
                    onCerrarTurnoClick = viewModel::onOpenCerrarTurnoDialog
                )

                // ── 2. BOTONES DE CHIP FIJOS: Mostrador / Repartidores ────────
                MostradorRepartidoresTabBar(
                    selectedTab = pagerState.currentPage,
                    onMostradorSelected = {
                        coroutineScope.launch { pagerState.animateScrollToPage(0) }
                    },
                    onRepartidoresSelected = {
                        coroutineScope.launch { pagerState.animateScrollToPage(1) }
                    }
                )

                // ── 3. HORIZONTAL PAGER: Subcontenido Dinámico ────────────────
                HorizontalPager(
                    state = pagerState,
                    modifier = Modifier
                        .fillMaxWidth()
                        .weight(1f)
                ) { page ->
                    when (page) {
                        0 -> MostradorSubContent(
                            uiState = uiState,
                            onQuantityChanged = viewModel::onQuantityChanged,
                            onOpenConfirmarVenta = viewModel::onOpenConfirmarVentaDialog,
                            onNavigateToHistorial = { onNavigateToHistorial("MOSTRADOR") }
                        )
                        else -> RepartidoresSubContent(
                            uiState = uiState,
                            onOpenSalida = viewModel::onOpenSalidaRepartidor,
                            onOpenLiquidar = viewModel::onOpenLiquidarRepartidor,
                            onShowDetalleLiquidado = { ruta ->
                                detalleLiquidadoModal = ruta
                            },
                            onCancelarSalida = viewModel::onCancelarSalidaRepartidor,
                            onNavigateToHistorial = { onNavigateToHistorial("REPARTIDOR") }
                        )
                    }
                }
            }
        }
    }

    // ── Diálogo: Confirmar Venta Mostrador ────────────────────────────────────
    if (uiState.showConfirmarVentaDialog) {
        DialogConfirmarVentaMostrador(
            proximoTicket = uiState.proximoTicketMostrador,
            subtotalOrden = uiState.subtotalOrdenActual,
            products = uiState.products,
            onConfirmar = viewModel::onConfirmarVenta,
            onDismiss = viewModel::onDismissConfirmarVentaDialog
        )
    }

    // ── Diálogo: Cerrar Turno ────────────────────────────────────────────────
    if (uiState.showCerrarTurnoDialog) {
        DialogCerrarTurno(
            tieneRutasActivas = uiState.rutasActivasCount > 0,
            rutasEnCalle = uiState.rutasRepartidores.filter { it.status == "EN_RUTA" },
            totalVentasDia = uiState.totalVentasDia,
            onConfirmar = viewModel::onConfirmarCerrarTurno,
            onDismiss = viewModel::onDismissCerrarTurnoDialog
        )
    }

    // ── Diálogo: Ingresar Datos de Salida Repartidor ──────────────────────────
    uiState.repartidorSeleccionadoParaSalida?.let { driver ->
        DialogSalidaRepartidor(
            driver = driver,
            onConfirmarSalida = viewModel::onConfirmarSalidaRepartidor,
            onDismiss = viewModel::onDismissSalidaRepartidor,
            precioPaqueteRepartidor = uiState.precioPaqueteRepartidor,
            pesoPaqueteRepartidorKg = uiState.pesoPaqueteRepartidorKg,
            onConfirmarSalidaConPaquetes = { kg, paq ->
                viewModel.onConfirmarSalidaRepartidor(kg, paq)
            }
        )
    }

    // ── Diálogo: Registrar Devolución y Liquidar Repartidor ───────────────────
    uiState.repartidorSeleccionadoParaLiquidar?.let { driver ->
        DialogLiquidarRepartidor(
            driver = driver,
            onConfirmarLiquidar = viewModel::onConfirmarLiquidarRepartidor,
            onDismiss = viewModel::onDismissLiquidarRepartidor,
            precioPaqueteRepartidor = uiState.precioPaqueteRepartidor,
            pesoPaqueteRepartidorKg = uiState.pesoPaqueteRepartidorKg,
            onConfirmarLiquidarConPaquetes = { devKg, cobrado, paqDev ->
                viewModel.onConfirmarLiquidarRepartidor(devKg, cobrado, paqDev)
            }
        )
    }

    // ── Diálogo: Detalle de Liquidación de Repartidor ─────────────────────────
    detalleLiquidadoModal?.let { driver ->
        DialogDetalleLiquidacion(
            driver = driver,
            onDismiss = { detalleLiquidadoModal = null }
        )
    }
}

@Preview(showBackground = true, name = "Teléfono Compacto")
@Preview(showBackground = true, name = "Tablet Landscape (Terminal POS)", widthDp = 840, heightDp = 480)
@Composable
fun PreviewMostradorRootScreen() {
    TortilleriaDerekTheme {
        MostradorSubContent(
            uiState = VentaUiState(),
            onQuantityChanged = { _, _ -> },
            onOpenConfirmarVenta = {},
            onNavigateToHistorial = {}
        )
    }
}
