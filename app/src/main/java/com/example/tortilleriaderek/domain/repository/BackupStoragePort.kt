package com.example.tortilleriaderek.domain.repository

import com.example.tortilleriaderek.domain.model.BackupExportInfo
import com.example.tortilleriaderek.domain.model.DbInspectionResult
import com.example.tortilleriaderek.domain.model.PoliticaPasswordAdmin

/**
 * Creado por 🏗️ mobile-developer y 🛡️ security-expert.
 * Puerto de abstracción para operaciones de archivos SQLite, Storage Access Framework (SAF)
 * e integridad física de bases de datos.
 */
interface BackupStoragePort {
    /**
     * Fuerza el checkpoint WAL (PRAGMA wal_checkpoint(FULL)) y exporta el archivo SQLite
     * hacia la URI SAF de destino provista por el usuario.
     */
    suspend fun exportarDbHaciaDestino(uriString: String): Result<BackupExportInfo>

    /**
     * Inspecciona una base de datos externa leyendo su PRAGMA user_version y metadatos
     * sin alterar la base de datos activa.
     */
    suspend fun inspeccionarDbDesdeOrigen(uriString: String): Result<DbInspectionResult>

    /**
     * Ejecuta el reemplazo de base de datos creando un snapshot previo de rollback (.prev),
     * cerrando conexiones, reemplazando el archivo .db, limpiando WAL/SHM, verificando integridad
     * y aplicando la política de contraseña seleccionada.
     */
    suspend fun restaurarDbDesdeOrigen(
        uriString: String,
        politicaPassword: PoliticaPasswordAdmin
    ): Result<Unit>
}
