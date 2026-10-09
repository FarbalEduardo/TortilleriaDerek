package com.example.tortilleriaderek.ui.components.historial

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.CalendarToday
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

/**
 * Cabecera superior para la pantalla de Historial de Ventas (Modo de Diseño).
 */
@Composable
fun HistorialTopHeader(
    fechaTexto: String,
    isTurnoCerrado: Boolean,
    onBack: () -> Unit,
    modifier: Modifier = Modifier
) {
    Row(
        modifier = modifier.fillMaxWidth(),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        Surface(
            shape = CircleShape,
            color = Color.White,
            border = BorderStroke(1.dp, BorderSubtle),
            modifier = Modifier.size(48.dp)
        ) {
            IconButton(
                onClick = onBack,
                modifier = Modifier
                    .fillMaxSize()
                    .testTag("historial_back_button")
            ) {
                Icon(
                    imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                    contentDescription = androidx.compose.ui.res.stringResource(com.example.tortilleriaderek.R.string.txt_volver),
                    tint = TextPrimary,
                    modifier = Modifier.size(20.dp)
                )
            }
        }

        Column(verticalArrangement = Arrangement.spacedBy(2.dp)) {
            Text(
                text = androidx.compose.ui.res.stringResource(com.example.tortilleriaderek.R.string.historial_titulo),
                fontSize = 18.sp,
                fontWeight = FontWeight.Bold,
                color = TextPrimary
            )
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(4.dp)
            ) {
                Icon(
                    imageVector = Icons.Default.CalendarToday,
                    contentDescription = null,
                    tint = TextSecondary,
                    modifier = Modifier.size(13.dp)
                )
                Text(
                    text = fechaTexto.ifBlank { "Hoy" },
                    fontSize = 12.sp,
                    fontWeight = FontWeight.Medium,
                    color = TextSecondary
                )
                if (isTurnoCerrado) {
                    Surface(
                        shape = CircleShape,
                        color = Color(0xFFF3F4F6)
                    ) {
                        Text(
                            text = "Cerrado",
                            fontSize = 10.sp,
                            fontWeight = FontWeight.Bold,
                            color = TextSecondary,
                            modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                        )
                    }
                }
            }
        }
    }
}

@Preview(showBackground = true)
@Composable
fun HistorialTopHeaderPreview() {
    TortilleriaDerekTheme {
        HistorialTopHeader(
            fechaTexto = "Lunes 7 de Septiembre",
            isTurnoCerrado = false,
            onBack = {}
        )
    }
}
