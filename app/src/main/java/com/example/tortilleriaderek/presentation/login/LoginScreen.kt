package com.example.tortilleriaderek.presentation.login

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
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.Person
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

private val BrandOrange = Color(0xFFFF6B00)
private val BrandOrangeSurface = Color(0xFFFFF7F2)
private val BrandOrangeBadgeBorder = Color(0xFFFFD7BA)
private val NeutralBg = Color(0xFFF8F9FA)
private val NeutralBorderSubtle = Color(0xFFE6E3E0)
private val NeutralBody = Color(0xFF736E69)

/**
 * Creado por 🎨 design-ui-expert y 🏗️ mobile-developer.
 * Pantalla modularizada de Login (Artículo III: <250 líneas).
 * Layout compacto Zero-Scroll: barra superior integrada, input de usuario,
 * selector horizontal y teclado numérico adaptado.
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
                .padding(horizontal = 20.dp, vertical = 12.dp)
                .widthIn(max = 440.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            // 1. Barra Superior Integrada: Status Pill a la izquierda | Respaldo y Ajustes a la derecha
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(bottom = 8.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                // Badge Status: TURNO CERRADO
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

                // Botones Reacomodados: Respaldo y Ajustes en fila horizontal
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
                            .size(38.dp)
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
                            .size(38.dp)
                            .clip(CircleShape)
                            .clickable { onEvent(LoginUiEvent.OnSettingsClick) }
                            .testTag("login_settings_button")
                    ) {
                        Box(contentAlignment = Alignment.Center) {
                            Icon(
                                imageVector = Icons.Default.Settings,
                                contentDescription = "Ajustes del Sistema",
                                tint = Color(0xFF524E4A),
                                modifier = Modifier.size(19.dp)
                            )
                        }
                    }
                }
            }

            // 2. Cabecera Compacta (Título y subtítulo sin caja registradora)
            LoginHeaderSection()

            Spacer(modifier = Modifier.height(10.dp))

            // 3. Campo de Escritura de Usuario (Permite cambiar o ver operador actual)
            OutlinedTextField(
                value = uiState.usernameInput,
                onValueChange = { onEvent(LoginUiEvent.OnUsernameChanged(it)) },
                modifier = Modifier
                    .fillMaxWidth()
                    .testTag("login_username_input"),
                shape = RoundedCornerShape(14.dp),
                label = {
                    Text(
                        text = stringResource(R.string.login_label_operador),
                        fontSize = 12.sp
                    )
                },
                placeholder = {
                    Text(
                        text = stringResource(R.string.login_operador_hint),
                        fontSize = 12.sp,
                        color = NeutralBody.copy(alpha = 0.6f)
                    )
                },
                leadingIcon = {
                    Icon(
                        imageVector = Icons.Default.Person,
                        contentDescription = null,
                        tint = BrandOrange,
                        modifier = Modifier.size(20.dp)
                    )
                },
                trailingIcon = {
                    if (uiState.usernameInput.isNotEmpty()) {
                        IconButton(
                            onClick = { onEvent(LoginUiEvent.OnUsernameChanged("")) },
                            modifier = Modifier.size(32.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Default.Close,
                                contentDescription = stringResource(R.string.login_limpiar_operador),
                                tint = NeutralBody.copy(alpha = 0.7f),
                                modifier = Modifier.size(16.dp)
                            )
                        }
                    }
                },
                singleLine = true,
                textStyle = LocalTextStyle.current.copy(fontSize = 13.sp, fontWeight = FontWeight.SemiBold),
                colors = OutlinedTextFieldDefaults.colors(
                    focusedContainerColor = Color.White,
                    unfocusedContainerColor = Color.White,
                    focusedBorderColor = BrandOrange,
                    unfocusedBorderColor = NeutralBorderSubtle,
                    focusedLabelColor = BrandOrange,
                    unfocusedLabelColor = NeutralBody
                )
            )

            Spacer(modifier = Modifier.height(8.dp))

            // 4. Selector de Modo Horizontal Compacto (Abrir Turno vs Solo Consulta)
            LoginModeSelector(
                selectedMode = uiState.selectedMode,
                onModeSelected = { onEvent(LoginUiEvent.OnModeSelected(it)) }
            )

            // Mensaje de error
            if (uiState.errorMessage != null) {
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = uiState.errorMessage,
                    color = MaterialTheme.colorScheme.error,
                    fontSize = 11.sp,
                    fontWeight = FontWeight.SemiBold,
                    modifier = Modifier.padding(vertical = 2.dp)
                )
            }

            // 5. Keypad Numérico Ergonómico de 58dp
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

            // 6. Footer institucional
            Spacer(modifier = Modifier.height(10.dp))
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
