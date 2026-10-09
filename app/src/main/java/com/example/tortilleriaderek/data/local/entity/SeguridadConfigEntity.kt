package com.example.tortilleriaderek.data.local.entity

import androidx.room.Entity
import androidx.room.PrimaryKey

/**
 * Creado por 🛡️ security-expert y 🏗️ mobile-developer.
 * Almacena los parámetros criptográficos de la Clave Maestra de Recuperación Offline,
 * configuración de biometría y control de tasa de intentos de PIN.
 */
@Entity(tableName = "seguridad_config")
data class SeguridadConfigEntity(
    @PrimaryKey val id: String = "DEFAULT",
    val masterKeyHash: String,
    val masterKeySalt: String,
    val biometriaHabilitada: Boolean = false,
    val intentosFallidos: Int = 0,
    val timestampBloqueo: Long = 0L,
    val fechaModificacion: Long = System.currentTimeMillis()
)
