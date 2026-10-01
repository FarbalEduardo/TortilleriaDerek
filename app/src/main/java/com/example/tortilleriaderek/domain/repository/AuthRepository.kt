package com.example.tortilleriaderek.domain.repository

import com.example.tortilleriaderek.domain.model.Usuario

/**
 * Creado por 🏗️ mobile-developer.
 * Contrato puro de dominio (Clean Architecture).
 */
interface AuthRepository {
    suspend fun login(username: String, passwordRaw: String): Result<Usuario>
}
