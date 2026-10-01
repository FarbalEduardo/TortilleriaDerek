package com.example.tortilleriaderek.data.local.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.example.tortilleriaderek.data.local.entity.MermaProduccionEntity
import com.example.tortilleriaderek.data.local.entity.TandaProduccionEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface ProduccionDao {

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertTanda(tanda: TandaProduccionEntity)

    @Query("SELECT * FROM tandas_produccion WHERE turnoId = :turnoId ORDER BY fecha ASC")
    fun getTandasPorTurno(turnoId: String): Flow<List<TandaProduccionEntity>>

    @Query("SELECT COALESCE(SUM(kgTortillaEstimada), 0.0) FROM tandas_produccion WHERE turnoId = :turnoId")
    fun getProducidoNetoPorTurno(turnoId: String): Flow<Double>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertMerma(merma: MermaProduccionEntity)

    @Query("SELECT * FROM mermas_produccion WHERE turnoId = :turnoId ORDER BY fecha ASC")
    fun getMermasPorTurno(turnoId: String): Flow<List<MermaProduccionEntity>>

    @Query("SELECT COALESCE(SUM(kgMerma), 0.0) FROM mermas_produccion WHERE turnoId = :turnoId")
    fun getMermaTotalPorTurno(turnoId: String): Flow<Double>

    @Query("SELECT * FROM tandas_produccion ORDER BY fecha DESC")
    fun getTodasLasTandas(): Flow<List<TandaProduccionEntity>>

    @Query("SELECT * FROM mermas_produccion ORDER BY fecha DESC")
    fun getTodasLasMermas(): Flow<List<MermaProduccionEntity>>

    @Query("""
        SELECT p.* FROM tandas_produccion p
        INNER JOIN turnos t ON p.turnoId = t.id
        WHERE t.estado = 'CERRADO' AND p.fecha BETWEEN :inicio AND :fin
        ORDER BY p.fecha ASC
    """)
    fun getTandasTurnosCerrados(inicio: Long, fin: Long): Flow<List<TandaProduccionEntity>>

    @Query("""
        SELECT p.* FROM tandas_produccion p
        INNER JOIN turnos t ON p.turnoId = t.id
        WHERE t.estado = 'CERRADO'
        ORDER BY p.fecha ASC
    """)
    fun getTodasTandasTurnosCerrados(): Flow<List<TandaProduccionEntity>>

    @Query("""
        SELECT m.* FROM mermas_produccion m
        INNER JOIN turnos t ON m.turnoId = t.id
        WHERE t.estado = 'CERRADO' AND m.fecha BETWEEN :inicio AND :fin
        ORDER BY m.fecha ASC
    """)
    fun getMermasTurnosCerrados(inicio: Long, fin: Long): Flow<List<MermaProduccionEntity>>

    @Query("""
        SELECT m.* FROM mermas_produccion m
        INNER JOIN turnos t ON m.turnoId = t.id
        WHERE t.estado = 'CERRADO'
        ORDER BY m.fecha ASC
    """)
    fun getTodasMermasTurnosCerrados(): Flow<List<MermaProduccionEntity>>
}
