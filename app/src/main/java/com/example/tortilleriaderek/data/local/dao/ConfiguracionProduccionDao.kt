package com.example.tortilleriaderek.data.local.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.example.tortilleriaderek.data.local.entity.ConfiguracionProduccionEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface ConfiguracionProduccionDao {

    @Query("SELECT * FROM configuracion_produccion WHERE id = :id LIMIT 1")
    fun getConfiguracion(id: String = "DEFAULT"): Flow<ConfiguracionProduccionEntity?>

    @Query("SELECT * FROM configuracion_produccion WHERE id = :id LIMIT 1")
    suspend fun getConfiguracionSync(id: String = "DEFAULT"): ConfiguracionProduccionEntity?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertOrUpdateConfiguracion(config: ConfiguracionProduccionEntity)
}
