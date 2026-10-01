package com.example.tortilleriaderek.ui.components

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Scale
import androidx.compose.material.icons.filled.TwoWheeler
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
 * Diálogo jerárquico para dar salida a un repartidor (US14).
 * Permite capturar la carga inicial en báscula con presets rápidos (50kg, 70kg, 90kg)
 * y muestra el cálculo dinámico del monto a liquidar esperado ($22/kg).
 */
@Composable
fun DialogSalidaRepartidor(
    driver: RutaRepartidorEntity,
    onConfirmarSalida: (Double) -> Unit,
    onDismiss: () -> Unit,
    precioPaqueteRepartidor: Double = 18.00,
    pesoPaqueteRepartidorKg: Double = 0.80,
    onConfirmarSalidaConPaquetes: ((cargaKg: Double, paquetes: Int) -> Unit)? = null,
    modifier: Modifier = Modifier
) {
    val pesoPaq = if (pesoPaqueteRepartidorKg > 0) pesoPaqueteRepartidorKg else 0.80
    val gramosPaq = (pesoPaq * 1000).toInt()
    var inputPaquetes by remember { mutableStateOf("70") }
    val presets = listOf("40", "60", "80", "100")

    val paquetes = inputPaquetes.toIntOrNull() ?: 0
    val kilosCalculados = paquetes * pesoPaq
    val montoEsperado = paquetes * precioPaqueteRepartidor

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
                    imageVector = Icons.Default.TwoWheeler,
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
                    text = "Asignar Carga de Mayoreo",
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
                        fontWeight = FontWeight.SemiBold,
                        color = MaizPrimary,
                        modifier = Modifier.padding(horizontal = 10.dp, vertical = 4.dp),
                        textAlign = TextAlign.Center
                    )
                }
            }
        },
        text = {
            Column(
                verticalArrangement = Arrangement.spacedBy(14.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                // 1. Selector de Presets de Paquetes
                Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = "Paquetes rápidos:",
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Bold,
                            color = Color(0xFF374151)
                        )
                        Text(
                            text = "Tarifa: $%,.2f/paq • %d g".format(precioPaqueteRepartidor, gramosPaq),
                            fontSize = 11.sp,
                            fontWeight = FontWeight.SemiBold,
                            color = Color(0xFFC04B00)
                        )
                    }
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        presets.forEach { preset ->
                            val isSelected = inputPaquetes.trim() == preset
                            Surface(
                                shape = CircleShape,
                                color = if (isSelected) MaizPrimary else Color(0xFFF3F4F6),
                                border = if (isSelected) null else BorderStroke(1.dp, Color(0xFFD1D5DB)),
                                modifier = Modifier
                                    .weight(1f)
                                    .clickable { inputPaquetes = preset }
                            ) {
                                Text(
                                    text = "$preset paq",
                                    fontSize = 12.sp,
                                    fontWeight = if (isSelected) FontWeight.Bold else FontWeight.SemiBold,
                                    color = if (isSelected) Color.White else Color(0xFF1F2937),
                                    textAlign = TextAlign.Center,
                                    modifier = Modifier.padding(vertical = 8.dp)
                                )
                            }
                        }
                    }
                }

                // 2. Campo de captura de Paquetes a Despachar
                OutlinedTextField(
                    value = inputPaquetes,
                    onValueChange = { inputPaquetes = it },
                    textStyle = LocalTextStyle.current.copy(
                        color = Color(0xFF111827),
                        fontSize = 16.sp,
                        fontWeight = FontWeight.Bold
                    ),
                    label = { Text("Paquetes a despachar", color = Color(0xFF374151), fontWeight = FontWeight.Medium) },
                    leadingIcon = {
                        Icon(
                            imageVector = Icons.Default.TwoWheeler,
                            contentDescription = null,
                            tint = MaizPrimary,
                            modifier = Modifier.size(18.dp)
                        )
                    },
                    suffix = { Text("paquetes", fontWeight = FontWeight.Bold, color = Color(0xFF111827)) },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                    singleLine = true,
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedTextColor = Color(0xFF111827),
                        unfocusedTextColor = Color(0xFF111827),
                        focusedContainerColor = Color.White,
                        unfocusedContainerColor = Color.White,
                        focusedBorderColor = MaizPrimary,
                        unfocusedBorderColor = Color(0xFF9CA3AF),
                        focusedLabelColor = MaizPrimary,
                        unfocusedLabelColor = Color(0xFF374151),
                        focusedSuffixColor = Color(0xFF111827),
                        unfocusedSuffixColor = Color(0xFF111827),
                        cursorColor = MaizPrimary
                    ),
                    shape = RoundedCornerShape(14.dp),
                    modifier = Modifier.fillMaxWidth()
                )

                // 3. Tarjeta de Impacto Financiero en Vivo
                Surface(
                    shape = RoundedCornerShape(16.dp),
                    color = Color(0xFFFFF9F5),
                    border = BorderStroke(1.dp, MaizPrimary.copy(alpha = 0.25f)),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 12.dp, horizontal = 16.dp),
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.spacedBy(2.dp)
                    ) {
                        Text(
                            text = "MONTO ESPERADO A LIQUIDAR ($paquetes PAQ @ $%,.2f)".format(precioPaqueteRepartidor),
                            fontSize = 10.sp,
                            fontWeight = FontWeight.Bold,
                            color = Color(0xFF4B5563),
                            letterSpacing = 0.5.sp
                        )
                        Text(
                            text = "$%,.2f MXN".format(montoEsperado),
                            fontSize = 22.sp,
                            fontWeight = FontWeight.ExtraBold,
                            color = MaizPrimary
                        )
                        Text(
                            text = "$paquetes paquetes equivalen a %.1f kg en báscula (%d g/paq)".format(kilosCalculados, gramosPaq),
                            fontSize = 11.sp,
                            color = Color(0xFFC04B00),
                            fontWeight = FontWeight.SemiBold
                        )
                    }
                }
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    val paqs = inputPaquetes.toIntOrNull() ?: 70
                    val kilos = paqs * pesoPaq
                    if (onConfirmarSalidaConPaquetes != null) {
                        onConfirmarSalidaConPaquetes(kilos, paqs)
                    } else {
                        onConfirmarSalida(kilos)
                    }
                },
                colors = ButtonDefaults.buttonColors(containerColor = MaizPrimary),
                shape = CircleShape,
                modifier = Modifier.height(44.dp)
            ) {
                Text(
                    text = "Dar Salida a Ruta",
                    fontWeight = FontWeight.Bold,
                    fontSize = 14.sp
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
private fun PreviewDialogSalidaRepartidor() {
    TortilleriaDerekTheme {
        DialogSalidaRepartidor(
            driver = RutaRepartidorEntity(
                id = "moto_01",
                turnoId = "t1",
                moto = "Moto 01",
                repartidorNombre = "Carlos Ruiz",
                nombreRuta = "Ruta San Juan",
                status = "PENDIENTE_SALIDA"
            ),
            onConfirmarSalida = {},
            onDismiss = {}
        )
    }
}
