package com.example.tortilleriaderek.presentation.login

import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Dialpad
import androidx.compose.material.icons.filled.GridOn
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.Security
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.filled.SwapHoriz
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.tortilleriaderek.R
import com.example.tortilleriaderek.ui.components.backup.DialogOpcionesBackup
import com.example.tortilleriaderek.ui.components.login.DialogRecuperacionMasterKey
import com.example.tortilleriaderek.ui.components.login.KeypadNumericoLogin
import com.example.tortilleriaderek.ui.components.login.LoginHeaderSection
import com.example.tortilleriaderek.ui.components.login.LoginModeSelector
import com.example.tortilleriaderek.ui.components.login.PatternLockView
import com.example.tortilleriaderek.ui.components.login.TabMetodoAcceso

private val BrandOrange = Color(0xFFFF6B00)
private val BrandOrangeSurface = Color(0xFFFFF7F2)
private val BrandOrangeBadgeBorder = Color(0xFFFFD7BA)
private val NeutralBg = Color(0xFFF8F9FA)
private val NeutralBorderSubtle = Color(0xFFE6E3E0)
private val NeutralBody = Color(0xFF736E69)

/**
 * Creado por 🎨 design-ui-expert y 🏗️ mobile-developer.
 * Pantalla modularizada de Login (Artículo III: <300 líneas).
 * Soporte exclusivo de Código Numérico (PIN) y Patrón táctil (3x3),
 * tarjetas verticales de modo de acceso y recuperación por Clave Maestra.
 */
@Composable
fun LoginScreenContent(
    uiState: LoginUiState,
    onEvent: (LoginUiEvent) -> Unit,
    onExportarBackup: () -> Unit = {},
    onImportarBackup: () -> Unit = {},
    modifier: Modifier = Modifier
) {
    var showBackupOptionsDialog by remember { mutableStateOf(false) }

    // Diálogo modal de Opciones de Backup (Exportar / Importar)
    if (showBackupOptionsDialog) {
        DialogOpcionesBackup(
            onExportar = {
                showBackupOptionsDialog = false
                onExportarBackup()
            },
            onImportar = {
                showBackupOptionsDialog = false
                onImportarBackup()
            },
            onDismiss = { showBackupOptionsDialog = false }
        )
    }

    // Diálogo modal de Recuperación por Clave Maestra
    if (uiState.showMasterKeyDialog) {
        DialogRecuperacionMasterKey(
            onDismiss = { onEvent(LoginUiEvent.OnCerrarDialogoMasterKey) },
            onConfirmarRestablecimiento = { masterKey, nuevoPin ->
                onEvent(LoginUiEvent.OnRestablecerPinMasterKey(masterKey, nuevoPin))
            },
            errorMensaje = uiState.masterKeyError,
            cargando = uiState.masterKeyCargando
        )
    }

    Box(
        modifier = modifier
            .fillMaxSize()
            .background(NeutralBg),
        contentAlignment = Alignment.TopCenter
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .verticalScroll(rememberScrollState())
                .statusBarsPadding()
                .padding(horizontal = 22.dp, vertical = 14.dp)
                .widthIn(max = 460.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            // 1. Barra Superior: Badge Status a la izquierda | Botones de Respaldo y Ajustes a la derecha
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(bottom = 12.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Box(
                    modifier = Modifier
                        .clip(CircleShape)
                        .background(BrandOrangeSurface)
                        .border(1.dp, BrandOrangeBadgeBorder, CircleShape)
                        .padding(horizontal = 12.dp, vertical = 6.dp)
                        .testTag("login_status_pill")
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.Lock,
                            contentDescription = null,
                            tint = BrandOrange,
                            modifier = Modifier.size(13.dp)
                        )
                        Text(
                            text = stringResource(R.string.login_turno_cerrado),
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Bold,
                            color = BrandOrange,
                            letterSpacing = 0.6.sp
                        )
                    }
                }

                Row(
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Surface(
                        shape = CircleShape,
                        color = Color.White,
                        border = BorderStroke(1.dp, NeutralBorderSubtle),
                        shadowElevation = 1.dp,
                        modifier = Modifier
                            .size(40.dp)
                            .clip(CircleShape)
                            .clickable { showBackupOptionsDialog = true }
                            .testTag("login_backup_button")
                    ) {
                        Box(contentAlignment = Alignment.Center) {
                            Icon(
                                imageVector = Icons.Default.SwapHoriz,
                                contentDescription = "Copia de Seguridad y Transferencia",
                                tint = BrandOrange,
                                modifier = Modifier.size(20.dp)
                            )
                        }
                    }

                    Surface(
                        shape = CircleShape,
                        color = Color.White,
                        border = BorderStroke(1.dp, NeutralBorderSubtle),
                        shadowElevation = 1.dp,
                        modifier = Modifier
                            .size(40.dp)
                            .clip(CircleShape)
                            .clickable { onEvent(LoginUiEvent.OnSettingsClick) }
                            .testTag("login_settings_button")
                    ) {
                        Box(contentAlignment = Alignment.Center) {
                            Icon(
                                imageVector = Icons.Default.Settings,
                                contentDescription = "Ajustes del Sistema",
                                tint = Color(0xFF524E4A),
                                modifier = Modifier.size(20.dp)
                            )
                        }
                    }
                }
            }

            // 2. Cabecera Visual Compacta (Título y subtítulo sin caja registradora)
            LoginHeaderSection()

            Spacer(modifier = Modifier.height(14.dp))

            // 3. Tarjetas Verticales de Modo de Acceso (Abrir Turno vs Solo Consulta)
            LoginModeSelector(
                selectedMode = uiState.selectedMode,
                onModeSelected = { onEvent(LoginUiEvent.OnModeSelected(it)) }
            )

            Spacer(modifier = Modifier.height(14.dp))

            // 4. Selector de Método de Desbloqueo: Código PIN vs Patrón
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(14.dp))
                    .background(Color.White)
                    .border(1.dp, NeutralBorderSubtle, RoundedCornerShape(14.dp))
                    .padding(4.dp),
                horizontalArrangement = Arrangement.spacedBy(4.dp)
            ) {
                TabMetodoAcceso(
                    titulo = stringResource(R.string.login_metodo_pin),
                    icon = Icons.Default.Dialpad,
                    seleccionado = uiState.metodoAcceso == MetodoAcceso.CODIGO_PIN,
                    onClick = { onEvent(LoginUiEvent.OnCambiarMetodoAcceso(MetodoAcceso.CODIGO_PIN)) },
                    testTag = "login_tab_pin",
                    modifier = Modifier.weight(1f)
                )

                TabMetodoAcceso(
                    titulo = stringResource(R.string.login_metodo_patron),
                    icon = Icons.Default.GridOn,
                    seleccionado = uiState.metodoAcceso == MetodoAcceso.PATRON,
                    onClick = { onEvent(LoginUiEvent.OnCambiarMetodoAcceso(MetodoAcceso.PATRON)) },
                    testTag = "login_tab_patron",
                    modifier = Modifier.weight(1f)
                )
            }

            // Mensaje de error si existe
            if (uiState.errorMessage != null) {
                Spacer(modifier = Modifier.height(6.dp))
                Text(
                    text = uiState.errorMessage,
                    color = MaterialTheme.colorScheme.error,
                    fontSize = 11.sp,
                    fontWeight = FontWeight.SemiBold,
                    modifier = Modifier.padding(vertical = 2.dp)
                )
            }

            Spacer(modifier = Modifier.height(8.dp))

            // 5. Contenido Dinámico: Teclado Numérico o Patrón Táctil
            if (uiState.metodoAcceso == MetodoAcceso.CODIGO_PIN) {
                KeypadNumericoLogin(
                    pinLength = uiState.pinInput.length,
                    maxDigits = 6,
                    tieneBiometria = uiState.tieneBiometria,
                    bloqueado = uiState.estaBloqueado,
                    segundosRestantes = uiState.segundosRestantesBloqueo,
                    onDigitoClick = { onEvent(LoginUiEvent.OnDigitoPresionado(it)) },
                    onBorrarClick = { onEvent(LoginUiEvent.OnBorrarDigito) },
                    onBiometriaClick = { onEvent(LoginUiEvent.OnBiometriaClick) },
                    onOlvidastePinClick = { onEvent(LoginUiEvent.OnAbrirDialogoMasterKey) }
                )
            } else {
                PatternLockView(
                    patron = uiState.patronInput,
                    bloqueado = uiState.estaBloqueado,
                    tieneBiometria = uiState.tieneBiometria,
                    onNodoSeleccionado = { onEvent(LoginUiEvent.OnPatronNodoSeleccionado(it)) },
                    onLimpiarPatron = { onEvent(LoginUiEvent.OnLimpiarPatron) },
                    onConfirmarPatron = { onEvent(LoginUiEvent.OnConfirmarPatron) },
                    onBiometriaClick = { onEvent(LoginUiEvent.OnBiometriaClick) }
                )
            }

            // 6. Enlace a Clave Maestra (cuando está en modo Patrón)
            if (uiState.metodoAcceso == MetodoAcceso.PATRON) {
                Spacer(modifier = Modifier.height(8.dp))
                TextButton(
                    onClick = { onEvent(LoginUiEvent.OnAbrirDialogoMasterKey) },
                    modifier = Modifier.testTag("login_link_master_key")
                ) {
                    Text(
                        text = stringResource(R.string.login_olvidaste_pin),
                        fontSize = 12.sp,
                        color = NeutralBody,
                        fontWeight = FontWeight.Medium
                    )
                }
            }

            // 7. Footer institucional
            Spacer(modifier = Modifier.height(14.dp))
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.Center,
                modifier = Modifier.padding(bottom = 2.dp)
            ) {
                Text(
                    text = stringResource(R.string.login_desarrollada_por),
                    fontSize = 11.sp,
                    color = Color(0xFFA39E99)
                )
                Text(
                    text = "FarbalApps",
                    fontSize = 11.sp,
                    color = BrandOrange,
                    fontWeight = FontWeight.Bold
                )
            }

            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.Center
            ) {
                Icon(
                    imageVector = Icons.Default.Security,
                    contentDescription = null,
                    tint = Color(0xFFA39E99),
                    modifier = Modifier.size(12.dp)
                )
                Spacer(modifier = Modifier.width(4.dp))
                Text(
                    text = stringResource(R.string.login_sistema_seguro),
                    fontSize = 10.sp,
                    color = Color(0xFFA39E99),
                    fontWeight = FontWeight.Medium
                )
            }
        }
    }
}


@Preview(showBackground = true, device = "spec:width=411dp,height=891dp", name = "Móvil")
@Composable
fun LoginScreenPreviewMobile() {
    MaterialTheme {
        LoginScreenContent(
            uiState = LoginUiState(pinInput = "12"),
            onEvent = {}
        )
    }
}

@Preview(showBackground = true, device = "spec:width=1280dp,height=800dp", name = "Tablet")
@Composable
fun LoginScreenPreviewTablet() {
    MaterialTheme {
        LoginScreenContent(
            uiState = LoginUiState(pinInput = "1234"),
            onEvent = {}
        )
    }
}
