package com.example.tortilleriaderek.data.repository

import com.example.tortilleriaderek.data.local.dao.UsuarioDao
import com.example.tortilleriaderek.data.local.entity.UsuarioEntity
import com.example.tortilleriaderek.data.security.CryptoManager
import io.mockk.coEvery
import io.mockk.mockk
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test

/**
 * Creado por 🏆 quality-pm-expert y 🛡️ security-expert.
 * Pruebas unitarias para AuthRepository. Valida el correcto funcionamiento del hashing.
 */
class AuthRepositoryImplTest {

    private lateinit var usuarioDao: UsuarioDao
    private lateinit var authRepository: AuthRepositoryImpl

    @Before
    fun setUp() {
        usuarioDao = mockk()
        authRepository = AuthRepositoryImpl(usuarioDao)
    }

    @Test
    fun `login con usuario inexistente, retorna Result failure`() = runTest {
        // Given
        coEvery { usuarioDao.getUsuarioByUsername("adminDesconocido") } returns null

        // When
        val result = authRepository.login("adminDesconocido", "12345")

        // Then
        assertTrue(result.isFailure)
        assertEquals("Usuario o contraseña incorrectos", result.exceptionOrNull()?.message)
    }

    @Test
    fun `login con password incorrecto, falla tras verificar hash`() = runTest {
        // Given
        val mockSalt = "S0m3S4lt"
        // Simulamos que en BD la password "admin123" generó cierto hash
        val realHashInDb = CryptoManager.hashPassword("admin123", mockSalt)
        
        val usuarioEnBD = UsuarioEntity(
            id = "1", 
            username = "admin1", 
            passwordHash = realHashInDb, 
            salt = mockSalt, 
            rol = "ADMIN"
        )
        
        coEvery { usuarioDao.getUsuarioByUsername("admin1") } returns usuarioEnBD

        // When (Intentamos login con "passwordEquivocada")
        val result = authRepository.login("admin1", "passwordEquivocada")

        // Then
        assertTrue(result.isFailure)
        assertEquals("Usuario o contraseña incorrectos", result.exceptionOrNull()?.message)
    }

    @Test
    fun `login con password correcta, el hash coincide y retorna Usuario`() = runTest {
        // Given
        val mockSalt = "AnotherS4lt"
        val realHashInDb = CryptoManager.hashPassword("correctPassword", mockSalt)
        
        val usuarioEnBD = UsuarioEntity(
            id = "2", 
            username = "admin2", 
            passwordHash = realHashInDb, 
            salt = mockSalt, 
            rol = "ADMIN"
        )
        
        coEvery { usuarioDao.getUsuarioByUsername("admin2") } returns usuarioEnBD

        // When
        val result = authRepository.login("admin2", "correctPassword")

        // Then
        assertTrue(result.isSuccess)
        val usuarioDomain = result.getOrNull()!!
        assertEquals("2", usuarioDomain.id)
        assertEquals("admin2", usuarioDomain.username)
        assertEquals("ADMIN", usuarioDomain.rol)
    }
}
