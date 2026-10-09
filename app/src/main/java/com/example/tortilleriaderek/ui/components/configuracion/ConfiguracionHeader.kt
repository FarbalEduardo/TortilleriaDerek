package com.example.tortilleriaderek.ui.components.configuracion

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
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
import com.example.tortilleriaderek.ui.theme.*

import androidx.compose.ui.res.stringResource
import com.example.tortilleriaderek.R

/**
 * Cabecera de la pantalla de Configuración con título, subtítulo y navegación de retorno.
 */
@Composable
fun ConfiguracionHeader(
    onBackToLogin: (() -> Unit)?,
    modifier: Modifier = Modifier
) {
    Row(
        modifier = modifier.fillMaxWidth(),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        if (onBackToLogin != null) {
            Surface(
                shape = CircleShape,
                color = Color.White,
                border = BorderStroke(1.dp, BorderSubtle),
                modifier = Modifier.size(38.dp)
            ) {
                IconButton(
                    onClick = onBackToLogin,
                    modifier = Modifier
                        .fillMaxSize()
                        .testTag("configuracion_back_button")
                ) {
                    Icon(
                        imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                        contentDescription = stringResource(R.string.config_btn_regresar_login),
                        tint = TextPrimary,
                        modifier = Modifier.size(18.dp)
                    )
                }
            }
        }

        Column(
            modifier = Modifier.weight(1f),
            verticalArrangement = Arrangement.spacedBy(2.dp)
        ) {
            Text(
                text = stringResource(R.string.config_header_titulo),
                fontSize = 22.sp,
                fontWeight = FontWeight.Black,
                color = TextPrimary
            )
            Text(
                text = stringResource(R.string.config_header_subtitulo),
                fontSize = 12.sp,
                color = TextSecondary
            )
        }
    }
}

@Preview(showBackground = true)
@Composable
fun ConfiguracionHeaderPreview() {
    TortilleriaDerekTheme {
        ConfiguracionHeader(onBackToLogin = {})
    }
}
