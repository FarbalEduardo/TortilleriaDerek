package com.example.tortilleriaderek.ui.components.historial

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.DeleteOutline
import androidx.compose.material.icons.filled.TwoWheeler
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.tortilleriaderek.presentation.historial.HistorialItemUi
import com.example.tortilleriaderek.ui.theme.*

/**
 * Tarjeta individual de transacción para el Historial (Modo de Diseño).
 * Visualiza folios, insignias de canal (Mostrador/Reparto con moto y ruta), monto tabular y eliminación.
 */
@Composable
fun TransactionCardItem(
    tx: HistorialItemUi,
    onDeleteClick: (() -> Unit)? = null,
    modifier: Modifier = Modifier
) {
    Surface(
        modifier = modifier
            .fillMaxWidth()
            .testTag("transaccion_card_${tx.ticketFolio}"),
        shape = RoundedCornerShape(16.dp),
        color = Color.White,
        border = BorderStroke(1.dp, BorderSubtle.copy(alpha = 0.7f)),
        shadowElevation = 0.5.dp
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 14.dp, vertical = 12.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Izquierda: Folio + Fecha/Hora + Detalle / Repartidor
            Column(
                modifier = Modifier
                    .weight(1f)
                    .padding(end = 8.dp),
                verticalArrangement = Arrangement.spacedBy(4.dp)
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    Text(
                        text = "#${tx.ticketFolio}",
                        fontSize = 14.sp,
                        fontWeight = FontWeight.Bold,
                        color = TextPrimary,
                        maxLines = 1,
                        softWrap = false
                    )

                    // Fecha y hora de venta
                    Surface(
                        shape = RoundedCornerShape(6.dp),
                        color = Color(0xFFF4F0EB)
                    ) {
                        Text(
                            text = "${tx.fechaTexto} • ${tx.hora}",
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Medium,
                            color = TextSecondary,
                            maxLines = 1,
                            softWrap = false,
                            modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                        )
                    }

                    // Tipo de venta badge pequeño
                    val isReparto = tx.tipo == "REPARTIDOR"
                    Surface(
                        shape = RoundedCornerShape(6.dp),
                        color = if (isReparto) Color(0xFFFFF0E5) else Color(0xFFEFF6FF)
                    ) {
                        Text(
                            text = if (isReparto) "Reparto" else "Mostrador",
                            fontSize = 10.sp,
                            fontWeight = FontWeight.Bold,
                            color = if (isReparto) MaizPrimary else Color(0xFF2563EB),
                            maxLines = 1,
                            softWrap = false,
                            modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                        )
                    }
                }

                // Detalle de la transacción
                if (tx.tipo == "REPARTIDOR") {
                    Surface(
                        shape = RoundedCornerShape(8.dp),
                        color = Color(0xFFFFF3EB),
                        border = BorderStroke(1.dp, Color(0xFFFFD7BF))
                    ) {
                        Row(
                            modifier = Modifier.padding(horizontal = 7.dp, vertical = 2.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(4.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Default.TwoWheeler,
                                contentDescription = null,
                                tint = MaizPrimary,
                                modifier = Modifier.size(12.dp)
                            )
                            val infoRepartidor = listOfNotNull(
                                tx.nombreRepartidor,
                                tx.motoAsignada,
                                tx.nombreRuta
                            ).joinToString(" • ").ifBlank { tx.detalleProductos }
                            Text(
                                text = infoRepartidor,
                                fontSize = 11.sp,
                                fontWeight = FontWeight.Bold,
                                color = Color(0xFFC04B00),
                                maxLines = 1,
                                overflow = TextOverflow.Ellipsis
                            )
                        }
                    }
                } else {
                    if (tx.detalleProductos.isNotBlank()) {
                        Text(
                            text = tx.detalleProductos,
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Normal,
                            color = TextSecondary,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis
                        )
                    }
                }
            }

            // Derecha: Total de la venta y botón de eliminar opcional
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(6.dp)
            ) {
                val entero = tx.monto.toInt()
                val decimales = ((tx.monto - entero) * 100).toInt()

                Row(
                    verticalAlignment = Alignment.Bottom,
                    modifier = Modifier.padding(vertical = 2.dp)
                ) {
                    Text(
                        text = "$%,d".format(entero),
                        fontSize = 17.sp,
                        fontWeight = FontWeight.ExtraBold,
                        color = if (tx.isHighlighted) MaizPrimary else TextPrimary,
                        maxLines = 1,
                        softWrap = false
                    )
                    Text(
                        text = ".%02d".format(decimales),
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Bold,
                        color = if (tx.isHighlighted) MaizPrimary else TextSecondary,
                        modifier = Modifier.padding(bottom = 1.dp),
                        maxLines = 1,
                        softWrap = false
                    )
                }

                if (onDeleteClick != null) {
                    IconButton(
                        onClick = onDeleteClick,
                        modifier = Modifier
                            .size(32.dp)
                            .testTag("btn_eliminar_venta_${tx.ticketFolio}")
                    ) {
                        Icon(
                            imageVector = Icons.Default.DeleteOutline,
                            contentDescription = "Eliminar venta",
                            tint = Color(0xFFEF4444),
                            modifier = Modifier.size(16.dp)
                        )
                    }
                }
            }
        }
    }
}

@Preview(showBackground = true)
@Composable
fun TransactionCardItemPreview() {
    TortilleriaDerekTheme {
        TransactionCardItem(
            tx = HistorialItemUi(
                id = "1",
                ticketFolio = "R-0038",
                tipo = "REPARTIDOR",
                fechaTimestamp = System.currentTimeMillis(),
                fechaTexto = "Hoy",
                hora = "10:30 AM",
                monto = 1870.00,
                detalleProductos = "Moto 01 (Carlos Ruiz)",
                usuarioNombre = "admin1",
                nombreRepartidor = "Carlos Ruiz",
                motoAsignada = "Moto 01",
                nombreRuta = "Ruta San Juan"
            ),
            onDeleteClick = {}
        )
    }
}
