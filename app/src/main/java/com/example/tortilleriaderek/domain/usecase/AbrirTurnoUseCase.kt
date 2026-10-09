package com.example.tortilleriaderek.domain.usecase

import com.example.tortilleriaderek.data.local.dao.RepartidorDao
import com.example.tortilleriaderek.data.local.dao.RutaRepartidorDao
import com.example.tortilleriaderek.data.local.dao.TurnoDao
import com.example.tortilleriaderek.data.local.entity.RutaRepartidorEntity
import com.example.tortilleriaderek.data.local.entity.TurnoEntity
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import java.util.UUID
import javax.inject.Inject

/**
 * Creado por 🏗️ mobile-developer y 🛡️ security-expert.
 * Genera el turno correlativo (folio CORTE-YYYYMMDD-0X), inicializa el fondo de caja
 * y prepara las rutas de reparto dinámicas basándose en el catálogo de Room.
 */
class AbrirTurnoUseCase @Inject constructor(
    private val turnoDao: TurnoDao,
    private val rutaRepartidorDao: RutaRepartidorDao,
    private val repartidorDao: RepartidorDao
) {
    suspend operator fun invoke(usuarioId: String, fondoInicial: Double = 0.0) {
        val ahora = System.currentTimeMillis()
        val formatoDia = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())
        val formatoCompacto = SimpleDateFormat("yyyyMMdd", Locale.getDefault())
        val fechaDate = Date(ahora)
        val fechaDiaTexto = formatoDia.format(fechaDate)
        val fechaCompacta = formatoCompacto.format(fechaDate)

        // Cierra cualquier turno anterior que haya quedado huérfano en estado ABIERTO
        turnoDao.cerrarTodosLosTurnosActivos(ahora)

        // Calcular número correlativo del turno en el día
        val siguienteNumero = turnoDao.getSiguienteNumeroTurnoDia(fechaDiaTexto)
        val folio = "CORTE-$fechaCompacta-%02d".format(siguienteNumero)

        val turnoId = UUID.randomUUID().toString()
        val turno = TurnoEntity(
            id = turnoId,
            folioCorte = folio,
            fechaDiaTexto = fechaDiaTexto,
            numeroTurnoDia = siguienteNumero,
            fechaApertura = ahora,
            estado = "ABIERTO",
            usuarioId = usuarioId,
            fondoInicial = fondoInicial,
            efectivoEsperado = fondoInicial
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
