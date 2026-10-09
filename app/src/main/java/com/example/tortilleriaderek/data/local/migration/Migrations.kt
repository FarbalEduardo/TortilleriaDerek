package com.example.tortilleriaderek.data.local.migration

import androidx.room.migration.Migration
import androidx.sqlite.db.SupportSQLiteDatabase

/**
 * Creado por 🏗️ mobile-developer y 🛡️ security-expert.
 * Migraciones seguras para Tortillería Derek (Room v7).
 * Previene la pérdida de datos y evita fallos por salto de versiones (v1..v6 -> v7).
 */
object Migrations {

    private fun migrarAVersion7(db: SupportSQLiteDatabase) {
        // 1. Garantizar existencia de todas las tablas requeridas por el esquema v7
        db.execSQL(
            """
            CREATE TABLE IF NOT EXISTS `turnos` (
                `id` TEXT NOT NULL,
                `fechaApertura` INTEGER NOT NULL,
                `estado` TEXT NOT NULL,
                `usuarioId` TEXT NOT NULL,
                `fechaCierre` INTEGER,
                `totalVentas` REAL NOT NULL DEFAULT 0.0,
                PRIMARY KEY(`id`)
            )
            """.trimIndent()
        )

        db.execSQL(
            """
            CREATE TABLE IF NOT EXISTS `ventas` (
                `id` TEXT NOT NULL,
                `turnoId` TEXT NOT NULL,
                `folioTicket` TEXT NOT NULL,
                `tipo` TEXT NOT NULL,
                `fecha` INTEGER NOT NULL,
                `hora` TEXT NOT NULL,
                `detalleProductos` TEXT NOT NULL,
                `total` REAL NOT NULL,
                `metodoPago` TEXT NOT NULL,
                `usuarioId` TEXT NOT NULL,
                `usuarioNombre` TEXT NOT NULL,
                `estado` TEXT NOT NULL DEFAULT 'ACTIVA',
                `nombreRepartidor` TEXT,
                `motoAsignada` TEXT,
                `nombreRuta` TEXT,
                PRIMARY KEY(`id`)
            )
            """.trimIndent()
        )

        db.execSQL(
            """
            CREATE TABLE IF NOT EXISTS `configuracion_produccion` (
                `id` TEXT NOT NULL,
                `pesoBultoHarinaKg` REAL NOT NULL DEFAULT 0.0,
                `kgMasaPorBulto` REAL NOT NULL DEFAULT 0.0,
                `rendimientoTortillaPorBulto` REAL NOT NULL DEFAULT 0.0,
                `mermaToleradaKgPorBulto` REAL NOT NULL DEFAULT 0.0,
                `porcentajeMermaTolerada` REAL NOT NULL DEFAULT 0.0,
                `precioKilo` REAL NOT NULL DEFAULT 0.0,
                `precioPaquete` REAL NOT NULL DEFAULT 0.0,
                `precioMedioPaquete` REAL NOT NULL DEFAULT 0.0,
                `precioPaqueteRepartidor` REAL NOT NULL DEFAULT 0.0,
                `pesoPaqueteGramos` INTEGER NOT NULL DEFAULT 0,
                `pesoMedioPaqueteGramos` INTEGER NOT NULL DEFAULT 0,
                `fechaModificacion` INTEGER NOT NULL DEFAULT 0,
                PRIMARY KEY(`id`)
            )
            """.trimIndent()
        )

        db.execSQL(
            """
            CREATE TABLE IF NOT EXISTS `usuarios` (
                `id` TEXT NOT NULL,
                `username` TEXT NOT NULL,
                `passwordHash` TEXT NOT NULL,
                `salt` TEXT NOT NULL,
                `rol` TEXT NOT NULL,
                PRIMARY KEY(`id`)
            )
            """.trimIndent()
        )

        db.execSQL(
            """
            CREATE TABLE IF NOT EXISTS `tandas_produccion` (
                `id` TEXT NOT NULL,
                `turnoId` TEXT NOT NULL,
                `fecha` INTEGER NOT NULL,
                `hora` TEXT NOT NULL,
                `bultosHarina` INTEGER NOT NULL,
                `pesoBultoKg` REAL NOT NULL,
                `kgMasaCruda` REAL NOT NULL,
                `kgTortillaEstimada` REAL NOT NULL,
                `usuarioId` TEXT NOT NULL,
                `usuarioNombre` TEXT NOT NULL,
                PRIMARY KEY(`id`)
            )
            """.trimIndent()
        )

        db.execSQL(
            """
            CREATE TABLE IF NOT EXISTS `mermas_produccion` (
                `id` TEXT NOT NULL,
                `turnoId` TEXT NOT NULL,
                `fecha` INTEGER NOT NULL,
                `hora` TEXT NOT NULL,
                `kgMerma` REAL NOT NULL,
                `motivo` TEXT NOT NULL,
                `usuarioId` TEXT NOT NULL,
                `usuarioNombre` TEXT NOT NULL,
                PRIMARY KEY(`id`)
            )
            """.trimIndent()
        )

        db.execSQL(
            """
            CREATE TABLE IF NOT EXISTS `repartidores` (
                `id` TEXT NOT NULL,
                `numeroBadge` TEXT NOT NULL,
                `nombre` TEXT NOT NULL,
                `moto` TEXT NOT NULL DEFAULT '',
                `detalleRuta` TEXT NOT NULL DEFAULT '',
                `fechaCreacion` INTEGER NOT NULL DEFAULT 0,
                PRIMARY KEY(`id`)
            )
            """.trimIndent()
        )

        db.execSQL(
            """
            CREATE TABLE IF NOT EXISTS `rutas_repartidores` (
                `id` TEXT NOT NULL,
                `turnoId` TEXT NOT NULL,
                `moto` TEXT NOT NULL,
                `repartidorNombre` TEXT NOT NULL,
                `nombreRuta` TEXT NOT NULL DEFAULT 'Ruta General',
                `status` TEXT NOT NULL,
                `cargaInicialKg` REAL NOT NULL DEFAULT 0.0,
                `pendienteCobro` REAL NOT NULL DEFAULT 0.0,
                `devolucionKg` REAL NOT NULL DEFAULT 0.0,
                `entregadoKg` REAL NOT NULL DEFAULT 0.0,
                `cobrado` REAL NOT NULL DEFAULT 0.0,
                `folioTicket` TEXT,
                `horaSalida` TEXT,
                `horaLiquidacion` TEXT,
                `paquetesCargados` INTEGER NOT NULL DEFAULT 0,
                `paquetesDevueltos` INTEGER NOT NULL DEFAULT 0,
                PRIMARY KEY(`id`)
            )
            """.trimIndent()
        )

        // 2. Garantizar que columnas agregadas en iteraciones recientes existan en tablas ya creadas
        asegurarColumna(db, "ventas", "estado", "TEXT NOT NULL DEFAULT 'ACTIVA'")
        asegurarColumna(db, "ventas", "nombreRepartidor", "TEXT")
        asegurarColumna(db, "ventas", "motoAsignada", "TEXT")
        asegurarColumna(db, "ventas", "nombreRuta", "TEXT")

        asegurarColumna(db, "turnos", "totalVentas", "REAL NOT NULL DEFAULT 0.0")
        asegurarColumna(db, "turnos", "fechaCierre", "INTEGER")

        asegurarColumna(db, "rutas_repartidores", "nombreRuta", "TEXT NOT NULL DEFAULT 'Ruta General'")
        asegurarColumna(db, "rutas_repartidores", "paquetesCargados", "INTEGER NOT NULL DEFAULT 0")
        asegurarColumna(db, "rutas_repartidores", "paquetesDevueltos", "INTEGER NOT NULL DEFAULT 0")
    }

    private fun migrarAVersion8(db: SupportSQLiteDatabase) {
        // Ejecutar primero la migración a v7 para asegurar tablas base
        migrarAVersion7(db)

        // Incorporar columnas requeridas para turnos y cortes múltiples en turnos
        asegurarColumna(db, "turnos", "folioCorte", "TEXT NOT NULL DEFAULT ''")
        asegurarColumna(db, "turnos", "fechaDiaTexto", "TEXT NOT NULL DEFAULT ''")
        asegurarColumna(db, "turnos", "numeroTurnoDia", "INTEGER NOT NULL DEFAULT 1")
        asegurarColumna(db, "turnos", "usuarioCierreId", "TEXT")
        asegurarColumna(db, "turnos", "fondoInicial", "REAL NOT NULL DEFAULT 0.0")
        asegurarColumna(db, "turnos", "totalVentasMostrador", "REAL NOT NULL DEFAULT 0.0")
        asegurarColumna(db, "turnos", "totalCobradoReparto", "REAL NOT NULL DEFAULT 0.0")
        asegurarColumna(db, "turnos", "efectivoEsperado", "REAL NOT NULL DEFAULT 0.0")
        asegurarColumna(db, "turnos", "efectivoContado", "REAL NOT NULL DEFAULT 0.0")
        asegurarColumna(db, "turnos", "diferenciaArqueo", "REAL NOT NULL DEFAULT 0.0")
        asegurarColumna(db, "turnos", "notasCierre", "TEXT")
    }

    private fun asegurarColumna(db: SupportSQLiteDatabase, tabla: String, columna: String, definicion: String) {
        try {
            val cursor = db.query("PRAGMA table_info(`$tabla`)")
            var existe = false
            while (cursor.moveToNext()) {
                val indexNombre = cursor.getColumnIndex("name")
                if (indexNombre != -1 && cursor.getString(indexNombre).equals(columna, ignoreCase = true)) {
                    existe = true
                    break
                }
            }
            cursor.close()
            if (!existe) {
                db.execSQL("ALTER TABLE `$tabla` ADD COLUMN `$columna` $definicion")
            }
        } catch (_: Exception) {
            // Si la tabla aún no existe o hay algún error menor en PRAGMA, se ignora
        }
    }

    val MIGRATION_1_7 = object : Migration(1, 7) {
        override fun migrate(db: SupportSQLiteDatabase) = migrarAVersion7(db)
    }

    val MIGRATION_2_7 = object : Migration(2, 7) {
        override fun migrate(db: SupportSQLiteDatabase) = migrarAVersion7(db)
    }

    val MIGRATION_3_7 = object : Migration(3, 7) {
        override fun migrate(db: SupportSQLiteDatabase) = migrarAVersion7(db)
    }

    val MIGRATION_4_7 = object : Migration(4, 7) {
        override fun migrate(db: SupportSQLiteDatabase) = migrarAVersion7(db)
    }

    val MIGRATION_5_7 = object : Migration(5, 7) {
        override fun migrate(db: SupportSQLiteDatabase) = migrarAVersion7(db)
    }

    val MIGRATION_6_7 = object : Migration(6, 7) {
        override fun migrate(db: SupportSQLiteDatabase) = migrarAVersion7(db)
    }

    val MIGRATION_7_8 = object : Migration(7, 8) {
        override fun migrate(db: SupportSQLiteDatabase) = migrarAVersion8(db)
    }

    val MIGRATION_6_8 = object : Migration(6, 8) {
        override fun migrate(db: SupportSQLiteDatabase) = migrarAVersion8(db)
    }

    val MIGRATION_1_8 = object : Migration(1, 8) {
        override fun migrate(db: SupportSQLiteDatabase) = migrarAVersion8(db)
    }

    val ALL_MIGRATIONS = arrayOf(
        MIGRATION_1_7,
        MIGRATION_2_7,
        MIGRATION_3_7,
        MIGRATION_4_7,
        MIGRATION_5_7,
        MIGRATION_6_7,
        MIGRATION_7_8,
        MIGRATION_6_8,
        MIGRATION_1_8
    )
}
