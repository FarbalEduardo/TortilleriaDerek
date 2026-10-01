package com.example.tortilleriaderek.ui.components

import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
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
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.tortilleriaderek.ui.theme.BorderLight
import com.example.tortilleriaderek.ui.theme.BorderSubtle
import com.example.tortilleriaderek.ui.theme.MaizPrimary
import com.example.tortilleriaderek.ui.theme.TextSecondary

@Composable
fun SegmentedTabControl(
    selectedTab: Int, // 0 for Mostrador, 1 for Repartidores
    onTabSelected: (Int) -> Unit,
    modifier: Modifier = Modifier
) {
    Box(
        modifier = modifier
            .fillMaxWidth()
            .clip(CircleShape)
            .background(BorderLight)
            .padding(4.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceEvenly
        ) {
            TabOption(
                label = "Mostrador",
                icon = Icons.Default.Storefront,
                isSelected = selectedTab == 0,
                onClick = { onTabSelected(0) },
                modifier = Modifier.weight(1f)
            )
            TabOption(
                label = "Repartidores",
                icon = Icons.Default.TwoWheeler,
                isSelected = selectedTab == 1,
                onClick = { onTabSelected(1) },
                modifier = Modifier.weight(1f)
            )
        }
    }
}

@Composable
private fun TabOption(
    label: String,
    icon: ImageVector,
    isSelected: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val bgColor by animateColorAsState(
        targetValue = if (isSelected) MaizPrimary else Color.Transparent,
        animationSpec = tween(durationMillis = 200, easing = FastOutSlowInEasing),
        label = "bgColor"
    )
    val contentColor by animateColorAsState(
        targetValue = if (isSelected) Color.White else TextSecondary,
        animationSpec = tween(durationMillis = 200, easing = FastOutSlowInEasing),
        label = "contentColor"
    )

    Box(
        modifier = modifier
            .clip(CircleShape)
            .background(bgColor)
            .clickable { onClick() }
            .padding(vertical = 9.dp),
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
                fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium
            )
        }
    }
}
