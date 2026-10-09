package com.example.tortilleriaderek.ui.components.historial

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.tortilleriaderek.presentation.historial.HistorialFiltroTipo
import com.example.tortilleriaderek.ui.theme.*

/**
 * Píldoras de filtrado horizontal por canal de venta (Modo de Diseño).
 */
@Composable
fun HistorialFilterChips(
    chipsOrden: List<HistorialFiltroTipo>,
    filtroSeleccionado: HistorialFiltroTipo,
    transaccionesTotalCount: Int,
    transaccionesMostradorCount: Int,
    transaccionesRepartidorCount: Int,
    onSelectFiltro: (HistorialFiltroTipo) -> Unit,
    modifier: Modifier = Modifier
) {
    Row(
        modifier = modifier
            .fillMaxWidth()
            .horizontalScroll(rememberScrollState()),
        horizontalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        chipsOrden.forEach { tipo ->
            val label = when (tipo) {
                HistorialFiltroTipo.TODAS -> androidx.compose.ui.res.stringResource(com.example.tortilleriaderek.R.string.historial_filtro_todas)
                HistorialFiltroTipo.MOSTRADOR -> androidx.compose.ui.res.stringResource(com.example.tortilleriaderek.R.string.historial_filtro_mostrador)
                HistorialFiltroTipo.REPARTIDOR -> androidx.compose.ui.res.stringResource(com.example.tortilleriaderek.R.string.historial_filtro_repartidores)
            }
            val count = when (tipo) {
                HistorialFiltroTipo.TODAS -> transaccionesTotalCount
                HistorialFiltroTipo.MOSTRADOR -> transaccionesMostradorCount
                HistorialFiltroTipo.REPARTIDOR -> transaccionesRepartidorCount
            }
            val isSelected = filtroSeleccionado == tipo

            Surface(
                shape = CircleShape,
                color = if (isSelected) MaizPrimary else Color.White,
                border = if (isSelected) null else BorderStroke(1.dp, BorderSubtle),
                modifier = Modifier
                    .clickable { onSelectFiltro(tipo) }
                    .testTag("filtro_chip_${tipo.name.lowercase()}")
            ) {
                Text(
                    text = "$label ($count)",
                    fontSize = 13.sp,
                    fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium,
                    color = if (isSelected) Color.White else TextSecondary,
                    maxLines = 1,
                    softWrap = false,
                    style = androidx.compose.ui.text.TextStyle(fontFeatureSettings = "tnum"),
                    modifier = Modifier.padding(horizontal = 16.dp, vertical = 10.dp)
                )
            }
        }
    }
}

@Preview(showBackground = true)
@Composable
fun HistorialFilterChipsPreview() {
    TortilleriaDerekTheme {
        HistorialFilterChips(
            chipsOrden = listOf(HistorialFiltroTipo.TODAS, HistorialFiltroTipo.MOSTRADOR, HistorialFiltroTipo.REPARTIDOR),
            filtroSeleccionado = HistorialFiltroTipo.TODAS,
            transaccionesTotalCount = 147,
            transaccionesMostradorCount = 142,
            transaccionesRepartidorCount = 5,
            onSelectFiltro = {}
        )
    }
}
