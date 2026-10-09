package com.example.tortilleriaderek.ui.components.mostrador

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ReceiptLong
import androidx.compose.material.icons.filled.Payments
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
import com.example.tortilleriaderek.model.ProductItem
import com.example.tortilleriaderek.ui.theme.*

/**
 * Diálogo modal para confirmar el cobro en efectivo de una venta de mostrador (Modo de Diseño).
 */
@Composable
fun DialogConfirmarVentaMostrador(
    proximoTicket: String,
    subtotalOrden: Double,
    products: List<ProductItem>,
    onConfirmar: () -> Unit,
    onDismiss: () -> Unit,
    modifier: Modifier = Modifier
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        modifier = modifier.testTag("dialog_confirmar_venta_mostrador"),
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
                    imageVector = Icons.AutoMirrored.Filled.ReceiptLong,
                    contentDescription = null,
                    tint = MaizPrimary,
                    modifier = Modifier.size(24.dp)
                )
            }
        },
        title = {
            Text(
                text = androidx.compose.ui.res.stringResource(com.example.tortilleriaderek.R.string.dialog_confirmar_venta_titulo),
                fontWeight = FontWeight.Bold,
                fontSize = 18.sp,
                color = TextPrimary,
                textAlign = TextAlign.Center
            )
        },
        text = {
            Column(
                verticalArrangement = Arrangement.spacedBy(14.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                // 1. Badges superiores: Folio Ticket e Insignia De Contado
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Surface(
                        shape = RoundedCornerShape(8.dp),
                        color = SurfaceContainerLow,
                        border = BorderStroke(1.dp, BorderSubtle)
                    ) {
                        Text(
                            text = androidx.compose.ui.res.stringResource(com.example.tortilleriaderek.R.string.mostrador_ticket_prefix, proximoTicket),
                            fontWeight = FontWeight.Bold,
                            color = MaizPrimary,
                            fontSize = 12.sp,
                            style = androidx.compose.ui.text.TextStyle(fontFeatureSettings = "tnum"),
                            modifier = Modifier.padding(horizontal = 10.dp, vertical = 4.dp)
                        )
                    }

                    Surface(
                        shape = RoundedCornerShape(8.dp),
                        color = VerdeAgaveLight
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(4.dp),
                            modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Default.Payments,
                                contentDescription = null,
                                tint = VerdeAgave,
                                modifier = Modifier.size(13.dp)
                            )
                            Text(
                                text = androidx.compose.ui.res.stringResource(com.example.tortilleriaderek.R.string.dialog_confirmar_venta_contado),
                                fontWeight = FontWeight.Bold,
                                color = VerdeAgave,
                                fontSize = 11.sp
                            )
                        }
                    }
                }

                // 2. Tarjeta Destacada de Total a Cobrar
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
                            text = androidx.compose.ui.res.stringResource(com.example.tortilleriaderek.R.string.dialog_confirmar_venta_total_a_cobrar),
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Bold,
                            color = TextSecondary,
                            letterSpacing = 0.5.sp
                        )
                        Text(
                            text = "$%,.2f MXN".format(subtotalOrden),
                            fontSize = 24.sp,
                            fontWeight = FontWeight.ExtraBold,
                            color = MaizPrimary,
                            style = androidx.compose.ui.text.TextStyle(fontFeatureSettings = "tnum")
                        )
                    }
                }

                // 3. Desglose de Productos Seleccionados
                Surface(
                    shape = RoundedCornerShape(12.dp),
                    color = SurfaceWarm,
                    border = BorderStroke(1.dp, BorderLight),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(
                        modifier = Modifier.padding(12.dp),
                        verticalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        Text(
                            text = androidx.compose.ui.res.stringResource(com.example.tortilleriaderek.R.string.dialog_confirmar_venta_articulos),
                            fontSize = 11.sp,
                            fontWeight = FontWeight.SemiBold,
                            color = TextSecondary
                        )
                        products.filter { it.quantity > 0 }.forEach { item ->
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Row(
                                    horizontalArrangement = Arrangement.spacedBy(6.dp),
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Surface(
                                        shape = CircleShape,
                                        color = MaizPrimaryLight
                                    ) {
                                        Text(
                                            text = "${item.quantity}",
                                            fontSize = 11.sp,
                                            fontWeight = FontWeight.Bold,
                                            color = MaizPrimary,
                                            style = androidx.compose.ui.text.TextStyle(fontFeatureSettings = "tnum"),
                                            modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                                        )
                                    }
                                    Text(
                                        text = item.name,
                                        fontSize = 13.sp,
                                        fontWeight = FontWeight.Medium,
                                        color = TextPrimary
                                    )
                                }
                                Text(
                                    text = "$%,.2f".format(item.subtotal),
                                    fontSize = 13.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = TextPrimary,
                                    style = androidx.compose.ui.text.TextStyle(fontFeatureSettings = "tnum")
                                )
                            }
                        }
                    }
                }
            }
        },
        confirmButton = {
            Button(
                onClick = onConfirmar,
                colors = ButtonDefaults.buttonColors(containerColor = MaizPrimary),
                shape = CircleShape,
                modifier = Modifier
                    .height(56.dp)
                    .testTag("btn_cobrar_e_imprimir")
            ) {
                Text(
                    text = androidx.compose.ui.res.stringResource(com.example.tortilleriaderek.R.string.dialog_confirmar_venta_btn_confirmar),
                    fontWeight = FontWeight.Bold,
                    fontSize = 14.sp
                )
            }
        },
        dismissButton = {
            OutlinedButton(
                onClick = onDismiss,
                shape = CircleShape,
                border = BorderStroke(1.dp, BorderSubtle),
                modifier = Modifier
                    .height(56.dp)
                    .testTag("btn_cancelar_cobro")
            ) {
                Text(
                    text = androidx.compose.ui.res.stringResource(com.example.tortilleriaderek.R.string.btn_cancelar),
                    color = TextSecondary,
                    fontWeight = FontWeight.SemiBold
                )
            }
        },
        shape = RoundedCornerShape(24.dp)
    )
}

@Preview(showBackground = true)
@Composable
fun DialogConfirmarVentaMostradorPreview() {
    TortilleriaDerekTheme {
        DialogConfirmarVentaMostrador(
            proximoTicket = "M-0143",
            subtotalOrden = 48.00,
            products = listOf(ProductItem(id = 1, name = "Kilo de Tortilla", price = 24.0, quantity = 2, pesoGramos = 1000)),
            onConfirmar = {},
            onDismiss = {}
        )
    }
}
