package com.example.tortilleriaderek.ui.components.repartidores

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowForward
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.DirectionsCar
import androidx.compose.material.icons.filled.HourglassEmpty
import androidx.compose.material.icons.filled.TwoWheeler
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.tortilleriaderek.data.local.entity.RutaRepartidorEntity
import com.example.tortilleriaderek.ui.theme.*

/**
 * Tarjeta individual para monitorear una ruta de repartidor (Modo de Diseño).
 */
@Composable
fun RepartidorCardItem(
    ruta: RutaRepartidorEntity,
    onClick: () -> Unit,
    onCancelarSalida: () -> Unit = {},
    modifier: Modifier = Modifier
) {
    Surface(
        modifier = modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(20.dp))
            .border(1.dp, BorderSubtle.copy(alpha = 0.8f), RoundedCornerShape(20.dp))
            .clickable { onClick() }
            .testTag("repartidor_card_${ruta.id}"),
        color = Color.White,
        shadowElevation = 1.dp
    ) {
        Column(
            modifier = Modifier.padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            // Fila Superior: Icono + Título + Status Badge
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    val (iconBg, iconColor, iconVector) = when (ruta.status) {
                        "EN_RUTA" -> Triple(Color(0xFFFFF0E5), MaizPrimary, Icons.Default.TwoWheeler)
                        "PENDIENTE_SALIDA" -> Triple(Color(0xFFEDF2F7), Color(0xFF64748B), Icons.Default.DirectionsCar)
                        else -> Triple(Color(0xFFE6F4EA), VerdeAgave, Icons.Default.CheckCircle)
                    }

                    Box(
                        modifier = Modifier
                            .size(38.dp)
                            .clip(CircleShape)
                            .background(iconBg),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = iconVector,
                            contentDescription = null,
                            tint = iconColor,
                            modifier = Modifier.size(20.dp)
                        )
                    }

                    Column(verticalArrangement = Arrangement.spacedBy(1.dp)) {
                        Text(
                            text = "${ruta.moto} – ${ruta.repartidorNombre}",
                            fontSize = 14.sp,
                            fontWeight = FontWeight.Bold,
                            color = TextPrimary
                        )
                        Text(
                            text = ruta.nombreRuta,
                            fontSize = 11.sp,
                            color = TextSecondary
                        )
                    }
                }

                // Badge de estado (En Ruta, Pendiente de Salida, Liquidado)
                when (ruta.status) {
                    "EN_RUTA" -> {
                        Surface(
                            shape = CircleShape,
                            color = Color(0xFFFFEDE2)
                        ) {
                            Row(
                                modifier = Modifier.padding(horizontal = 9.dp, vertical = 4.dp),
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(4.dp)
                            ) {
                                Box(
                                    modifier = Modifier
                                        .size(5.dp)
                                        .clip(CircleShape)
                                        .background(MaizPrimary)
                                )
                                Text(
                                    text = "En Ruta",
                                    fontSize = 11.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = Color(0xFFC2410C)
                                )
                            }
                        }
                    }
                    "PENDIENTE_SALIDA" -> {
                        Surface(
                            shape = CircleShape,
                            color = Color(0xFFEFF3F8)
                        ) {
                            Row(
                                modifier = Modifier.padding(horizontal = 9.dp, vertical = 4.dp),
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(4.dp)
                            ) {
                                Box(
                                    modifier = Modifier
                                        .size(5.dp)
                                        .clip(CircleShape)
                                        .background(Color(0xFF64748B))
                                )
                                Text(
                                    text = "Pendiente de Salida",
                                    fontSize = 11.sp,
                                    fontWeight = FontWeight.Medium,
                                    color = Color(0xFF475569)
                                )
                            }
                        }
                    }
                    else -> {
                        Surface(
                            shape = CircleShape,
                            color = Color(0xFFE6F4EA)
                        ) {
                            Row(
                                modifier = Modifier.padding(horizontal = 9.dp, vertical = 4.dp),
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(3.dp)
                            ) {
                                Icon(
                                    imageVector = Icons.Default.Check,
                                    contentDescription = null,
                                    tint = VerdeAgave,
                                    modifier = Modifier.size(12.dp)
                                )
                                Text(
                                    text = "Liquidado",
                                    fontSize = 11.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = VerdeAgave
                                )
                            }
                        }
                    }
                }
            }

            // Fila Media: Métricas en caja gris tenue
            Surface(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(12.dp),
                color = Color(0xFFF9F7F4),
                border = BorderStroke(1.dp, BorderLight)
            ) {
                when (ruta.status) {
                    "EN_RUTA" -> {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(horizontal = 14.dp, vertical = 10.dp),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Column(verticalArrangement = Arrangement.spacedBy(2.dp)) {
                                Text(
                                    text = "CARGA INICIAL",
                                    fontSize = 10.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = TextSecondary,
                                    letterSpacing = 0.5.sp
                                )
                                Text(
                                    text = if (ruta.paquetesCargados > 0) "${ruta.paquetesCargados} paq (%.1f kg)".format(ruta.cargaInicialKg) else "${ruta.cargaInicialKg.toInt()} kg",
                                    fontSize = 14.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = TextPrimary
                                )
                            }

                            Column(
                                horizontalAlignment = Alignment.End,
                                verticalArrangement = Arrangement.spacedBy(2.dp)
                            ) {
                                Text(
                                    text = "PENDIENTE POR COBRAR",
                                    fontSize = 10.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = TextSecondary,
                                    letterSpacing = 0.5.sp
                                )
                                Text(
                                    text = "$%,.2f MXN".format(ruta.pendienteCobro),
                                    fontSize = 14.sp,
                                    fontWeight = FontWeight.ExtraBold,
                                    color = MaizPrimary
                                )
                            }
                        }
                    }
                    "PENDIENTE_SALIDA" -> {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(horizontal = 14.dp, vertical = 10.dp),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(6.dp)
                            ) {
                                Icon(
                                    imageVector = Icons.Default.HourglassEmpty,
                                    contentDescription = null,
                                    tint = TextSecondary,
                                    modifier = Modifier.size(15.dp)
                                )
                                Text(
                                    text = "Sin carga asignada hoy",
                                    fontSize = 12.sp,
                                    fontWeight = FontWeight.Medium,
                                    color = TextSecondary
                                )
                            }
                            Text(
                                text = "0.0 KG",
                                fontSize = 12.sp,
                                fontWeight = FontWeight.Bold,
                                color = TextSecondary
                            )
                        }
                    }
                    else -> {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(horizontal = 14.dp, vertical = 10.dp),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Column(verticalArrangement = Arrangement.spacedBy(2.dp)) {
                                Text(
                                    text = "ENTREGADO",
                                    fontSize = 10.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = TextSecondary,
                                    letterSpacing = 0.5.sp
                                )
                                val paqEntregados = (ruta.paquetesCargados - ruta.paquetesDevueltos).coerceAtLeast(0)
                                Text(
                                    text = if (paqEntregados > 0) "$paqEntregados paq (%.1f kg)".format(ruta.entregadoKg) else "${ruta.entregadoKg.toInt()} kg",
                                    fontSize = 14.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = TextPrimary
                                )
                            }

                            Column(
                                horizontalAlignment = Alignment.End,
                                verticalArrangement = Arrangement.spacedBy(2.dp)
                            ) {
                                Text(
                                    text = "COBRADO",
                                    fontSize = 10.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = TextSecondary,
                                    letterSpacing = 0.5.sp
                                )
                                Text(
                                    text = "$%,.2f MXN".format(ruta.cobrado),
                                    fontSize = 14.sp,
                                    fontWeight = FontWeight.ExtraBold,
                                    color = VerdeAgave
                                )
                            }
                        }
                    }
                }
            }

            // Fila Inferior: Enlaces y acciones
            if (ruta.status == "EN_RUTA") {
                HorizontalDivider(color = Color(0xFFF3F4F6), thickness = 1.dp)
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    TextButton(
                        onClick = onCancelarSalida,
                        contentPadding = PaddingValues(horizontal = 4.dp, vertical = 2.dp)
                    ) {
                        Text(
                            text = "✖ Cancelar salida",
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Bold,
                            color = Color(0xFFDC2626)
                        )
                    }

                    Button(
                        onClick = onClick,
                        shape = CircleShape,
                        colors = ButtonDefaults.buttonColors(
                            containerColor = MaizPrimary,
                            contentColor = Color.White
                        ),
                        contentPadding = PaddingValues(horizontal = 14.dp, vertical = 6.dp),
                        modifier = Modifier
                            .height(34.dp)
                            .testTag("btn_liquidar_ruta_${ruta.id}"),
                        elevation = ButtonDefaults.buttonElevation(defaultElevation = 2.dp)
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(5.dp)
                        ) {
                            Text(
                                text = "Liquidar ruta",
                                fontSize = 12.sp,
                                fontWeight = FontWeight.Bold,
                                color = Color.White
                            )
                            Icon(
                                imageVector = Icons.AutoMirrored.Filled.ArrowForward,
                                contentDescription = null,
                                tint = Color.White,
                                modifier = Modifier.size(13.dp)
                            )
                        }
                    }
                }
            } else {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = when (ruta.status) {
                            "PENDIENTE_SALIDA" -> "Tocar para ingresar datos de salida"
                            else -> "Ver detalles"
                        },
                        fontSize = 12.sp,
                        fontWeight = FontWeight.SemiBold,
                        color = if (ruta.status == "PENDIENTE_SALIDA") TextSecondary else VerdeAgave
                    )
                    Icon(
                        imageVector = Icons.AutoMirrored.Filled.ArrowForward,
                        contentDescription = null,
                        tint = if (ruta.status == "PENDIENTE_SALIDA") TextSecondary else VerdeAgave,
                        modifier = Modifier.size(14.dp)
                    )
                }
            }
        }
    }
}

@Preview(showBackground = true)
@Composable
fun RepartidorCardItemPreview() {
    TortilleriaDerekTheme {
        RepartidorCardItem(
            ruta = RutaRepartidorEntity(
                id = "1",
                turnoId = "turno_1",
                repartidorNombre = "Carlos Ruiz",
                moto = "Moto 01",
                nombreRuta = "Ruta Norte",
                status = "EN_RUTA",
                cargaInicialKg = 50.0,
                pendienteCobro = 1100.0,
                paquetesCargados = 50
            ),
            onClick = {}
        )
    }
}
