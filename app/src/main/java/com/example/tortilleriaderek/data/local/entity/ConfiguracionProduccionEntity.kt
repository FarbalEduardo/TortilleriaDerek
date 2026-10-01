package com.example.tortilleriaderek.data.local.entity

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "configuracion_produccion")
data class ConfiguracionProduccionEntity(
    @PrimaryKey val id: String = "DEFAULT",
    val pesoBultoHarinaKg: Double = 0.0,
    val kgMasaPorBulto: Double = 0.0,
    val rendimientoTortillaPorBulto: Double = 0.0,
    val mermaToleradaKgPorBulto: Double = 0.0,
    val porcentajeMermaTolerada: Double = 0.0,

    // Precios Dinámicos Mostrador
    val precioKilo: Double = 0.0,
    val precioPaquete: Double = 0.0,
    val precioMedioPaquete: Double = 0.0,

    // Precio Dinámico Repartidor (Mayoreo)
    val precioPaqueteRepartidor: Double = 0.0,

    // Tamaños / Pesos Dinámicos de Paquetes (en gramos)
    val pesoPaqueteGramos: Int = 0,
    val pesoMedioPaqueteGramos: Int = 0,

    val fechaModificacion: Long = System.currentTimeMillis()
) {
    val pesoPaqueteKg: Double
        get() = pesoPaqueteGramos / 1000.0

    val pesoMedioPaqueteKg: Double
        get() = pesoMedioPaqueteGramos / 1000.0
}

