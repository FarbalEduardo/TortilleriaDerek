package com.example.tortilleriaderek.domain.model

/**
 * Entidad pura de Dominio para el Usuario.
 * No tiene dependencias del framework de Android.
 */
data class Usuario(
    val id: String,
    val username: String,
    val rol: String
)
