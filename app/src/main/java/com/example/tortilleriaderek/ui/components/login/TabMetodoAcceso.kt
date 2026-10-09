package com.example.tortilleriaderek.ui.components.login

import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Icon
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

private val BrandOrange = Color(0xFFFF6B00)
private val NeutralBody = Color(0xFF736E69)

/**
 * Creado por 🎨 design-ui-expert.
 * Pestaña interactiva stateless para alternar entre Código PIN y Patrón táctil.
 */
@Composable
fun TabMetodoAcceso(
    titulo: String,
    icon: ImageVector,
    seleccionado: Boolean,
    onClick: () -> Unit,
    testTag: String,
    modifier: Modifier = Modifier
) {
    val bg by animateColorAsState(
        targetValue = if (seleccionado) BrandOrange else Color.Transparent,
        animationSpec = tween(150),
        label = "tabBg"
    )
    val contentColor by animateColorAsState(
        targetValue = if (seleccionado) Color.White else NeutralBody,
        animationSpec = tween(150),
        label = "tabContentColor"
    )

    Surface(
        shape = RoundedCornerShape(10.dp),
        color = bg,
        modifier = modifier
            .height(38.dp)
            .clip(RoundedCornerShape(10.dp))
            .clickable(onClick = onClick)
            .testTag(testTag)
    ) {
        Row(
            modifier = Modifier.fillMaxSize(),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.Center
        ) {
            Icon(
                imageVector = icon,
                contentDescription = null,
                tint = contentColor,
                modifier = Modifier.size(16.dp)
            )
            Spacer(modifier = Modifier.width(6.dp))
            Text(
                text = titulo,
                fontSize = 12.sp,
                fontWeight = if (seleccionado) FontWeight.Bold else FontWeight.Medium,
                color = contentColor
            )
        }
    }
}
