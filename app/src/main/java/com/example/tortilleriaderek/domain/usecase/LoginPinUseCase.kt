package com.example.tortilleriaderek.domain.usecase

import com.example.tortilleriaderek.domain.model.Usuario
import com.example.tortilleriaderek.domain.repository.AuthRepository
import javax.inject.Inject

/**
 * Creado por 🏗️ mobile-developer.
 * Permite la autenticación ágil mediante PIN numérico o biometría.
 */
class LoginPinUseCase @Inject constructor(
    private val authRepository: AuthRepository
) {
    suspend fun autenticarConPin(pinRaw: String): Result<Usuario> {
        val cleanPin = pinRaw.trim()
        if (cleanPin.length < 4) {
            return Result.failure(IllegalArgumentException("El PIN debe tener al menos 4 dígitos."))
        }
        val result = authRepository.loginConPin(cleanPin)
        if (result.isSuccess) {
            authRepository.resetearIntentosFallidos()
        } else {
            authRepository.registrarIntentoFallido()
        }
        return result
    }

    suspend fun autenticarConBiometria(): Result<Usuario> {
        val result = authRepository.loginRapidoAdminBiometria()
        if (result.isSuccess) {
            authRepository.resetearIntentosFallidos()
        }
        return result
    }
}
