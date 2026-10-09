package com.example.tortilleriaderek.data.local.entity

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "turnos")
data class TurnoEntity(
    @PrimaryKey val id: String,
    val folioCorte: String = "",
    val fechaDiaTexto: String = "",
    val numeroTurnoDia: Int = 1,
    val fechaApertura: Long,
    val fechaCierre: Long? = null,
    val estado: String, // "ABIERTO", "CERRADO"
    val usuarioId: String,
    val usuarioCierreId: String? = null,
    val fondoInicial: Double = 0.0,
    val totalVentasMostrador: Double = 0.0,
    val totalCobradoReparto: Double = 0.0,
    val totalVentas: Double = 0.0,
    val efectivoEsperado: Double = 0.0,
    val efectivoContado: Double = 0.0,
    val diferenciaArqueo: Double = 0.0,
    val notasCierre: String? = null
)
