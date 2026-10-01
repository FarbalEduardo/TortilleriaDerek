package com.example.tortilleriaderek.domain.usecase

import com.example.tortilleriaderek.domain.model.Usuario
import com.example.tortilleriaderek.domain.repository.AuthRepository
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.mockk
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test

/**
 * Creado por 🏆 quality-pm-expert.
 * Pruebas unitarias para LoginUseCase.
 */
class LoginUseCaseTest {

    private lateinit var authRepository: AuthRepository
    private lateinit var loginUseCase: LoginUseCase

    @Before
    fun setUp() {
        authRepository = mockk()
        loginUseCase = LoginUseCase(authRepository)
    }

    @Test
    fun `cuando username esta vacio, retorna Result failure`() = runTest {
        // When
        val result = loginUseCase(username = "", passwordRaw = "password123")

        // Then
        assertTrue(result.isFailure)
        assertEquals("Los campos no pueden estar vacíos", result.exceptionOrNull()?.message)
        
        // Verifica que no se llamó al repositorio
        coVerify(exactly = 0) { authRepository.login(any(), any()) }
    }

    @Test
    fun `cuando password esta vacio, retorna Result failure`() = runTest {
        // When
        val result = loginUseCase(username = "admin1", passwordRaw = "  ")

        // Then
        assertTrue(result.isFailure)
        assertEquals("Los campos no pueden estar vacíos", result.exceptionOrNull()?.message)
        
        // Verifica que no se llamó al repositorio
        coVerify(exactly = 0) { authRepository.login(any(), any()) }
    }

    @Test
    fun `cuando credenciales son correctas, retorna el Usuario exitosamente`() = runTest {
        // Given
        val usuario = Usuario(id = "1", username = "admin1", rol = "ADMIN")
        coEvery { authRepository.login("admin1", "admin123") } returns Result.success(usuario)

        // When
        val result = loginUseCase(username = "admin1", passwordRaw = "admin123")

        // Then
        assertTrue(result.isSuccess)
        assertEquals(usuario, result.getOrNull())
        
        coVerify(exactly = 1) { authRepository.login("admin1", "admin123") }
    }

    @Test
    fun `cuando credenciales son incorrectas, propaga Result failure del repositorio`() = runTest {
        // Given
        val exception = Exception("Usuario o contraseña incorrectos")
        coEvery { authRepository.login("admin1", "wrongPass") } returns Result.failure(exception)

        // When
        val result = loginUseCase(username = "admin1", passwordRaw = "wrongPass")

        // Then
        assertTrue(result.isFailure)
        assertEquals("Usuario o contraseña incorrectos", result.exceptionOrNull()?.message)
        
        coVerify(exactly = 1) { authRepository.login("admin1", "wrongPass") }
    }
}
