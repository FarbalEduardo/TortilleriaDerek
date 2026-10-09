package com.example.tortilleriaderek.presentation.login

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
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
import androidx.compose.ui.zIndex
import com.example.tortilleriaderek.R
import com.example.tortilleriaderek.ui.components.backup.DialogOpcionesBackup
import com.example.tortilleriaderek.ui.components.login.DialogRecuperacionMasterKey
import com.example.tortilleriaderek.ui.components.login.KeypadNumericoLogin
import com.example.tortilleriaderek.ui.components.login.LoginHeaderSection
import com.example.tortilleriaderek.ui.components.login.LoginModeSelector

private val BrandOrange = Color(0xFFFF6B00)
private val NeutralBg = Color(0xFFF8F9FA)
private val NeutralBorderSubtle = Color(0xFFE6E3E0)

/**
 * Creado por 🎨 design-ui-expert y 🏗️ mobile-developer.
 * Pantalla modularizada de Login (Artículo III: <300 líneas de código).
 * Integra Keypad numérico ergonómico (Artículo VI), biometría y recuperación con Clave Maestra.
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
        // Botones superiores: Ajustes y Respaldo
        Column(
            modifier = Modifier
                .align(Alignment.TopEnd)
                .statusBarsPadding()
                .padding(end = 20.dp, top = 12.dp)
                .zIndex(20f),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            Surface(
                shape = CircleShape,
                color = Color.White,
                border = BorderStroke(1.dp, NeutralBorderSubtle),
                shadowElevation = 2.dp,
                modifier = Modifier
                    .size(44.dp)
                    .clip(CircleShape)
                    .clickable { onEvent(LoginUiEvent.OnSettingsClick) }
                    .testTag("login_settings_button")
            ) {
                Box(contentAlignment = Alignment.Center) {
                    Icon(
                        imageVector = Icons.Default.Settings,
                        contentDescription = "Ajustes del Sistema",
                        tint = Color(0xFF524E4A),
                        modifier = Modifier.size(22.dp)
                    )
                }
            }

            Surface(
                shape = CircleShape,
                color = Color.White,
                border = BorderStroke(1.dp, NeutralBorderSubtle),
                shadowElevation = 2.dp,
                modifier = Modifier
                    .size(44.dp)
                    .clip(CircleShape)
                    .clickable { showBackupOptionsDialog = true }
                    .testTag("login_backup_button")
            ) {
                Box(contentAlignment = Alignment.Center) {
                    Icon(
                        imageVector = Icons.Default.SwapHoriz,
                        contentDescription = "Copia de Seguridad y Transferencia",
                        tint = BrandOrange,
                        modifier = Modifier.size(22.dp)
                    )
                }
            }
        }

        // Contenedor scrolleable centrado
        Column(
            modifier = Modifier
                .fillMaxSize()
                .verticalScroll(rememberScrollState())
                .statusBarsPadding()
                .padding(horizontal = 24.dp, vertical = 20.dp)
                .widthIn(max = 480.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Spacer(modifier = Modifier.height(10.dp))

            // 1. Cabecera Visual (Logo + Status Pill + Título)
            LoginHeaderSection()

            Spacer(modifier = Modifier.height(20.dp))

            // 2. Selector de Modo de Acceso (Abrir Turno vs Solo Consulta)
            LoginModeSelector(
                selectedMode = uiState.selectedMode,
                onModeSelected = { onEvent(LoginUiEvent.OnModeSelected(it)) }
            )

            Spacer(modifier = Modifier.height(16.dp))

            // Aviso de error si existe
            if (uiState.errorMessage != null) {
                Text(
                    text = uiState.errorMessage,
                    color = MaterialTheme.colorScheme.error,
                    fontSize = 12.sp,
                    fontWeight = FontWeight.SemiBold,
                    modifier = Modifier.padding(bottom = 8.dp)
                )
            }

            // 3. Keypad Numérico Ergonómico de 64dp
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

            // 4. Footer institucional
            Spacer(modifier = Modifier.height(24.dp))
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.Center,
                modifier = Modifier.padding(bottom = 4.dp)
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
                    modifier = Modifier.size(13.dp)
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
