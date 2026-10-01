package com.example.tortilleriaderek.domain.usecase

import app.cash.turbine.test
import com.example.tortilleriaderek.data.local.dao.TurnoDao
import com.example.tortilleriaderek.data.local.entity.TurnoEntity
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.mockk
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test

/**
 * Creado por 🏆 quality-pm-expert.
 * Pruebas unitarias para VerificarTurnoActivoUseCase (US8).
 */
class VerificarTurnoActivoUseCaseTest {

    private lateinit var turnoDao: TurnoDao
    private lateinit var verificarTurnoActivoUseCase: VerificarTurnoActivoUseCase

    @Before
    fun setUp() {
        turnoDao = mockk()
        verificarTurnoActivoUseCase = VerificarTurnoActivoUseCase(turnoDao)
    }

    @Test
    fun `cuando existe turno en estado ABIERTO, retorna true`() = runTest {
        // Given
        val turnoAbierto = TurnoEntity(
            id = "turno-1",
            fechaApertura = System.currentTimeMillis(),
            estado = "ABIERTO",
            usuarioId = "user-1"
        )
        coEvery { turnoDao.getTurnoActivoSync() } returns turnoAbierto

        // When
        val result = verificarTurnoActivoUseCase()

        // Then
        assertTrue(result)
        coVerify(exactly = 1) { turnoDao.getTurnoActivoSync() }
    }

    @Test
    fun `cuando no existe turno activo (null), retorna false`() = runTest {
        // Given
        coEvery { turnoDao.getTurnoActivoSync() } returns null

        // When
        val result = verificarTurnoActivoUseCase()

        // Then
        assertFalse(result)
        coVerify(exactly = 1) { turnoDao.getTurnoActivoSync() }
    }

    @Test
    fun `cuando el turno existe pero su estado es CERRADO, retorna false`() = runTest {
        // Given
        val turnoCerrado = TurnoEntity(
            id = "turno-1",
            fechaApertura = System.currentTimeMillis(),
            fechaCierre = System.currentTimeMillis() + 3600000,
            estado = "CERRADO",
            usuarioId = "user-1",
            totalVentas = 1500.0
        )
        coEvery { turnoDao.getTurnoActivoSync() } returns turnoCerrado

        // When
        val result = verificarTurnoActivoUseCase()

        // Then
        assertFalse(result)
        coVerify(exactly = 1) { turnoDao.getTurnoActivoSync() }
    }

    @Test
    fun `observar emite true cuando getTurnoActivo emite un turno ABIERTO`() = runTest {
        // Given
        val turnoAbierto = TurnoEntity(
            id = "turno-2",
            fechaApertura = System.currentTimeMillis(),
            estado = "ABIERTO",
            usuarioId = "user-2"
        )
        coEvery { turnoDao.getTurnoActivo() } returns kotlinx.coroutines.flow.flowOf(turnoAbierto)

        // When & Then
        verificarTurnoActivoUseCase.observar().test {
            assertTrue(awaitItem())
            awaitComplete()
        }
    }

    @Test
    fun `observar emite false cuando getTurnoActivo emite null tras cerrar turno`() = runTest {
        // Given
        coEvery { turnoDao.getTurnoActivo() } returns kotlinx.coroutines.flow.flowOf(null)

        // When & Then
        verificarTurnoActivoUseCase.observar().test {
            assertFalse(awaitItem())
            awaitComplete()
        }
    }
}
