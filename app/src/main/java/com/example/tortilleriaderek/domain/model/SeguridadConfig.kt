package com.example.tortilleriaderek.domain.model

/**
 * Creado por 🏗️ mobile-developer.
 * Modelo inmutable de dominio para estado de seguridad, biometría y bloqueo.
 */
data class SeguridadConfig(
    val id: String = "DEFAULT",
    val tieneMasterKey: Boolean = true,
    val biometriaHabilitada: Boolean = false,
    val intentosFallidos: Int = 0,
    val timestampBloqueo: Long = 0L
) {
    val estaBloqueadoTemporalmente: Boolean
        get() = timestampBloqueo > System.currentTimeMillis()

    val segundosRestantesBloqueo: Long
        get() = if (estaBloqueadoTemporalmente) {
            ((timestampBloqueo - System.currentTimeMillis()) / 1000L).coerceAtLeast(0L)
        } else 0L
}
