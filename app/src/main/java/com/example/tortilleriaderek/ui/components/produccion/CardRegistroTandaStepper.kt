package com.example.tortilleriaderek.ui.components.produccion

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Inventory2
import androidx.compose.material.icons.filled.Remove
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.tortilleriaderek.ui.theme.*
import java.util.Locale

/**
 * Tarjeta interactiva con stepper táctil para configurar los bultos a producir y su equivalencia en masa/tortilla.
 */
@Composable
fun CardRegistroTandaStepper(
    pesoBultoHarinaKg: Double,
    rendimientoPorBulto: Double,
    bultosHarinaInput: Int,
    masaCrudaCalculada: Double,
    tortillaCalculada: Double,
    onIncrementar: () -> Unit,
    onDecrementar: () -> Unit,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier.fillMaxWidth(),
        verticalArrangement = Arrangement.spacedBy(10.dp)
    ) {
        // Encabezado de sección
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = "Registro de Nueva Tanda",
                fontSize = 16.sp,
                fontWeight = FontWeight.Bold,
                color = TextPrimary
            )

            Surface(
                shape = RoundedCornerShape(12.dp),
                color = Color(0xFFFFF3EB),
                border = BorderStroke(1.dp, Color(0xFFFFD7BF))
            ) {
                Text(
                    text = "Rendimiento: %.1f kg / bulto".format(rendimientoPorBulto),
                    fontSize = 10.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color(0xFFE05300),
                    modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                )
            }
        }

        // Card Interactiva de Insumos (Bultos de Harina)
        Surface(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(20.dp),
            color = Color.White,
            border = BorderStroke(1.dp, BorderSubtle),
            shadowElevation = 2.dp
        ) {
            Column(
                modifier = Modifier.padding(18.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                // Encabezado del insumo
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.spacedBy(3.dp)
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.Inventory2,
                            contentDescription = null,
                            tint = MaizPrimary,
                            modifier = Modifier.size(18.dp)
                        )
                        Text(
                            text = "Bultos de Harina (%.0f kg c/u)".format(pesoBultoHarinaKg),
                            fontSize = 15.sp,
                            fontWeight = FontWeight.Bold,
                            color = TextPrimary
                        )
                    }

                    Text(
                        text = "Rinde ~%.1f kg de tortilla cocida por bulto".format(rendimientoPorBulto),
                        fontSize = 12.sp,
                        color = TextSecondary
                    )
                }

                // Stepper central táctil (touch target >= 44-56dp)
                Surface(
                    shape = RoundedCornerShape(26.dp),
                    color = Color(0xFFF7F5F2),
                    border = BorderStroke(1.dp, BorderSubtle.copy(alpha = 0.5f))
                ) {
                    Row(
                        modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(16.dp)
                    ) {
                        // Botón Menos
                        FilledIconButton(
                            onClick = onDecrementar,
                            enabled = bultosHarinaInput > 0,
                            modifier = Modifier
                                .size(48.dp)
                                .testTag("produccion_stepper_decrement"),
                            shape = CircleShape,
                            colors = IconButtonDefaults.filledIconButtonColors(
                                containerColor = Color.White,
                                contentColor = TextPrimary,
                                disabledContainerColor = Color.White.copy(alpha = 0.5f),
                                disabledContentColor = Color.LightGray
                            )
                        ) {
                            Icon(Icons.Default.Remove, contentDescription = "Reducir bultos")
                        }

                        // Contador central
                        Row(
                            verticalAlignment = Alignment.Bottom,
                            horizontalArrangement = Arrangement.spacedBy(4.dp)
                        ) {
                            Text(
                                text = "$bultosHarinaInput",
                                fontSize = 26.sp,
                                fontWeight = FontWeight.Black,
                                color = TextPrimary
                            )
                            Text(
                                text = "bultos",
                                fontSize = 13.sp,
                                fontWeight = FontWeight.Medium,
                                color = TextSecondary,
                                modifier = Modifier.padding(bottom = 3.dp)
                            )
                        }

                        // Botón Más
                        FilledIconButton(
                            onClick = onIncrementar,
                            modifier = Modifier
                                .size(48.dp)
                                .testTag("produccion_stepper_increment"),
                            shape = CircleShape,
                            colors = IconButtonDefaults.filledIconButtonColors(
                                containerColor = MaizPrimary,
                                contentColor = Color.White
                            )
                        ) {
                            Icon(Icons.Default.Add, contentDescription = "Aumentar bultos")
                        }
                    }
                }

                // Equivalencia en masa cruda y tortilla
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    Text(
                        text = "Equivale a ",
                        fontSize = 12.sp,
                        color = TextSecondary
                    )
                    Text(
                        text = String.format(Locale.US, "%.0f kg", masaCrudaCalculada),
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Bold,
                        color = TextPrimary
                    )
                    Text(
                        text = "masa cruda",
                        fontSize = 12.sp,
                        color = TextSecondary
                    )
                    Text(
                        text = "➔",
                        fontSize = 13.sp,
                        fontWeight = FontWeight.Bold,
                        color = MaizPrimary
                    )
                    Text(
                        text = String.format(Locale.US, "%.1f kg", tortillaCalculada),
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Bold,
                        color = MaizPrimary
                    )
                    Text(
                        text = "tortilla",
                        fontSize = 12.sp,
                        color = TextSecondary
                    )
                }
            }
        }
    }
}

@Preview(showBackground = true)
@Composable
fun CardRegistroTandaStepperPreview() {
    TortilleriaDerekTheme {
        CardRegistroTandaStepper(
            pesoBultoHarinaKg = 50.0,
            rendimientoPorBulto = 38.5,
            bultosHarinaInput = 2,
            masaCrudaCalculada = 180.0,
            tortillaCalculada = 77.0,
            onIncrementar = {},
            onDecrementar = {}
        )
    }
}
