package com.example.tortilleriaderek.ui.components.configuracion

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Visibility
import androidx.compose.material.icons.filled.VisibilityOff
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.tortilleriaderek.ui.screens.UsuarioConfig
import com.example.tortilleriaderek.ui.theme.*

/**
 * Diálogo modal para registrar una nueva cuenta de usuario en el sistema.
 */
@Composable
fun DialogAgregarUsuario(
    usernameInput: String,
    onUsernameChange: (String) -> Unit,
    rolInput: String,
    onRolChange: (String) -> Unit,
    passwordInput: String,
    onPasswordChange: (String) -> Unit,
    passwordVisible: Boolean,
    onTogglePasswordVisible: () -> Unit,
    onConfirmar: () -> Unit,
    onDismiss: () -> Unit,
    modifier: Modifier = Modifier
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        modifier = modifier.testTag("dialog_agregar_usuario"),
        shape = RoundedCornerShape(20.dp),
        containerColor = Color.White,
        titleContentColor = Color(0xFF111827),
        textContentColor = Color(0xFF111827),
        title = {
            Text(
                text = "Nuevo Usuario",
                fontWeight = FontWeight.Bold,
                fontSize = 16.sp,
                color = Color(0xFF111827)
            )
        },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                OutlinedTextField(
                    value = usernameInput,
                    onValueChange = onUsernameChange,
                    textStyle = LocalTextStyle.current.copy(
                        color = Color(0xFF111827),
                        fontWeight = FontWeight.Medium,
                        fontSize = 14.sp
                    ),
                    label = { Text("Nombre de Usuario", color = Color(0xFF374151)) },
                    singleLine = true,
                    shape = RoundedCornerShape(12.dp),
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("input_usuario_nombre"),
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

                // Selector de Rol
                Text(
                    text = "Rol de Acceso",
                    fontSize = 12.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color(0xFF374151)
                )
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Surface(
                        onClick = { onRolChange("EMPLEADO") },
                        shape = RoundedCornerShape(10.dp),
                        color = if (rolInput == "EMPLEADO") Color(0xFFFFF3EB) else Color(0xFFFAFAFA),
                        border = BorderStroke(1.dp, if (rolInput == "EMPLEADO") MaizPrimary else BorderSubtle),
                        modifier = Modifier
                            .weight(1f)
                            .testTag("btn_rol_empleado")
                    ) {
                        Text(
                            text = "Empleado",
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Bold,
                            color = if (rolInput == "EMPLEADO") Color(0xFFC04B00) else Color(0xFF4B5563),
                            modifier = Modifier.padding(vertical = 10.dp, horizontal = 12.dp)
                        )
                    }

                    Surface(
                        onClick = { onRolChange("ADMIN") },
                        shape = RoundedCornerShape(10.dp),
                        color = if (rolInput == "ADMIN") Color(0xFFFFF3EB) else Color(0xFFFAFAFA),
                        border = BorderStroke(1.dp, if (rolInput == "ADMIN") MaizPrimary else BorderSubtle),
                        modifier = Modifier
                            .weight(1f)
                            .testTag("btn_rol_admin")
                    ) {
                        Text(
                            text = "Administrador",
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Bold,
                            color = if (rolInput == "ADMIN") Color(0xFFC04B00) else Color(0xFF4B5563),
                            modifier = Modifier.padding(vertical = 10.dp, horizontal = 12.dp)
                        )
                    }
                }

                OutlinedTextField(
                    value = passwordInput,
                    onValueChange = onPasswordChange,
                    textStyle = LocalTextStyle.current.copy(
                        color = Color(0xFF111827),
                        fontWeight = FontWeight.Medium,
                        fontSize = 14.sp
                    ),
                    label = { Text("Contraseña o PIN", color = Color(0xFF374151)) },
                    visualTransformation = if (passwordVisible) VisualTransformation.None else PasswordVisualTransformation(),
                    trailingIcon = {
                        IconButton(onClick = onTogglePasswordVisible) {
                            Icon(
                                imageVector = if (passwordVisible) Icons.Default.VisibilityOff else Icons.Default.Visibility,
                                contentDescription = null,
                                tint = Color(0xFF4B5563)
                            )
                        }
                    },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Password),
                    singleLine = true,
                    shape = RoundedCornerShape(12.dp),
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("input_usuario_password"),
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
                modifier = Modifier.testTag("btn_guardar_agregar_usuario")
            ) {
                Text("Crear Usuario", fontWeight = FontWeight.Bold, color = Color.White)
            }
        },
        dismissButton = {
            TextButton(
                onClick = onDismiss,
                modifier = Modifier.testTag("btn_cancelar_agregar_usuario")
            ) {
                Text("Cancelar", color = Color(0xFF4B5563))
            }
        }
    )
}

/**
 * Diálogo modal para editar una cuenta existente.
 */
@Composable
fun DialogEditarUsuario(
    usuario: UsuarioConfig,
    usernameInput: String,
    onUsernameChange: (String) -> Unit,
    rolInput: String,
    onRolChange: (String) -> Unit,
    passwordInput: String,
    onPasswordChange: (String) -> Unit,
    passwordVisible: Boolean,
    onTogglePasswordVisible: () -> Unit,
    onConfirmar: () -> Unit,
    onDismiss: () -> Unit,
    modifier: Modifier = Modifier
) {
    val esAdmin1 = usuario.username.equals("admin1", ignoreCase = true) || usuario.id == "1"

    AlertDialog(
        onDismissRequest = onDismiss,
        modifier = modifier.testTag("dialog_editar_usuario"),
        shape = RoundedCornerShape(20.dp),
        containerColor = Color.White,
        titleContentColor = Color(0xFF111827),
        textContentColor = Color(0xFF111827),
        title = {
            Text(
                text = "Editar Usuario",
                fontWeight = FontWeight.Bold,
                fontSize = 16.sp,
                color = Color(0xFF111827)
            )
        },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                OutlinedTextField(
                    value = usernameInput,
                    onValueChange = { if (!esAdmin1) onUsernameChange(it) },
                    enabled = !esAdmin1,
                    textStyle = LocalTextStyle.current.copy(
                        color = Color(0xFF111827),
                        fontWeight = FontWeight.Medium,
                        fontSize = 14.sp
                    ),
                    label = { Text(if (esAdmin1) "Nombre de Usuario (admin1 Permanente)" else "Nombre de Usuario", color = Color(0xFF374151)) },
                    singleLine = true,
                    shape = RoundedCornerShape(12.dp),
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("input_editar_usuario_nombre"),
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

                Text(
                    text = "Rol de Acceso",
                    fontSize = 12.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color(0xFF374151)
                )
                if (esAdmin1) {
                    Surface(
                        shape = RoundedCornerShape(10.dp),
                        color = Color(0xFFFFF3EB),
                        border = BorderStroke(1.dp, MaizPrimary),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Text(
                            text = "Administrador Principal (Rol Permanente)",
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Bold,
                            color = Color(0xFFC04B00),
                            modifier = Modifier.padding(vertical = 10.dp, horizontal = 12.dp)
                        )
                    }
                } else {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Surface(
                            onClick = { onRolChange("EMPLEADO") },
                            shape = RoundedCornerShape(10.dp),
                            color = if (rolInput == "EMPLEADO") Color(0xFFFFF3EB) else Color(0xFFFAFAFA),
                            border = BorderStroke(1.dp, if (rolInput == "EMPLEADO") MaizPrimary else BorderSubtle),
                            modifier = Modifier.weight(1f)
                        ) {
                            Text(
                                text = "Empleado",
                                fontSize = 12.sp,
                                fontWeight = FontWeight.Bold,
                                color = if (rolInput == "EMPLEADO") Color(0xFFC04B00) else Color(0xFF4B5563),
                                modifier = Modifier.padding(vertical = 10.dp, horizontal = 12.dp)
                            )
                        }

                        Surface(
                            onClick = { onRolChange("ADMIN") },
                            shape = RoundedCornerShape(10.dp),
                            color = if (rolInput == "ADMIN") Color(0xFFFFF3EB) else Color(0xFFFAFAFA),
                            border = BorderStroke(1.dp, if (rolInput == "ADMIN") MaizPrimary else BorderSubtle),
                            modifier = Modifier.weight(1f)
                        ) {
                            Text(
                                text = "Administrador",
                                fontSize = 12.sp,
                                fontWeight = FontWeight.Bold,
                                color = if (rolInput == "ADMIN") Color(0xFFC04B00) else Color(0xFF4B5563),
                                modifier = Modifier.padding(vertical = 10.dp, horizontal = 12.dp)
                            )
                        }
                    }
                }

                OutlinedTextField(
                    value = passwordInput,
                    onValueChange = onPasswordChange,
                    textStyle = LocalTextStyle.current.copy(
                        color = Color(0xFF111827),
                        fontWeight = FontWeight.Medium,
                        fontSize = 14.sp
                    ),
                    label = { Text("Nueva Contraseña / PIN (Opcional)", color = Color(0xFF374151)) },
                    visualTransformation = if (passwordVisible) VisualTransformation.None else PasswordVisualTransformation(),
                    trailingIcon = {
                        IconButton(onClick = onTogglePasswordVisible) {
                            Icon(
                                imageVector = if (passwordVisible) Icons.Default.VisibilityOff else Icons.Default.Visibility,
                                contentDescription = null,
                                tint = Color(0xFF4B5563)
                            )
                        }
                    },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Password),
                    singleLine = true,
                    shape = RoundedCornerShape(12.dp),
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("input_editar_usuario_password"),
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
                modifier = Modifier.testTag("btn_guardar_editar_usuario")
            ) {
                Text("Guardar Cambios", fontWeight = FontWeight.Bold, color = Color.White)
            }
        },
        dismissButton = {
            TextButton(
                onClick = onDismiss,
                modifier = Modifier.testTag("btn_cancelar_editar_usuario")
            ) {
                Text("Cancelar", color = Color(0xFF4B5563))
            }
        }
    )
}

/**
 * Diálogo de confirmación para eliminar un usuario.
 */
@Composable
fun DialogEliminarUsuarioConfirmacion(
    usuario: UsuarioConfig,
    onConfirmar: () -> Unit,
    onDismiss: () -> Unit,
    modifier: Modifier = Modifier
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        modifier = modifier.testTag("dialog_eliminar_usuario_confirmacion"),
        shape = RoundedCornerShape(20.dp),
        containerColor = Color.White,
        titleContentColor = Color(0xFF111827),
        textContentColor = Color(0xFF111827),
        title = {
            Text(
                text = "Eliminar Usuario",
                fontWeight = FontWeight.Bold,
                fontSize = 16.sp,
                color = Color(0xFF111827)
            )
        },
        text = {
            Text(
                text = "¿Estás seguro de que deseas eliminar la cuenta '${usuario.username}' (${usuario.rol})? Esta acción revocará su acceso inmediatamente.",
                fontSize = 13.sp,
                color = Color(0xFF374151)
            )
        },
        confirmButton = {
            Button(
                onClick = onConfirmar,
                colors = ButtonDefaults.buttonColors(containerColor = ErrorRed),
                shape = RoundedCornerShape(12.dp),
                modifier = Modifier.testTag("btn_confirmar_eliminar_usuario")
            ) {
                Text("Eliminar", fontWeight = FontWeight.Bold, color = Color.White)
            }
        },
        dismissButton = {
            TextButton(
                onClick = onDismiss,
                modifier = Modifier.testTag("btn_cancelar_eliminar_usuario")
            ) {
                Text("Cancelar", color = Color(0xFF4B5563))
            }
        }
    )
}

@Preview
@Composable
fun DialogsUsuarioCrudPreview() {
    TortilleriaDerekTheme {
        DialogAgregarUsuario(
            usernameInput = "juanito",
            onUsernameChange = {},
            rolInput = "EMPLEADO",
            onRolChange = {},
            passwordInput = "1234",
            onPasswordChange = {},
            passwordVisible = false,
            onTogglePasswordVisible = {},
            onConfirmar = {},
            onDismiss = {}
        )
    }
}
