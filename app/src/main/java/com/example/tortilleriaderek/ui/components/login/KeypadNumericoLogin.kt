package com.example.tortilleriaderek.ui.components.login

import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.Backspace
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
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.tortilleriaderek.R

private val BrandOrange = Color(0xFFFF6B00)
private val BrandOrangeLight = Color(0xFFFFF0E5)
private val NeutralBorderSubtle = Color(0xFFE6E3E0)
private val NeutralTitle = Color(0xFF181615)
private val NeutralBody = Color(0xFF736E69)

/**
 * Creado por 🎨 design-ui-expert y 🛡️ security-expert.
 * Keypad táctil numérico industrial (Artículo VI de la Constitución: Touch targets 64dp y cifras tabulares tnum).
 * Diseñado para operar en mostrador de tortillería con dedos enharinados o húmedos.
 */
@Composable
fun KeypadNumericoLogin(
    pinLength: Int,
    maxDigits: Int = 6,
    tieneBiometria: Boolean = false,
    bloqueado: Boolean = false,
    segundosRestantes: Long = 0L,
    onDigitoClick: (String) -> Unit,
    onBorrarClick: () -> Unit,
    onBiometriaClick: () -> Unit = {},
    onOlvidastePinClick: () -> Unit = {},
    modifier: Modifier = Modifier
) {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        modifier = modifier
            .fillMaxWidth()
            .testTag("keypad_numerico_login")
    ) {
        // Indicador de Puntos (PIN Dots)
        Row(
            horizontalArrangement = Arrangement.spacedBy(14.dp),
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier
                .padding(vertical = 12.dp)
                .testTag("keypad_pin_dots")
        ) {
            for (i in 0 until 4) {
                val isFilled = i < pinLength
                val dotColor by animateColorAsState(
                    targetValue = if (isFilled) BrandOrange else NeutralBorderSubtle,
                    animationSpec = tween(150),
                    label = "dotColor"
                )
                Box(
                    modifier = Modifier
                        .size(16.dp)
                        .clip(CircleShape)
                        .background(dotColor)
                        .border(1.5.dp, if (isFilled) BrandOrange else Color(0xFFCCC7C2), CircleShape)
                )
            }
        }

        if (bloqueado) {
            Text(
                text = stringResource(R.string.login_bloqueo_aviso, segundosRestantes),
                color = MaterialTheme.colorScheme.error,
                fontSize = 12.sp,
                fontWeight = FontWeight.SemiBold,
                textAlign = TextAlign.Center,
                modifier = Modifier.padding(bottom = 8.dp)
            )
        }

        Spacer(modifier = Modifier.height(8.dp))

        // Filas del Teclado Numérico (64dp touch target conforme a la Constitución)
        val filas = listOf(
            listOf("1", "2", "3"),
            listOf("4", "5", "6"),
            listOf("7", "8", "9")
        )

        for (fila in filas) {
            Row(
                horizontalArrangement = Arrangement.spacedBy(16.dp),
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier.padding(vertical = 5.dp)
            ) {
                for (digito in fila) {
                    BotonDigitoKeypad(
                        digito = digito,
                        enabled = !bloqueado && pinLength < maxDigits,
                        onClick = { onDigitoClick(digito) }
                    )
                }
            }
        }

        // Fila Inferior: Biometría / Vacío | "0" | Borrar
        Row(
            horizontalArrangement = Arrangement.spacedBy(16.dp),
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier.padding(vertical = 5.dp)
        ) {
            // Botón Biometría o Espaciador
            if (tieneBiometria) {
                Surface(
                    shape = CircleShape,
                    color = BrandOrangeLight,
                    border = androidx.compose.foundation.BorderStroke(1.dp, BrandOrange.copy(alpha = 0.4f)),
                    modifier = Modifier
                        .size(64.dp)
                        .clip(CircleShape)
                        .clickable(enabled = !bloqueado, onClick = onBiometriaClick)
                        .testTag("keypad_btn_biometria")
                ) {
                    Box(contentAlignment = Alignment.Center) {
                        Icon(
                            imageVector = Icons.Default.Fingerprint,
                            contentDescription = stringResource(R.string.login_btn_biometria_desc),
                            tint = BrandOrange,
                            modifier = Modifier.size(30.dp)
                        )
                    }
                }
            } else {
                Spacer(modifier = Modifier.size(64.dp))
            }

            // Dígito "0"
            BotonDigitoKeypad(
                digito = "0",
                enabled = !bloqueado && pinLength < maxDigits,
                onClick = { onDigitoClick("0") }
            )

            // Botón Borrar (Backspace)
            Surface(
                shape = CircleShape,
                color = Color.White,
                border = androidx.compose.foundation.BorderStroke(1.dp, NeutralBorderSubtle),
                shadowElevation = 1.dp,
                modifier = Modifier
                    .size(64.dp)
                    .clip(CircleShape)
                    .clickable(enabled = !bloqueado && pinLength > 0, onClick = onBorrarClick)
                    .testTag("keypad_btn_backspace")
            ) {
                Box(contentAlignment = Alignment.Center) {
                    Icon(
                        imageVector = Icons.AutoMirrored.Filled.Backspace,
                        contentDescription = "Borrar",
                        tint = if (pinLength > 0) NeutralTitle else NeutralBody.copy(alpha = 0.4f),
                        modifier = Modifier.size(24.dp)
                    )
                }
            }
        }

        Spacer(modifier = Modifier.height(14.dp))

        // Enlace táctil de recuperación con Clave Maestra
        Text(
            text = stringResource(R.string.login_olvidaste_pin),
            fontSize = 12.sp,
            fontWeight = FontWeight.SemiBold,
            color = BrandOrange,
            textAlign = TextAlign.Center,
            modifier = Modifier
                .clip(CircleShape)
                .clickable { onOlvidastePinClick() }
                .padding(horizontal = 12.dp, vertical = 6.dp)
                .testTag("login_enlace_master_key")
        )
    }
}

@Composable
private fun BotonDigitoKeypad(
    digito: String,
    enabled: Boolean,
    onClick: () -> Unit
) {
    Surface(
        shape = CircleShape,
        color = Color.White,
        border = androidx.compose.foundation.BorderStroke(1.dp, NeutralBorderSubtle),
        shadowElevation = if (enabled) 2.dp else 0.dp,
        modifier = Modifier
            .size(64.dp) // Touch target ergonómico de 64dp (Artículo VI)
            .clip(CircleShape)
            .clickable(enabled = enabled, onClick = onClick)
            .testTag("keypad_digito_$digito")
    ) {
        Box(contentAlignment = Alignment.Center) {
            Text(
                text = digito,
                fontSize = 24.sp,
                fontWeight = FontWeight.Bold,
                color = if (enabled) NeutralTitle else NeutralBody.copy(alpha = 0.4f),
                style = LocalTextStyle.current.copy(
                    fontFeatureSettings = "tnum" // Cifras tabulares tnum
                )
            )
        }
    }
}
