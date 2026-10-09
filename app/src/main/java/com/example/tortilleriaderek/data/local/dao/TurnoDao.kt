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

    @Query("SELECT COALESCE(MAX(numeroTurnoDia), 0) + 1 FROM turnos WHERE fechaDiaTexto = :fechaDiaTexto")
    suspend fun getSiguienteNumeroTurnoDia(fechaDiaTexto: String): Int

    @Query("SELECT * FROM turnos WHERE fechaDiaTexto = :fechaDiaTexto ORDER BY numeroTurnoDia ASC")
    fun getCortesDelDia(fechaDiaTexto: String): Flow<List<TurnoEntity>>

    @Query("""
        SELECT 
            COUNT(id) as cantidadCortes,
            COALESCE(SUM(totalVentasMostrador), 0.0) as granTotalMostrador,
            COALESCE(SUM(totalCobradoReparto), 0.0) as granTotalReparto,
            COALESCE(SUM(totalVentas), 0.0) as granTotalDia,
            COALESCE(SUM(diferenciaArqueo), 0.0) as balanceDiferencias
        FROM turnos 
        WHERE fechaDiaTexto = :fechaDiaTexto
    """)
    fun getConsolidadoDiario(fechaDiaTexto: String): Flow<ConsolidadoDiarioResult?>

    @Query("""
        UPDATE turnos SET 
            estado = 'CERRADO', 
            fechaCierre = :fechaCierre, 
            usuarioCierreId = :usuarioCierreId,
            totalVentasMostrador = :totalVentasMostrador,
            totalCobradoReparto = :totalCobradoReparto,
            totalVentas = :totalVentas,
            efectivoEsperado = :efectivoEsperado,
            efectivoContado = :efectivoContado,
            diferenciaArqueo = :diferenciaArqueo,
            notasCierre = :notasCierre
        WHERE id = :turnoId
    """)
    suspend fun cerrarTurnoConArqueo(
        turnoId: String,
        fechaCierre: Long,
        usuarioCierreId: String?,
        totalVentasMostrador: Double,
        totalCobradoReparto: Double,
        totalVentas: Double,
        efectivoEsperado: Double,
        efectivoContado: Double,
        diferenciaArqueo: Double,
        notasCierre: String?
    )

    @Query("SELECT * FROM turnos WHERE estado = 'CERRADO' AND fechaApertura BETWEEN :inicio AND :fin ORDER BY fechaApertura ASC")
    fun getTurnosCerradosPorRango(inicio: Long, fin: Long): Flow<List<TurnoEntity>>

    @Query("SELECT * FROM turnos WHERE estado = 'CERRADO' ORDER BY fechaApertura ASC")
    fun getTodosLosTurnosCerrados(): Flow<List<TurnoEntity>>
}

data class ConsolidadoDiarioResult(
    val cantidadCortes: Int = 0,
    val granTotalMostrador: Double = 0.0,
    val granTotalReparto: Double = 0.0,
    val granTotalDia: Double = 0.0,
    val balanceDiferencias: Double = 0.0
)
