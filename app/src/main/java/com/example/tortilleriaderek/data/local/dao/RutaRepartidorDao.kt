package com.example.tortilleriaderek.data.local.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import com.example.tortilleriaderek.data.local.entity.RutaRepartidorEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface RutaRepartidorDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertRuta(ruta: RutaRepartidorEntity)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertRutas(rutas: List<RutaRepartidorEntity>)

    @Update
    suspend fun updateRuta(ruta: RutaRepartidorEntity)

    @Query("SELECT * FROM rutas_repartidores WHERE turnoId = :turnoId ORDER BY id ASC")
    fun getRutasPorTurno(turnoId: String): Flow<List<RutaRepartidorEntity>>

    @Query("SELECT COUNT(*) FROM rutas_repartidores WHERE turnoId = :turnoId AND status = 'EN_RUTA'")
    fun getRutasActivasCount(turnoId: String): Flow<Int>

    @Query("SELECT * FROM rutas_repartidores WHERE id = :id LIMIT 1")
    suspend fun getRutaById(id: String): RutaRepartidorEntity?

    @Query("DELETE FROM rutas_repartidores WHERE id = :id")
    suspend fun deleteRutaById(id: String)

    @Query("DELETE FROM rutas_repartidores WHERE turnoId = :turnoId AND repartidorNombre = :repartidorNombre")
    suspend fun deleteRutasByRepartidorNombre(turnoId: String, repartidorNombre: String)

    @Query("""
        SELECT r.* FROM rutas_repartidores r
        INNER JOIN turnos t ON r.turnoId = t.id
        WHERE t.estado = 'CERRADO' AND r.status = 'LIQUIDADO'
    """)
    fun getRutasTurnosCerrados(): Flow<List<RutaRepartidorEntity>>
}
