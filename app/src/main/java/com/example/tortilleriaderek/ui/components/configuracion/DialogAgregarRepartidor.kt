package com.example.tortilleriaderek.ui.components.configuracion

import androidx.compose.foundation.layout.*
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
 * Diálogo modal para registrar un nuevo repartidor/chofer y su moto/ruta.
 */
@Composable
fun DialogAgregarRepartidor(
    nombreInput: String,
    onNombreChange: (String) -> Unit,
    motoRutaInput: String,
    onMotoRutaChange: (String) -> Unit,
    onConfirmar: () -> Unit,
    onDismiss: () -> Unit,
    modifier: Modifier = Modifier
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        modifier = modifier.testTag("dialog_agregar_repartidor"),
        shape = RoundedCornerShape(20.dp),
        containerColor = Color.White,
        titleContentColor = Color(0xFF111827),
        textContentColor = Color(0xFF111827),
        title = {
            Text(
                text = "Nuevo Repartidor",
                fontWeight = FontWeight.Bold,
                fontSize = 16.sp,
                color = Color(0xFF111827)
            )
        },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                OutlinedTextField(
                    value = nombreInput,
                    onValueChange = onNombreChange,
                    textStyle = LocalTextStyle.current.copy(
                        color = Color(0xFF111827),
                        fontWeight = FontWeight.Medium,
                        fontSize = 14.sp
                    ),
                    label = { Text("Nombre Completo", color = Color(0xFF374151)) },
                    singleLine = true,
                    shape = RoundedCornerShape(12.dp),
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("input_repartidor_nombre"),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedTextColor = Color(0xFF111827),
                        unfocusedTextColor = Color(0xFF111827),
                        focusedContainerColor = Color.White,
                        unfocusedContainerColor = Color.White,
                        focusedBorderColor = MaizPrimary,
                        unfocusedBorderColor = Color(0xFFD1D5DB),
                        focusedLabelColor = MaizPrimary,
                        unfocusedLabelColor = Color(0xFF374151)
                    )
                )
                OutlinedTextField(
                    value = motoRutaInput,
                    onValueChange = onMotoRutaChange,
                    textStyle = LocalTextStyle.current.copy(
                        color = Color(0xFF111827),
                        fontWeight = FontWeight.Medium,
                        fontSize = 14.sp
                    ),
                    label = { Text("Moto y Ruta (ej. Moto Honda • San Juan)", color = Color(0xFF374151)) },
                    singleLine = true,
                    shape = RoundedCornerShape(12.dp),
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("input_repartidor_ruta"),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedTextColor = Color(0xFF111827),
                        unfocusedTextColor = Color(0xFF111827),
                        focusedContainerColor = Color.White,
                        unfocusedContainerColor = Color.White,
                        focusedBorderColor = MaizPrimary,
                        unfocusedBorderColor = Color(0xFFD1D5DB),
                        focusedLabelColor = MaizPrimary,
                        unfocusedLabelColor = Color(0xFF374151)
                    )
                )
            }
        },
        confirmButton = {
            Button(
                onClick = onConfirmar,
                colors = ButtonDefaults.buttonColors(containerColor = MaizPrimary),
                shape = RoundedCornerShape(12.dp),
                modifier = Modifier.testTag("btn_guardar_agregar_repartidor")
            ) {
                Text("Agregar", fontWeight = FontWeight.Bold, color = Color.White)
            }
        },
        dismissButton = {
            TextButton(
                onClick = onDismiss,
                modifier = Modifier.testTag("btn_cancelar_agregar_repartidor")
            ) {
                Text("Cancelar", color = Color(0xFF4B5563))
            }
        }
    )
}

@Preview
@Composable
fun DialogAgregarRepartidorPreview() {
    TortilleriaDerekTheme {
        DialogAgregarRepartidor(
            nombreInput = "Carlos Méndez",
            onNombreChange = {},
            motoRutaInput = "Moto 1 • Ruta Norte",
            onMotoRutaChange = {},
            onConfirmar = {},
            onDismiss = {}
        )
    }
}
