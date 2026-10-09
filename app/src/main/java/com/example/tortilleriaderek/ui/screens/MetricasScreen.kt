package com.example.tortilleriaderek.ui.screens

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.tortilleriaderek.presentation.metricas.MetricasUiEvent
import com.example.tortilleriaderek.presentation.metricas.MetricasUiState
import com.example.tortilleriaderek.presentation.metricas.MetricasViewModel
import com.example.tortilleriaderek.presentation.metricas.PuntoGraficaDia
import com.example.tortilleriaderek.ui.components.TortilleriaNavBar
import com.example.tortilleriaderek.ui.components.metricas.*
import com.example.tortilleriaderek.ui.theme.*
import java.time.LocalDate

/**
 * Pantalla de Métricas ("Productividad y Ventas") conectada a Room y MetricasViewModel.
 * Arquitectura modular desacoplada según la Constitución de Agentes.
 */
@Composable
fun MetricasScreen(
    viewModel: MetricasViewModel = hiltViewModel(),
    onNavigateToVenta: () -> Unit = {},
    onNavigateToProduccion: () -> Unit = {},
    onNavigateToMetricas: () -> Unit = {},
    onNavigateToConfiguracion: () -> Unit = {},
    mostrarBottomBar: Boolean = true,
    onCerrarConsulta: () -> Unit = {},
    modifier: Modifier = Modifier
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()

    MetricasScreenContent(
        uiState = uiState,
        onEvent = viewModel::onEvent,
        onNavigateToVenta = onNavigateToVenta,
        onNavigateToProduccion = onNavigateToProduccion,
        onNavigateToMetricas = onNavigateToMetricas,
        onNavigateToConfiguracion = onNavigateToConfiguracion,
        mostrarBottomBar = mostrarBottomBar,
        onCerrarConsulta = onCerrarConsulta,
        modifier = modifier
    )
}

@Composable
fun MetricasScreenContent(
    uiState: MetricasUiState,
    onEvent: (MetricasUiEvent) -> Unit,
    onNavigateToVenta: () -> Unit = {},
    onNavigateToProduccion: () -> Unit = {},
    onNavigateToMetricas: () -> Unit = {},
    onNavigateToConfiguracion: () -> Unit = {},
    mostrarBottomBar: Boolean = true,
    onCerrarConsulta: () -> Unit = {},
    modifier: Modifier = Modifier
) {
    Scaffold(
        modifier = modifier.fillMaxSize(),
        containerColor = SurfaceWarm,
        contentWindowInsets = WindowInsets.navigationBars,
        bottomBar = {
            if (mostrarBottomBar) {
                TortilleriaNavBar(
                    activeItem = "Métricas",
                    onNavigateToVenta = onNavigateToVenta,
                    onNavigateToProduccion = onNavigateToProduccion,
                    onNavigateToMetricas = onNavigateToMetricas,
                    onNavigateToConfiguracion = onNavigateToConfiguracion
                )
            }
        }
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .background(SurfaceWarm)
                .verticalScroll(rememberScrollState())
                .padding(horizontal = 16.dp, vertical = 12.dp),
            verticalArrangement = Arrangement.spacedBy(14.dp)
        ) {
            // ── BANNER MODO SOLO CONSULTA ─────────────────────────────────────
            if (!mostrarBottomBar) {
                Surface(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(16.dp),
                    color = Color(0xFFFFF3E8),
                    border = BorderStroke(1.dp, Color(0xFFFFD8BF))
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 14.dp, vertical = 6.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(6.dp)
                        ) {
                            Box(
                                modifier = Modifier
                                    .size(8.dp)
                                    .clip(CircleShape)
                                    .background(MaizPrimary)
                            )
                            Text(
                                text = "Modo Solo Consulta (Solo Lectura)",
                                fontSize = 12.sp,
                                fontWeight = FontWeight.Bold,
                                color = Color(0xFFC04B00)
                            )
                        }

                        TextButton(
                            onClick = onCerrarConsulta,
                            contentPadding = PaddingValues(horizontal = 8.dp, vertical = 2.dp)
                        ) {
                            Text(
                                text = "Salir ➔",
                                fontSize = 12.sp,
                                fontWeight = FontWeight.ExtraBold,
                                color = Color(0xFFC04B00)
                            )
                        }
                    }
                }
            }

            // ── 1. CABECERA Y SELECTORES HORIZONTALES ─────────────────────────
            MetricasHeaderPeriodo(
                rangoFechasTexto = uiState.rangoFechasTexto,
                periodoSeleccionado = uiState.periodoSeleccionado,
                metricaSeleccionada = uiState.metricaSeleccionada,
                onAbrirSelectorFecha = { onEvent(MetricasUiEvent.ShowCustomDatePicker) },
                onSelectPeriodo = { onEvent(MetricasUiEvent.SelectPeriodo(it)) },
                onSelectMetrica = { onEvent(MetricasUiEvent.SelectMetrica(it)) }
            )

            // ── 2. CARD COMPORTAMIENTO DEL PERÍODO (GRÁFICA BEZIER Y TOOLTIP) ──
            MetricasGraficaBezierCanvas(
                unidadMetrica = uiState.unidadMetrica,
                totalPeriodoActual = uiState.totalPeriodoActual,
                variacionPorcentual = uiState.variacionPorcentual,
                puntos = uiState.puntosGrafica,
                tooltipPicoTexto = uiState.tooltipPicoTexto
            )

            // ── 3. CARD DISTRIBUCIÓN DE CANALES (MOSTRADOR VS REPARTO) ─────────
            MetricasCanalesVentaBar(
                montoMostrador = uiState.montoMostrador,
                montoReparto = uiState.montoReparto,
                porcentajeMostrador = uiState.porcentajeMostrador,
                porcentajeReparto = uiState.porcentajeReparto,
                kgMostrador = uiState.kgMostrador,
                kgReparto = uiState.kgReparto
            )

            // ── 4. INDICADORES INFERIORES (PROMEDIO Y MERMA) ──────────────────
            MetricasRitmoMermaCards(
                promedioDiarioTexto = uiState.promedioDiarioTexto,
                porcentajeMerma = uiState.porcentajeMerma,
                esMermaOptima = uiState.esMermaOptima
            )

            Spacer(modifier = Modifier.height(10.dp))
        }
    }

    // ── DIÁLOGO DE RANGO DE FECHAS PERSONALIZADO ──────────────────────
    if (uiState.showCustomDatePicker) {
        DialogRangoFechasPersonalizado(
            onDismiss = { onEvent(MetricasUiEvent.DismissCustomDatePicker) },
            onConfirmarRango = { start, end ->
                onEvent(MetricasUiEvent.SetCustomDateRange(start, end))
            }
        )
    }
}

@Preview(showBackground = true, name = "Teléfono Compacto")
@Preview(showBackground = true, name = "Tablet Landscape (Métricas)", widthDp = 840, heightDp = 480)
@Composable
fun MetricasScreenPreview() {
    TortilleriaDerekTheme {
        MetricasScreenContent(
            uiState = MetricasUiState(
                totalPeriodoActual = 1245.0,
                variacionPorcentual = 12.8,
                rangoFechasTexto = "1 – 7 Sept 2026",
                tooltipPicoTexto = "210.0 kg • Jueves",
                montoMostrador = 12400.0,
                montoReparto = 6850.0,
                porcentajeMostrador = 65f,
                porcentajeReparto = 35f,
                kgMostrador = 810.0,
                kgReparto = 435.0,
                promedioDiarioTexto = "177.8 kg/día",
                porcentajeMerma = 3.4,
                puntosGrafica = listOf(
                    PuntoGraficaDia(LocalDate.now().minusDays(6), "Lun", 70.0, "70.0 kg"),
                    PuntoGraficaDia(LocalDate.now().minusDays(5), "Mar", 100.0, "100.0 kg"),
                    PuntoGraficaDia(LocalDate.now().minusDays(4), "Mié", 150.0, "150.0 kg"),
                    PuntoGraficaDia(LocalDate.now().minusDays(3), "Jue", 210.0, "210.0 kg", esPico = true),
                    PuntoGraficaDia(LocalDate.now().minusDays(2), "Vie", 140.0, "140.0 kg"),
                    PuntoGraficaDia(LocalDate.now().minusDays(1), "Sáb", 165.0, "165.0 kg"),
                    PuntoGraficaDia(LocalDate.now(), "Dom", 170.0, "170.0 kg")
                )
            ),
            onEvent = {}
        )
    }
}
