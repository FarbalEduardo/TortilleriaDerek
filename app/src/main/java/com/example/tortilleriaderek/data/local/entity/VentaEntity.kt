package com.example.tortilleriaderek.data.local.entity

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "ventas")
data class VentaEntity(
    @PrimaryKey val id: String,
    val turnoId: String,
    val folioTicket: String, // ej. "M-0143", "R-0038"
    val tipo: String,        // "MOSTRADOR" o "REPARTIDOR"
    val fecha: Long,         // timestamp
    val hora: String,        // ej. "11:42 AM"
    val detalleProductos: String,
    val total: Double,
    val metodoPago: String,  // "Efectivo", "Tarjeta Débito", "Tarjeta Crédito"
    val usuarioId: String,
    val usuarioNombre: String,
    val estado: String = "ACTIVA", // "ACTIVA", "CANCELADA"
    val nombreRepartidor: String? = null,
    val motoAsignada: String? = null,
    val nombreRuta: String? = null
)
