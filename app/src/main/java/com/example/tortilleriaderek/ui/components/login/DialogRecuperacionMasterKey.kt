package com.example.tortilleriaderek.ui.components.login

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Key
import androidx.compose.material.icons.filled.LockReset
import androidx.compose.material.icons.filled.Visibility
import androidx.compose.material.icons.filled.VisibilityOff
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import com.example.tortilleriaderek.R

private val BrandOrange = Color(0xFFFF6B00)
private val NeutralTitle = Color(0xFF181615)
private val NeutralBody = Color(0xFF736E69)
private val NeutralBorderSubtle = Color(0xFFE6E3E0)

/**
 * Creado por 🎨 design-ui-expert y 🛡️ security-expert.
 * Diálogo modal para restablecer el PIN de Administrador usando la Clave Maestra Offline.
 */
@Composable
fun DialogRecuperacionMasterKey(
    onDismiss: () -> Unit,
    onConfirmarRestablecimiento: (claveMaestra: String, nuevoPin: String) -> Unit,
    errorMensaje: String? = null,
    cargando: Boolean = false,
    modifier: Modifier = Modifier
) {
    var claveMaestraInput by remember { mutableStateOf("") }
    var nuevoPinInput by remember { mutableStateOf("") }
    var confirmarPinInput by remember { mutableStateOf("") }
    var esPasswordVisible by remember { mutableStateOf(false) }
    var localError by remember { mutableStateOf<String?>(null) }

    val errorNoCoinciden = stringResource(R.string.dialog_recuperacion_error_no_coinciden)
    val errorLongitud = stringResource(R.string.dialog_recuperacion_error_longitud)

    Dialog(onDismissRequest = onDismiss) {
        Card(
            shape = RoundedCornerShape(24.dp),
            colors = CardDefaults.cardColors(containerColor = Color.White),
            elevation = CardDefaults.cardElevation(defaultElevation = 8.dp),
            modifier = modifier
                .fillMaxWidth()
                .padding(vertical = 16.dp)
                .testTag("dialog_recuperacion_master_key")
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(22.dp)
            ) {
                // Icono y Título
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    Box(
                        modifier = Modifier
                            .size(44.dp)
                            .background(BrandOrange.copy(alpha = 0.12f), RoundedCornerShape(12.dp)),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = Icons.Default.LockReset,
                            contentDescription = null,
                            tint = BrandOrange,
                            modifier = Modifier.size(24.dp)
                        )
                    }

                    Column {
                        Text(
                            text = stringResource(R.string.dialog_recuperacion_titulo),
                            fontSize = 17.sp,
                            fontWeight = FontWeight.Bold,
                            color = NeutralTitle
                        )
                        Text(
                            text = "Acceso de Contingencia Offline",
                            fontSize = 11.sp,
                            color = NeutralBody
                        )
                    }
                }

                Spacer(modifier = Modifier.height(14.dp))

                Text(
                    text = stringResource(R.string.dialog_recuperacion_subtitulo),
                    fontSize = 12.sp,
                    color = NeutralBody,
                    lineHeight = 16.sp
                )

                Spacer(modifier = Modifier.height(16.dp))

                // Campo 1: Clave Maestra
                OutlinedTextField(
                    value = claveMaestraInput,
                    onValueChange = {
                        claveMaestraInput = it
                        localError = null
                    },
                    label = { Text(stringResource(R.string.dialog_recuperacion_label_master_key), fontSize = 12.sp) },
                    leadingIcon = {
                        Icon(Icons.Default.Key, contentDescription = null, tint = BrandOrange)
                    },
                    singleLine = true,
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("input_master_key"),
                    shape = RoundedCornerShape(14.dp),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = BrandOrange,
                        unfocusedBorderColor = NeutralBorderSubtle
                    )
                )

                Spacer(modifier = Modifier.height(10.dp))

                // Campo 2: Nuevo PIN (4-6 dígitos)
                OutlinedTextField(
                    value = nuevoPinInput,
                    onValueChange = {
                        if (it.length <= 6 && it.all { char -> char.isDigit() }) {
                            nuevoPinInput = it
                            localError = null
                        }
                    },
                    label = { Text(stringResource(R.string.dialog_recuperacion_label_nuevo_pin), fontSize = 12.sp) },
                    visualTransformation = if (esPasswordVisible) VisualTransformation.None else PasswordVisualTransformation(),
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.NumberPassword),
                    trailingIcon = {
                        IconButton(onClick = { esPasswordVisible = !esPasswordVisible }) {
                            Icon(
                                imageVector = if (esPasswordVisible) Icons.Default.VisibilityOff else Icons.Default.Visibility,
                                contentDescription = "Alternar visibilidad"
                            )
                        }
                    },
                    singleLine = true,
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("input_nuevo_pin"),
                    shape = RoundedCornerShape(14.dp),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = BrandOrange,
                        unfocusedBorderColor = NeutralBorderSubtle
                    )
                )

                Spacer(modifier = Modifier.height(10.dp))

                // Campo 3: Confirmar Nuevo PIN
                OutlinedTextField(
                    value = confirmarPinInput,
                    onValueChange = {
                        if (it.length <= 6 && it.all { char -> char.isDigit() }) {
                            confirmarPinInput = it
                            localError = null
                        }
                    },
                    label = { Text(stringResource(R.string.dialog_recuperacion_label_confirmar_pin), fontSize = 12.sp) },
                    visualTransformation = if (esPasswordVisible) VisualTransformation.None else PasswordVisualTransformation(),
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.NumberPassword),
                    singleLine = true,
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("input_confirmar_pin"),
                    shape = RoundedCornerShape(14.dp),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = BrandOrange,
                        unfocusedBorderColor = NeutralBorderSubtle
                    )
                )

                // Error
                val errorMostrado = localError ?: errorMensaje
                if (errorMostrado != null) {
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(
                        text = errorMostrado,
                        color = MaterialTheme.colorScheme.error,
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Medium
                    )
                }

                Spacer(modifier = Modifier.height(20.dp))

                // Botones de Acción
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    OutlinedButton(
                        onClick = onDismiss,
                        modifier = Modifier
                            .weight(1f)
                            .height(52.dp)
                            .testTag("btn_cancelar_recuperacion"),
                        shape = RoundedCornerShape(14.dp)
                    ) {
                        Text(stringResource(R.string.btn_cancelar), color = NeutralTitle)
                    }

                    Button(
                        onClick = {
                            if (nuevoPinInput.length < 4) {
                                localError = errorLongitud
                            } else if (nuevoPinInput != confirmarPinInput) {
                                localError = errorNoCoinciden
                            } else if (claveMaestraInput.isBlank()) {
                                localError = "Ingresa la Clave Maestra de Rescate."
                            } else {
                                onConfirmarRestablecimiento(claveMaestraInput, nuevoPinInput)
                            }
                        },
                        enabled = !cargando,
                        modifier = Modifier
                            .weight(1.3f)
                            .height(52.dp)
                            .testTag("btn_confirmar_recuperacion"),
                        colors = ButtonDefaults.buttonColors(containerColor = BrandOrange),
                        shape = RoundedCornerShape(14.dp)
                    ) {
                        if (cargando) {
                            CircularProgressIndicator(color = Color.White, modifier = Modifier.size(20.dp))
                        } else {
                            Text(
                                text = stringResource(R.string.btn_guardar),
                                fontWeight = FontWeight.Bold,
                                color = Color.White
                            )
                        }
                    }
                }
            }
        }
    }
}
