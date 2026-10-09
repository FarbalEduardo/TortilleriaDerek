package com.example.tortilleriaderek.ui.components.produccion

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowForward
import androidx.compose.material.icons.filled.DeleteOutline
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
 * Grupo de botones de acción táctiles para la pantalla de Producción (Modo de Diseño).
 */
@Composable
fun ProduccionActionButtons(
    onRegistrarTandaClick: () -> Unit,
    onRegistrarMermaClick: () -> Unit,
    onVerHistorialClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier.fillMaxWidth(),
        verticalArrangement = Arrangement.spacedBy(10.dp)
    ) {
        // CTA Principal Naranja (Touch Target Óptimo 64dp)
        Button(
            onClick = onRegistrarTandaClick,
            modifier = Modifier
                .fillMaxWidth()
                .height(64.dp)
                .testTag("produccion_btn_registrar_tanda"),
            shape = RoundedCornerShape(26.dp),
            colors = ButtonDefaults.buttonColors(containerColor = MaizPrimary),
            elevation = ButtonDefaults.buttonElevation(defaultElevation = 2.dp)
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Text(
                    text = androidx.compose.ui.res.stringResource(com.example.tortilleriaderek.R.string.produccion_btn_registrar_tanda),
                    fontSize = 16.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color.White
                )
                Icon(
                    imageVector = Icons.AutoMirrored.Filled.ArrowForward,
                    contentDescription = null,
                    tint = Color.White,
                    modifier = Modifier.size(20.dp)
                )
            }
        }

        // CTA Merma Directa en Producción (56dp touch target)
        OutlinedButton(
            onClick = onRegistrarMermaClick,
            modifier = Modifier
                .fillMaxWidth()
                .height(56.dp)
                .testTag("produccion_btn_registrar_merma"),
            shape = RoundedCornerShape(25.dp),
            border = BorderStroke(1.dp, Color(0xFFEF4444)),
            colors = ButtonDefaults.outlinedButtonColors(
                containerColor = Color(0xFFFEF2F2),
                contentColor = Color(0xFFDC2626)
            )
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Icon(
                    imageVector = Icons.Default.DeleteOutline,
                    contentDescription = null,
                    tint = Color(0xFFDC2626),
                    modifier = Modifier.size(18.dp)
                )
                Text(
                    text = androidx.compose.ui.res.stringResource(com.example.tortilleriaderek.R.string.produccion_btn_registrar_merma),
                    fontSize = 14.sp,
                    fontWeight = FontWeight.SemiBold,
                    color = Color(0xFFDC2626)
                )
            }
        }

        // CTA Secundario Outlined: Historial (56dp touch target)
        OutlinedButton(
            onClick = onVerHistorialClick,
            modifier = Modifier
                .fillMaxWidth()
                .height(56.dp)
                .testTag("produccion_btn_ver_historial"),
            shape = RoundedCornerShape(25.dp),
            border = BorderStroke(1.dp, BorderSubtle),
            colors = ButtonDefaults.outlinedButtonColors(
                containerColor = Color.White,
                contentColor = Color(0xFF2C5282)
            )
        ) {
            Text(
                text = "Historial de Producción",
                fontSize = 14.sp,
                fontWeight = FontWeight.SemiBold,
                color = Color(0xFF3182CE)
            )
        }
    }
}

@Preview(showBackground = true)
@Composable
fun ProduccionActionButtonsPreview() {
    TortilleriaDerekTheme {
        ProduccionActionButtons(
            onRegistrarTandaClick = {},
            onRegistrarMermaClick = {},
            onVerHistorialClick = {}
        )
    }
}
