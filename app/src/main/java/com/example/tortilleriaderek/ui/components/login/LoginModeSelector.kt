package com.example.tortilleriaderek.ui.components.login

import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.PointOfSale
import androidx.compose.material.icons.filled.Visibility
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
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.tortilleriaderek.R
import com.example.tortilleriaderek.presentation.login.LoginMode

private val BrandOrange = Color(0xFFFF6B00)
private val BrandOrangeSurface = Color(0xFFFFF7F2)
private val NeutralTitle = Color(0xFF181615)
private val NeutralBody = Color(0xFF736E69)
private val NeutralBorderSubtle = Color(0xFFE6E3E0)

/**
 * Creado por 🎨 design-ui-expert.
 * Selector de modo horizontal compacto (~44dp de alto) que evita el scroll vertical en pantalla.
 */
@Composable
fun LoginModeSelector(
    selectedMode: LoginMode,
    onModeSelected: (LoginMode) -> Unit,
    modifier: Modifier = Modifier
) {
    Row(
        modifier = modifier
            .fillMaxWidth()
            .testTag("login_mode_selector"),
        horizontalArrangement = Arrangement.spacedBy(10.dp)
    ) {
        SegmentedModeButton(
            title = stringResource(R.string.login_modo_turno),
            icon = Icons.Default.PointOfSale,
            isSelected = selectedMode == LoginMode.ABRIR_TURNO,
            onClick = { onModeSelected(LoginMode.ABRIR_TURNO) },
            testTag = "login_mode_abrir_turno",
            modifier = Modifier.weight(1f)
        )

        SegmentedModeButton(
            title = stringResource(R.string.login_modo_consulta),
            icon = Icons.Default.Visibility,
            isSelected = selectedMode == LoginMode.SOLO_CONSULTA,
            onClick = { onModeSelected(LoginMode.SOLO_CONSULTA) },
            testTag = "login_mode_solo_consulta",
            modifier = Modifier.weight(1f)
        )
    }
}

@Composable
private fun SegmentedModeButton(
    title: String,
    icon: ImageVector,
    isSelected: Boolean,
    onClick: () -> Unit,
    testTag: String,
    modifier: Modifier = Modifier
) {
    val containerColor by animateColorAsState(
        targetValue = if (isSelected) BrandOrangeSurface else Color.White,
        animationSpec = tween(200),
        label = "containerColor"
    )
    val borderColor by animateColorAsState(
        targetValue = if (isSelected) BrandOrange else NeutralBorderSubtle,
        animationSpec = tween(200),
        label = "borderColor"
    )
    val contentColor by animateColorAsState(
        targetValue = if (isSelected) BrandOrange else NeutralBody,
        animationSpec = tween(200),
        label = "contentColor"
    )

    Surface(
        shape = RoundedCornerShape(14.dp),
        color = containerColor,
        border = BorderStroke(if (isSelected) 1.5.dp else 1.dp, borderColor),
        shadowElevation = if (isSelected) 2.dp else 0.dp,
        modifier = modifier
            .height(44.dp)
            .clip(RoundedCornerShape(14.dp))
            .clickable(onClick = onClick)
            .testTag(testTag)
    ) {
        Row(
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = 12.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.Center
        ) {
            Icon(
                imageVector = icon,
                contentDescription = null,
                tint = contentColor,
                modifier = Modifier.size(18.dp)
            )
            Spacer(modifier = Modifier.width(6.dp))
            Text(
                text = title,
                fontSize = 13.sp,
                fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium,
                color = if (isSelected) NeutralTitle else contentColor
            )
        }
    }
}
