package com.example.tortilleriaderek.ui.components.login

import androidx.compose.foundation.layout.*
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.tortilleriaderek.R

private val NeutralTitle = Color(0xFF181615)
private val NeutralBody = Color(0xFF736E69)

/**
 * Creado por 🎨 design-ui-expert.
 * Cabecera compacta de Login sin icono de caja registradora para optimizar espacio vertical.
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
        Text(
            text = stringResource(R.string.login_titulo),
            fontSize = 20.sp,
            fontWeight = FontWeight.Bold,
            color = NeutralTitle,
            textAlign = TextAlign.Center
        )
        Spacer(modifier = Modifier.height(2.dp))
        Text(
            text = stringResource(R.string.login_subtitulo),
            fontSize = 12.sp,
            color = NeutralBody,
            textAlign = TextAlign.Center
        )
    }
}
