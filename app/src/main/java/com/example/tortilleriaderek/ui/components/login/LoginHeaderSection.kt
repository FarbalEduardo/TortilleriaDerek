package com.example.tortilleriaderek.ui.components.login

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.PointOfSale
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.tortilleriaderek.R

private val BrandOrange = Color(0xFFFF6B00)
private val BrandOrangeSurface = Color(0xFFFFF7F2)
private val BrandOrangeBadgeBorder = Color(0xFFFFD7BA)
private val NeutralTitle = Color(0xFF181615)
private val NeutralBody = Color(0xFF736E69)

/**
 * Creado por 🎨 design-ui-expert.
 * Cabecera stateless de Login con identidad visual oficial Maíz & Masa POS.
 */
@Composable
fun LoginHeaderSection(
    modifier: Modifier = Modifier
) {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        modifier = modifier
            .fillMaxWidth()
            .testTag("login_header_section")
    ) {
        // Logo / Icono Principal de la Marca
        Box(
            modifier = Modifier
                .size(72.dp)
                .shadow(6.dp, CircleShape, spotColor = BrandOrange.copy(alpha = 0.35f))
                .clip(CircleShape)
                .background(BrandOrange),
            contentAlignment = Alignment.Center
        ) {
            Icon(
                imageVector = Icons.Default.PointOfSale,
                contentDescription = null,
                tint = Color.White,
                modifier = Modifier.size(36.dp)
            )
        }

        Spacer(modifier = Modifier.height(14.dp))

        // Badge Status: TURNO CERRADO
        Box(
            modifier = Modifier
                .clip(CircleShape)
                .background(BrandOrangeSurface)
                .border(1.dp, BrandOrangeBadgeBorder, CircleShape)
                .padding(horizontal = 16.dp, vertical = 6.dp)
                .testTag("login_status_pill"),
            contentAlignment = Alignment.Center
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(6.dp)
            ) {
                Icon(
                    imageVector = Icons.Default.Lock,
                    contentDescription = null,
                    tint = BrandOrange,
                    modifier = Modifier.size(14.dp)
                )
                Text(
                    text = stringResource(R.string.login_turno_cerrado),
                    fontSize = 12.sp,
                    fontWeight = FontWeight.Bold,
                    color = BrandOrange,
                    letterSpacing = 0.8.sp
                )
            }
        }

        Spacer(modifier = Modifier.height(14.dp))

        // Título Principal
        Text(
            text = stringResource(R.string.login_titulo),
            fontSize = 24.sp,
            fontWeight = FontWeight.Bold,
            color = NeutralTitle,
            textAlign = TextAlign.Center
        )

        Spacer(modifier = Modifier.height(4.dp))

        // Subtítulo
        Text(
            text = stringResource(R.string.login_subtitulo),
            fontSize = 13.sp,
            color = NeutralBody,
            textAlign = TextAlign.Center
        )
    }
}
