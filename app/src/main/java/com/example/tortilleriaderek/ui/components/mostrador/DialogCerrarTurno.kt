package com.example.tortilleriaderek.ui.components.mostrador

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
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
import com.example.tortilleriaderek.ui.theme.*

/**
 * Diálogo modal para confirmar el cierre de turno y corte de caja con bloqueo por rutas activas.
 */
@Composable
fun DialogCerrarTurno(
    tieneRutasActivas: Boolean,
    rutasEnCalle: List<RutaRepartidorEntity>,
    totalVentasDia: Double,
    onConfirmar: () -> Unit,
    onDismiss: () -> Unit,
    modifier: Modifier = Modifier
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        modifier = modifier.testTag("dialog_cerrar_turno"),
        containerColor = Color.White,
        titleContentColor = Color(0xFF111827),
        textContentColor = Color(0xFF111827),
        icon = {
            Box(
                modifier = Modifier
                    .size(46.dp)
                    .clip(CircleShape)
                    .background(if (tieneRutasActivas) TerracotaLight else MaizPrimaryLight),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = if (tieneRutasActivas) Icons.Default.Warning else Icons.Default.Lock,
                    contentDescription = null,
                    tint = if (tieneRutasActivas) TerracotaSecondary else MaizPrimary,
                    modifier = Modifier.size(24.dp)
                )
            }
        },
        title = {
            Text(
                text = if (tieneRutasActivas) {
                    androidx.compose.ui.res.stringResource(com.example.tortilleriaderek.R.string.dialog_cerrar_turno_titulo_rutas_activas)
                } else {
                    androidx.compose.ui.res.stringResource(com.example.tortilleriaderek.R.string.dialog_cerrar_turno_titulo_normal)
                },
                fontWeight = FontWeight.Bold,
                fontSize = 17.sp,
                color = if (tieneRutasActivas) TerracotaSecondary else TextPrimary,
                textAlign = TextAlign.Center
            )
        },
        text = {
            Column(
                verticalArrangement = Arrangement.spacedBy(10.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                if (tieneRutasActivas) {
                    Surface(
                        shape = RoundedCornerShape(12.dp),
                        color = TerracotaLight.copy(alpha = 0.5f),
                        border = BorderStroke(1.dp, TerracotaSecondary.copy(alpha = 0.4f)),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Column(
                            modifier = Modifier.padding(12.dp),
                            verticalArrangement = Arrangement.spacedBy(6.dp)
                        ) {
                            Text(
                                text = androidx.compose.ui.res.stringResource(com.example.tortilleriaderek.R.string.dialog_cerrar_turno_rutas_advertencia),
                                fontSize = 12.sp,
                                fontWeight = FontWeight.Bold,
                                color = TerracotaSecondary
                            )
                            rutasEnCalle.forEach { ruta ->
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Text(
                                        text = "• ${ruta.moto} (${ruta.repartidorNombre})",
                                        fontSize = 12.sp,
                                        fontWeight = FontWeight.Medium,
                                        color = TextPrimary
                                    )
                                    Text(
                                        text = androidx.compose.ui.res.stringResource(com.example.tortilleriaderek.R.string.repartidores_lbl_kilos_en_ruta, ruta.cargaInicialKg),
                                        fontSize = 12.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = TerracotaSecondary,
                                        style = androidx.compose.ui.text.TextStyle(fontFeatureSettings = "tnum")
                                    )
                                }
                            }
                        }
                    }
                    Text(
                        text = androidx.compose.ui.res.stringResource(com.example.tortilleriaderek.R.string.dialog_cerrar_turno_rutas_instruccion),
                        fontSize = 12.sp,
                        color = TextSecondary,
                        textAlign = TextAlign.Center
                    )
                } else {
                    Text(
                        text = androidx.compose.ui.res.stringResource(
                            com.example.tortilleriaderek.R.string.dialog_cerrar_turno_confirmacion_mensaje,
                            "$%,.2f".format(totalVentasDia)
                        ),
                        fontSize = 13.sp,
                        color = TextSecondary,
                        textAlign = TextAlign.Center
                    )
                }
            }
        },
        confirmButton = {
            Button(
                onClick = onConfirmar,
                enabled = !tieneRutasActivas,
                colors = ButtonDefaults.buttonColors(
                    containerColor = MaizPrimary,
                    disabledContainerColor = Color.LightGray.copy(alpha = 0.5f)
                ),
                shape = CircleShape,
                modifier = Modifier
                    .height(56.dp)
                    .testTag("btn_confirmar_cierre_turno")
            ) {
                Text(
                    text = if (tieneRutasActivas) {
                        androidx.compose.ui.res.stringResource(com.example.tortilleriaderek.R.string.dialog_cerrar_turno_btn_bloqueado)
                    } else {
                        androidx.compose.ui.res.stringResource(com.example.tortilleriaderek.R.string.dialog_cerrar_turno_btn_confirmar)
                    },
                    fontWeight = FontWeight.Bold
                )
            }
        },
        dismissButton = {
            OutlinedButton(
                onClick = onDismiss,
                shape = CircleShape,
                modifier = Modifier
                    .height(56.dp)
                    .testTag("btn_cancelar_cierre_turno")
            ) {
                Text(
                    text = androidx.compose.ui.res.stringResource(com.example.tortilleriaderek.R.string.btn_cancelar)
                )
            }
        },
        shape = RoundedCornerShape(22.dp)
    )
}

@Preview(showBackground = true)
@Composable
fun DialogCerrarTurnoPreview() {
    TortilleriaDerekTheme {
        DialogCerrarTurno(
            tieneRutasActivas = false,
            rutasEnCalle = emptyList(),
            totalVentasDia = 18450.0,
            onConfirmar = {},
            onDismiss = {}
        )
    }
}
