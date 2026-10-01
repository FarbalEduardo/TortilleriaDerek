package com.example.tortilleriaderek.data.local.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import com.example.tortilleriaderek.data.local.entity.TurnoEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface TurnoDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertTurno(turno: TurnoEntity)

    @Update
    suspend fun updateTurno(turno: TurnoEntity)

    @Query("SELECT * FROM turnos WHERE estado = 'ABIERTO' ORDER BY fechaApertura DESC LIMIT 1")
    fun getTurnoActivo(): Flow<TurnoEntity?>

    @Query("SELECT * FROM turnos WHERE estado = 'ABIERTO' ORDER BY fechaApertura DESC LIMIT 1")
    suspend fun getTurnoActivoSync(): TurnoEntity?

    @Query("SELECT * FROM turnos ORDER BY fechaApertura DESC LIMIT 1")
    fun getUltimoTurno(): Flow<TurnoEntity?>

    @Query("UPDATE turnos SET estado = 'CERRADO', fechaCierre = :fechaCierre, totalVentas = :totalVentas WHERE id = :turnoId")
    suspend fun cerrarTurno(turnoId: String, fechaCierre: Long, totalVentas: Double)

    @Query("UPDATE turnos SET estado = 'CERRADO', fechaCierre = :fechaCierre WHERE estado = 'ABIERTO'")
    suspend fun cerrarTodosLosTurnosActivos(fechaCierre: Long)

    @Query("SELECT * FROM turnos WHERE estado = 'CERRADO' AND fechaApertura BETWEEN :inicio AND :fin ORDER BY fechaApertura ASC")
    fun getTurnosCerradosPorRango(inicio: Long, fin: Long): Flow<List<TurnoEntity>>

    @Query("SELECT * FROM turnos WHERE estado = 'CERRADO' ORDER BY fechaApertura ASC")
    fun getTodosLosTurnosCerrados(): Flow<List<TurnoEntity>>
}
