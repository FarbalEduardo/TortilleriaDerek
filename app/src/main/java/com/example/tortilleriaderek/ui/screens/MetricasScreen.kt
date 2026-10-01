package com.example.tortilleriaderek.ui.screens

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ShowChart
import androidx.compose.material.icons.automirrored.filled.TrendingUp
import androidx.compose.material.icons.filled.CalendarToday
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Factory
import androidx.compose.material.icons.filled.MoreVert
import androidx.compose.material.icons.filled.Payments
import androidx.compose.material.icons.filled.Storefront
import androidx.compose.material.icons.filled.TrackChanges
import androidx.compose.material.icons.filled.TwoWheeler
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.*
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.layout.Layout
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.tortilleriaderek.presentation.metricas.MetricaTipo
import com.example.tortilleriaderek.presentation.metricas.MetricasUiEvent
import com.example.tortilleriaderek.presentation.metricas.MetricasUiState
import com.example.tortilleriaderek.presentation.metricas.MetricasViewModel
import com.example.tortilleriaderek.presentation.metricas.PeriodoFiltro
import com.example.tortilleriaderek.presentation.metricas.PuntoGraficaDia
import com.example.tortilleriaderek.ui.components.TortilleriaNavBar
import com.example.tortilleriaderek.ui.theme.*
import java.time.Instant
import java.time.LocalDate
import java.time.ZoneId
import java.time.temporal.ChronoUnit

/**
 * Pantalla de Métricas ("Productividad y Ventas") conectada a Room y MetricasViewModel.
 *
 * Incluye:
 * - Filtro estricto de turnos en estado CERRADO (excluye turnos activos en curso).
 * - Consolidación multiturno por día calendario (suma turnos cerrados en la misma fecha).
 * - Selector de 4 métricas con desplazamiento horizontal:
 *   1. Producción Total (kg)
 *   2. Venta Total ($)
 *   3. Venta Mostrador ($)
 *   4. Venta Repartidor ($)
 * - Curva Bezier dinámica calculada reactivamente con detección de pico y tooltip.
 * - Desglose de canales (Mostrador vs Reparto Mayoreo).
 * - Indicadores de promedio diario y merma óptima.
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

@OptIn(ExperimentalMaterial3Api::class)
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
            // ── BANNER MODO SOLO CONSULTA (SIN NAVEGACIÓN INFERIOR) ───────────
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

            // ── 1. CABECERA: TÍTULO Y SELECTOR DE FECHAS ──────────────────────
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.Top
            ) {
                Column {
                    Text(
                        text = "Productividad y",
                        fontSize = 22.sp,
                        fontWeight = FontWeight.Black,
                        color = TextPrimary,
                        lineHeight = 26.sp
                    )
                    Text(
                        text = "Ventas",
                        fontSize = 22.sp,
                        fontWeight = FontWeight.Black,
                        color = TextPrimary,
                        lineHeight = 26.sp
                    )
                }

                // Píldora selector de rango de fecha (interactiva)
                Surface(
                    onClick = { onEvent(MetricasUiEvent.ShowCustomDatePicker) },
                    shape = RoundedCornerShape(16.dp),
                    color = Color.White,
                    border = BorderStroke(1.dp, BorderSubtle),
                    shadowElevation = 1.dp
                ) {
                    Row(
                        modifier = Modifier.padding(horizontal = 12.dp, vertical = 8.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.CalendarToday,
                            contentDescription = "Calendario",
                            tint = MaizPrimary,
                            modifier = Modifier.size(15.dp)
                        )
                        Text(
                            text = uiState.rangoFechasTexto.ifBlank { "Seleccionar rango" },
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Bold,
                            color = TextPrimary
                        )
                    }
                }
            }

            // ── 2. SELECTOR DE PERÍODO (CHIPS HORIZONTALES) ───────────────────
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .horizontalScroll(rememberScrollState()),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                PeriodoFiltro.entries.forEach { periodo ->
                    val isSelected = periodo == uiState.periodoSeleccionado
                    Surface(
                        onClick = {
                            if (periodo == PeriodoFiltro.PERSONALIZADO) {
                                onEvent(MetricasUiEvent.ShowCustomDatePicker)
                            } else {
                                onEvent(MetricasUiEvent.SelectPeriodo(periodo))
                            }
                        },
                        shape = RoundedCornerShape(16.dp),
                        color = if (isSelected) MaizPrimary else Color.White,
                        border = if (isSelected) null else BorderStroke(1.dp, BorderSubtle),
                        shadowElevation = if (isSelected) 2.dp else 0.dp
                    ) {
                        Text(
                            text = periodo.label,
                            fontSize = 12.sp,
                            fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium,
                            color = if (isSelected) Color.White else TextSecondary,
                            modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp)
                        )
                    }
                }
            }

            // ── 3. TOGGLE / SELECTOR DE LAS 4 MÉTRICAS (SCROLL HORIZONTAL) ────
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .horizontalScroll(rememberScrollState()),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                MetricaTipo.entries.forEach { metrica ->
                    val isSelected = metrica == uiState.metricaSeleccionada
                    val icon = when (metrica) {
                        MetricaTipo.PRODUCCION_TOTAL -> Icons.Default.Factory
                        MetricaTipo.VENTA_TOTAL -> Icons.Default.Payments
                        MetricaTipo.VENTA_MOSTRADOR -> Icons.Default.Storefront
                        MetricaTipo.VENTA_REPARTIDOR -> Icons.Default.TwoWheeler
                    }

                    Surface(
                        onClick = { onEvent(MetricasUiEvent.SelectMetrica(metrica)) },
                        shape = RoundedCornerShape(20.dp),
                        color = if (isSelected) Color(0xFFFFF7F2) else Color.White,
                        border = BorderStroke(
                            1.5.dp,
                            if (isSelected) MaizPrimary else BorderSubtle
                        ),
                        shadowElevation = if (isSelected) 1.dp else 0.dp
                    ) {
                        Row(
                            modifier = Modifier.padding(horizontal = 14.dp, vertical = 10.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.Center
                        ) {
                            Icon(
                                imageVector = icon,
                                contentDescription = null,
                                tint = if (isSelected) MaizPrimary else TextSecondary,
                                modifier = Modifier.size(16.dp)
                            )
                            Spacer(modifier = Modifier.width(6.dp))
                            Text(
                                text = metrica.label,
                                fontSize = 12.sp,
                                fontWeight = FontWeight.Bold,
                                color = if (isSelected) Color(0xFFC04B00) else TextSecondary
                            )
                        }
                    }
                }
            }

            // ── 4. CARD COMPORTAMIENTO DEL PERÍODO (GRÁFICA BEZIER Y TOOLTIP) ──
            Surface(
                modifier = Modifier
                    .fillMaxWidth()
                    .testTag("metricas_chart_card"),
                shape = RoundedCornerShape(22.dp),
                color = Color.White,
                border = BorderStroke(1.dp, BorderSubtle),
                shadowElevation = 2.dp
            ) {
                Column(
                    modifier = Modifier.padding(16.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    // Header de la Card
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = "COMPORTAMIENTO DEL PERIODO (TURNOS CERRADOS)",
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Bold,
                            color = TextSecondary,
                            letterSpacing = 0.5.sp
                        )
                        Icon(
                            imageVector = Icons.Default.MoreVert,
                            contentDescription = "Opciones",
                            tint = TextSecondary,
                            modifier = Modifier.size(18.dp)
                        )
                    }

                    // Total Principal y Badge de Variación
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Row(
                            verticalAlignment = Alignment.Bottom,
                            horizontalArrangement = Arrangement.spacedBy(4.dp)
                        ) {
                            val formattedTotal = if (uiState.unidadMetrica == "kg") {
                                "%,.1f".format(uiState.totalPeriodoActual)
                            } else {
                                "$%,.0f".format(uiState.totalPeriodoActual)
                            }
                            Text(
                                text = formattedTotal,
                                fontSize = 30.sp,
                                fontWeight = FontWeight.Black,
                                color = TextPrimary
                            )
                            Text(
                                text = uiState.unidadMetrica,
                                fontSize = 14.sp,
                                fontWeight = FontWeight.Bold,
                                color = TextSecondary,
                                modifier = Modifier.padding(bottom = 5.dp)
                            )
                        }

                        // Badge de Variación porcentual
                        val esPositiva = uiState.variacionPorcentual >= 0.0
                        Surface(
                            shape = RoundedCornerShape(12.dp),
                            color = if (esPositiva) Color(0xFFE6F9EE) else Color(0xFFFDE8E8)
                        ) {
                            Row(
                                modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(4.dp)
                            ) {
                                Icon(
                                    imageVector = Icons.AutoMirrored.Filled.TrendingUp,
                                    contentDescription = null,
                                    tint = if (esPositiva) Color(0xFF16A34A) else Color(0xFFDC2626),
                                    modifier = Modifier
                                        .size(14.dp)
                                        .rotate(if (esPositiva) 0f else 180f)
                                )
                                val signo = if (esPositiva) "+" else ""
                                Text(
                                    text = "$signo%.1f%% vs ciclo ant.".format(uiState.variacionPorcentual),
                                    fontSize = 11.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = if (esPositiva) Color(0xFF16A34A) else Color(0xFFDC2626)
                                )
                            }
                        }
                    }

                    // ── GRÁFICA CURVA BEZIER DINÁMICA CON GRADIENTE Y TOOLTIP ──
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(190.dp)
                    ) {
                        val puntos = uiState.puntosGrafica

                        Canvas(modifier = Modifier.fillMaxSize()) {
                            val w = size.width
                            val h = size.height
                            val bottomMargin = 20.dp.toPx()
                            val chartH = h - bottomMargin

                            // Líneas horizontales de guía (Grid)
                            val gridLines = 4
                            for (i in 0 until gridLines) {
                                val y = chartH * (i.toFloat() / (gridLines - 1).toFloat())
                                drawLine(
                                    color = Color(0xFFEFECE6),
                                    start = Offset(0f, y),
                                    end = Offset(w, y),
                                    strokeWidth = 1.dp.toPx(),
                                    pathEffect = PathEffect.dashPathEffect(floatArrayOf(6f, 6f))
                                )
                            }

                            if (puntos.isNotEmpty()) {
                                val maxVal = puntos.maxOfOrNull { it.valor } ?: 0.0
                                val maxScale = if (maxVal > 0.0) (maxVal * 1.25) else 100.0

                                val pointsCount = puntos.size
                                val marginX = w * 0.06f
                                val availableWidth = w - 2 * marginX

                                val offsets = puntos.mapIndexed { idx, p ->
                                    val x = if (pointsCount > 1) {
                                        marginX + (idx.toFloat() / (pointsCount - 1).toFloat()) * availableWidth
                                    } else {
                                        w / 2f
                                    }
                                    val yFraction = ((p.valor / maxScale).toFloat()).coerceIn(0f, 1f)
                                    val y = chartH * (1f - (yFraction * 0.75f + 0.10f))
                                    Offset(x, y)
                                }

                                if (offsets.size >= 2) {
                                    // Curva Bezier suave
                                    val path = Path().apply {
                                        moveTo(offsets[0].x, offsets[0].y)
                                        for (i in 0 until offsets.size - 1) {
                                            val p0 = offsets[i]
                                            val p1 = offsets[i + 1]
                                            val controlX = (p0.x + p1.x) / 2
                                            cubicTo(controlX, p0.y, controlX, p1.y, p1.x, p1.y)
                                        }
                                    }

                                    // Área degradada
                                    val fillPath = Path().apply {
                                        addPath(path)
                                        lineTo(offsets.last().x, chartH)
                                        lineTo(offsets.first().x, chartH)
                                        close()
                                    }

                                    drawPath(
                                        path = fillPath,
                                        brush = Brush.verticalGradient(
                                            colors = listOf(
                                                Color(0xFFFF7A00).copy(alpha = 0.35f),
                                                Color(0xFFFF7A00).copy(alpha = 0.02f)
                                            ),
                                            startY = 0f,
                                            endY = chartH
                                        )
                                    )

                                    // Trazo de la curva
                                    drawPath(
                                        path = path,
                                        color = Color(0xFFFF6B00),
                                        style = Stroke(width = 3.dp.toPx(), cap = StrokeCap.Round)
                                    )
                                }

                                // Dibujar puntos y destacar pico
                                offsets.forEachIndexed { i, pt ->
                                    val puntoData = puntos[i]
                                    if (puntoData.esPico) {
                                        // Línea vertical punteada al piso
                                        drawLine(
                                            color = Color(0xFFFF7A00),
                                            start = Offset(pt.x, pt.y),
                                            end = Offset(pt.x, chartH),
                                            strokeWidth = 1.5.dp.toPx(),
                                            pathEffect = PathEffect.dashPathEffect(floatArrayOf(8f, 6f))
                                        )

                                        // Halo de pico
                                        drawCircle(color = Color.White, radius = 6.dp.toPx(), center = pt)
                                        drawCircle(color = Color(0xFFFF6B00), radius = 4.dp.toPx(), center = pt)
                                    } else {
                                        drawCircle(color = Color.White, radius = 4.dp.toPx(), center = pt)
                                        drawCircle(
                                            color = Color(0xFFFF6B00),
                                            radius = 4.dp.toPx(),
                                            center = pt,
                                            style = Stroke(width = 2.dp.toPx())
                                        )
                                    }
                                }
                            }
                        }

                        // Tooltip flotante oscuro en el pico
                        if (uiState.tooltipPicoTexto.isNotBlank()) {
                            Box(
                                modifier = Modifier
                                    .align(Alignment.TopCenter)
                                    .padding(top = 2.dp)
                            ) {
                                Surface(
                                    shape = RoundedCornerShape(12.dp),
                                    color = Color(0xFF1E293B),
                                    shadowElevation = 4.dp
                                ) {
                                    val partes = uiState.tooltipPicoTexto.split("•")
                                    Row(
                                        modifier = Modifier.padding(horizontal = 10.dp, vertical = 5.dp),
                                        verticalAlignment = Alignment.CenterVertically,
                                        horizontalArrangement = Arrangement.spacedBy(5.dp)
                                    ) {
                                        Text(
                                            text = partes.getOrNull(0)?.trim() ?: "",
                                            fontSize = 11.sp,
                                            fontWeight = FontWeight.ExtraBold,
                                            color = Color.White
                                        )
                                        Box(
                                            modifier = Modifier
                                                .size(4.dp)
                                                .clip(CircleShape)
                                                .background(Color(0xFFFF7A00))
                                        )
                                        Text(
                                            text = partes.getOrNull(1)?.trim() ?: "",
                                            fontSize = 11.sp,
                                            fontWeight = FontWeight.Bold,
                                            color = Color(0xFFFF9E59)
                                        )
                                    }
                                }
                            }
                        }
                    }

                    // Eje X: Días del período con alineación matemática a los puntos del Canvas
                    val puntos = uiState.puntosGrafica
                    val pointsCount = puntos.size
                    // Si son más de 14 días (ej. mes de 30 días o rango personalizado), los números aparecen de dos en dos
                    val mostrarDeDosEnDos = pointsCount > 14

                    Layout(
                        content = {
                            puntos.forEachIndexed { idx, punto ->
                                val visible = !mostrarDeDosEnDos || (idx % 2 == 0) || punto.esPico
                                if (visible) {
                                    if (punto.esPico) {
                                        Surface(
                                            shape = RoundedCornerShape(10.dp),
                                            color = Color(0xFFFFEDE0)
                                        ) {
                                            Text(
                                                text = punto.diaEtiqueta,
                                                fontSize = if (mostrarDeDosEnDos) 10.sp else 11.sp,
                                                fontWeight = FontWeight.ExtraBold,
                                                color = Color(0xFFE05300),
                                                modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                                            )
                                        }
                                    } else {
                                        Text(
                                            text = punto.diaEtiqueta,
                                            fontSize = if (mostrarDeDosEnDos) 10.sp else 11.sp,
                                            fontWeight = FontWeight.SemiBold,
                                            color = TextSecondary
                                        )
                                    }
                                }
                            }
                        },
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(26.dp)
                    ) { measurables, constraints ->
                        val w = constraints.maxWidth
                        val marginX = w * 0.06f
                        val availableWidth = w - 2 * marginX
                        val placeables = measurables.map { it.measure(constraints.copy(minWidth = 0, minHeight = 0)) }

                        layout(w, placeables.maxOfOrNull { it.height } ?: 0) {
                            var visibleIndex = 0
                            puntos.forEachIndexed { idx, punto ->
                                val visible = !mostrarDeDosEnDos || (idx % 2 == 0) || punto.esPico
                                if (visible && visibleIndex < placeables.size) {
                                    val placeable = placeables[visibleIndex]
                                    val fraction = if (pointsCount > 1) idx.toFloat() / (pointsCount - 1).toFloat() else 0.5f
                                    val centerX = marginX + fraction * availableWidth
                                    val x = (centerX - placeable.width / 2f).toInt()
                                        .coerceIn(0, w - placeable.width)
                                    placeable.placeRelative(x, 0)
                                    visibleIndex++
                                }
                            }
                        }
                    }
                }
            }

            // ── 5. CARD DISTRIBUCIÓN DE CANALES (MOSTRADOR VS REPARTO) ─────────
            Surface(
                modifier = Modifier
                    .fillMaxWidth()
                    .testTag("metricas_canales_card"),
                shape = RoundedCornerShape(22.dp),
                color = Color.White,
                border = BorderStroke(1.dp, BorderSubtle),
                shadowElevation = 2.dp
            ) {
                Column(
                    modifier = Modifier.padding(16.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    // Encabezado
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = "Distribución de Canales",
                            fontSize = 15.sp,
                            fontWeight = FontWeight.Bold,
                            color = TextPrimary
                        )

                        Surface(
                            shape = RoundedCornerShape(10.dp),
                            color = Color(0xFFF3F4F6)
                        ) {
                            val totalVentas = uiState.montoMostrador + uiState.montoReparto
                            Text(
                                text = "Total: $%,.0f MXN".format(totalVentas),
                                fontSize = 11.sp,
                                fontWeight = FontWeight.Bold,
                                color = TextPrimary,
                                modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                            )
                        }
                    }

                    // Barra segmentada bi-color
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(8.dp)
                            .clip(CircleShape)
                            .background(Color(0xFFE5E7EB))
                    ) {
                        Row(modifier = Modifier.fillMaxSize()) {
                            val weightMostrador = (uiState.porcentajeMostrador / 100f).coerceIn(0.01f, 0.99f)
                            val weightReparto = (uiState.porcentajeReparto / 100f).coerceIn(0.01f, 0.99f)

                            // Mostrador
                            Box(
                                modifier = Modifier
                                    .fillMaxHeight()
                                    .weight(weightMostrador)
                                    .background(Color(0xFFFF6B00))
                            )
                            // Reparto Mayoreo
                            Box(
                                modifier = Modifier
                                    .fillMaxHeight()
                                    .weight(weightReparto)
                                    .background(Color(0xFFC2610C))
                            )
                        }
                    }

                    // Desglose de Canales
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        // Canal Mostrador
                        Column(verticalArrangement = Arrangement.spacedBy(2.dp)) {
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(6.dp)
                            ) {
                                Box(
                                    modifier = Modifier
                                        .size(8.dp)
                                        .clip(CircleShape)
                                        .background(Color(0xFFFF6B00))
                                )
                                Text(
                                    text = "Mostrador (%.0f%%)".format(uiState.porcentajeMostrador),
                                    fontSize = 12.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = TextPrimary
                                )
                            }
                            Row(
                                verticalAlignment = Alignment.Bottom,
                                horizontalArrangement = Arrangement.spacedBy(3.dp)
                            ) {
                                Text(
                                    text = "$%,.0f".format(uiState.montoMostrador),
                                    fontSize = 16.sp,
                                    fontWeight = FontWeight.Black,
                                    color = TextPrimary
                                )
                                Text(
                                    text = "MXN",
                                    fontSize = 10.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = TextSecondary,
                                    modifier = Modifier.padding(bottom = 2.dp)
                                )
                            }
                            Text(
                                text = "%.1f kg entregados".format(uiState.kgMostrador),
                                fontSize = 11.sp,
                                color = TextSecondary
                            )
                        }

                        // Canal Reparto Mayoreo
                        Column(verticalArrangement = Arrangement.spacedBy(2.dp)) {
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(6.dp)
                            ) {
                                Box(
                                    modifier = Modifier
                                        .size(8.dp)
                                        .clip(CircleShape)
                                        .background(Color(0xFFC2610C))
                                )
                                Text(
                                    text = "Reparto Mayoreo (%.0f%%)".format(uiState.porcentajeReparto),
                                    fontSize = 12.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = TextPrimary
                                )
                            }
                            Row(
                                verticalAlignment = Alignment.Bottom,
                                horizontalArrangement = Arrangement.spacedBy(3.dp)
                            ) {
                                Text(
                                    text = "$%,.0f".format(uiState.montoReparto),
                                    fontSize = 16.sp,
                                    fontWeight = FontWeight.Black,
                                    color = TextPrimary
                                )
                                Text(
                                    text = "MXN",
                                    fontSize = 10.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = TextSecondary,
                                    modifier = Modifier.padding(bottom = 2.dp)
                                )
                            }
                            Text(
                                text = "%.1f kg despachados".format(uiState.kgReparto),
                                fontSize = 11.sp,
                                color = TextSecondary
                            )
                        }
                    }
                }
            }

            // ── 6. FILA INFERIOR DE INDICADORES (PROMEDIO Y MERMA) ─────────────
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                // Card Promedio Diario
                Surface(
                    modifier = Modifier.weight(1f),
                    shape = RoundedCornerShape(20.dp),
                    color = Color.White,
                    border = BorderStroke(1.dp, BorderSubtle),
                    shadowElevation = 1.dp
                ) {
                    Column(
                        modifier = Modifier.padding(14.dp),
                        verticalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = "Promedio Diario",
                                fontSize = 11.sp,
                                fontWeight = FontWeight.Bold,
                                color = TextSecondary
                            )
                            Icon(
                                imageVector = Icons.AutoMirrored.Filled.ShowChart,
                                contentDescription = null,
                                tint = Color(0xFF6366F1),
                                modifier = Modifier.size(16.dp)
                            )
                        }
                        Text(
                            text = uiState.promedioDiarioTexto.ifBlank { "0.0 kg/día" },
                            fontSize = 16.sp,
                            fontWeight = FontWeight.Black,
                            color = TextPrimary
                        )
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(4.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Default.CheckCircle,
                                contentDescription = null,
                                tint = Color(0xFF16A34A),
                                modifier = Modifier.size(14.dp)
                            )
                            Text(
                                text = "Ritmo óptimo",
                                fontSize = 11.sp,
                                fontWeight = FontWeight.Bold,
                                color = Color(0xFF16A34A)
                            )
                        }
                    }
                }

                // Card Merma Promedio
                Surface(
                    modifier = Modifier.weight(1f),
                    shape = RoundedCornerShape(20.dp),
                    color = Color.White,
                    border = BorderStroke(1.dp, BorderSubtle),
                    shadowElevation = 1.dp
                ) {
                    Column(
                        modifier = Modifier.padding(14.dp),
                        verticalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = "Merma Promedio",
                                fontSize = 11.sp,
                                fontWeight = FontWeight.Bold,
                                color = TextSecondary
                            )
                            Icon(
                                imageVector = Icons.Default.TrackChanges,
                                contentDescription = null,
                                tint = Color(0xFFEC4899),
                                modifier = Modifier.size(16.dp)
                            )
                        }
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(4.dp)
                        ) {
                            val mermaColor = if (uiState.esMermaOptima) Color(0xFF16A34A) else Color(0xFFDC2626)
                            Text(
                                text = "%.1f%%".format(uiState.porcentajeMerma),
                                fontSize = 16.sp,
                                fontWeight = FontWeight.Black,
                                color = mermaColor
                            )
                            Text(
                                text = if (uiState.esMermaOptima) "(Óptimo)" else "(Elevada)",
                                fontSize = 11.sp,
                                fontWeight = FontWeight.Bold,
                                color = mermaColor
                            )
                        }
                        Text(
                            text = "Meta: menor a 5.0%",
                            fontSize = 11.sp,
                            color = TextSecondary
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(10.dp))
        }
    }

    // ── DIÁLOGO DE RANGO DE FECHAS PERSONALIZADO ──────────────────────
    if (uiState.showCustomDatePicker) {
        val zoneUtc = ZoneId.of("UTC")
        val hoyUtc = LocalDate.now(zoneUtc)
        val selectableDates = remember {
            object : SelectableDates {
                override fun isSelectableDate(utcTimeMillis: Long): Boolean {
                    val fecha = Instant.ofEpochMilli(utcTimeMillis).atZone(zoneUtc).toLocalDate()
                    return !fecha.isAfter(hoyUtc)
                }
            }
        }
        val datePickerState = rememberDateRangePickerState(selectableDates = selectableDates)

        val startMillis = datePickerState.selectedStartDateMillis
        val endMillis = datePickerState.selectedEndDateMillis

        val startDate = startMillis?.let { Instant.ofEpochMilli(it).atZone(zoneUtc).toLocalDate() }
        val endDate = endMillis?.let { Instant.ofEpochMilli(it).atZone(zoneUtc).toLocalDate() }

        val rangoCompletoSeleccionado = startDate != null && endDate != null
        val diasSeleccionados = if (rangoCompletoSeleccionado) {
            ChronoUnit.DAYS.between(startDate, endDate) + 1
        } else 0

        val excede30Dias = diasSeleccionados > 30

            DatePickerDialog(
                onDismissRequest = { onEvent(MetricasUiEvent.DismissCustomDatePicker) },
                confirmButton = {
                    // Al momento de elegir inicio y final aparece el botón para aplicar la selección a la gráfica
                    if (rangoCompletoSeleccionado && !excede30Dias) {
                        Button(
                            onClick = {
                                onEvent(MetricasUiEvent.SetCustomDateRange(startMillis!!, endMillis!!))
                            },
                            colors = ButtonDefaults.buttonColors(containerColor = MaizPrimary),
                            shape = CircleShape,
                            contentPadding = PaddingValues(horizontal = 16.dp, vertical = 8.dp)
                        ) {
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(6.dp)
                            ) {
                                Icon(
                                    imageVector = Icons.Default.CheckCircle,
                                    contentDescription = null,
                                    tint = Color.White,
                                    modifier = Modifier.size(16.dp)
                                )
                                Text(
                                    text = "Aplicar a la gráfica ($diasSeleccionados días)",
                                    color = Color.White,
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 13.sp
                                )
                            }
                        }
                    }
                },
                dismissButton = {
                    TextButton(onClick = { onEvent(MetricasUiEvent.DismissCustomDatePicker) }) {
                        Text("Cancelar", color = TextSecondary, fontWeight = FontWeight.SemiBold)
                    }
                },
                colors = DatePickerDefaults.colors(
                    containerColor = Color.White
                )
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .background(Color.White)
                ) {
                    if (excede30Dias) {
                        Surface(
                            shape = RoundedCornerShape(12.dp),
                            color = Color(0xFFFDE8E8),
                            border = BorderStroke(1.dp, Color(0xFFF87171)),
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(horizontal = 16.dp, vertical = 6.dp)
                        ) {
                            Row(
                                modifier = Modifier.padding(10.dp),
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(8.dp)
                            ) {
                                Icon(
                                    imageVector = Icons.Default.Warning,
                                    contentDescription = null,
                                    tint = Color(0xFFDC2626),
                                    modifier = Modifier.size(20.dp)
                                )
                                Column {
                                    Text(
                                        text = "No se permiten más de 30 días",
                                        fontSize = 13.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = Color(0xFFDC2626)
                                    )
                                    Text(
                                        text = "Seleccionaste $diasSeleccionados días. Por favor reduce el rango a 30 días o menos.",
                                        fontSize = 11.sp,
                                        color = Color(0xFF991B1B)
                                    )
                                }
                            }
                        }
                    } else if (startDate != null && endDate == null) {
                        Surface(
                            shape = RoundedCornerShape(12.dp),
                            color = Color(0xFFFFF7ED),
                            border = BorderStroke(1.dp, Color(0xFFFFEDD5)),
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(horizontal = 16.dp, vertical = 6.dp)
                        ) {
                            Row(
                                modifier = Modifier.padding(8.dp),
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(8.dp)
                            ) {
                                Icon(
                                    imageVector = Icons.Default.CalendarToday,
                                    contentDescription = null,
                                    tint = MaizPrimary,
                                    modifier = Modifier.size(15.dp)
                                )
                                Text(
                                    text = "Inicio seleccionado. Ahora selecciona la fecha final (máx. 30 días).",
                                    fontSize = 11.sp,
                                    fontWeight = FontWeight.Medium,
                                    color = Color(0xFFC2410C)
                                )
                            }
                        }
                    }

                    DateRangePicker(
                        state = datePickerState,
                        title = {
                            Text(
                                text = "Seleccionar Rango Personalizado (Máx. 30 días)",
                                modifier = Modifier.padding(horizontal = 16.dp, vertical = 12.dp),
                                fontWeight = FontWeight.Bold,
                                fontSize = 15.sp,
                                color = TextPrimary
                            )
                        },
                        modifier = Modifier.weight(1f, fill = false),
                        colors = DatePickerDefaults.colors(
                            containerColor = Color.White,
                            titleContentColor = TextPrimary,
                            headlineContentColor = TextPrimary,
                            weekdayContentColor = TextSecondary,
                            subheadContentColor = TextSecondary,
                            navigationContentColor = MaizPrimary,
                            yearContentColor = TextPrimary,
                            currentYearContentColor = MaizPrimary,
                            selectedYearContentColor = Color.White,
                            selectedYearContainerColor = MaizPrimary,
                            dayContentColor = Color(0xFF1F2937),
                            selectedDayContainerColor = MaizPrimary,
                            dayInSelectionRangeContainerColor = Color(0xFFFFEDD5),
                            dayInSelectionRangeContentColor = Color(0xFF9A3412),
                            selectedDayContentColor = Color.White,
                            todayDateBorderColor = MaizPrimary,
                            todayContentColor = MaizPrimary
                        )
                    )
                }
            }
    }
}

@Preview(showBackground = true, showSystemUi = true)
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
