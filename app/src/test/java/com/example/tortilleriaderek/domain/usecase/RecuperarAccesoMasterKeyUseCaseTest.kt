package com.example.tortilleriaderek.domain.usecase

import com.example.tortilleriaderek.domain.repository.AuthRepository
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.mockk
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test

/**
 * Creado por 🏆 quality-pm-expert.
 * Pruebas unitarias para RecuperarAccesoMasterKeyUseCase.
 */
class RecuperarAccesoMasterKeyUseCaseTest {

    private lateinit var authRepository: AuthRepository
    private lateinit var useCase: RecuperarAccesoMasterKeyUseCase

    @Before
    fun setUp() {
        authRepository = mockk(relaxed = true)
        useCase = RecuperarAccesoMasterKeyUseCase(authRepository)
    }

    @Test
    fun `verificarMasterKey con cadena en blanco retorna false sin consultar repositorio`() = runTest {
        val result = useCase.verificarMasterKey("   ")
        assertFalse(result)
        coVerify(exactly = 0) { authRepository.verificarMasterKey(any()) }
    }

    @Test
    fun `verificarMasterKey valida correctamente con repositorio`() = runTest {
        coEvery { authRepository.verificarMasterKey("DEREK2026") } returns true

        val result = useCase.verificarMasterKey("DEREK2026")

        assertTrue(result)
        coVerify(exactly = 1) { authRepository.verificarMasterKey("DEREK2026") }
    }

    @Test
    fun `restablecerPinAdmin falla si el nuevo PIN tiene menos de 4 digitos`() = runTest {
        val result = useCase.restablecerPinAdmin("DEREK2026", "123")

        assertTrue(result.isFailure)
        assertEquals("El PIN debe tener al menos 4 dígitos.", result.exceptionOrNull()?.message)
        coVerify(exactly = 0) { authRepository.restablecerPinAdminConMasterKey(any(), any()) }
    }

    @Test
    fun `restablecerPinAdmin tiene exito cuando clave y nuevo PIN son validos`() = runTest {
        coEvery {
            authRepository.restablecerPinAdminConMasterKey("DEREK2026", "5821")
        } returns Result.success(Unit)

        val result = useCase.restablecerPinAdmin("DEREK2026", "5821")

        assertTrue(result.isSuccess)
        coVerify(exactly = 1) {
            authRepository.restablecerPinAdminConMasterKey("DEREK2026", "5821")
        }
    }
}
