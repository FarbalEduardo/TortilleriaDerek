package com.example.tortilleriaderek.ui.components.produccion

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.tortilleriaderek.ui.theme.*

/**
 * Diálogo modal para registrar merma operativa de turno directamente en Producción.
 */
@Composable
fun DialogRegistrarMerma(
    mermaKgInput: String,
    motivoMermaInput: String,
    disponibleEnTienda: Double,
    errorMessage: String?,
    onMermaKgChange: (String) -> Unit,
    onMotivoChange: (String) -> Unit,
    onConfirmar: () -> Unit,
    onDismiss: () -> Unit,
    modifier: Modifier = Modifier
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        modifier = modifier.testTag("dialog_registrar_merma"),
        shape = RoundedCornerShape(20.dp),
        containerColor = Color.White,
        titleContentColor = TextPrimary,
        textContentColor = TextPrimary,
        title = {
            Text(
                text = androidx.compose.ui.res.stringResource(com.example.tortilleriaderek.R.string.produccion_dialog_merma_titulo),
                fontWeight = FontWeight.Bold,
                color = TextPrimary
            )
        },
        text = {
            Column(
                modifier = Modifier.fillMaxWidth(),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                Text(
                    text = "Esta merma se descontará directamente del stock disponible de tortillas en tienda.",
                    fontSize = 13.sp,
                    color = TextSecondary
                )

                OutlinedTextField(
                    value = mermaKgInput,
                    onValueChange = onMermaKgChange,
                    label = { Text(androidx.compose.ui.res.stringResource(com.example.tortilleriaderek.R.string.produccion_dialog_merma_cantidad)) },
                    singleLine = true,
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                    textStyle = androidx.compose.ui.text.TextStyle(fontFeatureSettings = "tnum"),
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("input_merma_kg")
                )

                OutlinedTextField(
                    value = motivoMermaInput,
                    onValueChange = onMotivoChange,
                    label = { Text(androidx.compose.ui.res.stringResource(com.example.tortilleriaderek.R.string.produccion_dialog_merma_motivo)) },
                    singleLine = true,
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("input_merma_motivo")
                )

                if (errorMessage != null) {
                    Text(
                        text = errorMessage,
                        fontSize = 12.sp,
                        color = Color.Red,
                        fontWeight = FontWeight.Medium
                    )
                }
            }
        },
        confirmButton = {
            val parsedMerma = mermaKgInput.replace(',', '.').toDoubleOrNull()
            val esMermaInvalida = errorMessage != null || parsedMerma == null || parsedMerma <= 0.0 || parsedMerma > disponibleEnTienda
            Button(
                onClick = onConfirmar,
                enabled = !esMermaInvalida,
                shape = CircleShape,
                modifier = Modifier
                    .height(56.dp)
                    .testTag("btn_confirmar_descontar_merma"),
                colors = ButtonDefaults.buttonColors(
                    containerColor = Color(0xFFDC2626),
                    disabledContainerColor = Color(0xFFE5E7EB),
                    disabledContentColor = Color(0xFF9CA3AF)
                )
            ) {
                Text(
                    text = androidx.compose.ui.res.stringResource(com.example.tortilleriaderek.R.string.produccion_btn_registrar_merma),
                    fontWeight = FontWeight.Bold
                )
            }
        },
        dismissButton = {
            OutlinedButton(
                onClick = onDismiss,
                shape = CircleShape,
                modifier = Modifier
                    .height(56.dp)
                    .testTag("btn_cancelar_descontar_merma")
            ) {
                Text(
                    text = androidx.compose.ui.res.stringResource(com.example.tortilleriaderek.R.string.btn_cancelar),
                    color = TextSecondary
                )
            }
        }
    )
}

@Preview(showBackground = true)
@Composable
fun DialogRegistrarMermaPreview() {
    TortilleriaDerekTheme {
        DialogRegistrarMerma(
            mermaKgInput = "2.5",
            motivoMermaInput = "Masa pegada en rodillo",
            disponibleEnTienda = 14.5,
            errorMessage = null,
            onMermaKgChange = {},
            onMotivoChange = {},
            onConfirmar = {},
            onDismiss = {}
        )
    }
}
