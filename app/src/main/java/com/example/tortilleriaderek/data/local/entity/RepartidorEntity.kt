package com.example.tortilleriaderek.data.local.entity

import androidx.room.Entity
import androidx.room.PrimaryKey

/**
 * Entidad que representa a un repartidor registrado en el catálogo persistente de la tortillería.
 * Permite mantener los repartidores guardados en Room independientemente de los turnos.
 */
@Entity(tableName = "repartidores")
data class RepartidorEntity(
    @PrimaryKey
    val id: String,
    val numeroBadge: String, // ej. "#01", "#02"
    val nombre: String,
    val moto: String = "",
    val detalleRuta: String = "",
    val fechaCreacion: Long = System.currentTimeMillis()
)
