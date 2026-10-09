package com.example.tortilleriaderek.domain.usecase

import com.example.tortilleriaderek.domain.repository.AuthRepository
import javax.inject.Inject

/**
 * Creado por 🏗️ mobile-developer y 🛡️ security-expert.
 * Permite al administrador desbloquear su acceso y redefinir su PIN utilizando la Clave Maestra offline.
 */
class RecuperarAccesoMasterKeyUseCase @Inject constructor(
    private val authRepository: AuthRepository
) {
    suspend fun verificarMasterKey(claveMaestraRaw: String): Boolean {
        if (claveMaestraRaw.isBlank()) return false
        return authRepository.verificarMasterKey(claveMaestraRaw.trim())
    }

    suspend fun restablecerPinAdmin(claveMaestraRaw: String, nuevoPin: String): Result<Unit> {
        val cleanClave = claveMaestraRaw.trim()
        val cleanPin = nuevoPin.trim()

        if (cleanPin.length < 4) {
            return Result.failure(IllegalArgumentException("El PIN debe tener al menos 4 dígitos."))
        }

        return authRepository.restablecerPinAdminConMasterKey(cleanClave, cleanPin)
    }
}
