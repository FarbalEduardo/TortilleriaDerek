package com.example.tortilleriaderek.ui.components.mostrador

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowForward
import androidx.compose.material.icons.automirrored.filled.ReceiptLong
import androidx.compose.material.icons.filled.History
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.tortilleriaderek.presentation.venta.VentaUiState
import com.example.tortilleriaderek.ui.theme.*

/**
 * Subcontenido de mostrador con productos, botón de ingreso de venta y navegación a historial.
 */
@Composable
fun MostradorSubContent(
    uiState: VentaUiState,
    onQuantityChanged: (Int, Int) -> Unit,
    onOpenConfirmarVenta: () -> Unit,
    onNavigateToHistorial: () -> Unit,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(bottom = 20.dp),
        verticalArrangement = Arrangement.spacedBy(14.dp)
    ) {
        // Cabecera de sección + Badge Folio Ticket
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = androidx.compose.ui.res.stringResource(com.example.tortilleriaderek.R.string.mostrador_registro_titulo),
                fontSize = 16.sp,
                fontWeight = FontWeight.Bold,
                color = TextPrimary
            )

            // Ticket badge suave: Ticket: M-0143
            Surface(
                shape = CircleShape,
                color = Color(0xFFFFEDE2)
            ) {
                Text(
                    text = androidx.compose.ui.res.stringResource(com.example.tortilleriaderek.R.string.mostrador_ticket_prefix, uiState.proximoTicketMostrador),
                    fontSize = 11.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color(0xFFC2410C),
                    style = androidx.compose.ui.text.TextStyle(fontFeatureSettings = "tnum"),
                    modifier = Modifier.padding(horizontal = 10.dp, vertical = 5.dp)
                )
            }
        }

        // Lista de Productos (Card blanca con los artículos)
        Surface(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(20.dp),
            color = Color.White,
            border = BorderStroke(1.dp, BorderSubtle.copy(alpha = 0.8f)),
            shadowElevation = 1.dp
        ) {
            Column {
                uiState.products.forEachIndexed { index, product ->
                    MostradorProductRow(
                        product = product,
                        onQuantityChanged = { newQty ->
                            onQuantityChanged(product.id, newQty)
                        }
                    )
                    if (index < uiState.products.size - 1) {
                        HorizontalDivider(
                            color = BorderLight,
                            thickness = 1.dp,
                            modifier = Modifier.padding(horizontal = 14.dp)
                        )
                    }
                }
            }
        }

        // Botón CTA Principal: Ingresar Venta (Touch Target Industrial Óptimo: 64dp)
        val orderTotal = uiState.subtotalOrdenActual
        val isButtonEnabled = orderTotal > 0 && !uiState.isTurnoCerrado

        Button(
            onClick = onOpenConfirmarVenta,
            modifier = Modifier
                .fillMaxWidth()
                .height(64.dp)
                .testTag("btn_ingresar_venta_mostrador"),
            shape = CircleShape,
            colors = ButtonDefaults.buttonColors(
                containerColor = if (isButtonEnabled) MaizPrimary else MaizPrimary.copy(alpha = 0.45f),
                disabledContainerColor = MaizPrimary.copy(alpha = 0.35f)
            ),
            enabled = isButtonEnabled,
            elevation = ButtonDefaults.buttonElevation(defaultElevation = 3.dp, pressedElevation = 1.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Icon(
                        imageVector = Icons.AutoMirrored.Filled.ReceiptLong,
                        contentDescription = null,
                        tint = Color.White,
                        modifier = Modifier.size(22.dp)
                    )
                    Text(
                        text = androidx.compose.ui.res.stringResource(com.example.tortilleriaderek.R.string.mostrador_btn_ingresar_venta),
                        fontSize = 16.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color.White
                    )
                }

                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    Text(
                        text = if (orderTotal > 0) "$%,.2f MXN".format(orderTotal) else "—",
                        fontSize = 16.sp,
                        fontWeight = FontWeight.ExtraBold,
                        color = Color.White,
                        style = androidx.compose.ui.text.TextStyle(fontFeatureSettings = "tnum")
                    )
                    Icon(
                        imageVector = Icons.AutoMirrored.Filled.ArrowForward,
                        contentDescription = null,
                        tint = Color.White,
                        modifier = Modifier.size(18.dp)
                    )
                }
            }
        }

        // Botón Secundario: Historial de Ventas (56dp touch target)
        OutlinedButton(
            onClick = onNavigateToHistorial,
            modifier = Modifier
                .fillMaxWidth()
                .height(56.dp)
                .testTag("btn_historial_mostrador"),
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
                    text = androidx.compose.ui.res.stringResource(com.example.tortilleriaderek.R.string.mostrador_ver_historial),
                    fontSize = 14.sp,
                    fontWeight = FontWeight.SemiBold,
                    color = TextSecondary
                )
            }
        }

        Spacer(modifier = Modifier.height(8.dp))
    }
}

@Preview(showBackground = true, name = "Teléfono Compacto")
@Preview(showBackground = true, name = "Tablet Landscape (Mostrador)", widthDp = 840, heightDp = 480)
@Composable
fun MostradorSubContentPreview() {
    TortilleriaDerekTheme {
        MostradorSubContent(
            uiState = VentaUiState(),
            onQuantityChanged = { _, _ -> },
            onOpenConfirmarVenta = {},
            onNavigateToHistorial = {}
        )
    }
}
