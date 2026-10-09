package com.example.tortilleriaderek.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.tortilleriaderek.presentation.historial.HistorialFiltroTipo
import com.example.tortilleriaderek.presentation.historial.HistorialItemUi
import com.example.tortilleriaderek.presentation.historial.HistorialUiEvent
import com.example.tortilleriaderek.presentation.historial.HistorialUiState
import com.example.tortilleriaderek.presentation.historial.HistorialViewModel
import com.example.tortilleriaderek.ui.components.historial.*
import com.example.tortilleriaderek.ui.theme.*

/**
 * Pantalla de Historial de Ventas modularizada conforme a la Constitución (Artículo III).
 * Descompuesta en subcomponentes stateless en `ui/components/historial/`.
 */
@Composable
fun HistorialScreen(
    filtroInicial: String = "TODAS",
    viewModel: HistorialViewModel = hiltViewModel(),
    onBack: () -> Unit = {},
    onNavigateToProduccion: () -> Unit = {},
    onNavigateToMetricas: () -> Unit = {},
    onNavigateToConfiguracion: () -> Unit = {},
    modifier: Modifier = Modifier
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()

    LaunchedEffect(filtroInicial) {
        val tipo = when (filtroInicial.uppercase()) {
            "MOSTRADOR" -> HistorialFiltroTipo.MOSTRADOR
            "REPARTIDOR" -> HistorialFiltroTipo.REPARTIDOR
            else -> HistorialFiltroTipo.TODAS
        }
        viewModel.onEvent(HistorialUiEvent.SetFiltroInicial(tipo))
    }

    HistorialScreenContent(
        uiState = uiState,
        onEvent = viewModel::onEvent,
        onBack = onBack,
        onNavigateToProduccion = onNavigateToProduccion,
        onNavigateToMetricas = onNavigateToMetricas,
        onNavigateToConfiguracion = onNavigateToConfiguracion,
        modifier = modifier
    )
}

@Composable
fun HistorialScreenContent(
    uiState: HistorialUiState,
    onEvent: (HistorialUiEvent) -> Unit,
    onBack: () -> Unit = {},
    onNavigateToProduccion: () -> Unit = {},
    onNavigateToMetricas: () -> Unit = {},
    onNavigateToConfiguracion: () -> Unit = {},
    modifier: Modifier = Modifier
) {
    Scaffold(
        modifier = modifier.fillMaxSize(),
        containerColor = SurfaceWarm,
        contentWindowInsets = WindowInsets.navigationBars
    ) { innerPadding ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .padding(horizontal = 16.dp),
            contentPadding = PaddingValues(top = 12.dp, bottom = 20.dp),
            verticalArrangement = Arrangement.spacedBy(14.dp)
        ) {
            // ── 1. Top Header con botón volver circular y fecha del turno ──────
            item {
                HistorialTopHeader(
                    fechaTexto = uiState.fechaTurnoTexto,
                    isTurnoCerrado = uiState.isTurnoCerrado,
                    onBack = onBack
                )
            }

            // ── 2. Card de Resumen de Recaudación ──────────────────────────────
            item {
                val tituloRecaudacion = when (uiState.filtroSeleccionado) {
                    HistorialFiltroTipo.MOSTRADOR -> "TOTAL RECAUDADO EN MOSTRADOR"
                    HistorialFiltroTipo.REPARTIDOR -> "TOTAL RECAUDADO EN REPARTO"
                    HistorialFiltroTipo.TODAS -> "TOTAL RECAUDADO EN TURNO"
                }

                CardResumenRecaudacion(
                    tituloRecaudacion = tituloRecaudacion,
                    montoTotal = uiState.totalMostradoEnHeader,
                    countVentas = uiState.numeroVentasMostradoEnHeader
                )
            }

            // ── 3. Chips de Filtro (Orden dinámico según origen) ──────────────
            item {
                HistorialFilterChips(
                    chipsOrden = uiState.chipsOrden,
                    filtroSeleccionado = uiState.filtroSeleccionado,
                    transaccionesTotalCount = uiState.transaccionesTotalCount,
                    transaccionesMostradorCount = uiState.transaccionesMostradorCount,
                    transaccionesRepartidorCount = uiState.transaccionesRepartidorCount,
                    onSelectFiltro = { onEvent(HistorialUiEvent.SelectFiltro(it)) }
                )
            }

            // ── 4. Cabecera de Lista de Transacciones ─────────────────────────
            item {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        Text(
                            text = "Transacciones",
                            fontSize = 15.sp,
                            fontWeight = FontWeight.Bold,
                            color = TextPrimary
                        )
                        Box(
                            modifier = Modifier
                                .size(7.dp)
                                .clip(CircleShape)
                                .background(MaizPrimary)
                        )
                    }

                    Text(
                        text = "Orden: Más reciente",
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Medium,
                        color = TextSecondary
                    )
                }
            }

            // ── 5. Items de Transacción o Estado Vacío ────────────────────────
            if (uiState.transaccionesFiltradas.isEmpty()) {
                item {
                    HistorialEmptyState()
                }
            } else {
                items(
                    items = uiState.transaccionesFiltradas,
                    key = { it.id }
                ) { tx ->
                    TransactionCardItem(
                        tx = tx,
                        onDeleteClick = if (!uiState.isTurnoCerrado) {
                            { onEvent(HistorialUiEvent.SolicitarEliminarVenta(tx)) }
                        } else null
                    )
                }
            }
        }
    }

    // Modal de confirmación para eliminar venta
    uiState.transaccionAEliminar?.let { tx ->
        DialogConfirmarEliminarVenta(
            tx = tx,
            onConfirmar = { onEvent(HistorialUiEvent.ConfirmarEliminarVenta) },
            onCancelar = { onEvent(HistorialUiEvent.CancelarEliminarVenta) }
        )
    }
}

@Preview(showBackground = true, name = "Teléfono Compacto")
@Preview(showBackground = true, name = "Tablet Landscape (Historial)", widthDp = 840, heightDp = 480)
@Composable
fun PreviewHistorialScreen() {
    TortilleriaDerekTheme {
        HistorialScreenContent(
            uiState = HistorialUiState(
                fechaTurnoTexto = "Hoy, Lunes 7 de Septiembre",
                totalVentasTurno = 18450.00,
                numeroVentasTotal = 143,
                transaccionesTotalCount = 147,
                transaccionesMostradorCount = 142,
                transaccionesRepartidorCount = 5,
                transaccionesFiltradas = listOf(
                    HistorialItemUi(
                        id = "1",
                        ticketFolio = "M-0142",
                        tipo = "MOSTRADOR",
                        fechaTimestamp = System.currentTimeMillis(),
                        fechaTexto = "Hoy",
                        hora = "11:42 AM",
                        monto = 48.00,
                        detalleProductos = "2 Kilo de Tortilla",
                        usuarioNombre = "admin1"
                    ),
                    HistorialItemUi(
                        id = "2",
                        ticketFolio = "R-0038",
                        tipo = "REPARTIDOR",
                        fechaTimestamp = System.currentTimeMillis(),
                        fechaTexto = "Hoy",
                        hora = "10:30 AM",
                        monto = 1870.00,
                        detalleProductos = "Moto 01 (Carlos Ruiz) - Liquidación",
                        usuarioNombre = "admin1",
                        nombreRepartidor = "Carlos Ruiz",
                        motoAsignada = "Moto 01",
                        nombreRuta = "Ruta San Juan"
                    )
                )
            ),
            onEvent = {}
        )
    }
}
