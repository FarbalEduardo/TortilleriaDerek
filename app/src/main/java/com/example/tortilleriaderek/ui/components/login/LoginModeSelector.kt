package com.example.tortilleriaderek.ui.components.login

import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.animateDpAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Check
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
private val BrandOrangeLight = Color(0xFFFFF0E5)
private val BrandOrangeSurface = Color(0xFFFFF7F2)
private val NeutralTitle = Color(0xFF181615)
private val NeutralBody = Color(0xFF736E69)
private val NeutralBorderSubtle = Color(0xFFE6E3E0)
private val NeutralIconBg = Color(0xFFF4F2F0)
private val NeutralIconTint = Color(0xFF524E4A)

/**
 * Creado por 🎨 design-ui-expert.
 * Selector de modo de acceso vertical con tarjetas descriptivas (Abrir Turno vs Solo Consulta).
 */
@Composable
fun LoginModeSelector(
    selectedMode: LoginMode,
    onModeSelected: (LoginMode) -> Unit,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier
            .fillMaxWidth()
            .testTag("login_mode_selector"),
        verticalArrangement = Arrangement.spacedBy(10.dp)
    ) {
        ModeCardOption(
            title = stringResource(R.string.login_modo_turno),
            description = stringResource(R.string.login_modo_turno_desc),
            icon = Icons.Default.PointOfSale,
            isSelected = selectedMode == LoginMode.ABRIR_TURNO,
            onClick = { onModeSelected(LoginMode.ABRIR_TURNO) },
            testTag = "login_mode_abrir_turno"
        )

        ModeCardOption(
            title = stringResource(R.string.login_modo_consulta),
            description = stringResource(R.string.login_modo_consulta_desc),
            icon = Icons.Default.Visibility,
            isSelected = selectedMode == LoginMode.SOLO_CONSULTA,
            onClick = { onModeSelected(LoginMode.SOLO_CONSULTA) },
            testTag = "login_mode_solo_consulta"
        )
    }
}

@Composable
private fun ModeCardOption(
    title: String,
    description: String,
    icon: ImageVector,
    isSelected: Boolean,
    onClick: () -> Unit,
    testTag: String,
    modifier: Modifier = Modifier
) {
    val borderColor by animateColorAsState(
        targetValue = if (isSelected) BrandOrange else NeutralBorderSubtle,
        animationSpec = tween(200),
        label = "modeBorderColor"
    )
    val borderWidth by animateDpAsState(
        targetValue = if (isSelected) 2.dp else 1.dp,
        animationSpec = tween(200),
        label = "modeBorderWidth"
    )
    val containerColor by animateColorAsState(
        targetValue = if (isSelected) BrandOrangeSurface else Color.White,
        animationSpec = tween(200),
        label = "modeContainerColor"
    )
    val iconBoxBg by animateColorAsState(
        targetValue = if (isSelected) BrandOrangeLight else NeutralIconBg,
        animationSpec = tween(200),
        label = "modeIconBoxBg"
    )
    val iconTint by animateColorAsState(
        targetValue = if (isSelected) BrandOrange else NeutralIconTint,
        animationSpec = tween(200),
        label = "modeIconTint"
    )

    Surface(
        modifier = modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(18.dp))
            .border(borderWidth, borderColor, RoundedCornerShape(18.dp))
            .clickable { onClick() }
            .testTag(testTag),
        color = containerColor,
        shadowElevation = if (isSelected) 2.dp else 1.dp
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(14.dp),
            verticalAlignment = Alignment.Top,
            horizontalArrangement = Arrangement.spacedBy(14.dp)
        ) {
            Box(
                modifier = Modifier
                    .size(42.dp)
                    .clip(RoundedCornerShape(12.dp))
                    .background(iconBoxBg),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = icon,
                    contentDescription = null,
                    tint = iconTint,
                    modifier = Modifier.size(20.dp)
                )
            }

            Column(
                modifier = Modifier.weight(1f)
            ) {
                Text(
                    text = title,
                    fontSize = 14.sp,
                    fontWeight = FontWeight.Bold,
                    color = NeutralTitle
                )
                Spacer(modifier = Modifier.height(2.dp))
                Text(
                    text = description,
                    fontSize = 12.sp,
                    fontWeight = FontWeight.Normal,
                    color = NeutralBody,
                    lineHeight = 16.sp
                )
            }

            Box(
                modifier = Modifier.size(20.dp),
                contentAlignment = Alignment.Center
            ) {
                if (isSelected) {
                    Box(
                        modifier = Modifier
                            .size(20.dp)
                            .clip(CircleShape)
                            .background(BrandOrange),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = Icons.Default.Check,
                            contentDescription = "Seleccionado",
                            tint = Color.White,
                            modifier = Modifier.size(13.dp)
                        )
                    }
                }
            }
        }
    }
}
