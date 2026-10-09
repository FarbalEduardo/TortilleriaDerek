package com.example.tortilleriaderek.ui.components.repartidores

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.example.tortilleriaderek.data.local.entity.RutaRepartidorEntity
import com.example.tortilleriaderek.ui.theme.*

/**
 * Diálogo modal para visualizar el detalle de una ruta liquidada.
 */
@Composable
fun DialogDetalleLiquidacion(
    driver: RutaRepartidorEntity,
    onDismiss: () -> Unit,
    modifier: Modifier = Modifier
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        modifier = modifier.testTag("dialog_detalle_liquidacion"),
        shape = RoundedCornerShape(22.dp),
        containerColor = Color.White,
        titleContentColor = Color(0xFF111827),
        textContentColor = Color(0xFF111827),
        title = {
            Text(
                text = androidx.compose.ui.res.stringResource(com.example.tortilleriaderek.R.string.dialog_detalle_liquidacion_titulo, driver.moto),
                fontWeight = FontWeight.Bold,
                color = Color(0xFF111827)
            )
        },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                Text("• Repartidor: ${driver.repartidorNombre}", color = Color(0xFF374151))
                Text(
                    text = "• Folio de liquidación: ${driver.folioTicket ?: "R-0038"}",
                    fontWeight = FontWeight.Bold,
                    color = MaizPrimary
                )
                Text(
                    text = "• " + androidx.compose.ui.res.stringResource(com.example.tortilleriaderek.R.string.repartidores_lbl_kilos_entregados, driver.entregadoKg),
                    color = Color(0xFF111827),
                    style = androidx.compose.ui.text.TextStyle(fontFeatureSettings = "tnum")
                )
                Text(
                    text = "• " + androidx.compose.ui.res.stringResource(com.example.tortilleriaderek.R.string.repartidores_lbl_kilos_devueltos, driver.devolucionKg),
                    color = Color(0xFF111827),
                    style = androidx.compose.ui.text.TextStyle(fontFeatureSettings = "tnum")
                )
                Text(
                    text = "• " + androidx.compose.ui.res.stringResource(
                        com.example.tortilleriaderek.R.string.repartidores_lbl_total_liquidado,
                        "$%,.2f".format(driver.cobrado)
                    ),
                    fontWeight = FontWeight.ExtraBold,
                    color = VerdeAgave,
                    style = androidx.compose.ui.text.TextStyle(fontFeatureSettings = "tnum")
                )
            }
        },
        confirmButton = {
            Button(
                onClick = onDismiss,
                colors = ButtonDefaults.buttonColors(containerColor = MaizPrimary),
                shape = CircleShape,
                modifier = Modifier
                    .height(56.dp)
                    .testTag("btn_aceptar_detalle_liquidacion")
            ) {
                Text(
                    text = androidx.compose.ui.res.stringResource(com.example.tortilleriaderek.R.string.btn_aceptar),
                    fontWeight = FontWeight.Bold,
                    color = Color.White
                )
            }
        }
    )
}

@Preview(showBackground = true)
@Composable
fun DialogDetalleLiquidacionPreview() {
    TortilleriaDerekTheme {
        DialogDetalleLiquidacion(
            driver = RutaRepartidorEntity(
                id = "1",
                turnoId = "turno_1",
                repartidorNombre = "Carlos Ruiz",
                moto = "Moto 01",
                nombreRuta = "Ruta Norte",
                status = "LIQUIDADO",
                cargaInicialKg = 50.0,
                entregadoKg = 48.0,
                devolucionKg = 2.0,
                cobrado = 1056.0,
                folioTicket = "R-0038"
            ),
            onDismiss = {}
        )
    }
}
