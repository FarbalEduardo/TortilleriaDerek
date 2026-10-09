package com.example.tortilleriaderek.ui.components.mostrador

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Remove
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
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
 * Fila interactiva de producto con Stepper táctil para el mostrador.
 */
@Composable
fun MostradorProductRow(
    product: ProductItem,
    onQuantityChanged: (Int) -> Unit,
    modifier: Modifier = Modifier
) {
    Row(
        modifier = modifier
            .fillMaxWidth()
            .padding(horizontal = 14.dp, vertical = 12.dp)
            .testTag("producto_row_${product.id}"),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        // Izquierda: Nombre + Precio unitario + Peso
        Column(
            modifier = Modifier.weight(1f),
            verticalArrangement = Arrangement.spacedBy(2.dp)
        ) {
            Text(
                text = product.name,
                fontSize = 15.sp,
                fontWeight = FontWeight.Bold,
                color = TextPrimary
            )
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(6.dp)
            ) {
                Text(
                    text = "$%,.2f".format(product.price),
                    fontSize = 12.sp,
                    fontWeight = FontWeight.Bold,
                    color = MaizPrimary
                )
                Text(
                    text = "• ${if (product.pesoGramos >= 1000) "1 kg" else "${product.pesoGramos} g"}",
                    fontSize = 12.sp,
                    fontWeight = FontWeight.Medium,
                    color = TextSecondary
                )
            }
        }

        // Derecha: Stepper pill + Subtotal
        Column(
            horizontalAlignment = Alignment.End,
            verticalArrangement = Arrangement.spacedBy(3.dp)
        ) {
            Row(
                modifier = Modifier
                    .clip(CircleShape)
                    .background(Color(0xFFF6ECE6).copy(alpha = 0.6f))
                    .border(1.dp, BorderSubtle.copy(alpha = 0.8f), CircleShape)
                    .padding(2.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                // Botón – (44dp touch target con guantes)
                Box(
                    modifier = Modifier
                        .size(44.dp)
                        .clip(CircleShape)
                        .background(Color.White)
                        .border(1.dp, BorderSubtle.copy(alpha = 0.6f), CircleShape)
                        .clickable(
                            enabled = product.quantity > 0,
                            onClick = { onQuantityChanged(product.quantity - 1) }
                        )
                        .testTag("btn_decrementar_producto_${product.id}"),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = Icons.Default.Remove,
                        contentDescription = androidx.compose.ui.res.stringResource(com.example.tortilleriaderek.R.string.txt_disminuir),
                        tint = if (product.quantity > 0) TextPrimary else TextMuted,
                        modifier = Modifier.size(18.dp)
                    )
                }

                // Cantidad con cifras tabulares tnum
                Text(
                    text = "${product.quantity}",
                    fontSize = 15.sp,
                    fontWeight = FontWeight.Bold,
                    color = if (product.quantity > 0) TextPrimary else TextMuted,
                    textAlign = TextAlign.Center,
                    style = androidx.compose.ui.text.TextStyle(fontFeatureSettings = "tnum"),
                    modifier = Modifier.widthIn(min = 32.dp)
                )

                // Botón + (44dp touch target con guantes)
                Box(
                    modifier = Modifier
                        .size(44.dp)
                        .clip(CircleShape)
                        .background(MaizPrimary)
                        .clickable { onQuantityChanged(product.quantity + 1) }
                        .testTag("btn_incrementar_producto_${product.id}"),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = Icons.Default.Add,
                        contentDescription = androidx.compose.ui.res.stringResource(com.example.tortilleriaderek.R.string.txt_aumentar),
                        tint = Color.White,
                        modifier = Modifier.size(18.dp)
                    )
                }
            }

            // Subtotal
            Text(
                text = "$%,.2f".format(product.subtotal),
                fontSize = 13.sp,
                fontWeight = FontWeight.Bold,
                color = if (product.quantity > 0) TextPrimary else TextMuted,
                style = androidx.compose.ui.text.TextStyle(fontFeatureSettings = "tnum"),
            )
        }
    }
}

@Preview(showBackground = true)
@Composable
fun MostradorProductRowPreview() {
    TortilleriaDerekTheme {
        MostradorProductRow(
            product = ProductItem(id = 1, name = "Kilo de Tortilla", price = 24.0, quantity = 2, pesoGramos = 1000),
            onQuantityChanged = {}
        )
    }
}
