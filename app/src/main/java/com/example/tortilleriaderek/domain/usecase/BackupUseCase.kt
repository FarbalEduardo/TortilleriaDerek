package com.example.tortilleriaderek.domain.usecase

import com.example.tortilleriaderek.data.local.dao.TurnoDao
import com.example.tortilleriaderek.data.local.dao.UsuarioDao
import com.example.tortilleriaderek.data.local.entity.UsuarioEntity
import com.example.tortilleriaderek.data.security.CryptoManager
import com.example.tortilleriaderek.domain.model.BackupError
import com.example.tortilleriaderek.domain.model.BackupExportInfo
import com.example.tortilleriaderek.domain.model.BatteryStatusProvider
import com.example.tortilleriaderek.domain.model.DbInspectionResult
import com.example.tortilleriaderek.domain.model.PoliticaPasswordAdmin
import com.example.tortilleriaderek.domain.repository.BackupStoragePort
import javax.inject.Inject

/**
 * Creado por 🏗️ mobile-developer y 🛡️ security-expert.
 * Caso de uso central para las operaciones de Respaldo y Transferencia SQLite.
 * Orquesta precondiciones de seguridad (batería, autenticación admin, turnos cerrados)
 * y delega las operaciones físicas al puerto de almacenamiento.
 */
class BackupUseCase @Inject constructor(
    private val turnoDao: TurnoDao,
    private val usuarioDao: UsuarioDao,
    private val batteryStatusProvider: BatteryStatusProvider,
    private val backupStoragePort: BackupStoragePort
) {
    companion object {
        const val NIVEL_BATERIA_MINIMO = 25
        const val VERSION_DB_ESPERADA = 8
        val VERSIONES_COMPATIBLES = setOf(7, 8)
    }

    /**
     * Valida que el nivel de batería sea >= 25% o que el dispositivo esté conectado a la corriente.
     */
    fun verificarBateriaSegura(): Result<Unit> {
        val nivel = batteryStatusProvider.obtenerNivelBateria()
        val cargando = batteryStatusProvider.estaCargando()
        return if (nivel >= NIVEL_BATERIA_MINIMO || cargando) {
            Result.success(Unit)
        } else {
            Result.failure(BackupError.BateriaBaja(nivel))
        }
    }

    /**
     * Autentica que el usuario ingresado exista, tenga rol 'ADMIN' y su PIN coincida criptográficamente.
     */
    suspend fun autenticarAdmin(username: String, pinRaw: String): Result<UsuarioEntity> {
        if (username.isBlank() || pinRaw.isBlank()) {
            return Result.failure(BackupError.CredencialesInvalidas)
        }
        val usuario = usuarioDao.getUsuarioByUsername(username.trim())
            ?: return Result.failure(BackupError.CredencialesInvalidas)

        if (usuario.rol != "ADMIN") {
            return Result.failure(BackupError.NoEsAdmin)
        }

        val calculatedHash = CryptoManager.hashPassword(pinRaw, usuario.salt)
        return if (calculatedHash == usuario.passwordHash) {
            Result.success(usuario)
        } else {
            Result.failure(BackupError.CredencialesInvalidas)
        }
    }

    /**
     * Bloquea la operación si existe un turno ABIERTO en este dispositivo local.
     */
    suspend fun verificarTurnoActivoLocal(): Result<Unit> {
        val turnoActivo = turnoDao.getTurnoActivoSync()
        return if (turnoActivo != null && turnoActivo.estado == "ABIERTO") {
            Result.failure(BackupError.TurnoActivoLocal)
        } else {
            Result.success(Unit)
        }
    }

    /**
     * Valida todas las precondiciones para iniciar el flujo de exportación:
     * 1. Batería >= 25% o cargador conectado.
     * 2. Credenciales del Administrador Principal válidas.
     * 3. Sin turnos abiertos locales.
     */
    suspend fun validarPrecondicionesExportacion(usernameAdmin: String, pinAdmin: String): Result<UsuarioEntity> {
        verificarBateriaSegura().getOrElse { return Result.failure(it) }
        val admin = autenticarAdmin(usernameAdmin, pinAdmin).getOrElse { return Result.failure(it) }
        verificarTurnoActivoLocal().getOrElse { return Result.failure(it) }
        return Result.success(admin)
    }

    /**
     * Ejecuta la exportación física hacia la URI SAF provista.
     */
    suspend fun exportarBaseDeDatos(uriString: String): Result<BackupExportInfo> {
        verificarBateriaSegura().getOrElse { return Result.failure(it) }
        verificarTurnoActivoLocal().getOrElse { return Result.failure(it) }
        return backupStoragePort.exportarDbHaciaDestino(uriString)
    }

    /**
     * Valida precondiciones de batería y turnos, e inspecciona el archivo entrante.
     */
    suspend fun inspeccionarArchivoImportacion(uriString: String): Result<DbInspectionResult> {
        verificarBateriaSegura().getOrElse { return Result.failure(it) }
        verificarTurnoActivoLocal().getOrElse { return Result.failure(it) }

        val inspection = backupStoragePort.inspeccionarDbDesdeOrigen(uriString).getOrElse {
            return Result.failure(it)
        }

        if (inspection.userVersion !in VERSIONES_COMPATIBLES) {
            return Result.failure(
                BackupError.VersionIncompatible(
                    versionEncontrada = inspection.userVersion,
                    versionEsperada = VERSION_DB_ESPERADA
                )
            )
        }

        return Result.success(inspection)
    }

    /**
     * Valida precondiciones de importación con credenciales admin e inspecciona el archivo entrante.
     */
    suspend fun inspeccionarArchivoImportacion(
        uriString: String,
        usernameAdmin: String,
        pinAdmin: String
    ): Result<DbInspectionResult> {
        verificarBateriaSegura().getOrElse { return Result.failure(it) }
        autenticarAdmin(usernameAdmin, pinAdmin).getOrElse { return Result.failure(it) }
        return inspeccionarArchivoImportacion(uriString)
    }

    /**
     * Ejecuta el proceso de restauración / reemplazo atómico con la política de contraseña seleccionada.
     */
    suspend fun restaurarBaseDeDatos(
        uriString: String,
        politicaPassword: PoliticaPasswordAdmin
    ): Result<Unit> {
        verificarBateriaSegura().getOrElse { return Result.failure(it) }
        verificarTurnoActivoLocal().getOrElse { return Result.failure(it) }
        return backupStoragePort.restaurarDbDesdeOrigen(uriString, politicaPassword)
    }
}
