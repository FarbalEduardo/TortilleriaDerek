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
 * Pruebas unitarias para LoginPinUseCase con código PIN, patrón y biometría.
 */
class LoginPinUseCaseTest {

    private lateinit var authRepository: AuthRepository
    private lateinit var loginPinUseCase: LoginPinUseCase

    @Before
    fun setUp() {
        authRepository = mockk(relaxed = true)
        loginPinUseCase = LoginPinUseCase(authRepository)
    }

    @Test
    fun `autenticarConPin falla si el PIN tiene menos de 4 digitos`() = runTest {
        val result = loginPinUseCase.autenticarConPin("12")

        assertTrue(result.isFailure)
        assertEquals("El código PIN debe tener al menos 4 dígitos.", result.exceptionOrNull()?.message)
        coVerify(exactly = 0) { authRepository.loginConPin(any()) }
    }

    @Test
    fun `autenticarConPin exitoso resetea intentos fallidos`() = runTest {
        val usuario = Usuario(id = "1", username = "admin1", rol = "ADMIN")
        coEvery { authRepository.loginConPin("1234") } returns Result.success(usuario)

        val result = loginPinUseCase.autenticarConPin("1234")

        assertTrue(result.isSuccess)
        assertEquals(usuario, result.getOrNull())
        coVerify(exactly = 1) { authRepository.resetearIntentosFallidos() }
        coVerify(exactly = 0) { authRepository.registrarIntentoFallido() }
    }

    @Test
    fun `autenticarConPin fallido registra intento fallido`() = runTest {
        coEvery { authRepository.loginConPin("9999") } returns Result.failure(Exception("PIN incorrecto"))

        val result = loginPinUseCase.autenticarConPin("9999")

        assertTrue(result.isFailure)
        coVerify(exactly = 1) { authRepository.registrarIntentoFallido() }
        coVerify(exactly = 0) { authRepository.resetearIntentosFallidos() }
    }

    @Test
    fun `autenticarConPatron falla si conecta menos de 4 nodos`() = runTest {
        val result = loginPinUseCase.autenticarConPatron("123")

        assertTrue(result.isFailure)
        assertEquals("El patrón debe conectar al menos 4 puntos.", result.exceptionOrNull()?.message)
        coVerify(exactly = 0) { authRepository.loginConPatron(any()) }
    }

    @Test
    fun `autenticarConPatron exitoso resetea intentos`() = runTest {
        val usuario = Usuario(id = "1", username = "admin1", rol = "ADMIN")
        coEvery { authRepository.loginConPatron("1234") } returns Result.success(usuario)

        val result = loginPinUseCase.autenticarConPatron("1234")

        assertTrue(result.isSuccess)
        assertEquals(usuario, result.getOrNull())
        coVerify(exactly = 1) { authRepository.resetearIntentosFallidos() }
    }

    @Test
    fun `autenticarConBiometria exitosa autentica y resetea intentos`() = runTest {
        val admin = Usuario(id = "1", username = "admin1", rol = "ADMIN")
        coEvery { authRepository.loginRapidoAdminBiometria() } returns Result.success(admin)

        val result = loginPinUseCase.autenticarConBiometria()

        assertTrue(result.isSuccess)
        assertEquals(admin, result.getOrNull())
        coVerify(exactly = 1) { authRepository.resetearIntentosFallidos() }
    }
}
