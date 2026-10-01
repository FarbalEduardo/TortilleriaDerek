package com.example.tortilleriaderek.data.local.entity

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "mermas_produccion")
data class MermaProduccionEntity(
    @PrimaryKey val id: String,
    val turnoId: String,
    val fecha: Long,
    val hora: String,
    val kgMerma: Double,
    val motivo: String,
    val usuarioId: String,
    val usuarioNombre: String
)
