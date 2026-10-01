package com.example.tortilleriaderek.data.local.entity

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "tandas_produccion")
data class TandaProduccionEntity(
    @PrimaryKey val id: String,
    val turnoId: String,
    val fecha: Long,
    val hora: String,
    val bultosHarina: Int,
    val pesoBultoKg: Double,
    val kgMasaCruda: Double,
    val kgTortillaEstimada: Double,
    val usuarioId: String,
    val usuarioNombre: String
)
