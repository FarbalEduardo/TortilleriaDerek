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
            return Result.failure(IllegalArgumentException("El código PIN debe tener al menos 4 dígitos."))
        }
        val result = authRepository.loginConPin(cleanPin)
        if (result.isSuccess) {
            authRepository.resetearIntentosFallidos()
        } else {
            authRepository.registrarIntentoFallido()
        }
        return result
    }

    suspend fun autenticarConPatron(patronRaw: String): Result<Usuario> {
        val cleanPatron = patronRaw.trim()
        if (cleanPatron.length < 4) {
            return Result.failure(IllegalArgumentException("El patrón debe conectar al menos 4 puntos."))
        }
        val result = authRepository.loginConPatron(cleanPatron)
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
