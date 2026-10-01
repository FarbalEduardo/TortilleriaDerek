package com.example.tortilleriaderek.data.local.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.example.tortilleriaderek.data.local.entity.VentaEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface VentaDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertVenta(venta: VentaEntity)

    @Query("DELETE FROM ventas WHERE id = :ventaId")
    suspend fun deleteVenta(ventaId: String)

    @Query("SELECT * FROM ventas WHERE turnoId = :turnoId ORDER BY fecha DESC")
    fun getVentasPorTurno(turnoId: String): Flow<List<VentaEntity>>

    @Query("SELECT * FROM ventas WHERE turnoId = :turnoId AND tipo = 'MOSTRADOR' ORDER BY fecha DESC")
    fun getVentasMostrador(turnoId: String): Flow<List<VentaEntity>>

    @Query("SELECT * FROM ventas WHERE turnoId = :turnoId AND tipo = 'REPARTIDOR' ORDER BY fecha DESC")
    fun getVentasRepartidor(turnoId: String): Flow<List<VentaEntity>>

    @Query("SELECT COALESCE(SUM(total), 0.0) FROM ventas WHERE turnoId = :turnoId AND estado = 'ACTIVA'")
    fun getTotalVentasPorTurno(turnoId: String): Flow<Double>

    @Query("SELECT folioTicket FROM ventas WHERE tipo = 'MOSTRADOR' ORDER BY fecha DESC LIMIT 1")
    suspend fun getUltimoFolioMostrador(): String?

    @Query("SELECT folioTicket FROM ventas WHERE tipo = 'REPARTIDOR' ORDER BY fecha DESC LIMIT 1")
    suspend fun getUltimoFolioRepartidor(): String?

    @Query("SELECT COUNT(*) FROM ventas WHERE turnoId = :turnoId")
    fun getConteoVentasPorTurno(turnoId: String): Flow<Int>

    @Query("""
        SELECT v.* FROM ventas v
        INNER JOIN turnos t ON v.turnoId = t.id
        WHERE t.estado = 'CERRADO' AND v.estado = 'ACTIVA' AND v.fecha BETWEEN :inicio AND :fin
        ORDER BY v.fecha ASC
    """)
    fun getVentasTurnosCerrados(inicio: Long, fin: Long): Flow<List<VentaEntity>>

    @Query("""
        SELECT v.* FROM ventas v
        INNER JOIN turnos t ON v.turnoId = t.id
        WHERE t.estado = 'CERRADO' AND v.estado = 'ACTIVA'
        ORDER BY v.fecha ASC
    """)
    fun getTodasVentasTurnosCerrados(): Flow<List<VentaEntity>>
}
