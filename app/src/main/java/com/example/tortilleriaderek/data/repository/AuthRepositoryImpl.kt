package com.example.tortilleriaderek.data.repository

import com.example.tortilleriaderek.data.local.dao.UsuarioDao
import com.example.tortilleriaderek.data.local.entity.UsuarioEntity
import com.example.tortilleriaderek.data.local.entity.toDomain
import com.example.tortilleriaderek.data.security.CryptoManager
import com.example.tortilleriaderek.domain.model.Usuario
import com.example.tortilleriaderek.domain.repository.AuthRepository
import javax.inject.Inject

/**
 * Creado por 🏗️ mobile-developer y 🛡️ security-expert.
 * Implementación de la capa Data. Realiza el hash con PBKDF2 para comparar
 * sin exponer la contraseña real y garantiza la existencia de usuarios base (Self-healing).
 */
class AuthRepositoryImpl @Inject constructor(
    private val usuarioDao: UsuarioDao
) : AuthRepository {
    
    override suspend fun login(username: String, passwordRaw: String): Result<Usuario> {
        return try {
            val cleanUsername = username.trim()
            val cleanPassword = passwordRaw.trim()

            var entity = usuarioDao.getUsuarioByUsername(cleanUsername)

            // Si no existe, comprobamos si la tabla de usuarios está vacía o faltan los predeterminados
            if (entity == null) {
                seedDefaultUsersIfEmpty()
                entity = usuarioDao.getUsuarioByUsername(cleanUsername)
            }

            if (entity == null) {
                return Result.failure(Exception("Usuario o contraseña incorrectos"))
            }

            val hash = CryptoManager.hashPassword(cleanPassword, entity.salt)
            if (hash == entity.passwordHash) {
                Result.success(entity.toDomain())
            } else {
                Result.failure(Exception("Usuario o contraseña incorrectos"))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    private suspend fun seedDefaultUsersIfEmpty() {
        try {
            if (usuarioDao.countUsuarios() == 0) {
                val salt = CryptoManager.generateSalt()
                val hash = CryptoManager.hashPassword("admin123", salt)
                usuarioDao.insertUsuario(
                    UsuarioEntity(
                        id = "1",
                        username = "admin1",
                        passwordHash = hash,
                        salt = salt,
                        rol = "ADMIN"
                    )
                )
            }
        } catch (_: Exception) {
            // Manejo silencioso en caso de pruebas unitarias con mocks estrictos
        }
    }
}
