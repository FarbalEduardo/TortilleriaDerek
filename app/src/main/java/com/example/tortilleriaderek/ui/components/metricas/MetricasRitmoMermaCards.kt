package com.example.tortilleriaderek.ui.components.metricas

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ShowChart
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.TrackChanges
import androidx.compose.material3.Icon
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.tortilleriaderek.ui.theme.BorderSubtle
import com.example.tortilleriaderek.ui.theme.TextPrimary
import com.example.tortilleriaderek.ui.theme.TextSecondary

/**
 * Fila de indicadores inferiores: Promedio Diario y Merma Promedio.
 */
@Composable
fun MetricasRitmoMermaCards(
    promedioDiarioTexto: String,
    porcentajeMerma: Double,
    esMermaOptima: Boolean,
    modifier: Modifier = Modifier
) {
    Row(
        modifier = modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        // Card Promedio Diario
        Surface(
            modifier = Modifier
                .weight(1f)
                .testTag("metricas_card_promedio_diario"),
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
                        text = androidx.compose.ui.res.stringResource(com.example.tortilleriaderek.R.string.metricas_promedio_diario),
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
                    text = promedioDiarioTexto.ifBlank { "0.0 kg/día" },
                    fontSize = 16.sp,
                    fontWeight = FontWeight.Black,
                    color = TextPrimary,
                    style = androidx.compose.ui.text.TextStyle(fontFeatureSettings = "tnum")
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
                        text = androidx.compose.ui.res.stringResource(com.example.tortilleriaderek.R.string.metricas_ritmo_optimo),
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color(0xFF16A34A)
                    )
                }
            }
        }

        // Card Merma Promedio
        Surface(
            modifier = Modifier
                .weight(1f)
                .testTag("metricas_card_merma_promedio"),
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
                        text = androidx.compose.ui.res.stringResource(com.example.tortilleriaderek.R.string.metricas_merma_promedio),
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
                    val mermaColor = if (esMermaOptima) Color(0xFF16A34A) else Color(0xFFDC2626)
                    Text(
                        text = "%.1f%%".format(porcentajeMerma),
                        fontSize = 16.sp,
                        fontWeight = FontWeight.Black,
                        color = mermaColor,
                        style = androidx.compose.ui.text.TextStyle(fontFeatureSettings = "tnum")
                    )
                    Text(
                        text = if (esMermaOptima) {
                            androidx.compose.ui.res.stringResource(com.example.tortilleriaderek.R.string.metricas_merma_optima)
                        } else {
                            androidx.compose.ui.res.stringResource(com.example.tortilleriaderek.R.string.metricas_merma_elevada)
                        },
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold,
                        color = mermaColor
                    )
                }
                Text(
                    text = androidx.compose.ui.res.stringResource(com.example.tortilleriaderek.R.string.metricas_merma_meta),
                    fontSize = 11.sp,
                    color = TextSecondary
                )
            }
        }
    }
}
