package com.example.tortilleriaderek.ui.components

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
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.tortilleriaderek.model.ProductItem
import com.example.tortilleriaderek.ui.theme.*

@Composable
fun ProductItemRow(
    product: ProductItem,
    onQuantityChanged: (Int) -> Unit,
    modifier: Modifier = Modifier
) {
    Row(
        modifier = modifier
            .fillMaxWidth()
            .padding(vertical = 12.dp, horizontal = 14.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        // Left Column: Name and Unit Price
        Column(
            modifier = Modifier.weight(1f),
            verticalArrangement = Arrangement.spacedBy(2.dp)
        ) {
            Text(
                text = product.name,
                color = TextPrimary,
                fontSize = 15.sp,
                fontWeight = FontWeight.Bold
            )
            Text(
                text = "$%,.2f / %s".format(product.price, product.unitLabel),
                color = TextSecondary,
                fontSize = 13.sp,
                fontWeight = FontWeight.Normal
            )
        }

        // Right Column: Counter steppers & Subtotal
        Column(
            horizontalAlignment = Alignment.End,
            verticalArrangement = Arrangement.spacedBy(4.dp)
        ) {
            Row(
                modifier = Modifier
                    .clip(CircleShape)
                    .background(BorderLight)
                    .padding(2.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(4.dp)
            ) {
                // Minus button
                Box(
                    modifier = Modifier
                        .size(28.dp)
                        .clip(CircleShape)
                        .background(Color.White)
                        .border(1.dp, BorderSubtle.copy(alpha = 0.6f), CircleShape)
                        .clickable(enabled = product.quantity > 0) {
                            onQuantityChanged(product.quantity - 1)
                        },
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = Icons.Default.Remove,
                        contentDescription = "Disminuir",
                        tint = if (product.quantity > 0) TextPrimary else TextMuted,
                        modifier = Modifier.size(15.dp)
                    )
                }

                // Quantity
                Text(
                    text = "${product.quantity}",
                    color = TextPrimary,
                    fontSize = 13.sp,
                    fontWeight = FontWeight.Bold,
                    modifier = Modifier.widthIn(min = 24.dp),
                    textAlign = androidx.compose.ui.text.style.TextAlign.Center
                )

                // Plus button
                Box(
                    modifier = Modifier
                        .size(28.dp)
                        .clip(CircleShape)
                        .background(MaizPrimary)
                        .clickable {
                            onQuantityChanged(product.quantity + 1)
                        },
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = Icons.Default.Add,
                        contentDescription = "Aumentar",
                        tint = Color.White,
                        modifier = Modifier.size(15.dp)
                    )
                }
            }

            // Subtotal
            Text(
                text = "$%,.2f".format(product.subtotal),
                color = if (product.quantity > 0) TextPrimary else TextMuted,
                fontSize = 13.sp,
                fontWeight = FontWeight.Bold
            )
        }
    }
}
