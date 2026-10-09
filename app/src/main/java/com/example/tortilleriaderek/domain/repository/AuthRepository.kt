package com.example.tortilleriaderek.domain.repository

import com.example.tortilleriaderek.domain.model.SeguridadConfig
import com.example.tortilleriaderek.domain.model.Usuario
import kotlinx.coroutines.flow.Flow

/**
 * Creado por 🏗️ mobile-developer.
 * Contrato puro de dominio (Clean Architecture).
 */
interface AuthRepository {
    suspend fun login(username: String, passwordRaw: String): Result<Usuario>
    suspend fun loginConCodigoOPatron(codigoOPatron: String): Result<Usuario>
    suspend fun loginConPin(pinRaw: String): Result<Usuario>
    suspend fun loginConPatron(patronRaw: String): Result<Usuario>
    suspend fun loginRapidoAdminBiometria(): Result<Usuario>

    fun getSeguridadConfig(): Flow<SeguridadConfig>
    suspend fun verificarMasterKey(claveRaw: String): Boolean
    suspend fun restablecerPinAdminConMasterKey(claveMaestraRaw: String, nuevoPin: String): Result<Unit>
    suspend fun setBiometriaHabilitada(habilitada: Boolean)
    suspend fun registrarIntentoFallido(): Int
    suspend fun resetearIntentosFallidos()
}

