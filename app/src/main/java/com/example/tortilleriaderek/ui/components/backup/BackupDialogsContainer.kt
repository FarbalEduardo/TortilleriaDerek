package com.example.tortilleriaderek.ui.components.backup

import android.app.Activity
import android.content.Context
import android.content.Intent
import android.net.Uri
import android.widget.Toast
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.runtime.*
import androidx.compose.ui.platform.LocalContext
import com.example.tortilleriaderek.presentation.backup.*

/**
 * Creado por 🏗️ mobile-developer y 🎨 design-ui-expert.
 * Contenedor orquestador de los 6 diálogos/overlays de Respaldo SQLite y sus contratos SAF.
 * Se integra de forma limpia y reactiva en LoginScreen y ConfiguracionScreen.
 */
@Composable
fun BackupDialogsContainer(
    viewModel: BackupViewModel
) {
    val context = LocalContext.current
    val uiState by viewModel.uiState.collectAsState()

    // Launcher para Storage Access Framework: Guardar (.db)
    val createDocumentLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.CreateDocument("application/x-sqlite3")
    ) { uri: Uri? ->
        uri?.let {
            viewModel.onIntent(BackupUiIntent.OnUriDestinoSeleccionadaExportar(it.toString()))
        }
    }

    // Launcher para Storage Access Framework: Abrir (.db)
    val openDocumentLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.OpenDocument()
    ) { uri: Uri? ->
        uri?.let {
            viewModel.onIntent(BackupUiIntent.OnUriOrigenSeleccionadaImportar(it.toString()))
        }
    }

    // Recolector de efectos MVI
    LaunchedEffect(Unit) {
        viewModel.uiEffect.collect { effect ->
            when (effect) {
                is BackupUiEffect.AbrirSelectorGuardar -> {
                    createDocumentLauncher.launch(effect.nombreArchivoSugerido)
                }
                is BackupUiEffect.AbrirSelectorLeer -> {
                    openDocumentLauncher.launch(arrayOf("application/x-sqlite3", "application/octet-stream", "*/*"))
                }
                is BackupUiEffect.CompartirArchivo -> {
                    val shareIntent = Intent(Intent.ACTION_SEND).apply {
                        type = "application/x-sqlite3"
                        putExtra(Intent.EXTRA_STREAM, Uri.parse(effect.uriString))
                        addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
                    }
                    context.startActivity(Intent.createChooser(shareIntent, "Compartir base de datos Tortillería Derek"))
                }
                is BackupUiEffect.ReiniciarApp -> {
                    val packageManager = context.packageManager
                    val launchIntent = packageManager.getLaunchIntentForPackage(context.packageName)
                    if (launchIntent != null) {
                        launchIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK)
                        context.startActivity(launchIntent)
                        (context as? Activity)?.finishAffinity()
                        android.os.Process.killProcess(android.os.Process.myPid())
                        kotlin.system.exitProcess(0)
                    }
                }
                is BackupUiEffect.ShowToast -> {
                    Toast.makeText(context, effect.mensaje, Toast.LENGTH_LONG).show()
                }
            }
        }
    }

    // 1. Diálogo de Batería Insuficiente (< 25%)
    if (uiState.mostrarDialogoBateria) {
        DialogBateriaInsuficiente(
            nivelBateria = uiState.nivelBateriaActual,
            onDismiss = { viewModel.onIntent(BackupUiIntent.OnCerrarDialogoBateria) }
        )
    }

    // 2. Diálogo de Autenticación de Administrador Principal
    if (uiState.mostrarDialogoAutenticacionAdmin) {
        DialogAutenticacionAdminBackup(
            operacion = uiState.operacionPendiente,
            errorMessage = uiState.errorAutenticacionAdmin,
            onConfirm = { user, pin ->
                viewModel.onIntent(BackupUiIntent.OnAutenticarAdmin(user, pin))
            },
            onDismiss = { viewModel.onIntent(BackupUiIntent.OnCerrarDialogoAutenticacion) }
        )
    }

    // 3. Diálogo de Advertencia Destructiva y Selector de Contraseña (Opción 1)
    if (uiState.mostrarDialogoAdvertenciaReemplazo) {
        DialogAdvertenciaReemplazoDb(
            inspectionResult = uiState.inspectionResult,
            tipoPolitica = uiState.tipoPoliticaPassword,
            nuevaPasswordInput = uiState.nuevaPasswordAdminInput,
            onTipoPoliticaChange = { viewModel.onIntent(BackupUiIntent.OnCambiarTipoPoliticaPassword(it)) },
            onNuevaPasswordChange = { viewModel.onIntent(BackupUiIntent.OnCambiarNuevaPasswordInput(it)) },
            onConfirmar = { viewModel.onIntent(BackupUiIntent.OnConfirmarImportacionDestructiva) },
            onCancelar = { viewModel.onIntent(BackupUiIntent.OnCancelarImportacionDestructiva) }
        )
    }

    // 4. Overlay de Carga Animada en Canvas
    if (uiState.mostrarOverlayCarga) {
        OverlayCargandoDbAnimado(faseActual = uiState.faseCargaActual)
    }

    // 5. Micro-Lección Animada de Versiones Incompatibles
    if (uiState.mostrarDialogoMicroLeccion) {
        DialogMicroLeccionVersion(
            versionEncontrada = uiState.versionEncontradaMicroLeccion,
            versionEsperada = uiState.versionEsperadaMicroLeccion,
            pasoActual = uiState.pasoMicroLeccionActual,
            onCambiarPaso = { viewModel.onIntent(BackupUiIntent.OnCambiarPasoMicroLeccion(it)) },
            onDismiss = { viewModel.onIntent(BackupUiIntent.OnCerrarMicroLeccion) }
        )
    }

    // 6. Diálogo de Reinicio Guiado Exitoso
    if (uiState.mostrarDialogoReinicioExitoso) {
        DialogReinicioExitosoDb(
            mensajeDetalle = uiState.mensajeExitoReinicio,
            onReiniciarApp = { viewModel.onIntent(BackupUiIntent.OnConfirmarReinicioApp) }
        )
    }
}
