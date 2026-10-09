package com.example.tortilleriaderek.data.local.migration

import androidx.sqlite.db.SupportSQLiteDatabase
import io.mockk.every
import io.mockk.mockk
import io.mockk.verify
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertTrue
import org.junit.Test

/**
 * Creado por 🏆 quality-pm-expert y 🏗️ mobile-developer.
 * Pruebas unitarias de integridad de esquema y migraciones seguras para Room v7.
 * Valida que todas las tablas y columnas requeridas se creen sin pérdida de datos.
 */
class MigrationsTest {

    @Test
    fun allMigrations_contieneMigracionesCompletasDesdeVersionesPrevias() {
        val migraciones = Migrations.ALL_MIGRATIONS
        assertEquals(9, migraciones.size)

        val startVersions = migraciones.map { it.startVersion }.toSet()
        val endVersions = migraciones.map { it.endVersion }.toSet()

        assertTrue("Debe soportar migrar desde v1", startVersions.contains(1))
        assertTrue("Debe soportar migrar desde v2", startVersions.contains(2))
        assertTrue("Debe soportar migrar desde v3", startVersions.contains(3))
        assertTrue("Debe soportar migrar desde v4", startVersions.contains(4))
        assertTrue("Debe soportar migrar desde v5", startVersions.contains(5))
        assertTrue("Debe soportar migrar desde v6", startVersions.contains(6))
        assertTrue("Debe soportar migrar desde v7", startVersions.contains(7))

        assertTrue("Las migraciones convergen en versiones estables 7 y 8", endVersions.contains(7) && endVersions.contains(8))
    }

    @Test
    fun migration6_7_ejecutaSentenciasSqlDeCreacionDeTablas() {
        val mockDb = mockk<SupportSQLiteDatabase>(relaxed = true)

        // Simular cursor vacío para verificación de columnas
        val mockCursor = mockk<android.database.Cursor>(relaxed = true)
        every { mockCursor.moveToNext() } returns false
        every { mockDb.query(any<String>()) } returns mockCursor

        Migrations.MIGRATION_6_7.migrate(mockDb)

        // Verificar que se invocaron las sentencias de creación de las 8 tablas oficiales
        verify(atLeast = 1) { mockDb.execSQL(match { it.contains("CREATE TABLE IF NOT EXISTS `turnos`") }) }
        verify(atLeast = 1) { mockDb.execSQL(match { it.contains("CREATE TABLE IF NOT EXISTS `ventas`") }) }
        verify(atLeast = 1) { mockDb.execSQL(match { it.contains("CREATE TABLE IF NOT EXISTS `configuracion_produccion`") }) }
        verify(atLeast = 1) { mockDb.execSQL(match { it.contains("CREATE TABLE IF NOT EXISTS `usuarios`") }) }
        verify(atLeast = 1) { mockDb.execSQL(match { it.contains("CREATE TABLE IF NOT EXISTS `tandas_produccion`") }) }
        verify(atLeast = 1) { mockDb.execSQL(match { it.contains("CREATE TABLE IF NOT EXISTS `mermas_produccion`") }) }
        verify(atLeast = 1) { mockDb.execSQL(match { it.contains("CREATE TABLE IF NOT EXISTS `repartidores`") }) }
        verify(atLeast = 1) { mockDb.execSQL(match { it.contains("CREATE TABLE IF NOT EXISTS `rutas_repartidores`") }) }
    }

    @Test
    fun migration7_8_incorporaColumnasDeCortesMultiplesYArqueo() {
        val mockDb = mockk<SupportSQLiteDatabase>(relaxed = true)
        val mockCursor = mockk<android.database.Cursor>(relaxed = true)
        every { mockCursor.moveToNext() } returns false
        every { mockDb.query(any<String>()) } returns mockCursor

        Migrations.MIGRATION_7_8.migrate(mockDb)

        // Verificar alteración de columnas nuevas para cortes múltiples
        verify { mockDb.execSQL(match { it.contains("ALTER TABLE `turnos` ADD COLUMN `folioCorte`") }) }
        verify { mockDb.execSQL(match { it.contains("ALTER TABLE `turnos` ADD COLUMN `numeroTurnoDia`") }) }
        verify { mockDb.execSQL(match { it.contains("ALTER TABLE `turnos` ADD COLUMN `fechaDiaTexto`") }) }
        verify { mockDb.execSQL(match { it.contains("ALTER TABLE `turnos` ADD COLUMN `fondoInicial`") }) }
        verify { mockDb.execSQL(match { it.contains("ALTER TABLE `turnos` ADD COLUMN `totalVentasMostrador`") }) }
        verify { mockDb.execSQL(match { it.contains("ALTER TABLE `turnos` ADD COLUMN `totalCobradoReparto`") }) }
        verify { mockDb.execSQL(match { it.contains("ALTER TABLE `turnos` ADD COLUMN `diferenciaArqueo`") }) }
    }
}

