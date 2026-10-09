package com.example.tortilleriaderek.presentation.backup

import com.example.tortilleriaderek.domain.model.DbInspectionResult

enum class OperacionBackup {
    EXPORTAR,
    IMPORTAR
}

enum class TipoPoliticaPassword {
    MANTENER_ACTUAL,
    USAR_RESPALDO,
    ESTABLECER_NUEVA
}

enum class FaseCargaDb(val mensaje: String) {
    INICIANDO("Preparando entorno seguro..."),
    VERIFICANDO_BATERIA("Comprobando nivel de energía..."),
    VOLCANDO_WAL("Sincronizando transacciones SQLite..."),
    ESCRIBIENDO_DB("Escribiendo base de datos en almacenamiento..."),
    VERIFICANDO_INTEGRIDAD("Ejecutando prueba de integridad estructural..."),
    APLICANDO_POLITICA("Configurando credenciales de acceso..."),
    FINALIZANDO("Finalizando operación...")
}

data class BackupUiState(
    val mostrarDialogoBateria: Boolean = false,
    val nivelBateriaActual: Int = 0,
    val mostrarDialogoAutenticacionAdmin: Boolean = false,
    val operacionPendiente: OperacionBackup? = null,
    val adminUsernameInput: String = "",
    val adminPinInput: String = "",
    val errorAutenticacionAdmin: String? = null,
    val adminAutenticadoHash: String? = null,
    val adminAutenticadoSalt: String? = null,
    val mostrarDialogoAdvertenciaReemplazo: Boolean = false,
    val uriPendienteImportacion: String? = null,
    val inspectionResult: DbInspectionResult? = null,
    val tipoPoliticaPassword: TipoPoliticaPassword = TipoPoliticaPassword.MANTENER_ACTUAL,
    val nuevaPasswordAdminInput: String = "",
    val mostrarOverlayCarga: Boolean = false,
    val faseCargaActual: FaseCargaDb = FaseCargaDb.INICIANDO,
    val mostrarDialogoMicroLeccion: Boolean = false,
    val versionEncontradaMicroLeccion: Int = 0,
    val versionEsperadaMicroLeccion: Int = 8,
    val pasoMicroLeccionActual: Int = 1,
    val mostrarDialogoReinicioExitoso: Boolean = false,
    val mensajeExitoReinicio: String = "",
    val ultimoArchivoExportadoUri: String? = null
)

sealed interface BackupUiIntent {
    data class IniciarExportacion(val esAdminLogueado: Boolean = false, val adminUsername: String? = null) : BackupUiIntent
    data class IniciarImportacion(val esAdminLogueado: Boolean = false, val adminUsername: String? = null) : BackupUiIntent
    data class OnAutenticarAdmin(val username: String, val pin: String) : BackupUiIntent
    object OnCerrarDialogoAutenticacion : BackupUiIntent
    object OnCerrarDialogoBateria : BackupUiIntent
    data class OnUriDestinoSeleccionadaExportar(val uriString: String) : BackupUiIntent
    data class OnUriOrigenSeleccionadaImportar(val uriString: String) : BackupUiIntent
    data class OnCambiarTipoPoliticaPassword(val tipo: TipoPoliticaPassword) : BackupUiIntent
    data class OnCambiarNuevaPasswordInput(val pass: String) : BackupUiIntent
    object OnConfirmarImportacionDestructiva : BackupUiIntent
    object OnCancelarImportacionDestructiva : BackupUiIntent
    data class OnCambiarPasoMicroLeccion(val paso: Int) : BackupUiIntent
    object OnCerrarMicroLeccion : BackupUiIntent
    object OnConfirmarReinicioApp : BackupUiIntent
    object OnCompartirUltimoArchivo : BackupUiIntent
}

sealed interface BackupUiEffect {
    data class AbrirSelectorGuardar(val nombreArchivoSugerido: String) : BackupUiEffect
    object AbrirSelectorLeer : BackupUiEffect
    data class CompartirArchivo(val uriString: String) : BackupUiEffect
    object ReiniciarApp : BackupUiEffect
    data class ShowToast(val mensaje: String) : BackupUiEffect
}
