package com.example.tortilleriaderek.ui.components.metricas

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.TrendingUp
import androidx.compose.material.icons.filled.MoreVert
import androidx.compose.material3.Icon
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
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
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.tortilleriaderek.presentation.metricas.PuntoGraficaDia
import com.example.tortilleriaderek.ui.theme.BorderSubtle
import com.example.tortilleriaderek.ui.theme.TextPrimary
import com.example.tortilleriaderek.ui.theme.TextSecondary

/**
 * Card gráfica con curva Bezier dinámica, gradiente naranja, indicadores de pico y tooltip.
 */
@Composable
fun MetricasGraficaBezierCanvas(
    unidadMetrica: String,
    totalPeriodoActual: Double,
    variacionPorcentual: Double,
    puntos: List<PuntoGraficaDia>,
    tooltipPicoTexto: String,
    modifier: Modifier = Modifier
) {
    Surface(
        modifier = modifier
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
                    text = androidx.compose.ui.res.stringResource(com.example.tortilleriaderek.R.string.metricas_comportamiento_titulo),
                    fontSize = 11.sp,
                    fontWeight = FontWeight.Bold,
                    color = TextSecondary,
                    letterSpacing = 0.5.sp
                )
                Icon(
                    imageVector = Icons.Default.MoreVert,
                    contentDescription = androidx.compose.ui.res.stringResource(com.example.tortilleriaderek.R.string.txt_opciones),
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
                    val formattedTotal = if (unidadMetrica == "kg") {
                        "%,.1f".format(totalPeriodoActual)
                    } else {
                        "$%,.0f".format(totalPeriodoActual)
                    }
                    Text(
                        text = formattedTotal,
                        fontSize = 30.sp,
                        fontWeight = FontWeight.Black,
                        color = TextPrimary,
                        style = androidx.compose.ui.text.TextStyle(fontFeatureSettings = "tnum")
                    )
                    Text(
                        text = unidadMetrica,
                        fontSize = 14.sp,
                        fontWeight = FontWeight.Bold,
                        color = TextSecondary,
                        modifier = Modifier.padding(bottom = 5.dp)
                    )
                }

                // Badge de Variación porcentual
                val esPositiva = variacionPorcentual >= 0.0
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
                            text = "$signo%.1f%% vs ciclo ant.".format(variacionPorcentual),
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
                if (tooltipPicoTexto.isNotBlank()) {
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
                            val partes = tooltipPicoTexto.split("•")
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
            val pointsCount = puntos.size
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
}
