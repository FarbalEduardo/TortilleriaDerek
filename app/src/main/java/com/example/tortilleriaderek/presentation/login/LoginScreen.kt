package com.example.tortilleriaderek.presentation.login

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.animateDpAsState
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowForward
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Error
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.PointOfSale
import androidx.compose.material.icons.filled.Security
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.filled.Visibility
import androidx.compose.material.icons.filled.VisibilityOff
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.testTag
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardCapitalization
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

import androidx.compose.foundation.BorderStroke
import androidx.compose.ui.zIndex

// =========================================================================
// PALETA DE COLORES Y TOKENS LOCALES (Fieles a maqueta Stitch 01_login)
// =========================================================================
private val BrandOrange = Color(0xFFFF6B00)
private val BrandOrangeDark = Color(0xFFE05E00)
private val BrandOrangeLight = Color(0xFFFFF0E5)
private val BrandOrangeSurface = Color(0xFFFFF7F2)
private val BrandOrangeBadgeBorder = Color(0xFFFFD7BA)

private val NeutralBg = Color(0xFFF8F9FA)
private val NeutralTitle = Color(0xFF181615)
private val NeutralBody = Color(0xFF736E69)
private val NeutralBorderSubtle = Color(0xFFE6E3E0)
private val NeutralIconBg = Color(0xFFF4F2F0)
private val NeutralIconTint = Color(0xFF524E4A)

/**
 * Creado por 🎨 design-ui-expert y 🏗️ mobile-developer.
 * Componente UI 100% Stateless fiel a la maqueta Stitch "Acceso al Sistema".
 * Cumple con las especificaciones de login_y_turnos_spec.md y MVI.
 */
@Composable
fun LoginScreenContent(
    uiState: LoginUiState,
    onEvent: (LoginUiEvent) -> Unit,
    modifier: Modifier = Modifier
) {
    Box(
        modifier = modifier
            .fillMaxSize()
            .background(NeutralBg),
        contentAlignment = Alignment.TopCenter
    ) {
        // Botón visible de Ajustes en esquina superior derecha con statusBarsPadding y zIndex (US6)
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .statusBarsPadding()
                .padding(horizontal = 20.dp, vertical = 12.dp)
                .zIndex(20f),
            horizontalArrangement = Arrangement.End
        ) {
            Surface(
                shape = CircleShape,
                color = Color.White,
                border = BorderStroke(1.dp, NeutralBorderSubtle),
                shadowElevation = 2.dp,
                modifier = Modifier.size(44.dp)
            ) {
                IconButton(
                    onClick = { onEvent(LoginUiEvent.OnSettingsClick) },
                    modifier = Modifier
                        .fillMaxSize()
                        .testTag("login_settings_button")
                ) {
                    Icon(
                        imageVector = Icons.Default.Settings,
                        contentDescription = "Ajustes",
                        tint = NeutralIconTint,
                        modifier = Modifier.size(22.dp)
                    )
                }
            }
        }

        // Contenedor principal centrado adaptable (Móvil y Tablet)
        Column(
            modifier = Modifier
                .widthIn(max = 430.dp)
                .fillMaxSize()
                .verticalScroll(rememberScrollState())
                .statusBarsPadding()
                .navigationBarsPadding()
                .padding(horizontal = 24.dp, vertical = 20.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.SpaceBetween
        ) {
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                modifier = Modifier.fillMaxWidth()
            ) {
                Spacer(modifier = Modifier.height(10.dp))

                // 1. Status Pill: TURNO CERRADO
                if (uiState.isTurnoCerrado) {
                    StatusPillTurnoCerrado()
                    Spacer(modifier = Modifier.height(18.dp))
                }

                // 2. Encabezado de la Pantalla
                Text(
                    text = "Tortilleria Derek",
                    fontSize = 25.sp,
                    fontWeight = FontWeight.ExtraBold,
                    color = NeutralTitle,
                    letterSpacing = (-0.5).sp
                )
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = "Selecciona el modo de inicio de sesión",
                    fontSize = 14.sp,
                    fontWeight = FontWeight.Normal,
                    color = NeutralBody
                )

                Spacer(modifier = Modifier.height(26.dp))

                // 3. Selector Dual de Modos (Tarjetas interactivas)
                Column(
                    modifier = Modifier.fillMaxWidth(),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    // Opción A: Abrir Nuevo Turno
                    ModeCardOption(
                        title = "Abrir Nuevo Turno",
                        description = "Permite registrar ventas de mostrador y liquidar repartidores",
                        icon = Icons.Default.PointOfSale,
                        isSelected = uiState.selectedMode == LoginMode.ABRIR_TURNO,
                        onClick = { onEvent(LoginUiEvent.OnModeSelected(LoginMode.ABRIR_TURNO)) },
                        modifier = Modifier.testTag("login_mode_turno")
                    )

                    // Opción B: Modo Solo Consulta
                    ModeCardOption(
                        title = "Modo Solo Consulta",
                        description = "Solo lectura para ver gráficas, reportes y métricas históricas",
                        icon = Icons.Default.Visibility,
                        isSelected = uiState.selectedMode == LoginMode.SOLO_CONSULTA,
                        onClick = { onEvent(LoginUiEvent.OnModeSelected(LoginMode.SOLO_CONSULTA)) },
                        modifier = Modifier.testTag("login_mode_consulta")
                    )
                }

                Spacer(modifier = Modifier.height(26.dp))

                // 4. Sección Credenciales de Acceso
                Text(
                    text = "CREDENCIALES DE ACCESO",
                    fontSize = 11.sp,
                    fontWeight = FontWeight.Bold,
                    color = NeutralBody,
                    letterSpacing = 1.2.sp,
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(start = 4.dp, bottom = 10.dp)
                )

                // Input Usuario
                OutlinedTextField(
                    value = uiState.usernameInput,
                    onValueChange = { onEvent(LoginUiEvent.OnUsernameChanged(it)) },
                    textStyle = LocalTextStyle.current.copy(
                        color = Color.Black,
                        fontSize = 14.sp,
                        fontWeight = FontWeight.Medium
                    ),
                    modifier = Modifier
                        .fillMaxWidth()
                        .shadow(1.dp, CircleShape)
                        .testTag("login_username_input"),
                    shape = CircleShape,
                    placeholder = {
                        Text(
                            text = "Usuario o Nombre de Operador",
                            fontSize = 14.sp,
                            color = NeutralBody.copy(alpha = 0.6f)
                        )
                    },
                    leadingIcon = {
                        Icon(
                            imageVector = Icons.Default.Person,
                            contentDescription = null,
                            tint = NeutralBody.copy(alpha = 0.65f),
                            modifier = Modifier.size(20.dp)
                        )
                    },
                    singleLine = true,
                    keyboardOptions = KeyboardOptions(
                        capitalization = KeyboardCapitalization.None,
                        autoCorrectEnabled = false,
                        keyboardType = KeyboardType.Text
                    ),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedTextColor = Color.Black,
                        unfocusedTextColor = Color.Black,
                        focusedContainerColor = Color.White,
                        unfocusedContainerColor = Color.White,
                        focusedBorderColor = BrandOrange,
                        unfocusedBorderColor = NeutralBorderSubtle
                    )
                )

                Spacer(modifier = Modifier.height(12.dp))

                // Input Contraseña / PIN
                OutlinedTextField(
                    value = uiState.passwordInput,
                    onValueChange = { onEvent(LoginUiEvent.OnPasswordChanged(it)) },
                    textStyle = LocalTextStyle.current.copy(
                        color = Color.Black,
                        fontSize = 14.sp,
                        fontWeight = FontWeight.Medium
                    ),
                    modifier = Modifier
                        .fillMaxWidth()
                        .shadow(1.dp, CircleShape)
                        .testTag("login_password_input"),
                    shape = CircleShape,
                    placeholder = {
                        Text(
                            text = "Contraseña o PIN",
                            fontSize = 14.sp,
                            color = NeutralBody.copy(alpha = 0.6f)
                        )
                    },
                    leadingIcon = {
                        Icon(
                            imageVector = Icons.Default.Lock,
                            contentDescription = null,
                            tint = NeutralBody.copy(alpha = 0.65f),
                            modifier = Modifier.size(20.dp)
                        )
                    },
                    trailingIcon = {
                        IconButton(onClick = { onEvent(LoginUiEvent.OnTogglePasswordVisibility) }) {
                            Icon(
                                imageVector = if (uiState.isPasswordVisible) Icons.Default.VisibilityOff else Icons.Default.Visibility,
                                contentDescription = if (uiState.isPasswordVisible) "Ocultar contraseña" else "Mostrar contraseña",
                                tint = NeutralBody.copy(alpha = 0.7f),
                                modifier = Modifier.size(20.dp)
                            )
                        }
                    },
                    visualTransformation = if (uiState.isPasswordVisible) VisualTransformation.None else PasswordVisualTransformation(),
                    singleLine = true,
                    keyboardOptions = KeyboardOptions(
                        capitalization = KeyboardCapitalization.None,
                        autoCorrectEnabled = false,
                        keyboardType = KeyboardType.Password
                    ),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedTextColor = Color.Black,
                        unfocusedTextColor = Color.Black,
                        focusedContainerColor = Color.White,
                        unfocusedContainerColor = Color.White,
                        focusedBorderColor = BrandOrange,
                        unfocusedBorderColor = NeutralBorderSubtle
                    )
                )

                // Espacio fijo reservado para aviso o mensaje de error:
                // Previene layout shift (el botón de Iniciar Sesión permanece fijo sin saltos)
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(30.dp)
                        .padding(horizontal = 8.dp),
                    contentAlignment = Alignment.CenterStart
                ) {
                    if (uiState.errorMessage != null) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(6.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Default.Error,
                                contentDescription = null,
                                tint = MaterialTheme.colorScheme.error,
                                modifier = Modifier.size(15.dp)
                            )
                            Text(
                                text = uiState.errorMessage,
                                color = MaterialTheme.colorScheme.error,
                                fontSize = 12.sp,
                                fontWeight = FontWeight.SemiBold
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.height(8.dp))

                // 5. Botón Principal CTA: Iniciar Sesión ->
                Button(
                    onClick = { onEvent(LoginUiEvent.OnIniciarSesionClick) },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(54.dp)
                        .shadow(8.dp, CircleShape, spotColor = BrandOrange.copy(alpha = 0.35f))
                        .testTag("login_abrir_turno_button"),
                    shape = CircleShape,
                    enabled = !uiState.isLoading,
                    colors = ButtonDefaults.buttonColors(
                        containerColor = BrandOrange,
                        contentColor = Color.White,
                        disabledContainerColor = BrandOrange.copy(alpha = 0.6f),
                        disabledContentColor = Color.White.copy(alpha = 0.8f)
                    )
                ) {
                    if (uiState.isLoading) {
                        CircularProgressIndicator(
                            modifier = Modifier.size(20.dp),
                            color = Color.White,
                            strokeWidth = 2.2.dp
                        )
                        Spacer(modifier = Modifier.width(10.dp))
                        Text(
                            text = "Verificando credenciales...",
                            fontSize = 14.sp,
                            fontWeight = FontWeight.SemiBold
                        )
                    } else {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.Center
                        ) {
                            Text(
                                text = "Iniciar Sesión",
                                fontSize = 16.sp,
                                fontWeight = FontWeight.Bold
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Icon(
                                imageVector = Icons.AutoMirrored.Filled.ArrowForward,
                                contentDescription = null,
                                modifier = Modifier.size(18.dp)
                            )
                        }
                    }
                }
            }

            // 6. Footer de la pantalla
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(top = 28.dp, bottom = 12.dp)
            ) {
                // Powered by FarbalApps
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.Center,
                    modifier = Modifier.padding(bottom = 6.dp)
                ) {
                    Text(
                        text = "powered by ",
                        fontSize = 11.sp,
                        color = Color(0xFFA39E99),
                        fontWeight = FontWeight.Normal
                    )
                    Text(
                        text = "FarbalApps",
                        fontSize = 11.sp,
                        color = BrandOrange,
                        fontWeight = FontWeight.Bold
                    )
                }

                // Insignia de seguridad y versión
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.Center
                ) {
                    Icon(
                        imageVector = Icons.Default.Security,
                        contentDescription = null,
                        tint = Color(0xFFA39E99),
                        modifier = Modifier.size(14.dp)
                    )
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(
                        text = "Sistema de Control Seguro • v1.0",
                        fontSize = 11.sp,
                        color = Color(0xFFA39E99),
                        fontWeight = FontWeight.Medium
                    )
                }
            }
        }
    }
}

// =========================================================================
// COMPONENTES AUXILIARES ESTÉTICOS
// =========================================================================

/**
 * Status Pill "TURNO CERRADO" con estilo idéntico a la maqueta Stitch.
 */
@Composable
private fun StatusPillTurnoCerrado(modifier: Modifier = Modifier) {
    Box(
        modifier = modifier
            .clip(CircleShape)
            .background(BrandOrangeSurface)
            .border(1.dp, BrandOrangeBadgeBorder, CircleShape)
            .padding(horizontal = 16.dp, vertical = 6.dp),
        contentAlignment = Alignment.Center
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(6.dp)
        ) {
            Icon(
                imageVector = Icons.Default.Lock,
                contentDescription = null,
                tint = BrandOrange,
                modifier = Modifier.size(14.dp)
            )
            Text(
                text = "TURNO CERRADO",
                fontSize = 12.sp,
                fontWeight = FontWeight.Bold,
                color = BrandOrange,
                letterSpacing = 0.8.sp
            )
        }
    }
}

/**
 * Tarjeta interactiva para la selección de modo de acceso.
 */
@Composable
private fun ModeCardOption(
    title: String,
    description: String,
    icon: ImageVector,
    isSelected: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val borderColor by animateColorAsState(
        targetValue = if (isSelected) BrandOrange else NeutralBorderSubtle,
        animationSpec = tween(200),
        label = "modeBorderColor"
    )
    val borderWidth by animateDpAsState(
        targetValue = if (isSelected) 2.dp else 1.dp,
        animationSpec = tween(200),
        label = "modeBorderWidth"
    )
    val containerColor by animateColorAsState(
        targetValue = if (isSelected) BrandOrangeSurface else Color.White,
        animationSpec = tween(200),
        label = "modeContainerColor"
    )
    val iconBoxBg by animateColorAsState(
        targetValue = if (isSelected) BrandOrangeLight else NeutralIconBg,
        animationSpec = tween(200),
        label = "modeIconBoxBg"
    )
    val iconTint by animateColorAsState(
        targetValue = if (isSelected) BrandOrange else NeutralIconTint,
        animationSpec = tween(200),
        label = "modeIconTint"
    )

    Surface(
        modifier = modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(20.dp))
            .border(borderWidth, borderColor, RoundedCornerShape(20.dp))
            .clickable { onClick() },
        color = containerColor,
        shadowElevation = if (isSelected) 2.dp else 1.dp
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalAlignment = Alignment.Top,
            horizontalArrangement = Arrangement.spacedBy(14.dp)
        ) {
            // Icon Box
            Box(
                modifier = Modifier
                    .size(44.dp)
                    .clip(RoundedCornerShape(12.dp))
                    .background(iconBoxBg),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = icon,
                    contentDescription = null,
                    tint = iconTint,
                    modifier = Modifier.size(22.dp)
                )
            }

            // Textos
            Column(
                modifier = Modifier.weight(1f)
            ) {
                Text(
                    text = title,
                    fontSize = 15.sp,
                    fontWeight = FontWeight.Bold,
                    color = NeutralTitle
                )
                Spacer(modifier = Modifier.height(2.dp))
                Text(
                    text = description,
                    fontSize = 12.sp,
                    fontWeight = FontWeight.Normal,
                    color = NeutralBody,
                    lineHeight = 16.sp
                )
            }

            // Checkmark Badge (solo cuando seleccionado)
            Box(
                modifier = Modifier.size(20.dp),
                contentAlignment = Alignment.Center
            ) {
                if (isSelected) {
                    Box(
                        modifier = Modifier
                            .size(20.dp)
                            .clip(CircleShape)
                            .background(BrandOrange),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = Icons.Default.Check,
                            contentDescription = "Seleccionado",
                            tint = Color.White,
                            modifier = Modifier.size(13.dp)
                        )
                    }
                }
            }
        }
    }
}

// =========================================================================
// PREVIEWS ADAPTATIVAS (Móvil y Tablet)
// =========================================================================

@Preview(showBackground = true, device = "spec:width=411dp,height=891dp", name = "Móvil")
@Composable
fun LoginScreenPreviewMobile() {
    MaterialTheme {
        LoginScreenContent(
            uiState = LoginUiState(
                usernameInput = "Operador Derek",
                passwordInput = "1234"
            ),
            onEvent = {}
        )
    }
}

@Preview(showBackground = true, device = "spec:width=1280dp,height=800dp", name = "Tablet")
@Composable
fun LoginScreenPreviewTablet() {
    MaterialTheme {
        LoginScreenContent(
            uiState = LoginUiState(
                usernameInput = "Operador Derek",
                passwordInput = "1234"
            ),
            onEvent = {}
        )
    }
}
