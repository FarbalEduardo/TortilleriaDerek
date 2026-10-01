package com.example.tortilleriaderek.data.local.dao

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import com.example.tortilleriaderek.data.local.entity.UsuarioEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface UsuarioDao {
    @Query("SELECT * FROM usuarios WHERE LOWER(TRIM(username)) = LOWER(TRIM(:username)) LIMIT 1")
    suspend fun getUsuarioByUsername(username: String): UsuarioEntity?

    @Query("SELECT * FROM usuarios WHERE id = :id LIMIT 1")
    suspend fun getUsuarioById(id: String): UsuarioEntity?

    @Query("SELECT COUNT(*) FROM usuarios")
    suspend fun countUsuarios(): Int

    @Query("SELECT COUNT(*) FROM usuarios WHERE rol = 'ADMIN'")
    suspend fun countAdmins(): Int

    @Query("SELECT * FROM usuarios ORDER BY username ASC")
    fun getAllUsuarios(): Flow<List<UsuarioEntity>>

    @Query("SELECT * FROM usuarios ORDER BY username ASC")
    suspend fun getAllUsuariosSync(): List<UsuarioEntity>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertUsuario(usuario: UsuarioEntity)

    @Update
    suspend fun updateUsuario(usuario: UsuarioEntity)

    @Query("DELETE FROM usuarios WHERE id = :id")
    suspend fun deleteUsuarioById(id: String)
}
