package com.example.tortilleriaderek.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowForward
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.tortilleriaderek.data.local.entity.RutaRepartidorEntity
import com.example.tortilleriaderek.presentation.venta.VentaUiState
import com.example.tortilleriaderek.ui.components.DialogLiquidarRepartidor
import com.example.tortilleriaderek.ui.components.DialogSalidaRepartidor
import com.example.tortilleriaderek.ui.components.HeroSalesCard
import com.example.tortilleriaderek.ui.theme.*

@Composable
fun RepartidoresScreen(
    uiState: VentaUiState = VentaUiState(),
    onSwitchToMostrador: () -> Unit = {},
    onScopeSelected: (String) -> Unit = {},
    onCerrarTurnoClick: () -> Unit = {},
    onOpenSalida: (RutaRepartidorEntity) -> Unit = {},
    onOpenLiquidar: (RutaRepartidorEntity) -> Unit = {},
    onConfirmarSalida: (Double) -> Unit = { _ -> },
    onConfirmarLiquidar: (Double, Double) -> Unit = { _, _ -> },
    onConfirmarSalidaConPaquetes: ((Double, Int) -> Unit)? = null,
    onConfirmarLiquidarConPaquetes: ((Double, Double, Int) -> Unit)? = null,
    onDismissSalida: () -> Unit = {},
    onDismissLiquidar: () -> Unit = {},
    onNavigateToHistorial: () -> Unit = {},
    modifier: Modifier = Modifier
) {
    var detalleLiquidadoModal by remember { mutableStateOf<RutaRepartidorEntity?>(null) }

    Column(
        modifier = modifier
            .fillMaxSize()
            .background(SurfaceWarm)
            .padding(horizontal = 16.dp)
            .padding(top = 12.dp),
        verticalArrangement = Arrangement.spacedBy(14.dp)
    ) {
        // ── 1. Hero Card Naranja ─────────────────────────────────────────────
        HeroSalesCard(
            fechaTexto = uiState.fechaTurnoTexto,
            totalAmount = uiState.totalMostradoEnHeader,
            currentScope = uiState.selectedScope,
            onScopeSelected = onScopeSelected,
            onCerrarTurnoClick = onCerrarTurnoClick
        )

        // ── 2. Selector Segmentado: Mostrador / Repartidores ─────────────────
        MostradorRepartidoresTabBar(
            selectedTab = 1,
            onMostradorSelected = onSwitchToMostrador,
            onRepartidoresSelected = { /* ya en esta pantalla */ }
        )

        // ── 3. Contenido de Repartidores ─────────────────────────────────────
        RepartidoresSubContent(
            uiState = uiState,
            onOpenSalida = onOpenSalida,
            onOpenLiquidar = onOpenLiquidar,
            onShowDetalleLiquidado = { detalleLiquidadoModal = it },
            onNavigateToHistorial = onNavigateToHistorial,
            modifier = Modifier.weight(1f)
        )
    }

    // ── Diálogo: Ingresar Datos de Salida ────────────────────────────────────
    uiState.repartidorSeleccionadoParaSalida?.let { driver ->
        DialogSalidaRepartidor(
            driver = driver,
            onConfirmarSalida = onConfirmarSalida,
            onDismiss = onDismissSalida,
            precioPaqueteRepartidor = uiState.precioPaqueteRepartidor,
            pesoPaqueteRepartidorKg = uiState.pesoPaqueteRepartidorKg,
            onConfirmarSalidaConPaquetes = onConfirmarSalidaConPaquetes
        )
    }

    // ── Diálogo: Registrar Devolución y Liquidar ─────────────────────────────
    uiState.repartidorSeleccionadoParaLiquidar?.let { driver ->
        DialogLiquidarRepartidor(
            driver = driver,
            onConfirmarLiquidar = onConfirmarLiquidar,
            onDismiss = onDismissLiquidar,
            precioPaqueteRepartidor = uiState.precioPaqueteRepartidor,
            pesoPaqueteRepartidorKg = uiState.pesoPaqueteRepartidorKg,
            onConfirmarLiquidarConPaquetes = onConfirmarLiquidarConPaquetes
        )
    }

    // ── Diálogo: Detalle de Liquidación ──────────────────────────────────────
    detalleLiquidadoModal?.let { driver ->
        AlertDialog(
            onDismissRequest = { detalleLiquidadoModal = null },
            shape = RoundedCornerShape(22.dp),
            containerColor = Color.White,
            titleContentColor = Color(0xFF111827),
            textContentColor = Color(0xFF111827),
            title = {
                Text(
                    text = "Detalle de Liquidación • ${driver.moto}",
                    fontWeight = FontWeight.Bold,
                    color = Color(0xFF111827)
                )
            },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                    Text("• Repartidor: ${driver.repartidorNombre}", color = Color(0xFF374151))
                    Text("• Folio de liquidación: ${driver.folioTicket ?: "R-0038"}", fontWeight = FontWeight.Bold, color = MaizPrimary)
                    Text("• Kilos entregados/vendidos: ${driver.entregadoKg} kg", color = Color(0xFF111827))
                    Text("• Kilos devueltos: ${driver.devolucionKg} kg", color = Color(0xFF111827))
                    Text("• Total liquidado en caja: $%,.2f MXN".format(driver.cobrado), fontWeight = FontWeight.ExtraBold, color = VerdeAgave)
                }
            },
            confirmButton = {
                Button(
                    onClick = { detalleLiquidadoModal = null },
                    colors = ButtonDefaults.buttonColors(containerColor = MaizPrimary),
                    shape = CircleShape
                ) {
                    Text("Aceptar", fontWeight = FontWeight.Bold, color = Color.White)
                }
            }
        )
    }
}

// ─────────────────────────────────────────────────────────────────────────────
// Subcontenido de Repartidores (Solo esta sección se mueve en el Pager)
// ─────────────────────────────────────────────────────────────────────────────
@Composable
fun RepartidoresSubContent(
    uiState: VentaUiState,
    onOpenSalida: (RutaRepartidorEntity) -> Unit,
    onOpenLiquidar: (RutaRepartidorEntity) -> Unit,
    onShowDetalleLiquidado: (RutaRepartidorEntity) -> Unit,
    onCancelarSalida: (RutaRepartidorEntity) -> Unit = {},
    onNavigateToHistorial: () -> Unit,
    modifier: Modifier = Modifier
) {
    var rutaACancelar by remember { mutableStateOf<RutaRepartidorEntity?>(null) }

    Column(
        modifier = modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(bottom = 20.dp),
        verticalArrangement = Arrangement.spacedBy(14.dp)
    ) {
        // Cabecera de sección + Rutas Activas Badge
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = "Control de Repartidores (Mayoreo)",
                fontSize = 16.sp,
                fontWeight = FontWeight.Bold,
                color = TextPrimary
            )

            // Badge suave: • 4 Rutas Activas
            Surface(
                shape = CircleShape,
                color = Color(0xFFFFEDE2)
            ) {
                Row(
                    modifier = Modifier.padding(horizontal = 10.dp, vertical = 5.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(5.dp)
                ) {
                    Box(
                        modifier = Modifier
                            .size(6.dp)
                            .clip(CircleShape)
                            .background(MaizPrimary)
                    )
                    Text(
                        text = "${uiState.rutasActivasCount} Rutas Activas",
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color(0xFFC2410C)
                    )
                }
            }
        }

        // Lista de Tarjetas de Repartidores
        if (uiState.rutasRepartidores.isEmpty()) {
            Surface(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(12.dp),
                color = Color.White,
                border = androidx.compose.foundation.BorderStroke(1.dp, BorderSubtle)
            ) {
                Column(
                    modifier = Modifier.padding(24.dp),
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.TwoWheeler,
                        contentDescription = null,
                        tint = TextSecondary,
                        modifier = Modifier.size(36.dp)
                    )
                    Text(
                        text = "No hay repartidores en este turno",
                        fontWeight = FontWeight.Bold,
                        fontSize = 14.sp,
                        color = TextPrimary
                    )
                    Text(
                        text = "Agrega repartidores desde la pantalla de Configuración para asignarlos a las rutas.",
                        fontSize = 12.sp,
                        color = TextSecondary,
                        textAlign = androidx.compose.ui.text.style.TextAlign.Center
                    )
                }
            }
        } else {
            uiState.rutasRepartidores.forEach { ruta ->
                RepartidorCardItem(
                    ruta = ruta,
                    onClick = {
                        when (ruta.status) {
                            "EN_RUTA" -> onOpenLiquidar(ruta)
                            "PENDIENTE_SALIDA" -> onOpenSalida(ruta)
                            "LIQUIDADO" -> onShowDetalleLiquidado(ruta)
                        }
                    },
                    onCancelarSalida = {
                        rutaACancelar = ruta
                    }
                )
            }
        }

        // Botón secundario: Historial de Ventas Repartidores
        OutlinedButton(
            onClick = onNavigateToHistorial,
            modifier = Modifier
                .fillMaxWidth()
                .height(48.dp),
            shape = CircleShape,
            border = androidx.compose.foundation.BorderStroke(1.dp, BorderSubtle),
            colors = ButtonDefaults.outlinedButtonColors(containerColor = Color.White)
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Icon(
                    imageVector = Icons.Default.History,
                    contentDescription = null,
                    tint = TextSecondary,
                    modifier = Modifier.size(18.dp)
                )
                Text(
                    text = "Historial de Ventas (Repartidores)",
                    fontSize = 13.sp,
                    fontWeight = FontWeight.SemiBold,
                    color = TextSecondary
                )
            }
        }

        Spacer(modifier = Modifier.height(8.dp))
    }

    // Diálogo Modal para Confirmar Cancelación de Salida
    rutaACancelar?.let { ruta ->
        AlertDialog(
            onDismissRequest = { rutaACancelar = null },
            shape = RoundedCornerShape(20.dp),
            containerColor = Color.White,
            titleContentColor = Color(0xFF111827),
            textContentColor = Color(0xFF111827),
            title = {
                Text(
                    text = "Cancelar Salida a Ruta",
                    fontWeight = FontWeight.Bold,
                    fontSize = 17.sp,
                    color = Color(0xFF111827)
                )
            },
            text = {
                Text(
                    text = "¿Deseas cancelar la salida de ${ruta.moto} (${ruta.repartidorNombre})?\n\nLa carga de %.1f kg (%d paquetes) se restituirá de inmediato a la tortilla disponible en tienda."
                        .format(ruta.cargaInicialKg, ruta.paquetesCargados),
                    fontSize = 14.sp,
                    color = Color(0xFF374151),
                    lineHeight = 20.sp
                )
            },
            confirmButton = {
                Button(
                    onClick = {
                        onCancelarSalida(ruta)
                        rutaACancelar = null
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFDC2626)),
                    shape = CircleShape
                ) {
                    Text("Sí, Cancelar Salida", fontWeight = FontWeight.Bold, color = Color.White)
                }
            },
            dismissButton = {
                OutlinedButton(
                    onClick = { rutaACancelar = null },
                    shape = CircleShape,
                    border = androidx.compose.foundation.BorderStroke(1.dp, Color(0xFFCBD5E1))
                ) {
                    Text("No, Mantener en Ruta", color = Color(0xFF374151), fontWeight = FontWeight.Bold)
                }
            }
        )
    }
}

@Composable
private fun RepartidorCardItem(
    ruta: RutaRepartidorEntity,
    onClick: () -> Unit,
    onCancelarSalida: () -> Unit = {}
) {
    Surface(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(20.dp))
            .border(1.dp, BorderSubtle.copy(alpha = 0.8f), RoundedCornerShape(20.dp))
            .clickable { onClick() },
        color = Color.White,
        shadowElevation = 1.dp
    ) {
        Column(
            modifier = Modifier.padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            // Fila Superior: Icono + Título + Status Badge
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    val (iconBg, iconColor, iconVector) = when (ruta.status) {
                        "EN_RUTA" -> Triple(Color(0xFFFFF0E5), MaizPrimary, Icons.Default.TwoWheeler)
                        "PENDIENTE_SALIDA" -> Triple(Color(0xFFEDF2F7), Color(0xFF64748B), Icons.Default.DirectionsCar)
                        else -> Triple(Color(0xFFE6F4EA), VerdeAgave, Icons.Default.CheckCircle)
                    }

                    Box(
                        modifier = Modifier
                            .size(38.dp)
                            .clip(CircleShape)
                            .background(iconBg),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = iconVector,
                            contentDescription = null,
                            tint = iconColor,
                            modifier = Modifier.size(20.dp)
                        )
                    }

                    Column(verticalArrangement = Arrangement.spacedBy(1.dp)) {
                        Text(
                            text = "${ruta.moto} – ${ruta.repartidorNombre}",
                            fontSize = 14.sp,
                            fontWeight = FontWeight.Bold,
                            color = TextPrimary
                        )
                        Text(
                            text = ruta.nombreRuta,
                            fontSize = 11.sp,
                            color = TextSecondary
                        )
                    }
                }

                // Badge de estado (En Ruta, Pendiente de Salida, Liquidado)
                when (ruta.status) {
                    "EN_RUTA" -> {
                        Surface(
                            shape = CircleShape,
                            color = Color(0xFFFFEDE2)
                        ) {
                            Row(
                                modifier = Modifier.padding(horizontal = 9.dp, vertical = 4.dp),
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(4.dp)
                            ) {
                                Box(
                                    modifier = Modifier
                                        .size(5.dp)
                                        .clip(CircleShape)
                                        .background(MaizPrimary)
                                )
                                Text(
                                    text = "En Ruta",
                                    fontSize = 11.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = Color(0xFFC2410C)
                                )
                            }
                        }
                    }
                    "PENDIENTE_SALIDA" -> {
                        Surface(
                            shape = CircleShape,
                            color = Color(0xFFEFF3F8)
                        ) {
                            Row(
                                modifier = Modifier.padding(horizontal = 9.dp, vertical = 4.dp),
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(4.dp)
                            ) {
                                Box(
                                    modifier = Modifier
                                        .size(5.dp)
                                        .clip(CircleShape)
                                        .background(Color(0xFF64748B))
                                )
                                Text(
                                    text = "Pendiente de Salida",
                                    fontSize = 11.sp,
                                    fontWeight = FontWeight.Medium,
                                    color = Color(0xFF475569)
                                )
                            }
                        }
                    }
                    else -> {
                        Surface(
                            shape = CircleShape,
                            color = Color(0xFFE6F4EA)
                        ) {
                            Row(
                                modifier = Modifier.padding(horizontal = 9.dp, vertical = 4.dp),
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(3.dp)
                            ) {
                                Icon(
                                    imageVector = Icons.Default.Check,
                                    contentDescription = null,
                                    tint = VerdeAgave,
                                    modifier = Modifier.size(12.dp)
                                )
                                Text(
                                    text = "Liquidado",
                                    fontSize = 11.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = VerdeAgave
                                )
                            }
                        }
                    }
                }
            }

            // Fila Media: Métricas en caja gris tenue
            Surface(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(12.dp),
                color = Color(0xFFF9F7F4),
                border = androidx.compose.foundation.BorderStroke(1.dp, BorderLight)
            ) {
                when (ruta.status) {
                    "EN_RUTA" -> {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(horizontal = 14.dp, vertical = 10.dp),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Column(verticalArrangement = Arrangement.spacedBy(2.dp)) {
                                Text(
                                    text = "CARGA INICIAL",
                                    fontSize = 10.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = TextSecondary,
                                    letterSpacing = 0.5.sp
                                )
                                Text(
                                    text = if (ruta.paquetesCargados > 0) "${ruta.paquetesCargados} paq (%.1f kg)".format(ruta.cargaInicialKg) else "${ruta.cargaInicialKg.toInt()} kg",
                                    fontSize = 14.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = TextPrimary
                                )
                            }

                            Column(
                                horizontalAlignment = Alignment.End,
                                verticalArrangement = Arrangement.spacedBy(2.dp)
                            ) {
                                Text(
                                    text = "PENDIENTE POR COBRAR",
                                    fontSize = 10.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = TextSecondary,
                                    letterSpacing = 0.5.sp
                                )
                                Text(
                                    text = "$%,.2f MXN".format(ruta.pendienteCobro),
                                    fontSize = 14.sp,
                                    fontWeight = FontWeight.ExtraBold,
                                    color = MaizPrimary
                                )
                            }
                        }
                    }
                    "PENDIENTE_SALIDA" -> {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(horizontal = 14.dp, vertical = 10.dp),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(6.dp)
                            ) {
                                Icon(
                                    imageVector = Icons.Default.HourglassEmpty,
                                    contentDescription = null,
                                    tint = TextSecondary,
                                    modifier = Modifier.size(15.dp)
                                )
                                Text(
                                    text = "Sin carga asignada hoy",
                                    fontSize = 12.sp,
                                    fontWeight = FontWeight.Medium,
                                    color = TextSecondary
                                )
                            }
                            Text(
                                text = "0.0 KG",
                                fontSize = 12.sp,
                                fontWeight = FontWeight.Bold,
                                color = TextSecondary
                            )
                        }
                    }
                    else -> {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(horizontal = 14.dp, vertical = 10.dp),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Column(verticalArrangement = Arrangement.spacedBy(2.dp)) {
                                Text(
                                    text = "ENTREGADO",
                                    fontSize = 10.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = TextSecondary,
                                    letterSpacing = 0.5.sp
                                )
                                val paqEntregados = (ruta.paquetesCargados - ruta.paquetesDevueltos).coerceAtLeast(0)
                                Text(
                                    text = if (paqEntregados > 0) "$paqEntregados paq (%.1f kg)".format(ruta.entregadoKg) else "${ruta.entregadoKg.toInt()} kg",
                                    fontSize = 14.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = TextPrimary
                                )
                            }

                            Column(
                                horizontalAlignment = Alignment.End,
                                verticalArrangement = Arrangement.spacedBy(2.dp)
                            ) {
                                Text(
                                    text = "COBRADO",
                                    fontSize = 10.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = TextSecondary,
                                    letterSpacing = 0.5.sp
                                )
                                Text(
                                    text = "$%,.2f MXN".format(ruta.cobrado),
                                    fontSize = 14.sp,
                                    fontWeight = FontWeight.ExtraBold,
                                    color = VerdeAgave
                                )
                            }
                        }
                    }
                }
            }

            // Fila Inferior: Enlaces y acciones
            if (ruta.status == "EN_RUTA") {
                HorizontalDivider(color = Color(0xFFF3F4F6), thickness = 1.dp)
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    TextButton(
                        onClick = onCancelarSalida,
                        contentPadding = PaddingValues(horizontal = 4.dp, vertical = 2.dp)
                    ) {
                        Text(
                            text = "✖ Cancelar salida",
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Bold,
                            color = Color(0xFFDC2626)
                        )
                    }

                    Button(
                        onClick = onClick,
                        shape = CircleShape,
                        colors = ButtonDefaults.buttonColors(
                            containerColor = MaizPrimary,
                            contentColor = Color.White
                        ),
                        contentPadding = PaddingValues(horizontal = 14.dp, vertical = 6.dp),
                        modifier = Modifier.height(34.dp),
                        elevation = ButtonDefaults.buttonElevation(defaultElevation = 2.dp)
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(5.dp)
                        ) {
                            Text(
                                text = "Liquidar ruta",
                                fontSize = 12.sp,
                                fontWeight = FontWeight.Bold,
                                color = Color.White
                            )
                            Icon(
                                imageVector = Icons.AutoMirrored.Filled.ArrowForward,
                                contentDescription = null,
                                tint = Color.White,
                                modifier = Modifier.size(13.dp)
                            )
                        }
                    }
                }
            } else {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = when (ruta.status) {
                            "PENDIENTE_SALIDA" -> "Tocar para ingresar datos de salida"
                            else -> "Ver detalles"
                        },
                        fontSize = 12.sp,
                        fontWeight = FontWeight.SemiBold,
                        color = if (ruta.status == "PENDIENTE_SALIDA") TextSecondary else VerdeAgave
                    )
                    Icon(
                        imageVector = Icons.AutoMirrored.Filled.ArrowForward,
                        contentDescription = null,
                        tint = if (ruta.status == "PENDIENTE_SALIDA") TextSecondary else VerdeAgave,
                        modifier = Modifier.size(14.dp)
                    )
                }
            }
        }
    }
}

@Preview(showBackground = true, showSystemUi = true)
@Composable
fun PreviewRepartidoresScreen() {
    TortilleriaDerekTheme {
        RepartidoresSubContent(
            uiState = VentaUiState(),
            onOpenSalida = {},
            onOpenLiquidar = {},
            onShowDetalleLiquidado = {},
            onNavigateToHistorial = {}
        )
    }
}
