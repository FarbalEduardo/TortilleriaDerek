package com.example.tortilleriaderek.domain.model

/**
 * Creado por 🏗️ mobile-developer y 🛡️ security-expert.
 * Modelos de dominio y contratos para el módulo de Respaldo y Transferencia SQLite.
 */

/**
 * Proveedor desacoplado del estado de batería del dispositivo para permitir pruebas unitarias.
 */
interface BatteryStatusProvider {
    fun obtenerNivelBateria(): Int
    fun estaCargando(): Boolean
}

/**
 * Política de asignación de contraseña del Administrador Principal tras la importación.
 * Mitiga el riesgo de lockout cuando el teléfono de origen y destino tienen credenciales distintas.
 */
sealed class PoliticaPasswordAdmin {
    data class MantenerActual(val hashActual: String, val saltActual: String) : PoliticaPasswordAdmin()
    object UsarDelRespaldo : PoliticaPasswordAdmin()
    data class EstablecerNueva(val nuevaPasswordRaw: String) : PoliticaPasswordAdmin()
}

/**
 * Resultado de la inspección de cabecera y metadatos de un archivo .db entrante sin abrir Room completo.
 */
data class DbInspectionResult(
    val userVersion: Int,
    val versionEsperada: Int = 8,
    val esCompatible: Boolean = userVersion in setOf(7, 8),
    val tieneTurnoAbierto: Boolean = false,
    val totalVentas: Int = 0,
    val totalTurnosCerrados: Int = 0,
    val totalUsuarios: Int = 0
)

/**
 * Información de trazabilidad del archivo de respaldo generado.
 */
data class BackupExportInfo(
    val uriString: String,
    val sha256: String,
    val tamanioBytes: Long,
    val nombreArchivo: String,
    val totalTurnosCerrados: Int,
    val totalVentas: Int
)

/**
 * Jerarquía de errores tipados de respaldo y restauración.
 */
sealed class BackupError(message: String) : Exception(message) {
    data class BateriaBaja(val nivelActual: Int) : BackupError(
        "Batería insuficiente ($nivelActual%). Se requiere al menos 25% o conectar el cargador para evitar corrupción de datos."
    )
    object TurnoActivoLocal : BackupError(
        "Existe un turno abierto en este teléfono. Debe cerrarse el corte de caja antes de transferir la base de datos."
    )
    data class VersionIncompatible(val versionEncontrada: Int, val versionEsperada: Int) : BackupError(
        "La base de datos (versión $versionEncontrada) no es compatible con esta aplicación (versión $versionEsperada)."
    )
    object CredencialesInvalidas : BackupError("Usuario o contraseña de Administrador incorrectos.")
    object NoEsAdmin : BackupError("Acceso denegado: solo el Administrador Principal puede exportar o importar datos.")
    object ArchivoInvalido : BackupError("El archivo seleccionado no es una base de datos SQLite válida o está dañado.")
    object ErrorEscritura : BackupError("Error al escribir el archivo de respaldo en el almacenamiento.")
    data class ErrorDesconocido(val error: Throwable) : BackupError(error.message ?: "Ocurrió un error inesperado.")
}
