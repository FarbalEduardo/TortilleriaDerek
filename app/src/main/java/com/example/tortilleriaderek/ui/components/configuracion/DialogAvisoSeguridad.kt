package com.example.tortilleriaderek.ui.components.configuracion

import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.tortilleriaderek.ui.theme.*

/**
 * Diálogo modal de alerta / aviso de seguridad.
 */
@Composable
fun DialogAvisoSeguridad(
    mensaje: String,
    onDismiss: () -> Unit,
    modifier: Modifier = Modifier
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        modifier = modifier.testTag("dialog_aviso_seguridad"),
        shape = RoundedCornerShape(20.dp),
        containerColor = Color.White,
        titleContentColor = Color(0xFFC04B00),
        textContentColor = Color(0xFF111827),
        title = {
            Text(
                text = "Aviso de Seguridad",
                fontWeight = FontWeight.Bold,
                color = Color(0xFFC04B00),
                fontSize = 16.sp
            )
        },
        text = {
            Text(text = mensaje, fontSize = 13.sp, color = Color(0xFF374151))
        },
        confirmButton = {
            Button(
                onClick = onDismiss,
                colors = ButtonDefaults.buttonColors(containerColor = MaizPrimary),
                shape = RoundedCornerShape(12.dp),
                modifier = Modifier.testTag("btn_entendido_aviso_seguridad")
            ) {
                Text("Entendido", fontWeight = FontWeight.Bold, color = Color.White)
            }
        }
    )
}

@Preview
@Composable
fun DialogAvisoSeguridadPreview() {
    TortilleriaDerekTheme {
        DialogAvisoSeguridad(
            mensaje = "El usuario debe tener al menos 3 caracteres y la contraseña al menos 4.",
            onDismiss = {}
        )
    }
}
