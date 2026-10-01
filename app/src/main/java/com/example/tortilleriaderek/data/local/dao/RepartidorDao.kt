package com.example.tortilleriaderek.data.local.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import com.example.tortilleriaderek.data.local.entity.RepartidorEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface RepartidorDao {
    @Query("SELECT * FROM repartidores ORDER BY fechaCreacion ASC")
    fun getAllRepartidores(): Flow<List<RepartidorEntity>>

    @Query("SELECT * FROM repartidores ORDER BY fechaCreacion ASC")
    suspend fun getRepartidoresList(): List<RepartidorEntity>

    @Query("SELECT * FROM repartidores WHERE id = :id LIMIT 1")
    suspend fun getRepartidorById(id: String): RepartidorEntity?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertRepartidor(repartidor: RepartidorEntity)

    @Update
    suspend fun updateRepartidor(repartidor: RepartidorEntity)

    @Query("DELETE FROM repartidores WHERE id = :id")
    suspend fun deleteRepartidorById(id: String)
}
