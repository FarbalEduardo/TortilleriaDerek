package com.example.tortilleriaderek.ui.screens

import android.widget.Toast
import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowForward
import androidx.compose.material.icons.automirrored.filled.ReceiptLong
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.tortilleriaderek.data.local.entity.RutaRepartidorEntity
import com.example.tortilleriaderek.model.ProductItem
import com.example.tortilleriaderek.presentation.venta.VentaUiEffect
import com.example.tortilleriaderek.presentation.venta.VentaUiState
import com.example.tortilleriaderek.presentation.venta.VentaViewModel
import com.example.tortilleriaderek.ui.components.DialogLiquidarRepartidor
import com.example.tortilleriaderek.ui.components.DialogSalidaRepartidor
import com.example.tortilleriaderek.ui.components.HeroSalesCard
import com.example.tortilleriaderek.ui.components.TortilleriaNavBar
import com.example.tortilleriaderek.ui.theme.*
import kotlinx.coroutines.launch

@Composable
fun MostradorRootScreen(
    viewModel: VentaViewModel = hiltViewModel(),
    onNavigateToProduccion: () -> Unit = {},
    onNavigateToMetricas: () -> Unit = {},
    onNavigateToConfiguracion: () -> Unit = {},
    onNavigateToHistorial: (String) -> Unit = {},
    onNavigateToLogin: () -> Unit = {},
    modifier: Modifier = Modifier
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    val context = LocalContext.current
    val coroutineScope = rememberCoroutineScope()

    // Pager para alternar únicamente el subcontenido inferior
    val pagerState = rememberPagerState(initialPage = 0, pageCount = { 2 })

    // Estados para diálogos de repartidores
    var detalleLiquidadoModal by remember { mutableStateOf<RutaRepartidorEntity?>(null) }

    LaunchedEffect(Unit) {
        viewModel.effect.collect { effect ->
            when (effect) {
                VentaUiEffect.NavigateToLogin -> onNavigateToLogin()
                VentaUiEffect.NavigateToHistorial -> onNavigateToHistorial("TODAS")
                is VentaUiEffect.ShowToast -> {
                    Toast.makeText(context, effect.mensaje, Toast.LENGTH_SHORT).show()
                }
            }
        }
    }

    Scaffold(
        modifier = modifier,
        containerColor = SurfaceWarm,
        contentWindowInsets = WindowInsets.navigationBars,
        bottomBar = {
            TortilleriaNavBar(
                activeItem = "Venta",
                onNavigateToVenta = { /* Ya estamos en venta */ },
                onNavigateToProduccion = onNavigateToProduccion,
                onNavigateToMetricas = onNavigateToMetricas,
                onNavigateToConfiguracion = onNavigateToConfiguracion
            )
        }
    ) { innerPadding ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .background(SurfaceWarm)
        ) {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(horizontal = 16.dp)
                    .padding(top = 12.dp),
                verticalArrangement = Arrangement.spacedBy(14.dp)
            ) {
                // ── 1. CABECERA FIJA: Hero Card Naranja ──────────────────────
                // Permanece completamente estática en la parte superior
                HeroSalesCard(
                    fechaTexto = uiState.fechaTurnoTexto,
                    totalAmount = uiState.totalMostradoEnHeader,
                    currentScope = uiState.selectedScope,
                    onScopeSelected = viewModel::onScopeSelected,
                    onCerrarTurnoClick = viewModel::onOpenCerrarTurnoDialog
                )

                // ── 2. BOTONES DE CHIP FIJOS: Mostrador / Repartidores ────────
                // Permanece fijo justo debajo del Hero Card
                MostradorRepartidoresTabBar(
                    selectedTab = pagerState.currentPage,
                    onMostradorSelected = {
                        coroutineScope.launch { pagerState.animateScrollToPage(0) }
                    },
                    onRepartidoresSelected = {
                        coroutineScope.launch { pagerState.animateScrollToPage(1) }
                    }
                )

                // ── 3. HORIZONTAL PAGER: Subcontenido Dinámico ────────────────
                // ÚNICAMENTE esta sección inferior se desliza/cambia
                HorizontalPager(
                    state = pagerState,
                    modifier = Modifier
                        .fillMaxWidth()
                        .weight(1f)
                ) { page ->
                    when (page) {
                        0 -> MostradorSubContent(
                            uiState = uiState,
                            onQuantityChanged = viewModel::onQuantityChanged,
                            onOpenConfirmarVenta = viewModel::onOpenConfirmarVentaDialog,
                            onNavigateToHistorial = { onNavigateToHistorial("MOSTRADOR") }
                        )
                        else -> RepartidoresSubContent(
                            uiState = uiState,
                            onOpenSalida = viewModel::onOpenSalidaRepartidor,
                            onOpenLiquidar = viewModel::onOpenLiquidarRepartidor,
                            onShowDetalleLiquidado = { ruta ->
                                detalleLiquidadoModal = ruta
                            },
                            onCancelarSalida = viewModel::onCancelarSalidaRepartidor,
                            onNavigateToHistorial = { onNavigateToHistorial("REPARTIDOR") }
                        )
                    }
                }
            }
        }
    }

    // ── Diálogo: Confirmar Venta Mostrador ────────────────────────────────────
    if (uiState.showConfirmarVentaDialog) {
        AlertDialog(
            onDismissRequest = viewModel::onDismissConfirmarVentaDialog,
            containerColor = Color.White,
            titleContentColor = Color(0xFF111827),
            textContentColor = Color(0xFF111827),
            icon = {
                Box(
                    modifier = Modifier
                        .size(48.dp)
                        .clip(CircleShape)
                        .background(MaizPrimaryLight),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = Icons.AutoMirrored.Filled.ReceiptLong,
                        contentDescription = null,
                        tint = MaizPrimary,
                        modifier = Modifier.size(24.dp)
                    )
                }
            },
            title = {
                Text(
                    text = "Confirmar Venta Mostrador",
                    fontWeight = FontWeight.Bold,
                    fontSize = 18.sp,
                    color = TextPrimary,
                    textAlign = TextAlign.Center
                )
            },
            text = {
                Column(
                    verticalArrangement = Arrangement.spacedBy(14.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    // 1. Badges superiores: Folio Ticket e Insignia De Contado
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Surface(
                            shape = RoundedCornerShape(8.dp),
                            color = SurfaceContainerLow,
                            border = androidx.compose.foundation.BorderStroke(1.dp, BorderSubtle)
                        ) {
                            Text(
                                text = "Ticket: ${uiState.proximoTicketMostrador}",
                                fontWeight = FontWeight.Bold,
                                color = MaizPrimary,
                                fontSize = 12.sp,
                                modifier = Modifier.padding(horizontal = 10.dp, vertical = 4.dp)
                            )
                        }

                        Surface(
                            shape = RoundedCornerShape(8.dp),
                            color = VerdeAgaveLight
                        ) {
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(4.dp),
                                modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                            ) {
                                Icon(
                                    imageVector = Icons.Default.Payments,
                                    contentDescription = null,
                                    tint = VerdeAgave,
                                    modifier = Modifier.size(13.dp)
                                )
                                Text(
                                    text = "De Contado",
                                    fontWeight = FontWeight.Bold,
                                    color = VerdeAgave,
                                    fontSize = 11.sp
                                )
                            }
                        }
                    }

                    // 2. Tarjeta Destacada de Total a Cobrar (Máxima jerarquía visual)
                    Surface(
                        shape = RoundedCornerShape(16.dp),
                        color = Color(0xFFFFF9F5),
                        border = androidx.compose.foundation.BorderStroke(1.dp, MaizPrimary.copy(alpha = 0.25f)),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Column(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(vertical = 12.dp, horizontal = 16.dp),
                            horizontalAlignment = Alignment.CenterHorizontally,
                            verticalArrangement = Arrangement.spacedBy(2.dp)
                        ) {
                            Text(
                                text = "TOTAL A COBRAR",
                                fontSize = 11.sp,
                                fontWeight = FontWeight.Bold,
                                color = TextSecondary,
                                letterSpacing = 0.5.sp
                            )
                            Text(
                                text = "$%,.2f MXN".format(uiState.subtotalOrdenActual),
                                fontSize = 24.sp,
                                fontWeight = FontWeight.ExtraBold,
                                color = MaizPrimary
                            )
                        }
                    }

                    // 3. Desglose de Productos Seleccionados
                    Surface(
                        shape = RoundedCornerShape(12.dp),
                        color = SurfaceWarm,
                        border = androidx.compose.foundation.BorderStroke(1.dp, BorderLight),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Column(
                            modifier = Modifier.padding(12.dp),
                            verticalArrangement = Arrangement.spacedBy(6.dp)
                        ) {
                            Text(
                                text = "Artículos en orden:",
                                fontSize = 11.sp,
                                fontWeight = FontWeight.SemiBold,
                                color = TextSecondary
                            )
                            uiState.products.filter { it.quantity > 0 }.forEach { item ->
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Row(
                                        horizontalArrangement = Arrangement.spacedBy(6.dp),
                                        verticalAlignment = Alignment.CenterVertically
                                    ) {
                                        Surface(
                                            shape = CircleShape,
                                            color = MaizPrimaryLight
                                        ) {
                                            Text(
                                                text = "${item.quantity}",
                                                fontSize = 11.sp,
                                                fontWeight = FontWeight.Bold,
                                                color = MaizPrimary,
                                                modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                                            )
                                        }
                                        Text(
                                            text = item.name,
                                            fontSize = 13.sp,
                                            fontWeight = FontWeight.Medium,
                                            color = TextPrimary
                                        )
                                    }
                                    Text(
                                        text = "$%,.2f".format(item.subtotal),
                                        fontSize = 13.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = TextPrimary
                                    )
                                }
                            }
                        }
                    }
                }
            },
            confirmButton = {
                Button(
                    onClick = viewModel::onConfirmarVenta,
                    colors = ButtonDefaults.buttonColors(containerColor = MaizPrimary),
                    shape = CircleShape,
                    modifier = Modifier.height(44.dp)
                ) {
                    Text(
                        text = "Cobrar e Imprimir",
                        fontWeight = FontWeight.Bold,
                        fontSize = 14.sp
                    )
                }
            },
            dismissButton = {
                OutlinedButton(
                    onClick = viewModel::onDismissConfirmarVentaDialog,
                    shape = CircleShape,
                    border = androidx.compose.foundation.BorderStroke(1.dp, BorderSubtle),
                    modifier = Modifier.height(44.dp)
                ) {
                    Text(
                        text = "Cancelar",
                        color = TextSecondary,
                        fontWeight = FontWeight.SemiBold
                    )
                }
            },
            shape = RoundedCornerShape(24.dp)
        )
    }

    // ── Diálogo: Cerrar Turno ────────────────────────────────────────────────
    if (uiState.showCerrarTurnoDialog) {
        val tieneRutasActivas = uiState.rutasActivasCount > 0
        val rutasEnCalle = uiState.rutasRepartidores.filter { it.status == "EN_RUTA" }

        AlertDialog(
            onDismissRequest = viewModel::onDismissCerrarTurnoDialog,
            containerColor = Color.White,
            titleContentColor = Color(0xFF111827),
            textContentColor = Color(0xFF111827),
            icon = {
                Box(
                    modifier = Modifier
                        .size(46.dp)
                        .clip(CircleShape)
                        .background(if (tieneRutasActivas) TerracotaLight else MaizPrimaryLight),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = if (tieneRutasActivas) Icons.Default.Warning else Icons.Default.Lock,
                        contentDescription = null,
                        tint = if (tieneRutasActivas) TerracotaSecondary else MaizPrimary,
                        modifier = Modifier.size(24.dp)
                    )
                }
            },
            title = {
                Text(
                    text = if (tieneRutasActivas) "¡Rutas Activas en Calle!" else "¿Cerrar Turno Actual?",
                    fontWeight = FontWeight.Bold,
                    fontSize = 17.sp,
                    color = if (tieneRutasActivas) TerracotaSecondary else TextPrimary,
                    textAlign = TextAlign.Center
                )
            },
            text = {
                Column(
                    verticalArrangement = Arrangement.spacedBy(10.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    if (tieneRutasActivas) {
                        Surface(
                            shape = RoundedCornerShape(12.dp),
                            color = TerracotaLight.copy(alpha = 0.5f),
                            border = androidx.compose.foundation.BorderStroke(1.dp, TerracotaSecondary.copy(alpha = 0.4f)),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Column(
                                modifier = Modifier.padding(12.dp),
                                verticalArrangement = Arrangement.spacedBy(6.dp)
                            ) {
                                Text(
                                    text = "No se puede cerrar turno mientras existan rutas activas en la calle:",
                                    fontSize = 12.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = TerracotaSecondary
                                )
                                rutasEnCalle.forEach { ruta ->
                                    Row(
                                        modifier = Modifier.fillMaxWidth(),
                                        horizontalArrangement = Arrangement.SpaceBetween,
                                        verticalAlignment = Alignment.CenterVertically
                                    ) {
                                        Text(
                                            text = "• ${ruta.moto} (${ruta.repartidorNombre})",
                                            fontSize = 12.sp,
                                            fontWeight = FontWeight.Medium,
                                            color = TextPrimary
                                        )
                                        Text(
                                            text = "${ruta.cargaInicialKg} kg en ruta",
                                            fontSize = 12.sp,
                                            fontWeight = FontWeight.Bold,
                                            color = TerracotaSecondary
                                        )
                                    }
                                }
                            }
                        }
                        Text(
                            text = "Por favor, liquida o da por terminada cada ruta en la pestaña de Repartidores antes de proceder con el corte de caja.",
                            fontSize = 12.sp,
                            color = TextSecondary,
                            textAlign = TextAlign.Center
                        )
                    } else {
                        Text(
                            text = "Se realizará el corte de caja y se consolidará el total de ventas acumuladas " +
                                    "($%,.2f MXN) en Room. Una vez cerrado el turno ya no se podrán modificar ni registrar nuevas ventas.".format(uiState.totalVentasDia),
                            fontSize = 13.sp,
                            color = TextSecondary,
                            textAlign = TextAlign.Center
                        )
                    }
                }
            },
            confirmButton = {
                Button(
                    onClick = viewModel::onConfirmarCerrarTurno,
                    enabled = !tieneRutasActivas,
                    colors = ButtonDefaults.buttonColors(
                        containerColor = MaizPrimary,
                        disabledContainerColor = Color.LightGray.copy(alpha = 0.5f)
                    ),
                    shape = CircleShape
                ) {
                    Text(
                        text = if (tieneRutasActivas) "Liquidación Pendiente" else "Confirmar y Salir",
                        fontWeight = FontWeight.Bold
                    )
                }
            },
            dismissButton = {
                OutlinedButton(onClick = viewModel::onDismissCerrarTurnoDialog, shape = CircleShape) {
                    Text("Cancelar")
                }
            },
            shape = RoundedCornerShape(22.dp)
        )
    }

    // ── Diálogo: Ingresar Datos de Salida Repartidor ──────────────────────────
    uiState.repartidorSeleccionadoParaSalida?.let { driver ->
        DialogSalidaRepartidor(
            driver = driver,
            onConfirmarSalida = viewModel::onConfirmarSalidaRepartidor,
            onDismiss = viewModel::onDismissSalidaRepartidor,
            precioPaqueteRepartidor = uiState.precioPaqueteRepartidor,
            pesoPaqueteRepartidorKg = uiState.pesoPaqueteRepartidorKg,
            onConfirmarSalidaConPaquetes = { kg, paq ->
                viewModel.onConfirmarSalidaRepartidor(kg, paq)
            }
        )
    }

    // ── Diálogo: Registrar Devolución y Liquidar Repartidor ───────────────────
    uiState.repartidorSeleccionadoParaLiquidar?.let { driver ->
        DialogLiquidarRepartidor(
            driver = driver,
            onConfirmarLiquidar = viewModel::onConfirmarLiquidarRepartidor,
            onDismiss = viewModel::onDismissLiquidarRepartidor,
            precioPaqueteRepartidor = uiState.precioPaqueteRepartidor,
            pesoPaqueteRepartidorKg = uiState.pesoPaqueteRepartidorKg,
            onConfirmarLiquidarConPaquetes = { devKg, cobrado, paqDev ->
                viewModel.onConfirmarLiquidarRepartidor(devKg, cobrado, paqDev)
            }
        )
    }

    // ── Diálogo: Detalle de Liquidación de Repartidor ─────────────────────────
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
// Subcontenido de Mostrador (Solo esta área se desliza en el Pager)
// ─────────────────────────────────────────────────────────────────────────────
@Composable
fun MostradorSubContent(
    uiState: VentaUiState,
    onQuantityChanged: (Int, Int) -> Unit,
    onOpenConfirmarVenta: () -> Unit,
    onNavigateToHistorial: () -> Unit,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(bottom = 20.dp),
        verticalArrangement = Arrangement.spacedBy(14.dp)
    ) {
        // Cabecera de sección + Badge Folio Ticket
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = "Registro de Mostrador",
                fontSize = 16.sp,
                fontWeight = FontWeight.Bold,
                color = TextPrimary
            )

            // Ticket badge suave: Ticket: M-0143
            Surface(
                shape = CircleShape,
                color = Color(0xFFFFEDE2)
            ) {
                Text(
                    text = "Ticket: ${uiState.proximoTicketMostrador}",
                    fontSize = 11.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color(0xFFC2410C),
                    modifier = Modifier.padding(horizontal = 10.dp, vertical = 5.dp)
                )
            }
        }

        // Lista de Productos (Card blanca con 3 artículos)
        Surface(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(20.dp),
            color = Color.White,
            border = androidx.compose.foundation.BorderStroke(1.dp, BorderSubtle.copy(alpha = 0.8f)),
            shadowElevation = 1.dp
        ) {
            Column {
                uiState.products.forEachIndexed { index, product ->
                    MostradorProductRow(
                        product = product,
                        onQuantityChanged = { newQty ->
                            onQuantityChanged(product.id, newQty)
                        }
                    )
                    if (index < uiState.products.size - 1) {
                        HorizontalDivider(
                            color = BorderLight,
                            thickness = 1.dp,
                            modifier = Modifier.padding(horizontal = 14.dp)
                        )
                    }
                }
            }
        }

        // Botón CTA Principal: Ingresar Venta
        val orderTotal = uiState.subtotalOrdenActual
        val isButtonEnabled = orderTotal > 0 && !uiState.isTurnoCerrado

        Button(
            onClick = onOpenConfirmarVenta,
            modifier = Modifier
                .fillMaxWidth()
                .height(56.dp),
            shape = CircleShape,
            colors = ButtonDefaults.buttonColors(
                containerColor = if (isButtonEnabled) MaizPrimary else MaizPrimary.copy(alpha = 0.45f),
                disabledContainerColor = MaizPrimary.copy(alpha = 0.35f)
            ),
            enabled = isButtonEnabled,
            elevation = ButtonDefaults.buttonElevation(defaultElevation = 3.dp, pressedElevation = 1.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Icon(
                        imageVector = Icons.AutoMirrored.Filled.ReceiptLong,
                        contentDescription = null,
                        tint = Color.White,
                        modifier = Modifier.size(20.dp)
                    )
                    Text(
                        text = "Ingresar venta",
                        fontSize = 15.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color.White
                    )
                }

                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    Text(
                        text = if (orderTotal > 0) "$%,.2f MXN".format(orderTotal) else "—",
                        fontSize = 15.sp,
                        fontWeight = FontWeight.ExtraBold,
                        color = Color.White
                    )
                    Icon(
                        imageVector = Icons.AutoMirrored.Filled.ArrowForward,
                        contentDescription = null,
                        tint = Color.White,
                        modifier = Modifier.size(17.dp)
                    )
                }
            }
        }

        // Botón Secundario: Historial de Ventas
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
                    text = "Historial de Ventas",
                    fontSize = 13.sp,
                    fontWeight = FontWeight.SemiBold,
                    color = TextSecondary
                )
            }
        }

        Spacer(modifier = Modifier.height(8.dp))
    }
}

// ─────────────────────────────────────────────────────────────────────────────
// Tab Segmentado: Mostrador ← → Repartidores
// ─────────────────────────────────────────────────────────────────────────────
@Composable
fun MostradorRepartidoresTabBar(
    selectedTab: Int,
    onMostradorSelected: () -> Unit,
    onRepartidoresSelected: () -> Unit,
    modifier: Modifier = Modifier
) {
    Box(
        modifier = modifier
            .fillMaxWidth()
            .height(44.dp)
            .clip(CircleShape)
            .background(BorderLight)
            .border(1.dp, BorderSubtle.copy(alpha = 0.8f), CircleShape)
            .padding(4.dp)
    ) {
        Row(modifier = Modifier.fillMaxSize()) {
            TabPill(
                label = "Mostrador",
                icon = Icons.Default.Storefront,
                isSelected = selectedTab == 0,
                onClick = onMostradorSelected,
                modifier = Modifier.weight(1f)
            )
            TabPill(
                label = "Reparto Mayoreo",
                icon = Icons.Default.TwoWheeler,
                isSelected = selectedTab == 1,
                onClick = onRepartidoresSelected,
                modifier = Modifier.weight(1f)
            )
        }
    }
}

@Composable
private fun TabPill(
    label: String,
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    isSelected: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val bgColor by animateColorAsState(
        targetValue = if (isSelected) MaizPrimary else Color.Transparent,
        animationSpec = tween(220, easing = FastOutSlowInEasing),
        label = "tabBg"
    )
    val contentColor by animateColorAsState(
        targetValue = if (isSelected) Color.White else TextSecondary,
        animationSpec = tween(220, easing = FastOutSlowInEasing),
        label = "tabContent"
    )

    Box(
        modifier = modifier
            .fillMaxHeight()
            .clip(CircleShape)
            .background(bgColor)
            .clickable { onClick() },
        contentAlignment = Alignment.Center
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(6.dp)
        ) {
            Icon(
                imageVector = icon,
                contentDescription = null,
                tint = contentColor,
                modifier = Modifier.size(16.dp)
            )
            Text(
                text = label,
                color = contentColor,
                fontSize = 13.sp,
                fontWeight = if (isSelected) FontWeight.Bold else FontWeight.SemiBold
            )
        }
    }
}

// ─────────────────────────────────────────────────────────────────────────────
// Fila de Producto (Stepper)
// ─────────────────────────────────────────────────────────────────────────────
@Composable
private fun MostradorProductRow(
    product: ProductItem,
    onQuantityChanged: (Int) -> Unit,
    modifier: Modifier = Modifier
) {
    Row(
        modifier = modifier
            .fillMaxWidth()
            .padding(horizontal = 14.dp, vertical = 12.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        // Izquierda: Nombre + Precio unitario + Peso
        Column(
            modifier = Modifier.weight(1f),
            verticalArrangement = Arrangement.spacedBy(2.dp)
        ) {
            Text(
                text = product.name,
                fontSize = 15.sp,
                fontWeight = FontWeight.Bold,
                color = TextPrimary
            )
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(6.dp)
            ) {
                Text(
                    text = "$%,.2f".format(product.price),
                    fontSize = 12.sp,
                    fontWeight = FontWeight.Bold,
                    color = MaizPrimary
                )
                Text(
                    text = "• ${if (product.pesoGramos >= 1000) "1 kg" else "${product.pesoGramos} g"}",
                    fontSize = 12.sp,
                    fontWeight = FontWeight.Medium,
                    color = TextSecondary
                )
            }
        }

        // Derecha: Stepper pill + Subtotal
        Column(
            horizontalAlignment = Alignment.End,
            verticalArrangement = Arrangement.spacedBy(3.dp)
        ) {
            Row(
                modifier = Modifier
                    .clip(CircleShape)
                    .background(Color(0xFFF6ECE6).copy(alpha = 0.6f))
                    .border(1.dp, BorderSubtle.copy(alpha = 0.8f), CircleShape)
                    .padding(2.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                // Botón –
                Box(
                    modifier = Modifier
                        .size(28.dp)
                        .clip(CircleShape)
                        .background(Color.White)
                        .border(1.dp, BorderSubtle.copy(alpha = 0.6f), CircleShape)
                        .clickable(
                            enabled = product.quantity > 0,
                            onClick = { onQuantityChanged(product.quantity - 1) }
                        ),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = Icons.Default.Remove,
                        contentDescription = "Disminuir",
                        tint = if (product.quantity > 0) TextPrimary else TextMuted,
                        modifier = Modifier.size(14.dp)
                    )
                }

                // Cantidad
                Text(
                    text = "${product.quantity}",
                    fontSize = 13.sp,
                    fontWeight = FontWeight.Bold,
                    color = if (product.quantity > 0) TextPrimary else TextMuted,
                    textAlign = TextAlign.Center,
                    modifier = Modifier.widthIn(min = 28.dp)
                )

                // Botón +
                Box(
                    modifier = Modifier
                        .size(28.dp)
                        .clip(CircleShape)
                        .background(MaizPrimary)
                        .clickable { onQuantityChanged(product.quantity + 1) },
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = Icons.Default.Add,
                        contentDescription = "Aumentar",
                        tint = Color.White,
                        modifier = Modifier.size(14.dp)
                    )
                }
            }

            // Subtotal
            Text(
                text = "$%,.2f".format(product.subtotal),
                fontSize = 12.sp,
                fontWeight = FontWeight.Bold,
                color = if (product.quantity > 0) TextPrimary else TextMuted,
                modifier = Modifier.padding(end = 2.dp)
            )
        }
    }
}

@Preview(showBackground = true, showSystemUi = true)
@Composable
fun PreviewMostradorRootScreen() {
    TortilleriaDerekTheme {
        MostradorSubContent(
            uiState = VentaUiState(),
            onQuantityChanged = { _, _ -> },
            onOpenConfirmarVenta = {},
            onNavigateToHistorial = {}
        )
    }
}
