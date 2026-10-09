package com.example.tortilleriaderek.ui.components.backup

import androidx.compose.animation.core.*
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import com.example.tortilleriaderek.presentation.backup.FaseCargaDb
import com.example.tortilleriaderek.ui.theme.*

/**
 * Creado por 🎨 design-ui-expert y 🛡️ security-expert.
 * Overlay bloqueante con animación continua en Canvas (silo de datos y pulsos concéntricos).
 * Transmite seguridad, evita toques accidentales y apagados durante operaciones críticas SQLite.
 */
@Composable
fun OverlayCargandoDbAnimado(
    faseActual: FaseCargaDb
) {
    // Diálogo a pantalla completa no cancelable por toques ni atrás
    Dialog(
        onDismissRequest = {},
        properties = DialogProperties(
            dismissOnBackPress = false,
            dismissOnClickOutside = false,
            usePlatformDefaultWidth = false
        )
    ) {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(Color.Black.copy(alpha = 0.65f))
                .clickable(
                    interactionSource = remember { MutableInteractionSource() },
                    indication = null,
                    onClick = {}
                ),
            contentAlignment = Alignment.Center
        ) {
            Card(
                shape = RoundedCornerShape(28.dp),
                colors = CardDefaults.cardColors(containerColor = SurfaceCard),
                elevation = CardDefaults.cardElevation(defaultElevation = 16.dp),
                modifier = Modifier
                    .fillMaxWidth(0.85f)
                    .padding(16.dp)
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(28.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    // Animación de Silo SQLite y Anillos Concéntricos en Canvas
                    AnimacionSiloBaseDatos(modifier = Modifier.size(110.dp))

                    Spacer(modifier = Modifier.height(20.dp))

                    Text(
                        text = "Transferencia en Progreso",
                        fontSize = 18.sp,
                        fontWeight = FontWeight.Bold,
                        color = TextPrimary
                    )

                    Spacer(modifier = Modifier.height(8.dp))

                    // Texto de la fase dinámica
                    Text(
                        text = faseActual.mensaje,
                        fontSize = 13.sp,
                        fontWeight = FontWeight.Medium,
                        color = MaizPrimaryDark,
                        textAlign = TextAlign.Center
                    )

                    Spacer(modifier = Modifier.height(18.dp))

                    // Barra de progreso indeterminada estilizada con Maíz & Masa POS
                    LinearProgressIndicator(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(6.dp)
                            .clip(RoundedCornerShape(3.dp)),
                        color = MaizPrimary,
                        trackColor = SurfaceContainerHigh
                    )

                    Spacer(modifier = Modifier.height(16.dp))

                    // Insignia de advertencia de seguridad
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier
                            .clip(RoundedCornerShape(10.dp))
                            .background(SurfaceWarm)
                            .border(1.dp, BorderSubtle, RoundedCornerShape(10.dp))
                            .padding(horizontal = 12.dp, vertical = 6.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.Lock,
                            contentDescription = null,
                            tint = TextSecondary,
                            modifier = Modifier.size(14.dp)
                        )
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(
                            text = "No apagues el teléfono ni cierres la app",
                            fontSize = 11.sp,
                            color = TextSecondary,
                            fontWeight = FontWeight.SemiBold
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun AnimacionSiloBaseDatos(modifier: Modifier = Modifier) {
    val infiniteTransition = rememberInfiniteTransition(label = "dbPulse")

    val pulseRadius by infiniteTransition.animateFloat(
        initialValue = 35f,
        targetValue = 65f,
        animationSpec = infiniteRepeatable(
            animation = tween(1400, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Restart
        ),
        label = "pulseRadius"
    )

    val pulseAlpha by infiniteTransition.animateFloat(
        initialValue = 0.7f,
        targetValue = 0f,
        animationSpec = infiniteRepeatable(
            animation = tween(1400, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Restart
        ),
        label = "pulseAlpha"
    )

    val verticalWave by infiniteTransition.animateFloat(
        initialValue = -4f,
        targetValue = 4f,
        animationSpec = infiniteRepeatable(
            animation = tween(900, easing = LinearEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "verticalWave"
    )

    Canvas(modifier = modifier) {
        val centerX = size.width / 2
        val centerY = size.height / 2

        // Onda concéntrica exterior
        drawCircle(
            color = MaizPrimary.copy(alpha = pulseAlpha),
            radius = pulseRadius * density,
            center = Offset(centerX, centerY),
            style = Stroke(width = 2.dp.toPx())
        )

        // Silo / Cilindro de Base de Datos
        val siloWidth = 44.dp.toPx()
        val siloHeight = 52.dp.toPx()
        val diskHeight = 14.dp.toPx()
        val left = centerX - siloWidth / 2
        val top = centerY - siloHeight / 2 + verticalWave

        // Fondo del cilindro dorado
        drawRoundRect(
            color = MaizPrimaryLight,
            topLeft = Offset(left, top),
            size = Size(siloWidth, siloHeight),
            cornerRadius = androidx.compose.ui.geometry.CornerRadius(siloWidth / 2, diskHeight / 2)
        )

        // Anillos de discos de datos (3 capas)
        val ringCount = 3
        for (i in 0 until ringCount) {
            val ringY = top + (i * (siloHeight - diskHeight) / (ringCount - 1))
            drawOval(
                color = MaizPrimary,
                topLeft = Offset(left, ringY),
                size = Size(siloWidth, diskHeight),
                style = Stroke(width = 2.5.dp.toPx())
            )
        }

        // Borde exterior del silo
        drawRoundRect(
            color = MaizPrimaryDark,
            topLeft = Offset(left, top),
            size = Size(siloWidth, siloHeight),
            cornerRadius = androidx.compose.ui.geometry.CornerRadius(siloWidth / 2, diskHeight / 2),
            style = Stroke(width = 2.5.dp.toPx())
        )
    }
}
