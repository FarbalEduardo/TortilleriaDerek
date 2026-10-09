package com.example.tortilleriaderek.ui.components.historial

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.layout.height
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
import androidx.compose.ui.unit.sp
import com.example.tortilleriaderek.presentation.historial.HistorialItemUi
import com.example.tortilleriaderek.ui.theme.*

/**
 * Diálogo modal para confirmar la eliminación de un ticket/venta en Historial.
 */
@Composable
fun DialogConfirmarEliminarVenta(
    tx: HistorialItemUi,
    onConfirmar: () -> Unit,
    onCancelar: () -> Unit,
    modifier: Modifier = Modifier
) {
    AlertDialog(
        onDismissRequest = onCancelar,
        modifier = modifier.testTag("dialog_confirmar_eliminar_venta"),
        shape = RoundedCornerShape(20.dp),
        containerColor = Color.White,
        title = {
            Text(
                text = "Eliminar Ticket ${tx.ticketFolio}",
                fontWeight = FontWeight.Bold,
                fontSize = 17.sp,
                color = Color(0xFF111827)
            )
        },
        text = {
            Text(
                text = "¿Estás seguro de que deseas eliminar esta venta de $%,.2f MXN registrada a las %s?"
                    .format(tx.monto, tx.hora),
                fontSize = 14.sp,
                color = Color(0xFF374151)
            )
        },
        confirmButton = {
            Button(
                onClick = onConfirmar,
                colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFDC2626)),
                shape = CircleShape,
                modifier = Modifier
                    .height(56.dp)
                    .testTag("btn_confirmar_eliminar_venta")
            ) {
                Text(
                    text = androidx.compose.ui.res.stringResource(com.example.tortilleriaderek.R.string.btn_eliminar),
                    fontWeight = FontWeight.Bold,
                    color = Color.White
                )
            }
        },
        dismissButton = {
            OutlinedButton(
                onClick = onCancelar,
                shape = CircleShape,
                border = BorderStroke(1.dp, Color(0xFFCBD5E1)),
                modifier = Modifier
                    .height(56.dp)
                    .testTag("btn_cancelar_eliminar_venta")
            ) {
                Text(
                    text = androidx.compose.ui.res.stringResource(com.example.tortilleriaderek.R.string.btn_cancelar),
                    color = Color(0xFF374151),
                    fontWeight = FontWeight.Bold
                )
            }
        }
    )
}

@Preview
@Composable
fun DialogConfirmarEliminarVentaPreview() {
    TortilleriaDerekTheme {
        DialogConfirmarEliminarVenta(
            tx = HistorialItemUi(
                id = "1",
                ticketFolio = "M-0142",
                tipo = "MOSTRADOR",
                fechaTimestamp = System.currentTimeMillis(),
                fechaTexto = "Hoy",
                hora = "11:42 AM",
                monto = 48.00,
                detalleProductos = "2 Kilo de Tortilla",
                usuarioNombre = "admin1"
            ),
            onConfirmar = {},
            onCancelar = {}
        )
    }
}
