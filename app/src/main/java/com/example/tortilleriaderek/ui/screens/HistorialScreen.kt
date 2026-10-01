package com.example.tortilleriaderek.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.ReceiptLong
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.tortilleriaderek.presentation.historial.HistorialFiltroTipo
import com.example.tortilleriaderek.presentation.historial.HistorialItemUi
import com.example.tortilleriaderek.presentation.historial.HistorialUiEvent
import com.example.tortilleriaderek.presentation.historial.HistorialUiState
import com.example.tortilleriaderek.presentation.historial.HistorialViewModel
import com.example.tortilleriaderek.ui.components.TortilleriaNavBar
import com.example.tortilleriaderek.ui.theme.*

@Composable
fun HistorialScreen(
    filtroInicial: String = "TODAS",
    viewModel: HistorialViewModel = hiltViewModel(),
    onBack: () -> Unit = {},
    onNavigateToProduccion: () -> Unit = {},
    onNavigateToMetricas: () -> Unit = {},
    onNavigateToConfiguracion: () -> Unit = {},
    modifier: Modifier = Modifier
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()

    LaunchedEffect(filtroInicial) {
        val tipo = when (filtroInicial.uppercase()) {
            "MOSTRADOR" -> HistorialFiltroTipo.MOSTRADOR
            "REPARTIDOR" -> HistorialFiltroTipo.REPARTIDOR
            else -> HistorialFiltroTipo.TODAS
        }
        viewModel.onEvent(HistorialUiEvent.SetFiltroInicial(tipo))
    }

    HistorialScreenContent(
        uiState = uiState,
        onEvent = viewModel::onEvent,
        onBack = onBack,
        onNavigateToProduccion = onNavigateToProduccion,
        onNavigateToMetricas = onNavigateToMetricas,
        onNavigateToConfiguracion = onNavigateToConfiguracion,
        modifier = modifier
    )
}

@Composable
fun HistorialScreenContent(
    uiState: HistorialUiState,
    onEvent: (HistorialUiEvent) -> Unit,
    onBack: () -> Unit = {},
    onNavigateToProduccion: () -> Unit = {},
    onNavigateToMetricas: () -> Unit = {},
    onNavigateToConfiguracion: () -> Unit = {},
    modifier: Modifier = Modifier
) {
    Scaffold(
        modifier = modifier.fillMaxSize(),
        containerColor = SurfaceWarm,
        contentWindowInsets = WindowInsets.navigationBars
    ) { innerPadding ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .padding(horizontal = 16.dp),
            contentPadding = PaddingValues(top = 12.dp, bottom = 20.dp),
            verticalArrangement = Arrangement.spacedBy(14.dp)
        ) {
            // ── 1. Top Header con botón volver circular y fecha del turno ──────
            item {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    Surface(
                        shape = CircleShape,
                        color = Color.White,
                        border = androidx.compose.foundation.BorderStroke(1.dp, BorderSubtle),
                        modifier = Modifier.size(40.dp)
                    ) {
                        IconButton(
                            onClick = onBack,
                            modifier = Modifier.fillMaxSize()
                        ) {
                            Icon(
                                imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                                contentDescription = "Volver",
                                tint = TextPrimary,
                                modifier = Modifier.size(18.dp)
                            )
                        }
                    }

                    Column(verticalArrangement = Arrangement.spacedBy(2.dp)) {
                        Text(
                            text = "Historial de Ventas",
                            fontSize = 18.sp,
                            fontWeight = FontWeight.Bold,
                            color = TextPrimary
                        )
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(4.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Default.CalendarToday,
                                contentDescription = null,
                                tint = TextSecondary,
                                modifier = Modifier.size(13.dp)
                            )
                            Text(
                                text = uiState.fechaTurnoTexto.ifBlank { "Hoy" },
                                fontSize = 12.sp,
                                fontWeight = FontWeight.Medium,
                                color = TextSecondary
                            )
                            if (uiState.isTurnoCerrado) {
                                Surface(
                                    shape = CircleShape,
                                    color = Color(0xFFF3F4F6)
                                ) {
                                    Text(
                                        text = "Cerrado",
                                        fontSize = 10.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = TextSecondary,
                                        modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                                    )
                                }
                            }
                        }
                    }
                }
            }

            // ── 2. Card de Resumen de Recaudación (Sin cajas de tarjeta/efectivo) ──
            item {
                Surface(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(22.dp),
                    color = Color.White,
                    border = androidx.compose.foundation.BorderStroke(1.dp, BorderSubtle.copy(alpha = 0.8f)),
                    shadowElevation = 1.dp
                ) {
                    Column(
                        modifier = Modifier.padding(18.dp),
                        verticalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.Top
                        ) {
                            val tituloRecaudacion = when (uiState.filtroSeleccionado) {
                                HistorialFiltroTipo.MOSTRADOR -> "TOTAL RECAUDADO EN MOSTRADOR"
                                HistorialFiltroTipo.REPARTIDOR -> "TOTAL RECAUDADO EN REPARTO"
                                HistorialFiltroTipo.TODAS -> "TOTAL RECAUDADO EN TURNO"
                            }

                            Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                                Text(
                                    text = tituloRecaudacion,
                                    fontSize = 11.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = TextSecondary,
                                    letterSpacing = 0.6.sp
                                )

                                val montoTotal = uiState.totalMostradoEnHeader
                                val entero = montoTotal.toInt()
                                val decimales = ((montoTotal - entero) * 100).toInt()

                                Row(verticalAlignment = Alignment.Bottom) {
                                    Text(
                                        text = "$%,d".format(entero),
                                        fontSize = 32.sp,
                                        fontWeight = FontWeight.Black,
                                        color = TextPrimary,
                                        letterSpacing = (-0.5).sp
                                    )
                                    Text(
                                        text = ".%02d".format(decimales),
                                        fontSize = 20.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = TextPrimary
                                    )
                                    Spacer(modifier = Modifier.width(5.dp))
                                    Text(
                                        text = "MXN",
                                        fontSize = 12.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = TextSecondary,
                                        modifier = Modifier.padding(bottom = 4.dp)
                                    )
                                }
                            }

                            // Badge: Conteo de ventas de negocio (Mostrador por ticket, Repartidor por liquidación)
                            Surface(
                                shape = CircleShape,
                                color = Color(0xFFFFEDE2)
                            ) {
                                Row(
                                    modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp),
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.spacedBy(5.dp)
                                ) {
                                    Box(
                                        modifier = Modifier
                                            .size(7.dp)
                                            .clip(CircleShape)
                                            .background(MaizPrimary)
                                    )
                                    val countVentas = uiState.numeroVentasMostradoEnHeader
                                    Text(
                                        text = if (countVentas == 1) "1 venta" else "$countVentas ventas",
                                        fontSize = 11.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = Color(0xFFC2410C),
                                        maxLines = 1,
                                        softWrap = false
                                    )
                                }
                            }
                        }
                    }
                }
            }

            // ── 3. Chips de Filtro (Orden dinámico según origen) ──
            item {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .horizontalScroll(rememberScrollState()),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    uiState.chipsOrden.forEach { tipo ->
                        val (label, count) = when (tipo) {
                            HistorialFiltroTipo.TODAS -> "Total de transacciones" to uiState.transaccionesTotalCount
                            HistorialFiltroTipo.MOSTRADOR -> "Venta Mostrador" to uiState.transaccionesMostradorCount
                            HistorialFiltroTipo.REPARTIDOR -> "Venta Repartidor" to uiState.transaccionesRepartidorCount
                        }
                        FilterTabChip(
                            label = "$label ($count)",
                            isSelected = uiState.filtroSeleccionado == tipo,
                            onClick = { onEvent(HistorialUiEvent.SelectFiltro(tipo)) }
                        )
                    }
                }
            }

            // ── 4. Cabecera de Lista de Transacciones ─────────────────────────
            item {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        Text(
                            text = "Transacciones",
                            fontSize = 15.sp,
                            fontWeight = FontWeight.Bold,
                            color = TextPrimary
                        )
                        Box(
                            modifier = Modifier
                                .size(7.dp)
                                .clip(CircleShape)
                                .background(MaizPrimary)
                        )
                    }

                    Text(
                        text = "Orden: Más reciente",
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Medium,
                        color = TextSecondary
                    )
                }
            }

            // ── 5. Items de Transacción o Estado Vacío ────────────────────────
            if (uiState.transaccionesFiltradas.isEmpty()) {
                item {
                    Surface(
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(16.dp),
                        color = Color.White,
                        border = androidx.compose.foundation.BorderStroke(1.dp, BorderSubtle)
                    ) {
                        Column(
                            modifier = Modifier.padding(28.dp),
                            horizontalAlignment = Alignment.CenterHorizontally,
                            verticalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            Icon(
                                imageVector = Icons.AutoMirrored.Filled.ReceiptLong,
                                contentDescription = null,
                                tint = TextSecondary,
                                modifier = Modifier.size(36.dp)
                            )
                            Text(
                                text = "Sin transacciones registradas",
                                fontSize = 14.sp,
                                fontWeight = FontWeight.Bold,
                                color = TextPrimary
                            )
                            Text(
                                text = "No se encontraron ventas para el filtro seleccionado.",
                                fontSize = 12.sp,
                                color = TextSecondary,
                                textAlign = androidx.compose.ui.text.style.TextAlign.Center
                            )
                        }
                    }
                }
            } else {
                items(
                    items = uiState.transaccionesFiltradas,
                    key = { it.id }
                ) { tx ->
                    TransactionCardItem(
                        tx = tx,
                        onDeleteClick = if (!uiState.isTurnoCerrado) {
                            { onEvent(HistorialUiEvent.SolicitarEliminarVenta(tx)) }
                        } else null
                    )
                }
            }
        }
    }

    // Modal de confirmación para eliminar venta
    uiState.transaccionAEliminar?.let { tx ->
        AlertDialog(
            onDismissRequest = { onEvent(HistorialUiEvent.CancelarEliminarVenta) },
            shape = RoundedCornerShape(20.dp),
            containerColor = Color.White,
            title = {
                Text(
                    text = "Eliminar Ticket ${tx.ticketFolio}",
                    fontWeight = FontWeight.Bold,
                    fontSize = 17.sp,
                    color = Color(0xFF111827)
                )
            },
            text = {
                Text(
                    text = "¿Estás seguro de que deseas eliminar esta venta de $%,.2f MXN registrada a las %s?"
                        .format(tx.monto, tx.hora),
                    fontSize = 14.sp,
                    color = Color(0xFF374151)
                )
            },
            confirmButton = {
                Button(
                    onClick = { onEvent(HistorialUiEvent.ConfirmarEliminarVenta) },
                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFDC2626)),
                    shape = CircleShape
                ) {
                    Text("Sí, Eliminar", fontWeight = FontWeight.Bold, color = Color.White)
                }
            },
            dismissButton = {
                OutlinedButton(
                    onClick = { onEvent(HistorialUiEvent.CancelarEliminarVenta) },
                    shape = CircleShape,
                    border = androidx.compose.foundation.BorderStroke(1.dp, Color(0xFFCBD5E1))
                ) {
                    Text("Cancelar", color = Color(0xFF374151), fontWeight = FontWeight.Bold)
                }
            }
        )
    }
}

@Composable
private fun FilterTabChip(
    label: String,
    isSelected: Boolean,
    onClick: () -> Unit
) {
    Surface(
        shape = CircleShape,
        color = if (isSelected) MaizPrimary else Color.White,
        border = if (isSelected) null else androidx.compose.foundation.BorderStroke(1.dp, BorderSubtle),
        modifier = Modifier.clickable { onClick() }
    ) {
        Text(
            text = label,
            fontSize = 12.sp,
            fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium,
            color = if (isSelected) Color.White else TextSecondary,
            maxLines = 1,
            softWrap = false,
            modifier = Modifier.padding(horizontal = 14.dp, vertical = 7.dp)
        )
    }
}

@Composable
private fun TransactionCardItem(
    tx: HistorialItemUi,
    onDeleteClick: (() -> Unit)? = null,
    modifier: Modifier = Modifier
) {
    Surface(
        modifier = modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        color = Color.White,
        border = androidx.compose.foundation.BorderStroke(1.dp, BorderSubtle.copy(alpha = 0.7f)),
        shadowElevation = 0.5.dp
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 14.dp, vertical = 12.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Izquierda: Folio + Fecha/Hora + Detalle / Repartidor
            Column(
                modifier = Modifier
                    .weight(1f)
                    .padding(end = 8.dp),
                verticalArrangement = Arrangement.spacedBy(4.dp)
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    Text(
                        text = "#${tx.ticketFolio}",
                        fontSize = 14.sp,
                        fontWeight = FontWeight.Bold,
                        color = TextPrimary,
                        maxLines = 1,
                        softWrap = false
                    )

                    // Fecha y hora de venta
                    Surface(
                        shape = RoundedCornerShape(6.dp),
                        color = Color(0xFFF4F0EB)
                    ) {
                        Text(
                            text = "${tx.fechaTexto} • ${tx.hora}",
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Medium,
                            color = TextSecondary,
                            maxLines = 1,
                            softWrap = false,
                            modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                        )
                    }

                    // Tipo de venta badge pequeño (con softWrap = false y maxLines = 1 para que nunca parta 'Mostrador' ni baje la 'r')
                    val isReparto = tx.tipo == "REPARTIDOR"
                    Surface(
                        shape = RoundedCornerShape(6.dp),
                        color = if (isReparto) Color(0xFFFFF0E5) else Color(0xFFEFF6FF)
                    ) {
                        Text(
                            text = if (isReparto) "Reparto" else "Mostrador",
                            fontSize = 10.sp,
                            fontWeight = FontWeight.Bold,
                            color = if (isReparto) MaizPrimary else Color(0xFF2563EB),
                            maxLines = 1,
                            softWrap = false,
                            modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                        )
                    }
                }

                // Detalle de la transacción
                if (tx.tipo == "REPARTIDOR") {
                    Surface(
                        shape = RoundedCornerShape(8.dp),
                        color = Color(0xFFFFF3EB),
                        border = androidx.compose.foundation.BorderStroke(1.dp, Color(0xFFFFD7BF))
                    ) {
                        Row(
                            modifier = Modifier.padding(horizontal = 7.dp, vertical = 2.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(4.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Default.TwoWheeler,
                                contentDescription = null,
                                tint = MaizPrimary,
                                modifier = Modifier.size(12.dp)
                            )
                            val infoRepartidor = listOfNotNull(
                                tx.nombreRepartidor,
                                tx.motoAsignada,
                                tx.nombreRuta
                            ).joinToString(" • ").ifBlank { tx.detalleProductos }
                            Text(
                                text = infoRepartidor,
                                fontSize = 11.sp,
                                fontWeight = FontWeight.Bold,
                                color = Color(0xFFC04B00),
                                maxLines = 1,
                                overflow = androidx.compose.ui.text.style.TextOverflow.Ellipsis
                            )
                        }
                    }
                } else {
                    if (tx.detalleProductos.isNotBlank()) {
                        Text(
                            text = tx.detalleProductos,
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Normal,
                            color = TextSecondary,
                            maxLines = 1,
                            overflow = androidx.compose.ui.text.style.TextOverflow.Ellipsis
                        )
                    }
                }
            }

            // Derecha: Total de la venta y botón de eliminar opcional
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(6.dp)
            ) {
                val entero = tx.monto.toInt()
                val decimales = ((tx.monto - entero) * 100).toInt()

                Row(
                    verticalAlignment = Alignment.Bottom,
                    modifier = Modifier.padding(vertical = 2.dp)
                ) {
                    Text(
                        text = "$%,d".format(entero),
                        fontSize = 17.sp,
                        fontWeight = FontWeight.ExtraBold,
                        color = if (tx.isHighlighted) MaizPrimary else TextPrimary,
                        maxLines = 1,
                        softWrap = false
                    )
                    Text(
                        text = ".%02d".format(decimales),
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Bold,
                        color = if (tx.isHighlighted) MaizPrimary else TextSecondary,
                        modifier = Modifier.padding(bottom = 1.dp),
                        maxLines = 1,
                        softWrap = false
                    )
                }

                if (onDeleteClick != null) {
                    IconButton(
                        onClick = onDeleteClick,
                        modifier = Modifier.size(28.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.DeleteOutline,
                            contentDescription = "Eliminar venta",
                            tint = Color(0xFFEF4444),
                            modifier = Modifier.size(16.dp)
                        )
                    }
                }
            }
        }
    }
}

@Preview(showBackground = true, showSystemUi = true)
@Composable
fun PreviewHistorialScreen() {
    TortilleriaDerekTheme {
        HistorialScreenContent(
            uiState = HistorialUiState(
                fechaTurnoTexto = "Hoy, Lunes 7 de Septiembre",
                totalVentasTurno = 18450.00,
                numeroVentasTotal = 143,
                transaccionesTotalCount = 147,
                transaccionesMostradorCount = 142,
                transaccionesRepartidorCount = 5,
                transaccionesFiltradas = listOf(
                    HistorialItemUi(
                        id = "1",
                        ticketFolio = "M-0142",
                        tipo = "MOSTRADOR",
                        fechaTimestamp = System.currentTimeMillis(),
                        fechaTexto = "Hoy",
                        hora = "11:42 AM",
                        monto = 48.00,
                        detalleProductos = "2 Kilo de Tortilla",
                        usuarioNombre = "admin1"
                    ),
                    HistorialItemUi(
                        id = "2",
                        ticketFolio = "R-0038",
                        tipo = "REPARTIDOR",
                        fechaTimestamp = System.currentTimeMillis(),
                        fechaTexto = "Hoy",
                        hora = "10:30 AM",
                        monto = 1870.00,
                        detalleProductos = "Moto 01 (Carlos Ruiz) - Liquidación",
                        usuarioNombre = "admin1",
                        nombreRepartidor = "Carlos Ruiz",
                        motoAsignada = "Moto 01",
                        nombreRuta = "Ruta San Juan"
                    )
                )
            ),
            onEvent = {}
        )
    }
}
