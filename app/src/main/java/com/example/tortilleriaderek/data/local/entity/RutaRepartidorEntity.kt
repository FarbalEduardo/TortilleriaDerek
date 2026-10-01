package com.example.tortilleriaderek.data.local.entity

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "rutas_repartidores")
data class RutaRepartidorEntity(
    @PrimaryKey val id: String,
    val turnoId: String,
    val moto: String,
    val repartidorNombre: String,
    val nombreRuta: String = "Ruta General",
    val status: String, // "PENDIENTE_SALIDA", "EN_RUTA", "LIQUIDADO"
    val cargaInicialKg: Double = 0.0,
    val pendienteCobro: Double = 0.0,
    val devolucionKg: Double = 0.0,
    val entregadoKg: Double = 0.0,
    val cobrado: Double = 0.0,
    val folioTicket: String? = null,
    val horaSalida: String? = null,
    val horaLiquidacion: String? = null,
    val paquetesCargados: Int = 0,
    val paquetesDevueltos: Int = 0
)
