package com.example.tortilleriaderek.domain.usecase

import com.example.tortilleriaderek.data.local.dao.UsuarioDao
import com.example.tortilleriaderek.data.local.entity.UsuarioEntity
import com.example.tortilleriaderek.data.security.CryptoManager
import kotlinx.coroutines.flow.Flow
import java.util.UUID
import javax.inject.Inject

/**
 * Caso de uso para la administración segura de usuarios del sistema (CRUD).
 * Diseñado por 🛡️ security-expert y 🏗️ mobile-developer conforme a US-CFG-01.
 */
class GestionUsuariosUseCase @Inject constructor(
    private val usuarioDao: UsuarioDao
) {
    fun obtenerUsuarios(): Flow<List<UsuarioEntity>> {
        return usuarioDao.getAllUsuarios()
    }

    suspend fun crearUsuario(
        username: String,
        passwordRaw: String,
        rol: String
    ): Result<UsuarioEntity> {
        val cleanUsername = username.trim()
        if (cleanUsername.length < 3) {
            return Result.failure(IllegalArgumentException("El nombre de usuario debe tener al menos 3 caracteres"))
        }

        if (passwordRaw.trim().length < 4) {
            return Result.failure(IllegalArgumentException("La contraseña o PIN debe tener al menos 4 dígitos"))
        }

        val existente = usuarioDao.getUsuarioByUsername(cleanUsername)
        if (existente != null) {
            return Result.failure(IllegalArgumentException("Ya existe un usuario con el nombre '$cleanUsername'"))
        }

        val salt = CryptoManager.generateSalt()
        val passwordHash = CryptoManager.hashPassword(passwordRaw.trim(), salt)
        val nuevoUsuario = UsuarioEntity(
            id = UUID.randomUUID().toString(),
            username = cleanUsername,
            passwordHash = passwordHash,
            salt = salt,
            rol = rol.uppercase()
        )

        usuarioDao.insertUsuario(nuevoUsuario)
        return Result.success(nuevoUsuario)
    }

    suspend fun editarUsuario(
        id: String,
        nuevoUsername: String,
        nuevoRol: String,
        nuevaPasswordRaw: String? = null
    ): Result<UsuarioEntity> {
        val actual = usuarioDao.getUsuarioById(id)
            ?: return Result.failure(IllegalArgumentException("Usuario no encontrado"))

        val cleanUsername = nuevoUsername.trim()
        if (cleanUsername.length < 3) {
            return Result.failure(IllegalArgumentException("El nombre de usuario debe tener al menos 3 caracteres"))
        }

        // Si cambia de ADMIN a EMPLEADO, validar que no sea el único ADMIN
        if (actual.rol == "ADMIN" && nuevoRol.uppercase() != "ADMIN") {
            val totalAdmins = usuarioDao.countAdmins()
            if (totalAdmins <= 1) {
                return Result.failure(IllegalStateException("No es posible degradar al único administrador del sistema"))
            }
        }

        val (nuevoHash, nuevoSalt) = if (!nuevaPasswordRaw.isNullOrBlank()) {
            if (nuevaPasswordRaw.trim().length < 4) {
                return Result.failure(IllegalArgumentException("La nueva contraseña debe tener al menos 4 caracteres"))
            }
            val salt = CryptoManager.generateSalt()
            val hash = CryptoManager.hashPassword(nuevaPasswordRaw.trim(), salt)
            Pair(hash, salt)
        } else {
            Pair(actual.passwordHash, actual.salt)
        }

        val usuarioActualizado = actual.copy(
            username = cleanUsername,
            rol = nuevoRol.uppercase(),
            passwordHash = nuevoHash,
            salt = nuevoSalt
        )

        usuarioDao.updateUsuario(usuarioActualizado)
        return Result.success(usuarioActualizado)
    }

    suspend fun eliminarUsuario(
        id: String,
        usuarioTurnoActivoId: String? = null
    ): Result<Unit> {
        val usuario = usuarioDao.getUsuarioById(id)
            ?: return Result.failure(IllegalArgumentException("El usuario a eliminar no existe"))

        if (usuarioTurnoActivoId != null && usuario.id == usuarioTurnoActivoId) {
            return Result.failure(IllegalStateException("No es posible eliminar al usuario con turno activo actualmente"))
        }

        if (usuario.rol == "ADMIN") {
            val totalAdmins = usuarioDao.countAdmins()
            if (totalAdmins <= 1) {
                return Result.failure(IllegalStateException("No se puede eliminar al único administrador del sistema"))
            }
        }

        usuarioDao.deleteUsuarioById(id)
        return Result.success(Unit)
    }
}
