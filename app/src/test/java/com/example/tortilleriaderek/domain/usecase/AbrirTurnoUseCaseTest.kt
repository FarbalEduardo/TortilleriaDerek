package com.example.tortilleriaderek.domain.usecase

import com.example.tortilleriaderek.data.local.dao.RepartidorDao
import com.example.tortilleriaderek.data.local.dao.RutaRepartidorDao
import com.example.tortilleriaderek.data.local.dao.TurnoDao
import com.example.tortilleriaderek.data.local.entity.TurnoEntity
import io.mockk.*
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test

/**
 * Creado por 🏆 quality-pm-expert y 🏗️ mobile-developer.
 * Pruebas unitarias para AbrirTurnoUseCase según cortes_y_turnos_multiples_spec.md.
 */
class AbrirTurnoUseCaseTest {

    private val turnoDao = mockk<TurnoDao>(relaxed = true)
    private val rutaRepartidorDao = mockk<RutaRepartidorDao>(relaxed = true)
    private val repartidorDao = mockk<RepartidorDao>(relaxed = true)

    private lateinit var useCase: AbrirTurnoUseCase

    @Before
    fun setUp() {
        useCase = AbrirTurnoUseCase(turnoDao, rutaRepartidorDao, repartidorDao)
    }

    @Test
    fun invoke_asignaNumeroTurno1YFolio01_cuandoNoHayCortesPreviosEnElDia() = runTest {
        coEvery { turnoDao.getSiguienteNumeroTurnoDia(any()) } returns 1
        coEvery { repartidorDao.getRepartidoresList() } returns emptyList()

        val slotTurno = slot<TurnoEntity>()
        coEvery { turnoDao.insertTurno(capture(slotTurno)) } just Runs

        useCase.invoke(usuarioId = "admin1", fondoInicial = 500.0)

        val turnoCapturado = slotTurno.captured
        assertEquals(1, turnoCapturado.numeroTurnoDia)
        assertTrue("El folio debe terminar en -01", turnoCapturado.folioCorte.endsWith("-01"))
        assertTrue("El folio debe iniciar con CORTE-", turnoCapturado.folioCorte.startsWith("CORTE-"))
        assertEquals(500.0, turnoCapturado.fondoInicial, 0.001)
        assertEquals("ABIERTO", turnoCapturado.estado)
    }

    @Test
    fun invoke_asignaNumeroTurno2YFolio02_cuandoYaHuboUnCortePrevio() = runTest {
        coEvery { turnoDao.getSiguienteNumeroTurnoDia(any()) } returns 2
        coEvery { repartidorDao.getRepartidoresList() } returns emptyList()

        val slotTurno = slot<TurnoEntity>()
        coEvery { turnoDao.insertTurno(capture(slotTurno)) } just Runs

        useCase.invoke(usuarioId = "admin1", fondoInicial = 300.0)

        val turnoCapturado = slotTurno.captured
        assertEquals(2, turnoCapturado.numeroTurnoDia)
        assertTrue("El folio debe terminar en -02", turnoCapturado.folioCorte.endsWith("-02"))
        assertEquals(300.0, turnoCapturado.fondoInicial, 0.001)
    }
}
