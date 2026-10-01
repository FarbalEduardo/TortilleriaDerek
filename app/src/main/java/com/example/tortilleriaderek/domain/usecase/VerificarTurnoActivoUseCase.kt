package com.example.tortilleriaderek.domain.usecase

import com.example.tortilleriaderek.data.local.dao.TurnoDao
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import javax.inject.Inject

/**
 * Creado por 🏗️ mobile-developer.
 * Comprueba si existe un turno activo en Room (estado == "ABIERTO")
 * para determinar si la aplicación debe iniciar en Mostrador o en Login.
 */
class VerificarTurnoActivoUseCase @Inject constructor(
    private val turnoDao: TurnoDao
) {
    suspend operator fun invoke(): Boolean {
        val turnoActivo = turnoDao.getTurnoActivoSync()
        return turnoActivo != null && turnoActivo.estado == "ABIERTO"
    }

    fun observar(): Flow<Boolean> {
        return turnoDao.getTurnoActivo().map { it != null && it.estado == "ABIERTO" }
    }
}
