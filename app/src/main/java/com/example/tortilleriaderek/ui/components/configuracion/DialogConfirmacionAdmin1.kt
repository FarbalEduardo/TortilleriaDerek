package com.example.tortilleriaderek.ui.components.configuracion

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Security
import androidx.compose.material.icons.filled.Visibility
import androidx.compose.material.icons.filled.VisibilityOff
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.tortilleriaderek.ui.theme.*

/**
 * Diálogo modal para solicitar autorización con credenciales de la cuenta principal admin1.
 */
@Composable
fun DialogConfirmacionAdmin1(
    usernameInput: String,
    onUsernameChange: (String) -> Unit,
    passwordInput: String,
    onPasswordChange: (String) -> Unit,
    passwordVisible: Boolean,
    onTogglePasswordVisible: () -> Unit,
    errorMessage: String?,
    onAutorizar: () -> Unit,
    onDismiss: () -> Unit,
    modifier: Modifier = Modifier
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        modifier = modifier.testTag("dialog_confirmacion_admin1"),
        shape = RoundedCornerShape(20.dp),
        containerColor = Color.White,
        titleContentColor = Color(0xFF111827),
        textContentColor = Color(0xFF111827),
        title = {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                Box(
                    modifier = Modifier
                        .size(38.dp)
                        .clip(RoundedCornerShape(10.dp))
                        .background(Color(0xFFFFF3EB)),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = Icons.Default.Security,
                        contentDescription = null,
                        tint = MaizPrimary,
                        modifier = Modifier.size(22.dp)
                    )
                }
                Column {
                    Text(
                        text = "Autorización Requerida",
                        fontWeight = FontWeight.Bold,
                        fontSize = 16.sp,
                        color = Color(0xFF111827)
                    )
                    Text(
                        text = "Cuenta Principal admin1",
                        fontSize = 11.sp,
                        color = Color(0xFF6B7280)
                    )
                }
            }
        },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                Text(
                    text = "Para modificar la configuración del sistema, ingrese la confirmación de la cuenta de administración principal.",
                    fontSize = 13.sp,
                    color = Color(0xFF374151),
                    lineHeight = 18.sp
                )

                OutlinedTextField(
                    value = usernameInput,
                    onValueChange = onUsernameChange,
                    textStyle = LocalTextStyle.current.copy(
                        color = Color(0xFF111827),
                        fontWeight = FontWeight.Medium,
                        fontSize = 14.sp
                    ),
                    label = { Text("Usuario Administrador", color = Color(0xFF374151)) },
                    singleLine = true,
                    shape = RoundedCornerShape(12.dp),
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("input_admin1_username"),
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
                    value = passwordInput,
                    onValueChange = onPasswordChange,
                    textStyle = LocalTextStyle.current.copy(
                        color = Color(0xFF111827),
                        fontWeight = FontWeight.Medium,
                        fontSize = 14.sp
                    ),
                    label = { Text("Contraseña de admin1", color = Color(0xFF374151)) },
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
                        .testTag("input_admin1_password"),
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

                errorMessage?.let { err ->
                    Surface(
                        shape = RoundedCornerShape(8.dp),
                        color = Color(0xFFFEE2E2),
                        border = BorderStroke(1.dp, Color(0xFFEF4444)),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Text(
                            text = err,
                            color = Color(0xFFB91C1C),
                            fontSize = 12.sp,
                            fontWeight = FontWeight.SemiBold,
                            modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp)
                        )
                    }
                }
            }
        },
        confirmButton = {
            Button(
                onClick = onAutorizar,
                colors = ButtonDefaults.buttonColors(containerColor = MaizPrimary),
                shape = RoundedCornerShape(12.dp),
                modifier = Modifier.testTag("btn_autorizar_admin1")
            ) {
                Text("Autorizar", fontWeight = FontWeight.Bold, color = Color.White)
            }
        },
        dismissButton = {
            TextButton(
                onClick = onDismiss,
                modifier = Modifier.testTag("btn_cancelar_admin1")
            ) {
                Text("Cancelar", color = Color(0xFF4B5563))
            }
        }
    )
}

@Preview
@Composable
fun DialogConfirmacionAdmin1Preview() {
    TortilleriaDerekTheme {
        DialogConfirmacionAdmin1(
            usernameInput = "admin1",
            onUsernameChange = {},
            passwordInput = "1234",
            onPasswordChange = {},
            passwordVisible = false,
            onTogglePasswordVisible = {},
            errorMessage = null,
            onAutorizar = {},
            onDismiss = {}
        )
    }
}
