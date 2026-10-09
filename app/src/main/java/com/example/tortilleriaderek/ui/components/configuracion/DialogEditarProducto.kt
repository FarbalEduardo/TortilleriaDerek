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
import androidx.compose.ui.unit.sp
import com.example.tortilleriaderek.ui.screens.ProductoConfig
import com.example.tortilleriaderek.ui.theme.*

/**
 * Diálogo modal para editar el precio y peso de un producto (Modo de Diseño).
 */
@Composable
fun DialogEditarProducto(
    producto: ProductoConfig,
    precioInput: String,
    onPrecioInputChange: (String) -> Unit,
    pesoInput: String,
    onPesoInputChange: (String) -> Unit,
    onConfirmar: () -> Unit,
    onDismiss: () -> Unit,
    modifier: Modifier = Modifier
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        modifier = modifier.testTag("dialog_editar_producto"),
        shape = RoundedCornerShape(20.dp),
        containerColor = Color.White,
        titleContentColor = Color(0xFF111827),
        textContentColor = Color(0xFF111827),
        title = {
            Text(
                text = "Editar Precio",
                fontWeight = FontWeight.Bold,
                fontSize = 16.sp,
                color = Color(0xFF111827)
            )
        },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                Text(
                    text = "${producto.nombre} (${producto.descripcionPeso})",
                    fontSize = 13.sp,
                    color = Color(0xFF374151)
                )
                OutlinedTextField(
                    value = precioInput,
                    onValueChange = onPrecioInputChange,
                    textStyle = LocalTextStyle.current.copy(
                        color = Color(0xFF111827),
                        fontWeight = FontWeight.SemiBold,
                        fontSize = 15.sp
                    ),
                    label = { Text("Precio Unitario", color = Color(0xFF374151)) },
                    prefix = { Text("$ ", fontWeight = FontWeight.Bold, color = Color(0xFF111827)) },
                    suffix = { Text("MXN", fontWeight = FontWeight.Medium, color = Color(0xFF374151)) },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                    singleLine = true,
                    shape = RoundedCornerShape(12.dp),
                    modifier = Modifier.testTag("input_editar_precio"),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedTextColor = Color(0xFF111827),
                        unfocusedTextColor = Color(0xFF111827),
                        focusedContainerColor = Color.White,
                        unfocusedContainerColor = Color.White,
                        focusedBorderColor = MaizPrimary,
                        unfocusedBorderColor = Color(0xFFD1D5DB),
                        focusedLabelColor = MaizPrimary,
                        unfocusedLabelColor = Color(0xFF374151),
                        focusedPrefixColor = Color(0xFF111827),
                        unfocusedPrefixColor = Color(0xFF111827),
                        focusedSuffixColor = Color(0xFF374151),
                        unfocusedSuffixColor = Color(0xFF374151)
                    )
                )
                if (producto.esPesoEditable) {
                    OutlinedTextField(
                        value = pesoInput,
                        onValueChange = onPesoInputChange,
                        textStyle = LocalTextStyle.current.copy(
                            color = Color(0xFF111827),
                            fontWeight = FontWeight.SemiBold,
                            fontSize = 15.sp
                        ),
                        label = { Text("Tamaño / Peso del Paquete", color = Color(0xFF374151)) },
                        suffix = { Text("gramos", fontWeight = FontWeight.Bold, color = Color(0xFF111827)) },
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                        singleLine = true,
                        shape = RoundedCornerShape(12.dp),
                        modifier = Modifier.testTag("input_editar_peso"),
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedTextColor = Color(0xFF111827),
                            unfocusedTextColor = Color(0xFF111827),
                            focusedContainerColor = Color.White,
                            unfocusedContainerColor = Color.White,
                            focusedBorderColor = MaizPrimary,
                            unfocusedBorderColor = Color(0xFFD1D5DB),
                            focusedLabelColor = MaizPrimary,
                            unfocusedLabelColor = Color(0xFF374151),
                            focusedSuffixColor = Color(0xFF111827),
                            unfocusedSuffixColor = Color(0xFF111827)
                        )
                    )
                }
            }
        },
        confirmButton = {
            Button(
                onClick = onConfirmar,
                colors = ButtonDefaults.buttonColors(containerColor = MaizPrimary),
                shape = RoundedCornerShape(12.dp),
                modifier = Modifier.testTag("btn_guardar_editar_producto")
            ) {
                Text("Guardar", fontWeight = FontWeight.Bold, color = Color.White)
            }
        },
        dismissButton = {
            TextButton(
                onClick = onDismiss,
                modifier = Modifier.testTag("btn_cancelar_editar_producto")
            ) {
                Text("Cancelar", color = Color(0xFF4B5563))
            }
        }
    )
}

@Preview
@Composable
fun DialogEditarProductoPreview() {
    TortilleriaDerekTheme {
        DialogEditarProducto(
            producto = ProductoConfig("3", "Pq", "Paquete Mostrador", "800 g peso estándar", 18.0, 800, true),
            precioInput = "18.0",
            onPrecioInputChange = {},
            pesoInput = "800",
            onPesoInputChange = {},
            onConfirmar = {},
            onDismiss = {}
        )
    }
}
