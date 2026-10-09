package com.example.tortilleriaderek.ui.components.repartidores

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.History
import androidx.compose.material.icons.filled.TwoWheeler
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.tortilleriaderek.data.local.entity.RutaRepartidorEntity
import com.example.tortilleriaderek.presentation.venta.VentaUiState
import com.example.tortilleriaderek.ui.theme.*

/**
 * Subcontenido con lista y control de repartidores para RepartidoresScreen.
 */
@Composable
fun RepartidoresSubContent(
    uiState: VentaUiState,
    onOpenSalida: (RutaRepartidorEntity) -> Unit,
    onOpenLiquidar: (RutaRepartidorEntity) -> Unit,
    onShowDetalleLiquidado: (RutaRepartidorEntity) -> Unit,
    onCancelarSalida: (RutaRepartidorEntity) -> Unit = {},
    onNavigateToHistorial: () -> Unit,
    modifier: Modifier = Modifier
) {
    var rutaACancelar by remember { mutableStateOf<RutaRepartidorEntity?>(null) }

    Column(
        modifier = modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(bottom = 20.dp),
        verticalArrangement = Arrangement.spacedBy(14.dp)
    ) {
        // Cabecera de sección + Rutas Activas Badge
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = androidx.compose.ui.res.stringResource(com.example.tortilleriaderek.R.string.repartidores_titulo),
                fontSize = 16.sp,
                fontWeight = FontWeight.Bold,
                color = TextPrimary
            )

            // Badge suave: • N Rutas Activas
            Surface(
                shape = CircleShape,
                color = Color(0xFFFFEDE2)
            ) {
                Row(
                    modifier = Modifier.padding(horizontal = 10.dp, vertical = 5.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(5.dp)
                ) {
                    Box(
                        modifier = Modifier
                            .size(6.dp)
                            .clip(CircleShape)
                            .background(MaizPrimary)
                    )
                    Text(
                        text = "${uiState.rutasActivasCount} Rutas Activas",
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color(0xFFC2410C),
                        style = androidx.compose.ui.text.TextStyle(fontFeatureSettings = "tnum")
                    )
                }
            }
        }

        // Lista de Tarjetas de Repartidores
        if (uiState.rutasRepartidores.isEmpty()) {
            Surface(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(12.dp),
                color = Color.White,
                border = BorderStroke(1.dp, BorderSubtle)
            ) {
                Column(
                    modifier = Modifier.padding(24.dp),
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.TwoWheeler,
                        contentDescription = null,
                        tint = TextSecondary,
                        modifier = Modifier.size(36.dp)
                    )
                    Text(
                        text = "No hay repartidores en este turno",
                        fontWeight = FontWeight.Bold,
                        fontSize = 14.sp,
                        color = TextPrimary
                    )
                    Text(
                        text = "Agrega repartidores desde la pantalla de Configuración para asignarlos a las rutas.",
                        fontSize = 12.sp,
                        color = TextSecondary,
                        textAlign = TextAlign.Center
                    )
                }
            }
        } else {
            uiState.rutasRepartidores.forEach { ruta ->
                RepartidorCardItem(
                    ruta = ruta,
                    onClick = {
                        when (ruta.status) {
                            "EN_RUTA" -> onOpenLiquidar(ruta)
                            "PENDIENTE_SALIDA" -> onOpenSalida(ruta)
                            "LIQUIDADO" -> onShowDetalleLiquidado(ruta)
                        }
                    },
                    onCancelarSalida = {
                        rutaACancelar = ruta
                    }
                )
            }
        }

        // Botón secundario: Historial de Ventas Repartidores (56dp touch target)
        OutlinedButton(
            onClick = onNavigateToHistorial,
            modifier = Modifier
                .fillMaxWidth()
                .height(56.dp)
                .testTag("btn_historial_repartidores"),
            shape = CircleShape,
            border = BorderStroke(1.dp, BorderSubtle),
            colors = ButtonDefaults.outlinedButtonColors(containerColor = Color.White)
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Icon(
                    imageVector = Icons.Default.History,
                    contentDescription = null,
                    tint = TextSecondary,
                    modifier = Modifier.size(18.dp)
                )
                Text(
                    text = "Historial de Ventas (Repartidores)",
                    fontSize = 14.sp,
                    fontWeight = FontWeight.SemiBold,
                    color = TextSecondary
                )
            }
        }

        Spacer(modifier = Modifier.height(8.dp))
    }

    // Diálogo Modal para Confirmar Cancelación de Salida
    rutaACancelar?.let { ruta ->
        AlertDialog(
            onDismissRequest = { rutaACancelar = null },
            modifier = Modifier.testTag("dialog_cancelar_salida_repartidor"),
            shape = RoundedCornerShape(20.dp),
            containerColor = Color.White,
            titleContentColor = Color(0xFF111827),
            textContentColor = Color(0xFF111827),
            title = {
                Text(
                    text = "Cancelar Salida a Ruta",
                    fontWeight = FontWeight.Bold,
                    fontSize = 17.sp,
                    color = Color(0xFF111827)
                )
            },
            text = {
                Text(
                    text = "¿Deseas cancelar la salida de ${ruta.moto} (${ruta.repartidorNombre})?\n\nLa carga de %.1f kg (%d paquetes) se restituirá de inmediato a la tortilla disponible en tienda."
                        .format(ruta.cargaInicialKg, ruta.paquetesCargados),
                    fontSize = 14.sp,
                    color = Color(0xFF374151),
                    lineHeight = 20.sp
                )
            },
            confirmButton = {
                Button(
                    onClick = {
                        onCancelarSalida(ruta)
                        rutaACancelar = null
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFDC2626)),
                    shape = CircleShape,
                    modifier = Modifier
                        .height(56.dp)
                        .testTag("btn_confirmar_cancelar_salida")
                ) {
                    Text("Sí, Cancelar Salida", fontWeight = FontWeight.Bold, color = Color.White)
                }
            },
            dismissButton = {
                OutlinedButton(
                    onClick = { rutaACancelar = null },
                    shape = CircleShape,
                    border = BorderStroke(1.dp, Color(0xFFCBD5E1)),
                    modifier = Modifier
                        .height(56.dp)
                        .testTag("btn_mantener_en_ruta")
                ) {
                    Text("No, Mantener en Ruta", color = Color(0xFF374151), fontWeight = FontWeight.Bold)
                }
            }
        )
    }
}

@Preview(showBackground = true, name = "Teléfono Compacto")
@Preview(showBackground = true, name = "Tablet Landscape (Reparto)", widthDp = 840, heightDp = 480)
@Composable
fun RepartidoresSubContentPreview() {
    TortilleriaDerekTheme {
        RepartidoresSubContent(
            uiState = VentaUiState(),
            onOpenSalida = {},
            onOpenLiquidar = {},
            onShowDetalleLiquidado = {},
            onNavigateToHistorial = {}
        )
    }
}
