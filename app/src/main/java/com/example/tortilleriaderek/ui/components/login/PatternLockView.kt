package com.example.tortilleriaderek.ui.components.login

import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Fingerprint
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.tortilleriaderek.R

private val BrandOrange = Color(0xFFFF6B00)
private val BrandOrangeLight = Color(0xFFFFF0E5)
private val NeutralBorderSubtle = Color(0xFFE6E3E0)
private val NeutralBody = Color(0xFF736E69)

/**
 * Creado por 🎨 design-ui-expert y 🏗️ mobile-developer.
 * Componente stateless ergonómico de patrón táctil (3x3 grid) para terminal POS.
 * Permite la selección secuencial de nodos conectables con confirmación y retroalimentación visual.
 */
@Composable
fun PatternLockView(
    patron: String,
    bloqueado: Boolean,
    tieneBiometria: Boolean,
    onNodoSeleccionado: (Int) -> Unit,
    onLimpiarPatron: () -> Unit,
    onConfirmarPatron: () -> Unit,
    onBiometriaClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val nodosSeleccionados = patron.mapNotNull { it.digitToIntOrNull() }

    Column(
        modifier = modifier
            .fillMaxWidth()
            .testTag("pattern_lock_view"),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        // Indicador de estado del patrón
        Row(
            modifier = Modifier.padding(bottom = 12.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.Center
        ) {
            Text(
                text = if (nodosSeleccionados.isEmpty()) {
                    stringResource(R.string.login_patron_instruccion)
                } else {
                    stringResource(R.string.login_patron_puntos_conectados, nodosSeleccionados.size)
                },
                fontSize = 12.sp,
                fontWeight = FontWeight.SemiBold,
                color = if (nodosSeleccionados.size >= 4) BrandOrange else NeutralBody
            )
        }

        // Matriz 3x3 de Nodos del Patrón
        val filas = listOf(
            listOf(1, 2, 3),
            listOf(4, 5, 6),
            listOf(7, 8, 9)
        )

        for (fila in filas) {
            Row(
                horizontalArrangement = Arrangement.spacedBy(24.dp),
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier.padding(vertical = 8.dp)
            ) {
                for (nodo in fila) {
                    val estaSeleccionado = nodosSeleccionados.contains(nodo)
                    val ordenSeleccion = nodosSeleccionados.indexOf(nodo) + 1

                    NodoPatron(
                        nodoId = nodo,
                        estaSeleccionado = estaSeleccionado,
                        ordenSeleccion = ordenSeleccion,
                        habilitado = !bloqueado,
                        onClick = {
                            if (!estaSeleccionado && !bloqueado) {
                                onNodoSeleccionado(nodo)
                            }
                        }
                    )
                }
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        // Fila de Controles: Biometría, Limpiar y Confirmar
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Botón de Biometría (si está disponible)
            if (tieneBiometria) {
                Surface(
                    shape = CircleShape,
                    color = BrandOrangeLight,
                    border = androidx.compose.foundation.BorderStroke(1.dp, BrandOrange.copy(alpha = 0.4f)),
                    modifier = Modifier
                        .size(48.dp)
                        .clip(CircleShape)
                        .clickable(enabled = !bloqueado, onClick = onBiometriaClick)
                        .testTag("pattern_btn_biometria")
                ) {
                    Box(contentAlignment = Alignment.Center) {
                        Icon(
                            imageVector = Icons.Default.Fingerprint,
                            contentDescription = stringResource(R.string.login_btn_biometria_desc),
                            tint = BrandOrange,
                            modifier = Modifier.size(24.dp)
                        )
                    }
                }
            } else {
                Spacer(modifier = Modifier.size(48.dp))
            }

            // Botón Limpiar Patrón
            OutlinedButton(
                onClick = onLimpiarPatron,
                enabled = !bloqueado && nodosSeleccionados.isNotEmpty(),
                shape = RoundedCornerShape(12.dp),
                modifier = Modifier.testTag("pattern_btn_limpiar")
            ) {
                Icon(
                    imageVector = Icons.Default.Delete,
                    contentDescription = null,
                    modifier = Modifier.size(16.dp)
                )
                Spacer(modifier = Modifier.width(6.dp))
                Text(
                    text = stringResource(R.string.login_patron_limpiar),
                    fontSize = 12.sp
                )
            }

            // Botón Confirmar Patrón
            Button(
                onClick = onConfirmarPatron,
                enabled = !bloqueado && nodosSeleccionados.size >= 4,
                shape = RoundedCornerShape(12.dp),
                colors = ButtonDefaults.buttonColors(
                    containerColor = BrandOrange,
                    contentColor = Color.White
                ),
                modifier = Modifier.testTag("pattern_btn_confirmar")
            ) {
                Icon(
                    imageVector = Icons.Default.Check,
                    contentDescription = null,
                    modifier = Modifier.size(16.dp)
                )
                Spacer(modifier = Modifier.width(6.dp))
                Text(
                    text = stringResource(R.string.btn_confirmar),
                    fontSize = 12.sp,
                    fontWeight = FontWeight.Bold
                )
            }
        }
    }
}

@Composable
private fun NodoPatron(
    nodoId: Int,
    estaSeleccionado: Boolean,
    ordenSeleccion: Int,
    habilitado: Boolean,
    onClick: () -> Unit
) {
    val bgColor by animateColorAsState(
        targetValue = if (estaSeleccionado) BrandOrange else Color.White,
        animationSpec = tween(200),
        label = "nodoBgColor"
    )
    val borderColor by animateColorAsState(
        targetValue = if (estaSeleccionado) BrandOrange else NeutralBorderSubtle,
        animationSpec = tween(200),
        label = "nodoBorderColor"
    )
    val textColor by animateColorAsState(
        targetValue = if (estaSeleccionado) Color.White else NeutralBody,
        animationSpec = tween(200),
        label = "nodoTextColor"
    )

    Surface(
        shape = CircleShape,
        color = bgColor,
        border = androidx.compose.foundation.BorderStroke(2.dp, borderColor),
        shadowElevation = if (estaSeleccionado) 4.dp else 1.dp,
        modifier = Modifier
            .size(58.dp)
            .clip(CircleShape)
            .clickable(enabled = habilitado, onClick = onClick)
            .testTag("pattern_node_$nodoId")
    ) {
        Box(contentAlignment = Alignment.Center) {
            if (estaSeleccionado) {
                Text(
                    text = ordenSeleccion.toString(),
                    fontSize = 14.sp,
                    fontWeight = FontWeight.ExtraBold,
                    color = textColor
                )
            } else {
                Box(
                    modifier = Modifier
                        .size(12.dp)
                        .clip(CircleShape)
                        .background(NeutralBorderSubtle)
                )
            }
        }
    }
}
