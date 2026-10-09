package com.example.tortilleriaderek.ui.components.backup

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.LockReset
import androidx.compose.material.icons.filled.RadioButtonChecked
import androidx.compose.material.icons.filled.RadioButtonUnchecked
import androidx.compose.material.icons.filled.WarningAmber
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import com.example.tortilleriaderek.domain.model.DbInspectionResult
import com.example.tortilleriaderek.presentation.backup.TipoPoliticaPassword
import com.example.tortilleriaderek.ui.theme.*

/**
 * Creado por 🎨 design-ui-expert y 🛡️ security-expert.
 * Modal de advertencia destructiva no genérico con semáforo de riesgo y selector
 * de política de contraseñas de administrador (Opción 1: Mantener actual, Usar respaldo, Definir nueva).
 */
@Composable
fun DialogAdvertenciaReemplazoDb(
    inspectionResult: DbInspectionResult?,
    tipoPolitica: TipoPoliticaPassword,
    nuevaPasswordInput: String,
    onTipoPoliticaChange: (TipoPoliticaPassword) -> Unit,
    onNuevaPasswordChange: (String) -> Unit,
    onConfirmar: () -> Unit,
    onCancelar: () -> Unit
) {
    Dialog(onDismissRequest = onCancelar) {
        Card(
            shape = RoundedCornerShape(24.dp),
            colors = CardDefaults.cardColors(containerColor = SurfaceCard),
            elevation = CardDefaults.cardElevation(defaultElevation = 10.dp),
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = 16.dp)
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .verticalScroll(rememberScrollState())
                    .padding(22.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                // Insignia de peligro en Terracota
                Box(
                    modifier = Modifier
                        .size(54.dp)
                        .clip(CircleShape)
                        .background(TerracotaLight)
                        .border(1.5.dp, TerracotaSecondary.copy(alpha = 0.4f), CircleShape),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = Icons.Default.WarningAmber,
                        contentDescription = "Advertencia de Reemplazo",
                        tint = TerracotaSecondary,
                        modifier = Modifier.size(32.dp)
                    )
                }

                Spacer(modifier = Modifier.height(12.dp))

                Text(
                    text = "Reemplazo de Base de Datos",
                    fontSize = 19.sp,
                    fontWeight = FontWeight.Bold,
                    color = TextPrimary
                )

                Spacer(modifier = Modifier.height(6.dp))

                Text(
                    text = "Los datos actuales de este teléfono serán sobreescritos de forma permanente por los del archivo importado.",
                    fontSize = 12.sp,
                    color = TextSecondary,
                    textAlign = TextAlign.Center,
                    lineHeight = 17.sp
                )

                Spacer(modifier = Modifier.height(14.dp))

                // Métricas del archivo que se importará
                if (inspectionResult != null) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(14.dp))
                            .background(SurfaceWarm)
                            .border(1.dp, BorderSubtle, RoundedCornerShape(14.dp))
                            .padding(12.dp),
                        horizontalArrangement = Arrangement.SpaceAround
                    ) {
                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            Text(text = "Ventas", fontSize = 11.sp, color = TextSecondary)
                            Text(
                                text = "${inspectionResult.totalVentas}",
                                fontSize = 16.sp,
                                fontWeight = FontWeight.Bold,
                                color = MaizPrimary
                            )
                        }
                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            Text(text = "Turnos Cerrados", fontSize = 11.sp, color = TextSecondary)
                            Text(
                                text = "${inspectionResult.totalTurnosCerrados}",
                                fontSize = 16.sp,
                                fontWeight = FontWeight.Bold,
                                color = VerdeAgave
                            )
                        }
                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            Text(text = "Usuarios", fontSize = 11.sp, color = TextSecondary)
                            Text(
                                text = "${inspectionResult.totalUsuarios}",
                                fontSize = 16.sp,
                                fontWeight = FontWeight.Bold,
                                color = TextPrimary
                            )
                        }
                    }
                    Spacer(modifier = Modifier.height(16.dp))
                }

                // Sección selector de Contraseña de Administrador (Opción 1)
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(
                        imageVector = Icons.Default.LockReset,
                        contentDescription = null,
                        tint = MaizPrimary,
                        modifier = Modifier.size(18.dp)
                    )
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(
                        text = "Contraseña de Administrador post-importación:",
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Bold,
                        color = TextPrimary
                    )
                }

                Spacer(modifier = Modifier.height(8.dp))

                // Opción 1: Mantener clave actual de este teléfono
                TarjetaOpcionPassword(
                    titulo = "Mantener contraseña actual",
                    descripcion = "Seguirás entrando con el PIN que ya usas en este teléfono (Recomendado).",
                    seleccionado = tipoPolitica == TipoPoliticaPassword.MANTENER_ACTUAL,
                    onClick = { onTipoPoliticaChange(TipoPoliticaPassword.MANTENER_ACTUAL) }
                )

                Spacer(modifier = Modifier.height(8.dp))

                // Opción 2: Usar clave del respaldo
                TarjetaOpcionPassword(
                    titulo = "Usar contraseña del respaldo",
                    descripcion = "Se adoptará la clave del Administrador del teléfono que generó el archivo.",
                    seleccionado = tipoPolitica == TipoPoliticaPassword.USAR_RESPALDO,
                    onClick = { onTipoPoliticaChange(TipoPoliticaPassword.USAR_RESPALDO) }
                )

                Spacer(modifier = Modifier.height(8.dp))

                // Opción 3: Definir nueva clave
                TarjetaOpcionPassword(
                    titulo = "Definir una nueva contraseña",
                    descripcion = "Crea un PIN nuevo para el Administrador en este momento.",
                    seleccionado = tipoPolitica == TipoPoliticaPassword.ESTABLECER_NUEVA,
                    onClick = { onTipoPoliticaChange(TipoPoliticaPassword.ESTABLECER_NUEVA) }
                )

                // Campo si seleccionó definir nueva
                if (tipoPolitica == TipoPoliticaPassword.ESTABLECER_NUEVA) {
                    Spacer(modifier = Modifier.height(10.dp))
                    OutlinedTextField(
                        value = nuevaPasswordInput,
                        onValueChange = onNuevaPasswordChange,
                        label = { Text("Nuevo PIN (mín. 4 dígitos)") },
                        visualTransformation = PasswordVisualTransformation(),
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Password),
                        singleLine = true,
                        shape = RoundedCornerShape(12.dp),
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = MaizPrimary,
                            unfocusedBorderColor = BorderSubtle
                        ),
                        modifier = Modifier.fillMaxWidth()
                    )
                }

                Spacer(modifier = Modifier.height(20.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    OutlinedButton(
                        onClick = onCancelar,
                        shape = RoundedCornerShape(12.dp),
                        modifier = Modifier
                            .weight(1f)
                            .height(46.dp),
                        colors = ButtonDefaults.outlinedButtonColors(contentColor = TextSecondary)
                    ) {
                        Text("Cancelar", fontWeight = FontWeight.SemiBold)
                    }

                    Button(
                        onClick = onConfirmar,
                        shape = RoundedCornerShape(12.dp),
                        colors = ButtonDefaults.buttonColors(containerColor = TerracotaSecondary),
                        modifier = Modifier
                            .weight(1.3f)
                            .height(46.dp)
                    ) {
                        Text("Reemplazar Datos", color = Color.White, fontWeight = FontWeight.Bold, fontSize = 13.sp)
                    }
                }
            }
        }
    }
}

@Composable
private fun TarjetaOpcionPassword(
    titulo: String,
    descripcion: String,
    seleccionado: Boolean,
    onClick: () -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(12.dp))
            .background(if (seleccionado) MaizPrimaryLight else SurfaceWarm)
            .border(
                width = if (seleccionado) 1.5.dp else 1.dp,
                color = if (seleccionado) MaizPrimary else BorderSubtle,
                shape = RoundedCornerShape(12.dp)
            )
            .clickable(onClick = onClick)
            .padding(10.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Icon(
            imageVector = if (seleccionado) Icons.Default.RadioButtonChecked else Icons.Default.RadioButtonUnchecked,
            contentDescription = null,
            tint = if (seleccionado) MaizPrimary else TextMuted,
            modifier = Modifier.size(20.dp)
        )
        Spacer(modifier = Modifier.width(10.dp))
        Column {
            Text(
                text = titulo,
                fontSize = 13.sp,
                fontWeight = if (seleccionado) FontWeight.Bold else FontWeight.SemiBold,
                color = if (seleccionado) MaizPrimaryDark else TextPrimary
            )
            Text(
                text = descripcion,
                fontSize = 11.sp,
                color = TextSecondary,
                lineHeight = 15.sp
            )
        }
    }
}
