package com.example.tortilleriaderek.ui.screens

data class ProductoConfig(
    val id: String,
    val monograma: String,
    val nombre: String,
    val descripcionPeso: String,
    val precio: Double,
    val pesoGramos: Int = 1000,
    val esPesoEditable: Boolean = false
)

data class RepartidorConfig(
    val id: String,
    val numeroBadge: String,
    val nombre: String,
    val detalleRuta: String
)

data class UsuarioConfig(
    val id: String,
    val username: String,
    val rol: String // "ADMIN", "EMPLEADO"
)
