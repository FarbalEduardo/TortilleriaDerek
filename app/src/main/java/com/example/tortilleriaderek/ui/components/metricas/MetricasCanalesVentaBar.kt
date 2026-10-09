package com.example.tortilleriaderek.ui.components.metricas

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.tortilleriaderek.ui.theme.BorderSubtle
import com.example.tortilleriaderek.ui.theme.TextPrimary
import com.example.tortilleriaderek.ui.theme.TextSecondary

/**
 * Card para la distribución de canales de venta (Mostrador vs Reparto Mayoreo).
 */
@Composable
fun MetricasCanalesVentaBar(
    montoMostrador: Double,
    montoReparto: Double,
    porcentajeMostrador: Float,
    porcentajeReparto: Float,
    kgMostrador: Double,
    kgReparto: Double,
    modifier: Modifier = Modifier
) {
    Surface(
        modifier = modifier
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
                    text = androidx.compose.ui.res.stringResource(com.example.tortilleriaderek.R.string.metricas_distribucion_canales),
                    fontSize = 15.sp,
                    fontWeight = FontWeight.Bold,
                    color = TextPrimary
                )

                Surface(
                    shape = RoundedCornerShape(10.dp),
                    color = Color(0xFFF3F4F6)
                ) {
                    val totalVentas = montoMostrador + montoReparto
                    Text(
                        text = "Total: $%,.0f MXN".format(totalVentas),
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold,
                        color = TextPrimary,
                        style = androidx.compose.ui.text.TextStyle(fontFeatureSettings = "tnum"),
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
                    val weightMostrador = (porcentajeMostrador / 100f).coerceIn(0.01f, 0.99f)
                    val weightReparto = (porcentajeReparto / 100f).coerceIn(0.01f, 0.99f)

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
                            text = "Mostrador (%.0f%%)".format(porcentajeMostrador),
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
                            text = "$%,.0f".format(montoMostrador),
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
                        text = "%.1f kg entregados".format(kgMostrador),
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
                            text = "Reparto Mayoreo (%.0f%%)".format(porcentajeReparto),
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
                            text = "$%,.0f".format(montoReparto),
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
                        text = "%.1f kg despachados".format(kgReparto),
                        fontSize = 11.sp,
                        color = TextSecondary
                    )
                }
            }
        }
    }
}
