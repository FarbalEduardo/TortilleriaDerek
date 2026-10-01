package com.example.tortilleriaderek.domain.usecase

import com.example.tortilleriaderek.domain.model.Usuario
import com.example.tortilleriaderek.domain.repository.AuthRepository
import javax.inject.Inject

/**
 * Creado por 🏗️ mobile-developer.
 * Caso de uso con única responsabilidad (Single Responsibility).
 */
class LoginUseCase @Inject constructor(
    private val repository: AuthRepository
) {
    suspend operator fun invoke(username: String, passwordRaw: String): Result<Usuario> {
        if (username.isBlank() || passwordRaw.isBlank()) {
            return Result.failure(Exception("Los campos no pueden estar vacíos"))
        }
        return repository.login(username, passwordRaw)
    }
}
