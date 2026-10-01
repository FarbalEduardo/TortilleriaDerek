package com.example.tortilleriaderek.ui.components

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Payments
import androidx.compose.material.icons.filled.RemoveShoppingCart
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.tortilleriaderek.data.local.entity.RutaRepartidorEntity
import com.example.tortilleriaderek.ui.theme.*

/**
 * Diálogo jerárquico para liquidar a un repartidor (US14).
 * Presenta la carga inicial despachada, cálculo dinámico en tiempo real
 * de la merma/devolución y los kilos netos vendidos, confirmando el efectivo
 * antes de emitir el ticket correlativo R-XXXX.
 */
@Composable
fun DialogLiquidarRepartidor(
    driver: RutaRepartidorEntity,
    onConfirmarLiquidar: (devolucionKg: Double, cobrado: Double) -> Unit,
    onDismiss: () -> Unit,
    precioPaqueteRepartidor: Double = 18.00,
    pesoPaqueteRepartidorKg: Double = 0.80,
    onConfirmarLiquidarConPaquetes: ((devolucionKg: Double, cobrado: Double, paquetesDevueltos: Int) -> Unit)? = null,
    modifier: Modifier = Modifier
) {
    val pesoPaq = if (pesoPaqueteRepartidorKg > 0) pesoPaqueteRepartidorKg else 0.80
    val gramosPaq = (pesoPaq * 1000).toInt()
    val paquetesIniciales = if (driver.paquetesCargados > 0) {
        driver.paquetesCargados
    } else {
        (driver.cargaInicialKg / pesoPaq).toInt()
    }

    var inputDevolucionPaquetes by remember { mutableStateOf("0") }
    val devolucionPaquetes = inputDevolucionPaquetes.toIntOrNull() ?: 0
    val devolucionKg = devolucionPaquetes * pesoPaq
    val paquetesVendidos = (paquetesIniciales - devolucionPaquetes).coerceAtLeast(0)
    val kilosVendidos = paquetesVendidos * pesoPaq
    val montoCalculado = paquetesVendidos * precioPaqueteRepartidor

    var inputCobradoMonto by remember { mutableStateOf("%.2f".format(montoCalculado)) }

    val presetsDevolucion = listOf("0", "2", "5", "10")

    val esDevolucionExcedida = devolucionPaquetes > paquetesIniciales || devolucionPaquetes < 0
    val montoCobradoParsed = inputCobradoMonto.toDoubleOrNull()
    val esMontoInvalido = montoCobradoParsed == null || montoCobradoParsed < 0.0
    val puedeLiquidar = !esDevolucionExcedida && !esMontoInvalido

    AlertDialog(
        onDismissRequest = onDismiss,
        modifier = modifier,
        containerColor = Color.White,
        titleContentColor = Color(0xFF111827),
        textContentColor = Color(0xFF111827),
        icon = {
            Box(
                modifier = Modifier
                    .size(48.dp)
                    .clip(CircleShape)
                    .background(MaizPrimaryLight),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = Icons.Default.CheckCircle,
                    contentDescription = null,
                    tint = MaizPrimary,
                    modifier = Modifier.size(26.dp)
                )
            }
        },
        title = {
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.spacedBy(4.dp)
            ) {
                Text(
                    text = "Liquidar Ruta de Mayoreo",
                    fontWeight = FontWeight.Bold,
                    fontSize = 18.sp,
                    color = Color(0xFF111827),
                    textAlign = TextAlign.Center
                )
                Surface(
                    shape = RoundedCornerShape(8.dp),
                    color = SurfaceContainerLow,
                    border = BorderStroke(1.dp, BorderSubtle)
                ) {
                    Text(
                        text = "${driver.moto} • ${driver.repartidorNombre} (${driver.nombreRuta})",
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Bold,
                        color = MaizPrimary,
                        modifier = Modifier.padding(horizontal = 10.dp, vertical = 4.dp),
                        textAlign = TextAlign.Center
                    )
                }
            }
        },
        text = {
            Column(
                verticalArrangement = Arrangement.spacedBy(12.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                // 1. Resumen de Carga Despachada y Tarifa
                Surface(
                    shape = RoundedCornerShape(12.dp),
                    color = Color(0xFFF9FAFB),
                    border = BorderStroke(1.dp, Color(0xFFE5E7EB)),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(12.dp),
                        verticalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Column {
                                Text(
                                    text = "Carga inicial despachada:",
                                    fontSize = 11.sp,
                                    color = Color(0xFF4B5563),
                                    fontWeight = FontWeight.Bold
                                )
                                Text(
                                    text = "$paquetesIniciales paq (%.1f kg)".format(driver.cargaInicialKg),
                                    fontSize = 15.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = Color(0xFF111827)
                                )
                            }
                            Column(horizontalAlignment = Alignment.End) {
                                Text(
                                    text = "Hora de salida:",
                                    fontSize = 11.sp,
                                    color = Color(0xFF4B5563),
                                    fontWeight = FontWeight.Bold
                                )
                                Text(
                                    text = driver.horaSalida ?: "--:--",
                                    fontSize = 13.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = Color(0xFF111827)
                                )
                            }
                        }
                        HorizontalDivider(color = Color(0xFFE5E7EB), thickness = 1.dp)
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = "Tarifa de mayoreo:",
                                fontSize = 11.sp,
                                color = Color(0xFF4B5563),
                                fontWeight = FontWeight.Medium
                            )
                            Text(
                                text = "$%,.2f / paq (%d g)".format(precioPaqueteRepartidor, gramosPaq),
                                fontSize = 12.sp,
                                fontWeight = FontWeight.Bold,
                                color = Color(0xFFC04B00)
                            )
                        }
                    }
                }

                // 2. Presets de Devolución Rápida
                Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                    Text(
                        text = "Devolución rápida de paquetes:",
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color(0xFF374151)
                    )
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        presetsDevolucion.forEach { preset ->
                            val isSelected = inputDevolucionPaquetes.trim() == preset
                            Surface(
                                shape = CircleShape,
                                color = if (isSelected) TerracotaSecondary else Color(0xFFF3F4F6),
                                border = if (isSelected) null else BorderStroke(1.dp, Color(0xFFD1D5DB)),
                                modifier = Modifier
                                    .weight(1f)
                                    .clickable {
                                        inputDevolucionPaquetes = preset
                                        val devP = preset.toIntOrNull() ?: 0
                                        val vendP = (paquetesIniciales - devP).coerceAtLeast(0)
                                        inputCobradoMonto = "%.2f".format(vendP * precioPaqueteRepartidor)
                                    }
                            ) {
                                Text(
                                    text = "$preset paq",
                                    fontSize = 11.sp,
                                    fontWeight = if (isSelected) FontWeight.Bold else FontWeight.SemiBold,
                                    color = if (isSelected) Color.White else Color(0xFF1F2937),
                                    textAlign = TextAlign.Center,
                                    modifier = Modifier.padding(vertical = 6.dp)
                                )
                            }
                        }
                    }
                }

                // 3. Captura de Paquetes Devueltos / Merma
                OutlinedTextField(
                    value = inputDevolucionPaquetes,
                    onValueChange = {
                        inputDevolucionPaquetes = it
                        val devP = it.toIntOrNull() ?: 0
                        val vendP = (paquetesIniciales - devP).coerceAtLeast(0)
                        inputCobradoMonto = "%.2f".format(vendP * precioPaqueteRepartidor)
                    },
                    textStyle = LocalTextStyle.current.copy(
                        color = Color(0xFF111827),
                        fontSize = 15.sp,
                        fontWeight = FontWeight.Bold
                    ),
                    label = { Text("Paquetes devueltos (sobrante no vendido)", color = Color(0xFF374151), fontWeight = FontWeight.Medium) },
                    leadingIcon = {
                        Icon(
                            imageVector = Icons.Default.RemoveShoppingCart,
                            contentDescription = null,
                            tint = TerracotaSecondary,
                            modifier = Modifier.size(18.dp)
                        )
                    },
                    suffix = {
                        Text(
                            text = "paq (%.1f kg)".format(devolucionKg),
                            fontWeight = FontWeight.Bold,
                            color = if (esDevolucionExcedida) Color(0xFFDC2626) else Color(0xFF111827)
                        )
                    },
                    isError = esDevolucionExcedida,
                    supportingText = {
                        if (esDevolucionExcedida) {
                            Text(
                                text = "La devolución ($devolucionPaquetes paq / %.1f kg) no puede superar la carga despachada ($paquetesIniciales paq / %.1f kg)"
                                    .format(devolucionKg, driver.cargaInicialKg),
                                color = Color(0xFFDC2626),
                                fontSize = 11.sp,
                                fontWeight = FontWeight.Bold
                            )
                        }
                    },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                    singleLine = true,
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedTextColor = Color(0xFF111827),
                        unfocusedTextColor = Color(0xFF111827),
                        focusedContainerColor = Color.White,
                        unfocusedContainerColor = Color.White,
                        focusedBorderColor = TerracotaSecondary,
                        unfocusedBorderColor = Color(0xFF9CA3AF),
                        errorBorderColor = Color(0xFFDC2626),
                        focusedLabelColor = TerracotaSecondary,
                        unfocusedLabelColor = Color(0xFF374151),
                        errorLabelColor = Color(0xFFDC2626),
                        focusedSuffixColor = Color(0xFF111827),
                        unfocusedSuffixColor = Color(0xFF111827),
                        cursorColor = TerracotaSecondary
                    ),
                    shape = RoundedCornerShape(14.dp),
                    modifier = Modifier.fillMaxWidth()
                )

                // 4. Resumen Financiero Dinámico
                Surface(
                    shape = RoundedCornerShape(16.dp),
                    color = Color(0xFFF0FDF4),
                    border = BorderStroke(1.dp, VerdeAgave.copy(alpha = 0.3f)),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 10.dp, horizontal = 14.dp),
                        verticalArrangement = Arrangement.spacedBy(4.dp)
                    ) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = "Paquetes vendidos:",
                                fontSize = 12.sp,
                                color = Color(0xFF4B5563),
                                fontWeight = FontWeight.Bold
                            )
                            Text(
                                text = "$paquetesVendidos paq (%.1f kg)".format(kilosVendidos),
                                fontSize = 13.sp,
                                fontWeight = FontWeight.Bold,
                                color = Color(0xFF111827)
                            )
                        }
                        HorizontalDivider(color = VerdeAgaveLight, thickness = 1.dp)
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = "TOTAL A COBRAR EN CAJA:",
                                fontSize = 11.sp,
                                fontWeight = FontWeight.Bold,
                                color = VerdeAgave
                            )
                            Text(
                                text = "$%,.2f MXN".format(montoCalculado),
                                fontSize = 18.sp,
                                fontWeight = FontWeight.ExtraBold,
                                color = VerdeAgave
                            )
                        }
                    }
                }

                // 5. Confirmación de Efectivo Recibido
                OutlinedTextField(
                    value = inputCobradoMonto,
                    onValueChange = { inputCobradoMonto = it },
                    textStyle = LocalTextStyle.current.copy(
                        color = Color(0xFF111827),
                        fontSize = 15.sp,
                        fontWeight = FontWeight.Bold
                    ),
                    label = { Text("Efectivo recibido en caja", color = Color(0xFF374151), fontWeight = FontWeight.Medium) },
                    leadingIcon = {
                        Icon(
                            imageVector = Icons.Default.Payments,
                            contentDescription = null,
                            tint = VerdeAgave,
                            modifier = Modifier.size(18.dp)
                        )
                    },
                    prefix = { Text("$ ", fontWeight = FontWeight.Bold, color = Color(0xFF111827)) },
                    suffix = { Text("MXN", fontSize = 12.sp, fontWeight = FontWeight.Bold, color = Color(0xFF111827)) },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                    singleLine = true,
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedTextColor = Color(0xFF111827),
                        unfocusedTextColor = Color(0xFF111827),
                        focusedContainerColor = Color.White,
                        unfocusedContainerColor = Color.White,
                        focusedBorderColor = VerdeAgave,
                        unfocusedBorderColor = Color(0xFF9CA3AF),
                        focusedLabelColor = VerdeAgave,
                        unfocusedLabelColor = Color(0xFF374151),
                        focusedPrefixColor = Color(0xFF111827),
                        unfocusedPrefixColor = Color(0xFF111827),
                        focusedSuffixColor = Color(0xFF111827),
                        unfocusedSuffixColor = Color(0xFF111827),
                        cursorColor = VerdeAgave
                    ),
                    shape = RoundedCornerShape(14.dp),
                    modifier = Modifier.fillMaxWidth()
                )
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    val cobrado = inputCobradoMonto.toDoubleOrNull() ?: montoCalculado
                    if (onConfirmarLiquidarConPaquetes != null) {
                        onConfirmarLiquidarConPaquetes(devolucionKg, cobrado, devolucionPaquetes)
                    } else {
                        onConfirmarLiquidar(devolucionKg, cobrado)
                    }
                },
                enabled = puedeLiquidar,
                colors = ButtonDefaults.buttonColors(
                    containerColor = MaizPrimary,
                    contentColor = Color.White,
                    disabledContainerColor = Color(0xFFE5E7EB),
                    disabledContentColor = Color(0xFF9CA3AF)
                ),
                shape = CircleShape,
                modifier = Modifier.height(44.dp)
            ) {
                Text(
                    text = "Liquidar y Emitir Ticket R-",
                    fontWeight = FontWeight.Bold,
                    fontSize = 14.sp,
                    color = Color.White
                )
            }
        },
        dismissButton = {
            OutlinedButton(
                onClick = onDismiss,
                shape = CircleShape,
                border = BorderStroke(1.dp, Color(0xFFCBD5E1)),
                modifier = Modifier.height(44.dp)
            ) {
                Text(
                    text = "Cancelar",
                    color = Color(0xFF374151),
                    fontWeight = FontWeight.Bold
                )
            }
        },
        shape = RoundedCornerShape(24.dp)
    )
}

@Preview(showBackground = true)
@Composable
private fun PreviewDialogLiquidarRepartidor() {
    TortilleriaDerekTheme {
        DialogLiquidarRepartidor(
            driver = RutaRepartidorEntity(
                id = "moto_01",
                turnoId = "t1",
                moto = "Moto 01",
                repartidorNombre = "Carlos Ruiz",
                nombreRuta = "Ruta San Juan",
                status = "EN_RUTA",
                cargaInicialKg = 90.0,
                pendienteCobro = 1980.00,
                horaSalida = "08:30 AM"
            ),
            onConfirmarLiquidar = { _, _ -> },
            onDismiss = {}
        )
    }
}
