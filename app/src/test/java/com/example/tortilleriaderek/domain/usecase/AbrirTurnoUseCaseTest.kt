package com.example.tortilleriaderek.domain.usecase

import com.example.tortilleriaderek.data.local.dao.RepartidorDao
import com.example.tortilleriaderek.data.local.dao.RutaRepartidorDao
import com.example.tortilleriaderek.data.local.dao.TurnoDao
import com.example.tortilleriaderek.data.local.entity.RepartidorEntity
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.mockk
import kotlinx.coroutines.test.runTest
import org.junit.Before
import org.junit.Test

/**
 * Creado por 🏆 quality-pm-expert.
 * Pruebas unitarias para AbrirTurnoUseCase (US13) con catálogo dinámico de repartidores en Room.
 */
class AbrirTurnoUseCaseTest {

    private lateinit var turnoDao: TurnoDao
    private lateinit var rutaRepartidorDao: RutaRepartidorDao
    private lateinit var repartidorDao: RepartidorDao
    private lateinit var abrirTurnoUseCase: AbrirTurnoUseCase

    @Before
    fun setUp() {
        turnoDao = mockk(relaxed = true)
        rutaRepartidorDao = mockk(relaxed = true)
        repartidorDao = mockk(relaxed = true)
        abrirTurnoUseCase = AbrirTurnoUseCase(turnoDao, rutaRepartidorDao, repartidorDao)
    }

    @Test
    fun `al abrir turno con repartidores registrados, inicializa rutas limpias en PENDIENTE_SALIDA con 0kg para ellos`() = runTest {
        // Given
        val dummyRepartidores = listOf(
            RepartidorEntity("rep-1", "#01", "Roberto Díaz", "Moto 01", "Ruta Centro"),
            RepartidorEntity("rep-2", "#02", "Ana Gómez", "Moto 02", "Ruta San Juan")
        )
        coEvery { repartidorDao.getRepartidoresList() } returns dummyRepartidores

        // When
        abrirTurnoUseCase(usuarioId = "user-123")

        // Then
        coVerify(exactly = 1) { turnoDao.cerrarTodosLosTurnosActivos(any()) }
        coVerify(exactly = 1) {
            turnoDao.insertTurno(match {
                it.usuarioId == "user-123" && it.estado == "ABIERTO"
            })
        }
        coVerify(exactly = 1) {
            rutaRepartidorDao.insertRutas(match { rutas ->
                rutas.size == 2 &&
                rutas[0].repartidorNombre == "Roberto Díaz" &&
                rutas[1].repartidorNombre == "Ana Gómez" &&
                rutas.all {
                    it.status == "PENDIENTE_SALIDA" &&
                    it.cargaInicialKg == 0.0 &&
                    it.pendienteCobro == 0.0
                }
            })
        }
    }

    @Test
    fun `al abrir turno sin repartidores registrados en Room, no inserta ninguna ruta ficticia`() = runTest {
        // Given
        coEvery { repartidorDao.getRepartidoresList() } returns emptyList()

        // When
        abrirTurnoUseCase(usuarioId = "user-123")

        // Then
        coVerify(exactly = 1) {
            turnoDao.insertTurno(match { it.usuarioId == "user-123" && it.estado == "ABIERTO" })
        }
        coVerify(exactly = 0) { rutaRepartidorDao.insertRutas(any()) }
    }
}
