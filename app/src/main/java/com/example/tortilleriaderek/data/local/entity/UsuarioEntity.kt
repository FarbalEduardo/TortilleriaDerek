package com.example.tortilleriaderek.data.local.entity

import androidx.room.Entity
import androidx.room.PrimaryKey
import com.example.tortilleriaderek.domain.model.Usuario

@Entity(tableName = "usuarios")
data class UsuarioEntity(
    @PrimaryKey val id: String,
    val username: String,
    val passwordHash: String,
    val salt: String,
    val rol: String
)

fun UsuarioEntity.toDomain(): Usuario {
    return Usuario(
        id = id,
        username = username,
        rol = rol
    )
}
