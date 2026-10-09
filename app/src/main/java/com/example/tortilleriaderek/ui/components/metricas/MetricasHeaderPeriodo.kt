package com.example.tortilleriaderek.ui.components.metricas

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CalendarToday
import androidx.compose.material.icons.filled.Factory
import androidx.compose.material.icons.filled.Payments
import androidx.compose.material.icons.filled.Storefront
import androidx.compose.material.icons.filled.TwoWheeler
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.tortilleriaderek.presentation.metricas.MetricaTipo
import com.example.tortilleriaderek.presentation.metricas.PeriodoFiltro
import com.example.tortilleriaderek.ui.theme.*

/**
 * Encabezado de métricas con selectores horizontales de período y tipo de métrica.
 */
@Composable
fun MetricasHeaderPeriodo(
    rangoFechasTexto: String,
    periodoSeleccionado: PeriodoFiltro,
    metricaSeleccionada: MetricaTipo,
    onAbrirSelectorFecha: () -> Unit,
    onSelectPeriodo: (PeriodoFiltro) -> Unit,
    onSelectMetrica: (MetricaTipo) -> Unit,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier.fillMaxWidth(),
        verticalArrangement = Arrangement.spacedBy(14.dp)
    ) {
        // ── 1. TÍTULO Y SELECTOR DE FECHAS ────────────────────────────────────
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.Top
        ) {
            Column {
                Text(
                    text = androidx.compose.ui.res.stringResource(com.example.tortilleriaderek.R.string.metricas_titulo_1),
                    fontSize = 22.sp,
                    fontWeight = FontWeight.Black,
                    color = TextPrimary,
                    lineHeight = 26.sp
                )
                Text(
                    text = androidx.compose.ui.res.stringResource(com.example.tortilleriaderek.R.string.metricas_titulo_2),
                    fontSize = 22.sp,
                    fontWeight = FontWeight.Black,
                    color = TextPrimary,
                    lineHeight = 26.sp
                )
            }

            // Píldora selector de rango de fecha
            Surface(
                onClick = onAbrirSelectorFecha,
                shape = RoundedCornerShape(16.dp),
                color = Color.White,
                border = BorderStroke(1.dp, BorderSubtle),
                shadowElevation = 1.dp,
                modifier = Modifier.testTag("metricas_btn_selector_fecha")
            ) {
                Row(
                    modifier = Modifier.padding(horizontal = 12.dp, vertical = 8.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.CalendarToday,
                        contentDescription = androidx.compose.ui.res.stringResource(com.example.tortilleriaderek.R.string.txt_calendario),
                        tint = MaizPrimary,
                        modifier = Modifier.size(15.dp)
                    )
                    Text(
                        text = rangoFechasTexto.ifBlank { androidx.compose.ui.res.stringResource(com.example.tortilleriaderek.R.string.metricas_seleccionar_rango) },
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Bold,
                        color = TextPrimary
                    )
                }
            }
        }

        // ── 2. SELECTOR DE PERÍODO (CHIPS HORIZONTALES) ───────────────────────
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .horizontalScroll(rememberScrollState()),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            PeriodoFiltro.entries.forEach { periodo ->
                val isSelected = periodo == periodoSeleccionado
                Surface(
                    onClick = {
                        if (periodo == PeriodoFiltro.PERSONALIZADO) {
                            onAbrirSelectorFecha()
                        } else {
                            onSelectPeriodo(periodo)
                        }
                    },
                    shape = RoundedCornerShape(16.dp),
                    color = if (isSelected) MaizPrimary else Color.White,
                    border = if (isSelected) null else BorderStroke(1.dp, BorderSubtle),
                    shadowElevation = if (isSelected) 2.dp else 0.dp,
                    modifier = Modifier.testTag("metricas_chip_periodo_${periodo.name}")
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

        // ── 3. TOGGLE / SELECTOR DE LAS 4 MÉTRICAS (SCROLL HORIZONTAL) ────────
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .horizontalScroll(rememberScrollState()),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            MetricaTipo.entries.forEach { metrica ->
                val isSelected = metrica == metricaSeleccionada
                val icon = when (metrica) {
                    MetricaTipo.PRODUCCION_TOTAL -> Icons.Default.Factory
                    MetricaTipo.VENTA_TOTAL -> Icons.Default.Payments
                    MetricaTipo.VENTA_MOSTRADOR -> Icons.Default.Storefront
                    MetricaTipo.VENTA_REPARTIDOR -> Icons.Default.TwoWheeler
                }

                Surface(
                    onClick = { onSelectMetrica(metrica) },
                    shape = RoundedCornerShape(20.dp),
                    color = if (isSelected) Color(0xFFFFF7F2) else Color.White,
                    border = BorderStroke(
                        1.5.dp,
                        if (isSelected) MaizPrimary else BorderSubtle
                    ),
                    shadowElevation = if (isSelected) 1.dp else 0.dp,
                    modifier = Modifier.testTag("metricas_chip_tipo_${metrica.name}")
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
    }
}
