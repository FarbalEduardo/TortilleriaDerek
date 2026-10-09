package com.example.tortilleriaderek.data.local.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import com.example.tortilleriaderek.data.local.entity.SeguridadConfigEntity
import kotlinx.coroutines.flow.Flow

/**
 * Creado por 🏗️ mobile-developer.
 * DAO para acceso reactivo y transaccional a la configuración de seguridad local.
 */
@Dao
interface SeguridadConfigDao {

    @Query("SELECT * FROM seguridad_config WHERE id = 'DEFAULT' LIMIT 1")
    fun getSeguridadConfig(): Flow<SeguridadConfigEntity?>

    @Query("SELECT * FROM seguridad_config WHERE id = 'DEFAULT' LIMIT 1")
    suspend fun getSeguridadConfigSync(): SeguridadConfigEntity?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertOrUpdate(config: SeguridadConfigEntity)

    @Update
    suspend fun update(config: SeguridadConfigEntity)

    @Query("UPDATE seguridad_config SET intentosFallidos = :intentos, timestampBloqueo = :timestamp WHERE id = 'DEFAULT'")
    suspend fun actualizarIntentosYBloqueo(intentos: Int, timestamp: Long)

    @Query("UPDATE seguridad_config SET biometriaHabilitada = :habilitada WHERE id = 'DEFAULT'")
    suspend fun actualizarBiometria(habilitada: Boolean)
}
