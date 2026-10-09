package com.example.tortilleriaderek.ui.components

import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Storefront
import androidx.compose.material.icons.filled.TwoWheeler
import androidx.compose.material3.Icon
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
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.tortilleriaderek.ui.theme.*

/**
 * Tab Segmentado para alternar entre Mostrador y Reparto Mayoreo.
 */
@Composable
fun MostradorRepartidoresTabBar(
    selectedTab: Int,
    onMostradorSelected: () -> Unit,
    onRepartidoresSelected: () -> Unit,
    modifier: Modifier = Modifier
) {
    Box(
        modifier = modifier
            .fillMaxWidth()
            .height(48.dp)
            .clip(CircleShape)
            .background(BorderLight)
            .border(1.dp, BorderSubtle.copy(alpha = 0.8f), CircleShape)
            .padding(4.dp)
            .testTag("tab_bar_mostrador_repartidores")
    ) {
        Row(modifier = Modifier.fillMaxSize()) {
            TabPill(
                label = androidx.compose.ui.res.stringResource(com.example.tortilleriaderek.R.string.tab_mostrador),
                icon = Icons.Default.Storefront,
                isSelected = selectedTab == 0,
                onClick = onMostradorSelected,
                modifier = Modifier
                    .weight(1f)
                    .testTag("tab_pill_mostrador")
            )
            TabPill(
                label = androidx.compose.ui.res.stringResource(com.example.tortilleriaderek.R.string.tab_reparto_mayoreo),
                icon = Icons.Default.TwoWheeler,
                isSelected = selectedTab == 1,
                onClick = onRepartidoresSelected,
                modifier = Modifier
                    .weight(1f)
                    .testTag("tab_pill_repartidores")
            )
        }
    }
}

@Composable
private fun TabPill(
    label: String,
    icon: ImageVector,
    isSelected: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val bgColor by animateColorAsState(
        targetValue = if (isSelected) MaizPrimary else Color.Transparent,
        animationSpec = tween(220, easing = FastOutSlowInEasing),
        label = "tabBg"
    )
    val contentColor by animateColorAsState(
        targetValue = if (isSelected) Color.White else TextSecondary,
        animationSpec = tween(220, easing = FastOutSlowInEasing),
        label = "tabContent"
    )

    Box(
        modifier = modifier
            .fillMaxHeight()
            .clip(CircleShape)
            .background(bgColor)
            .clickable { onClick() },
        contentAlignment = Alignment.Center
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(6.dp)
        ) {
            Icon(
                imageVector = icon,
                contentDescription = null,
                tint = contentColor,
                modifier = Modifier.size(16.dp)
            )
            Text(
                text = label,
                color = contentColor,
                fontSize = 13.sp,
                fontWeight = if (isSelected) FontWeight.Bold else FontWeight.SemiBold
            )
        }
    }
}

@Preview(showBackground = true)
@Composable
fun MostradorRepartidoresTabBarPreview() {
    TortilleriaDerekTheme {
        MostradorRepartidoresTabBar(
            selectedTab = 0,
            onMostradorSelected = {},
            onRepartidoresSelected = {}
        )
    }
}
