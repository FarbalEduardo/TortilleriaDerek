package com.example.tortilleriaderek.data.local.entity

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "turnos")
data class TurnoEntity(
    @PrimaryKey val id: String,
    val fechaApertura: Long,
    val estado: String, // "ABIERTO", "CERRADO"
    val usuarioId: String,
    val fechaCierre: Long? = null,
    val totalVentas: Double = 0.0
)
