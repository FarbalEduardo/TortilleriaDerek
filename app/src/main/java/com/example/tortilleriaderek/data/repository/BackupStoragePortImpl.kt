package com.example.tortilleriaderek.data.repository

import android.content.Context
import android.database.sqlite.SQLiteDatabase
import android.net.Uri
import com.example.tortilleriaderek.data.local.TortilleriaDatabase
import com.example.tortilleriaderek.data.security.CryptoManager
import com.example.tortilleriaderek.domain.model.BackupError
import com.example.tortilleriaderek.domain.model.BackupExportInfo
import com.example.tortilleriaderek.domain.model.DbInspectionResult
import com.example.tortilleriaderek.domain.model.PoliticaPasswordAdmin
import com.example.tortilleriaderek.domain.repository.BackupStoragePort
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.io.File
import java.io.FileOutputStream
import java.security.MessageDigest
import javax.inject.Inject
import javax.inject.Singleton

/**
 * Creado por 🏗️ mobile-developer y 🛡️ security-expert.
 * Implementación de infraestructura para la gestión de archivos SQLite y Storage Access Framework.
 * Implementa checkpoint WAL, validación de integridad PRAGMA, rollback atómico con .prev
 * y aplicación de políticas de contraseñas de administrador.
 */
@Singleton
class BackupStoragePortImpl @Inject constructor(
    @ApplicationContext private val context: Context,
    private val database: TortilleriaDatabase
) : BackupStoragePort {

    override suspend fun exportarDbHaciaDestino(uriString: String): Result<BackupExportInfo> = withContext(Dispatchers.IO) {
        try {
            // 1. Forzar volcado completo de páginas WAL a la base de datos principal
            try {
                database.openHelper.writableDatabase.query("PRAGMA wal_checkpoint(FULL)").use { cursor ->
                    cursor.moveToFirst()
                }
            } catch (_: Exception) {}

            val dbFile = context.getDatabasePath("tortilleria_db")
            if (!dbFile.exists()) {
                return@withContext Result.failure(BackupError.ArchivoInvalido)
            }

            // 2. Calcular hash criptográfico SHA-256
            val digest = MessageDigest.getInstance("SHA-256")
            val buffer = ByteArray(8192)
            dbFile.inputStream().use { input ->
                var read: Int
                while (input.read(buffer).also { read = it } != -1) {
                    digest.update(buffer, 0, read)
                }
            }
            val sha256 = digest.digest().joinToString("") { "%02x".format(it) }

            // 3. Escribir stream hacia la URI del Storage Access Framework
            val uri = Uri.parse(uriString)
            context.contentResolver.openOutputStream(uri)?.use { output ->
                dbFile.inputStream().use { input ->
                    input.copyTo(output)
                }
            } ?: return@withContext Result.failure(BackupError.ErrorEscritura)

            // 4. Métricas de resumen
            var totalTurnosCerrados = 0
            var totalVentas = 0
            try {
                database.openHelper.readableDatabase.query("SELECT COUNT(*) FROM turnos WHERE estado = 'CERRADO'").use { c ->
                    if (c.moveToFirst()) totalTurnosCerrados = c.getInt(0)
                }
                database.openHelper.readableDatabase.query("SELECT COUNT(*) FROM ventas").use { c ->
                    if (c.moveToFirst()) totalVentas = c.getInt(0)
                }
            } catch (_: Exception) {}

            Result.success(
                BackupExportInfo(
                    uriString = uriString,
                    sha256 = sha256,
                    tamanioBytes = dbFile.length(),
                    nombreArchivo = dbFile.name,
                    totalTurnosCerrados = totalTurnosCerrados,
                    totalVentas = totalVentas
                )
            )
        } catch (e: Exception) {
            Result.failure(BackupError.ErrorDesconocido(e))
        }
    }

    override suspend fun inspeccionarDbDesdeOrigen(uriString: String): Result<DbInspectionResult> = withContext(Dispatchers.IO) {
        val stagingFile = File(context.cacheDir, "staged_backup_import.db")
        try {
            val uri = Uri.parse(uriString)
            context.contentResolver.openInputStream(uri)?.use { input ->
                FileOutputStream(stagingFile).use { output ->
                    input.copyTo(output)
                }
            } ?: return@withContext Result.failure(BackupError.ArchivoInvalido)

            val sqlite = try {
                SQLiteDatabase.openDatabase(stagingFile.path, null, SQLiteDatabase.OPEN_READWRITE)
            } catch (_: Exception) {
                try {
                    SQLiteDatabase.openDatabase(stagingFile.path, null, SQLiteDatabase.OPEN_READONLY)
                } catch (e: Exception) {
                    stagingFile.delete()
                    return@withContext Result.failure(BackupError.ArchivoInvalido)
                }
            }

            var userVersion = 0
            var tieneTurnoAbierto = false
            var totalVentas = 0
            var totalTurnosCerrados = 0
            var totalUsuarios = 0

            sqlite.rawQuery("PRAGMA user_version", null).use { cursor ->
                if (cursor.moveToFirst()) {
                    userVersion = cursor.getInt(0)
                }
            }

            try {
                sqlite.rawQuery("SELECT COUNT(*) FROM turnos WHERE estado = 'ABIERTO'", null).use { cursor ->
                    if (cursor.moveToFirst()) tieneTurnoAbierto = cursor.getInt(0) > 0
                }
                sqlite.rawQuery("SELECT COUNT(*) FROM turnos WHERE estado = 'CERRADO'", null).use { cursor ->
                    if (cursor.moveToFirst()) totalTurnosCerrados = cursor.getInt(0)
                }
            } catch (_: Exception) {}

            try {
                sqlite.rawQuery("SELECT COUNT(*) FROM ventas", null).use { cursor ->
                    if (cursor.moveToFirst()) totalVentas = cursor.getInt(0)
                }
            } catch (_: Exception) {}

            try {
                sqlite.rawQuery("SELECT COUNT(*) FROM usuarios", null).use { cursor ->
                    if (cursor.moveToFirst()) totalUsuarios = cursor.getInt(0)
                }
            } catch (_: Exception) {}

            sqlite.close()

            Result.success(
                DbInspectionResult(
                    userVersion = userVersion,
                    versionEsperada = 8,
                    esCompatible = (userVersion in setOf(7, 8)),
                    tieneTurnoAbierto = tieneTurnoAbierto,
                    totalVentas = totalVentas,
                    totalTurnosCerrados = totalTurnosCerrados,
                    totalUsuarios = totalUsuarios
                )
            )
        } catch (e: Exception) {
            stagingFile.delete()
            Result.failure(BackupError.ErrorDesconocido(e))
        }
    }

    override suspend fun restaurarDbDesdeOrigen(
        uriString: String,
        politicaPassword: PoliticaPasswordAdmin
    ): Result<Unit> = withContext(Dispatchers.IO) {
        val dbFile = context.getDatabasePath("tortilleria_db")
        dbFile.parentFile?.let { if (!it.exists()) it.mkdirs() }
        val prevFile = File(dbFile.parentFile, "tortilleria_db.prev")
        val stagingFile = File(context.cacheDir, "staged_backup_import.db")

        // 1. Snapshot de rollback previo
        if (dbFile.exists()) {
            dbFile.copyTo(prevFile, overwrite = true)
        }

        // 2. Cerrar Room activa
        try {
            database.close()
        } catch (_: Exception) {}

        try {
            // 3. Escribir archivo de respaldo sobre la ruta oficial (desde staging o URI directa)
            if (stagingFile.exists() && stagingFile.length() > 0) {
                stagingFile.copyTo(dbFile, overwrite = true)
                stagingFile.delete()
            } else {
                val uri = Uri.parse(uriString)
                context.contentResolver.openInputStream(uri)?.use { input ->
                    FileOutputStream(dbFile).use { output ->
                        input.copyTo(output)
                    }
                } ?: throw BackupError.ErrorEscritura
            }

            // 4. Limpiar archivos efímeros WAL y SHM
            val walFile = File(dbFile.parentFile, "tortilleria_db-wal")
            if (walFile.exists()) walFile.delete()
            val shmFile = File(dbFile.parentFile, "tortilleria_db-shm")
            if (shmFile.exists()) shmFile.delete()

            // 5. Verificar integridad física
            val sqlite = try {
                SQLiteDatabase.openDatabase(dbFile.path, null, SQLiteDatabase.OPEN_READWRITE)
            } catch (e: Exception) {
                throw BackupError.ArchivoInvalido
            }

            var integrityOk = false
            sqlite.rawQuery("PRAGMA integrity_check", null).use { cursor ->
                if (cursor.moveToFirst()) {
                    val status = cursor.getString(0)
                    integrityOk = status.equals("ok", ignoreCase = true)
                }
            }
            if (!integrityOk) {
                sqlite.close()
                throw BackupError.ArchivoInvalido
            }

            // 6. Cierre de emergencia si el archivo importado contenía turno abierto (CL-12)
            sqlite.execSQL(
                "UPDATE turnos SET estado = 'CERRADO', fechaCierre = ? WHERE estado = 'ABIERTO'",
                arrayOf(System.currentTimeMillis())
            )

            // 7. Aplicar Política de Contraseña de Administrador (CL-05, CL-06)
            when (politicaPassword) {
                is PoliticaPasswordAdmin.MantenerActual -> {
                    sqlite.execSQL(
                        "UPDATE usuarios SET passwordHash = ?, salt = ? WHERE rol = 'ADMIN'",
                        arrayOf(politicaPassword.hashActual, politicaPassword.saltActual)
                    )
                }
                is PoliticaPasswordAdmin.EstablecerNueva -> {
                    val newSalt = CryptoManager.generateSalt()
                    val newHash = CryptoManager.hashPassword(politicaPassword.nuevaPasswordRaw, newSalt)
                    sqlite.execSQL(
                        "UPDATE usuarios SET passwordHash = ?, salt = ? WHERE rol = 'ADMIN'",
                        arrayOf(newHash, newSalt)
                    )
                }
                is PoliticaPasswordAdmin.UsarDelRespaldo -> {
                    // Se preserva la contraseña del origen
                }
            }

            // 8. Supervivencia y Admin de Rescate (CL-07)
            var adminCount = 0
            sqlite.rawQuery("SELECT COUNT(*) FROM usuarios WHERE rol = 'ADMIN'", null).use { cursor ->
                if (cursor.moveToFirst()) {
                    adminCount = cursor.getInt(0)
                }
            }
            if (adminCount == 0) {
                val rescueSalt = CryptoManager.generateSalt()
                val rescueHash = CryptoManager.hashPassword("admin123", rescueSalt)
                sqlite.execSQL(
                    "INSERT INTO usuarios (id, username, passwordHash, salt, rol) VALUES (?, ?, ?, ?, ?)",
                    arrayOf("admin_rescue", "admin1", rescueHash, rescueSalt, "ADMIN")
                )
            }

            sqlite.close()

            // Éxito: eliminar archivo de rollback
            if (prevFile.exists()) prevFile.delete()

            Result.success(Unit)
        } catch (e: Exception) {
            // Rollback automático ante fallo
            if (prevFile.exists()) {
                try {
                    prevFile.copyTo(dbFile, overwrite = true)
                    prevFile.delete()
                } catch (_: Exception) {}
            }
            if (e is BackupError) Result.failure(e) else Result.failure(BackupError.ErrorDesconocido(e))
        }
    }
}
