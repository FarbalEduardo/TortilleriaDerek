package com.example.tortilleriaderek.ui.components.backup

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Security
import androidx.compose.material.icons.filled.Visibility
import androidx.compose.material.icons.filled.VisibilityOff
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import com.example.tortilleriaderek.presentation.backup.OperacionBackup
import com.example.tortilleriaderek.ui.theme.*

/**
 * Creado por 🎨 design-ui-expert y 🛡️ security-expert.
 * Modal no genérico para autenticar al Administrador Principal previo a la lectura o exportación de BD.
 * Requerido cuando la operación se inicia sin una sesión activa abierta (ej. desde Login).
 */
@Composable
fun DialogAutenticacionAdminBackup(
    operacion: OperacionBackup?,
    errorMessage: String?,
    onConfirm: (username: String, pin: String) -> Unit,
    onDismiss: () -> Unit
) {
    var username by remember { mutableStateOf("") }
    var pin by remember { mutableStateOf("") }
    var showPin by remember { mutableStateOf(false) }

    val titulo = when (operacion) {
        OperacionBackup.EXPORTAR -> "Autorizar Exportación"
        OperacionBackup.IMPORTAR -> "Autorizar Importación"
        null -> "Autorización de Administrador"
    }

    val descripcion = when (operacion) {
        OperacionBackup.EXPORTAR -> "Para generar un respaldo con las ventas y catálogos, ingresa las credenciales del Administrador Principal."
        OperacionBackup.IMPORTAR -> "La transferencia de base de datos reemplazará los registros actuales. Ingresa tus credenciales de Administrador para proceder."
        null -> "Ingresa las credenciales del Administrador Principal para continuar."
    }

    Dialog(onDismissRequest = onDismiss) {
        Card(
            shape = RoundedCornerShape(24.dp),
            colors = CardDefaults.cardColors(containerColor = SurfaceCard),
            elevation = CardDefaults.cardElevation(defaultElevation = 8.dp),
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp)
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(24.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                // Escudo Maíz & Masa POS
                Box(
                    modifier = Modifier
                        .size(56.dp)
                        .clip(CircleShape)
                        .background(MaizPrimaryLight)
                        .border(1.5.dp, MaizPrimary.copy(alpha = 0.3f), CircleShape),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = Icons.Default.Security,
                        contentDescription = "Seguridad Admin",
                        tint = MaizPrimary,
                        modifier = Modifier.size(30.dp)
                    )
                }

                Spacer(modifier = Modifier.height(14.dp))

                Text(
                    text = titulo,
                    fontSize = 19.sp,
                    fontWeight = FontWeight.Bold,
                    color = TextPrimary
                )

                Spacer(modifier = Modifier.height(6.dp))

                Text(
                    text = descripcion,
                    fontSize = 12.sp,
                    color = TextSecondary,
                    textAlign = TextAlign.Center,
                    lineHeight = 17.sp
                )

                Spacer(modifier = Modifier.height(18.dp))

                // Campo Usuario
                OutlinedTextField(
                    value = username,
                    onValueChange = { username = it },
                    label = { Text("Usuario Administrador") },
                    leadingIcon = {
                        Icon(Icons.Default.Person, contentDescription = null, tint = MaizPrimary)
                    },
                    singleLine = true,
                    shape = RoundedCornerShape(14.dp),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = MaizPrimary,
                        unfocusedBorderColor = BorderSubtle,
                        focusedLabelColor = MaizPrimary
                    ),
                    modifier = Modifier.fillMaxWidth()
                )

                Spacer(modifier = Modifier.height(12.dp))

                // Campo PIN / Contraseña
                OutlinedTextField(
                    value = pin,
                    onValueChange = { pin = it },
                    label = { Text("Contraseña o PIN") },
                    leadingIcon = {
                        Icon(Icons.Default.Lock, contentDescription = null, tint = MaizPrimary)
                    },
                    trailingIcon = {
                        IconButton(onClick = { showPin = !showPin }) {
                            Icon(
                                imageVector = if (showPin) Icons.Default.VisibilityOff else Icons.Default.Visibility,
                                contentDescription = "Mostrar PIN",
                                tint = TextSecondary
                            )
                        }
                    },
                    visualTransformation = if (showPin) VisualTransformation.None else PasswordVisualTransformation(),
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Password),
                    singleLine = true,
                    shape = RoundedCornerShape(14.dp),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = MaizPrimary,
                        unfocusedBorderColor = BorderSubtle,
                        focusedLabelColor = MaizPrimary
                    ),
                    modifier = Modifier.fillMaxWidth()
                )

                // Error de autenticación si existe
                if (errorMessage != null) {
                    Spacer(modifier = Modifier.height(10.dp))
                    Text(
                        text = errorMessage,
                        color = ErrorRed,
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Medium,
                        textAlign = TextAlign.Center
                    )
                }

                Spacer(modifier = Modifier.height(20.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    OutlinedButton(
                        onClick = onDismiss,
                        shape = RoundedCornerShape(12.dp),
                        modifier = Modifier
                            .weight(1f)
                            .height(46.dp),
                        colors = ButtonDefaults.outlinedButtonColors(contentColor = TextSecondary)
                    ) {
                        Text("Cancelar", fontWeight = FontWeight.SemiBold)
                    }

                    Button(
                        onClick = {
                            if (username.isNotBlank() && pin.isNotBlank()) {
                                onConfirm(username.trim(), pin.trim())
                            }
                        },
                        enabled = username.isNotBlank() && pin.isNotBlank(),
                        shape = RoundedCornerShape(12.dp),
                        colors = ButtonDefaults.buttonColors(containerColor = MaizPrimary),
                        modifier = Modifier
                            .weight(1f)
                            .height(46.dp)
                    ) {
                        Text("Continuar", color = Color.White, fontWeight = FontWeight.Bold)
                    }
                }
            }
        }
    }
}
