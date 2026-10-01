package com.example.tortilleriaderek.domain.usecase

import com.example.tortilleriaderek.data.local.dao.RepartidorDao
import com.example.tortilleriaderek.data.local.dao.RutaRepartidorDao
import com.example.tortilleriaderek.data.local.dao.TurnoDao
import com.example.tortilleriaderek.data.local.entity.RutaRepartidorEntity
import com.example.tortilleriaderek.data.local.entity.TurnoEntity
import java.util.UUID
import javax.inject.Inject

/**
 * Creado por 🏗️ mobile-developer y 🛡️ security-expert.
 * Genera el turno asociado al usuario e inicializa limpias las rutas de repartidores
 * con status PENDIENTE_SALIDA y carga en 0.0 kg basándose en el catálogo dinámico de Room.
 */
class AbrirTurnoUseCase @Inject constructor(
    private val turnoDao: TurnoDao,
    private val rutaRepartidorDao: RutaRepartidorDao,
    private val repartidorDao: RepartidorDao
) {
    suspend operator fun invoke(usuarioId: String) {
        val ahora = System.currentTimeMillis()
        // Cierra cualquier turno anterior que haya quedado huérfano en estado ABIERTO
        turnoDao.cerrarTodosLosTurnosActivos(ahora)
        val turnoId = UUID.randomUUID().toString()
        val turno = TurnoEntity(
            id = turnoId,
            fechaApertura = ahora,
            estado = "ABIERTO",
            usuarioId = usuarioId
        )
        turnoDao.insertTurno(turno)

        // Inicializar rutas únicamente para los repartidores registrados en Room
        val repartidores = repartidorDao.getRepartidoresList()
        if (repartidores.isNotEmpty()) {
            val rutasIniciales = repartidores.mapIndexed { index, rep ->
                val motoNombre = if (rep.moto.isNotBlank()) {
                    rep.moto
                } else if (rep.detalleRuta.contains("•")) {
                    rep.detalleRuta.substringBefore("•").trim()
                } else {
                    "Moto %02d".format(index + 1)
                }

                val rutaNombre = if (rep.detalleRuta.contains("•")) {
                    rep.detalleRuta.substringAfter("•").trim()
                } else {
                    rep.detalleRuta.ifBlank { "Ruta General" }
                }

                RutaRepartidorEntity(
                    id = "${turnoId}_${rep.id}",
                    turnoId = turnoId,
                    moto = motoNombre,
                    repartidorNombre = rep.nombre,
                    nombreRuta = rutaNombre,
                    status = "PENDIENTE_SALIDA",
                    cargaInicialKg = 0.0,
                    pendienteCobro = 0.00
                )
            }
            rutaRepartidorDao.insertRutas(rutasIniciales)
        }
    }
}
