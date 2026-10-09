package com.example.tortilleriaderek.ui.components.configuracion

import androidx.compose.foundation.layout.*
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
import com.example.tortilleriaderek.ui.theme.*

/**
 * Diálogo modal para actualizar los estándares de producción (Fase 2 Modo de Diseño).
 */
@Composable
fun DialogEditarEstandaresProduccion(
    pesoBultoInput: String,
    onPesoBultoChange: (String) -> Unit,
    kgMasaInput: String,
    onKgMasaChange: (String) -> Unit,
    rendimientoInput: String,
    onRendimientoChange: (String) -> Unit,
    mermaInput: String,
    onMermaChange: (String) -> Unit,
    onGuardar: () -> Unit,
    onDismiss: () -> Unit,
    modifier: Modifier = Modifier
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        modifier = modifier.testTag("dialog_editar_estandares"),
        shape = RoundedCornerShape(20.dp),
        containerColor = Color.White,
        titleContentColor = TextPrimary,
        textContentColor = TextPrimary,
        title = {
            Text("Estándares de Producción", fontWeight = FontWeight.Bold, color = TextPrimary)
        },
        text = {
            Column(
                modifier = Modifier.fillMaxWidth(),
                verticalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                OutlinedTextField(
                    value = pesoBultoInput,
                    onValueChange = onPesoBultoChange,
                    label = { Text("Peso Bulto Harina (kg)") },
                    singleLine = true,
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("input_estandares_peso_bulto")
                )
                OutlinedTextField(
                    value = kgMasaInput,
                    onValueChange = onKgMasaChange,
                    label = { Text("Total Masa por Bulto (kg)") },
                    singleLine = true,
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("input_estandares_kg_masa")
                )
                OutlinedTextField(
                    value = rendimientoInput,
                    onValueChange = onRendimientoChange,
                    label = { Text("Rendimiento Tortilla por Bulto (kg)") },
                    singleLine = true,
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("input_estandares_rendimiento")
                )
                OutlinedTextField(
                    value = mermaInput,
                    onValueChange = onMermaChange,
                    label = { Text("Merma Tolerada por Bulto (kg)") },
                    singleLine = true,
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("input_estandares_merma")
                )
            }
        },
        confirmButton = {
            Button(
                onClick = onGuardar,
                colors = ButtonDefaults.buttonColors(containerColor = MaizPrimary),
                shape = RoundedCornerShape(12.dp),
                modifier = Modifier.testTag("btn_guardar_estandares")
            ) {
                Text("Guardar", color = Color.White, fontWeight = FontWeight.Bold)
            }
        },
        dismissButton = {
            TextButton(
                onClick = onDismiss,
                modifier = Modifier.testTag("btn_cancelar_estandares")
            ) {
                Text("Cancelar", color = TextSecondary)
            }
        }
    )
}

@Preview
@Composable
fun DialogEditarEstandaresProduccionPreview() {
    TortilleriaDerekTheme {
        DialogEditarEstandaresProduccion(
            pesoBultoInput = "50.0",
            onPesoBultoChange = {},
            kgMasaInput = "90.0",
            onKgMasaChange = {},
            rendimientoInput = "85.0",
            onRendimientoChange = {},
            mermaInput = "3.4",
            onMermaChange = {},
            onGuardar = {},
            onDismiss = {}
        )
    }
}
