package com.example.tortilleriaderek.ui.components.backup

import androidx.compose.animation.*
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.ArrowForward
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Lightbulb
import androidx.compose.material.icons.filled.School
import androidx.compose.material.icons.filled.SystemUpdate
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import com.example.tortilleriaderek.ui.theme.*

/**
 * Creado por 🎨 design-ui-expert y 🏆 quality-pm-expert.
 * Micro-lección interactiva en 3 pasos cuando se detecta un archivo con versión incompatible.
 * Enseña al usuario el motivo y la solución paso a paso en lugar de mostrar un error técnico crudo.
 */
@Composable
fun DialogMicroLeccionVersion(
    versionEncontrada: Int,
    versionEsperada: Int,
    pasoActual: Int,
    onCambiarPaso: (Int) -> Unit,
    onDismiss: () -> Unit
) {
    Dialog(onDismissRequest = onDismiss) {
        Card(
            shape = RoundedCornerShape(24.dp),
            colors = CardDefaults.cardColors(containerColor = SurfaceCard),
            elevation = CardDefaults.cardElevation(defaultElevation = 10.dp),
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
                // Header con insignia de aprendizaje
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Box(
                            modifier = Modifier
                                .size(36.dp)
                                .clip(CircleShape)
                                .background(MaizPrimaryLight),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                imageVector = Icons.Default.School,
                                contentDescription = null,
                                tint = MaizPrimary,
                                modifier = Modifier.size(20.dp)
                            )
                        }
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = "Micro-Lección",
                            fontSize = 15.sp,
                            fontWeight = FontWeight.Bold,
                            color = TextPrimary
                        )
                    }

                    // Indicador de pasos: Píldora 1 de 3
                    Text(
                        text = "Paso $pasoActual de 3",
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Bold,
                        color = MaizPrimaryDark,
                        modifier = Modifier
                            .clip(RoundedCornerShape(12.dp))
                            .background(SurfaceContainerLow)
                            .padding(horizontal = 10.dp, vertical = 4.dp)
                    )
                }

                Spacer(modifier = Modifier.height(18.dp))

                // Contenido dinámico según el paso
                AnimatedContent(
                    targetState = pasoActual,
                    label = "microLessonStep"
                ) { paso ->
                    when (paso) {
                        1 -> PasoDiagnostico(versionEncontrada, versionEsperada)
                        2 -> PasoSolucion()
                        3 -> PasoAlternativa()
                        else -> PasoDiagnostico(versionEncontrada, versionEsperada)
                    }
                }

                Spacer(modifier = Modifier.height(22.dp))

                // Botones de navegación del tutorial
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    if (pasoActual > 1) {
                        OutlinedButton(
                            onClick = { onCambiarPaso(pasoActual - 1) },
                            shape = RoundedCornerShape(12.dp),
                            modifier = Modifier.height(44.dp)
                        ) {
                            Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = null, modifier = Modifier.size(16.dp))
                            Spacer(modifier = Modifier.width(4.dp))
                            Text("Atrás")
                        }
                    } else {
                        TextButton(onClick = onDismiss) {
                            Text("Cerrar", color = TextSecondary)
                        }
                    }

                    if (pasoActual < 3) {
                        Button(
                            onClick = { onCambiarPaso(pasoActual + 1) },
                            shape = RoundedCornerShape(12.dp),
                            colors = ButtonDefaults.buttonColors(containerColor = MaizPrimary),
                            modifier = Modifier.height(44.dp)
                        ) {
                            Text("Siguiente", color = Color.White, fontWeight = FontWeight.Bold)
                            Spacer(modifier = Modifier.width(4.dp))
                            Icon(Icons.AutoMirrored.Filled.ArrowForward, contentDescription = null, modifier = Modifier.size(16.dp))
                        }
                    } else {
                        Button(
                            onClick = onDismiss,
                            shape = RoundedCornerShape(12.dp),
                            colors = ButtonDefaults.buttonColors(containerColor = VerdeAgave),
                            modifier = Modifier.height(44.dp)
                        ) {
                            Icon(Icons.Default.Check, contentDescription = null, modifier = Modifier.size(16.dp))
                            Spacer(modifier = Modifier.width(4.dp))
                            Text("Entendido", color = Color.White, fontWeight = FontWeight.Bold)
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun PasoDiagnostico(versionEncontrada: Int, versionEsperada: Int) {
    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        Box(
            modifier = Modifier
                .size(60.dp)
                .clip(CircleShape)
                .background(TerracotaLight),
            contentAlignment = Alignment.Center
        ) {
            Icon(Icons.Default.Lightbulb, contentDescription = null, tint = TerracotaSecondary, modifier = Modifier.size(32.dp))
        }
        Spacer(modifier = Modifier.height(12.dp))
        Text(
            text = "¿Por qué se detuvo la importación?",
            fontSize = 16.sp,
            fontWeight = FontWeight.Bold,
            color = TextPrimary,
            textAlign = TextAlign.Center
        )
        Spacer(modifier = Modifier.height(8.dp))
        Text(
            text = "El archivo que seleccionaste tiene una estructura interna diferente (v$versionEncontrada) a la que espera esta aplicación (v$versionEsperada).\n\nPara proteger tus cortes de caja y evitar que la app falle, bloqueamos la carga hasta igualar versiones.",
            fontSize = 13.sp,
            color = TextSecondary,
            textAlign = TextAlign.Center,
            lineHeight = 18.sp
        )
    }
}

@Composable
private fun PasoSolucion() {
    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        Box(
            modifier = Modifier
                .size(60.dp)
                .clip(CircleShape)
                .background(VerdeAgaveLight),
            contentAlignment = Alignment.Center
        ) {
            Icon(Icons.Default.SystemUpdate, contentDescription = null, tint = VerdeAgave, modifier = Modifier.size(32.dp))
        }
        Spacer(modifier = Modifier.height(12.dp))
        Text(
            text = "¿Cómo solucionarlo?",
            fontSize = 16.sp,
            fontWeight = FontWeight.Bold,
            color = TextPrimary,
            textAlign = TextAlign.Center
        )
        Spacer(modifier = Modifier.height(8.dp))
        Text(
            text = "1. Instala la misma versión del APK de Tortillería Derek en ambos teléfonos.\n2. Vuelve al teléfono anterior y genera un nuevo archivo de respaldo.\n3. Pásalo a este teléfono e impórtalo nuevamente. ¡Cargará al instante!",
            fontSize = 13.sp,
            color = TextSecondary,
            textAlign = TextAlign.Start,
            lineHeight = 19.sp
        )
    }
}

@Composable
private fun PasoAlternativa() {
    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        Box(
            modifier = Modifier
                .size(60.dp)
                .clip(CircleShape)
                .background(SurfaceContainerLow),
            contentAlignment = Alignment.Center
        ) {
            Icon(Icons.Default.Check, contentDescription = null, tint = MaizPrimary, modifier = Modifier.size(32.dp))
        }
        Spacer(modifier = Modifier.height(12.dp))
        Text(
            text = "¿Qué pasa con tus datos?",
            fontSize = 16.sp,
            fontWeight = FontWeight.Bold,
            color = TextPrimary,
            textAlign = TextAlign.Center
        )
        Spacer(modifier = Modifier.height(8.dp))
        Text(
            text = "¡Tus datos están 100% seguros! Ni el teléfono emisor ni este teléfono sufrieron modificaciones. Puedes seguir trabajando y cobrando con normalidad en el teléfono anterior hasta que actualices.",
            fontSize = 13.sp,
            color = TextSecondary,
            textAlign = TextAlign.Center,
            lineHeight = 18.sp
        )
    }
}
