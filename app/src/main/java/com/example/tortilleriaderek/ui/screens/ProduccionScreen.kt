package com.example.tortilleriaderek.ui.screens

import android.widget.Toast
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowForward
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.CalendarToday
import androidx.compose.material.icons.filled.DeleteOutline
import androidx.compose.material.icons.filled.Inventory2
import androidx.compose.material.icons.filled.Remove
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.tortilleriaderek.presentation.produccion.ProduccionUiEffect
import com.example.tortilleriaderek.presentation.produccion.ProduccionUiEvent
import com.example.tortilleriaderek.presentation.produccion.ProduccionUiState
import com.example.tortilleriaderek.presentation.produccion.ProduccionViewModel
import com.example.tortilleriaderek.ui.components.TortilleriaNavBar
import com.example.tortilleriaderek.ui.theme.*
import java.util.Locale

/**
 * Pantalla de Producción conectada reactivamente con ProduccionViewModel, Room y Contrato MVI.
 *
 * Muestra el balance de tortilla disponible en tienda (iniciando en 0.0 kg al abrir turno),
 * progreso de venta en tiempo real, stepper de bultos con cálculo según receta configurable,
 * registro directo de merma operativa (Caso 6) y navegación al historial.
 */
@Composable
fun ProduccionScreen(
    viewModel: ProduccionViewModel = hiltViewModel(),
    onNavigateToVenta: () -> Unit = {},
    onNavigateToProduccion: () -> Unit = {},
    onNavigateToMetricas: () -> Unit = {},
    onNavigateToConfiguracion: () -> Unit = {},
    onVerHistorialProduccion: () -> Unit = {},
    modifier: Modifier = Modifier
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    val context = LocalContext.current

    LaunchedEffect(Unit) {
        viewModel.effect.collect { effect ->
            when (effect) {
                is ProduccionUiEffect.ShowToast -> {
                    Toast.makeText(context, effect.mensaje, Toast.LENGTH_SHORT).show()
                }
                is ProduccionUiEffect.ShowError -> {
                    Toast.makeText(context, effect.error, Toast.LENGTH_LONG).show()
                }
                ProduccionUiEffect.NavigateToHistorial -> {
                    onVerHistorialProduccion()
                }
            }
        }
    }

    ProduccionScreenContent(
        uiState = uiState,
        onEvent = viewModel::onEvent,
        onNavigateToVenta = onNavigateToVenta,
        onNavigateToProduccion = onNavigateToProduccion,
        onNavigateToMetricas = onNavigateToMetricas,
        onNavigateToConfiguracion = onNavigateToConfiguracion,
        onVerHistorialProduccion = onVerHistorialProduccion,
        modifier = modifier
    )
}

@Composable
fun ProduccionScreenContent(
    uiState: ProduccionUiState,
    onEvent: (ProduccionUiEvent) -> Unit,
    onNavigateToVenta: () -> Unit = {},
    onNavigateToProduccion: () -> Unit = {},
    onNavigateToMetricas: () -> Unit = {},
    onNavigateToConfiguracion: () -> Unit = {},
    onVerHistorialProduccion: () -> Unit = {},
    modifier: Modifier = Modifier
) {
    Scaffold(
        modifier = modifier.fillMaxSize(),
        containerColor = SurfaceWarm,
        contentWindowInsets = WindowInsets.navigationBars,
        bottomBar = {
            TortilleriaNavBar(
                activeItem = "Producción",
                onNavigateToVenta = onNavigateToVenta,
                onNavigateToProduccion = onNavigateToProduccion,
                onNavigateToMetricas = onNavigateToMetricas,
                onNavigateToConfiguracion = onNavigateToConfiguracion
            )
        }
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .background(SurfaceWarm)
                .verticalScroll(rememberScrollState())
                .padding(horizontal = 16.dp, vertical = 12.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            // ── 1. HERO CARD NARANJA SUPERIOR ────────────────────────────────
            Surface(
                modifier = Modifier
                    .fillMaxWidth()
                    .testTag("produccion_hero_card"),
                shape = RoundedCornerShape(24.dp),
                color = Color.Transparent,
                shadowElevation = 4.dp
            ) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .background(
                            brush = Brush.verticalGradient(
                                colors = listOf(Color(0xFFFF7A00), Color(0xFFFF5700))
                            ),
                            shape = RoundedCornerShape(24.dp)
                        )
                        .padding(18.dp)
                ) {
                    Column(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.spacedBy(14.dp)
                    ) {
                        // Badges superiores: Fecha y Estatus
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Surface(
                                shape = CircleShape,
                                color = Color.White.copy(alpha = 0.20f)
                            ) {
                                Row(
                                    modifier = Modifier.padding(horizontal = 10.dp, vertical = 5.dp),
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.spacedBy(5.dp)
                                ) {
                                    Icon(
                                        imageVector = Icons.Default.CalendarToday,
                                        contentDescription = null,
                                        tint = Color.White,
                                        modifier = Modifier.size(13.dp)
                                    )
                                    Text(
                                        text = uiState.fechaTurnoTexto,
                                        fontSize = 11.sp,
                                        fontWeight = FontWeight.Medium,
                                        color = Color.White
                                    )
                                }
                            }

                            Surface(
                                shape = CircleShape,
                                color = Color.White.copy(alpha = 0.20f)
                            ) {
                                Row(
                                    modifier = Modifier.padding(horizontal = 10.dp, vertical = 5.dp),
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                                ) {
                                    Box(
                                        modifier = Modifier
                                            .size(7.dp)
                                            .clip(CircleShape)
                                            .background(if (uiState.isTurnoCerrado) Color.LightGray else Color.White)
                                    )
                                    Text(
                                        text = if (uiState.isTurnoCerrado) "Turno Cerrado" else uiState.estadoTurnoTexto,
                                        fontSize = 11.sp,
                                        fontWeight = FontWeight.SemiBold,
                                        color = Color.White
                                    )
                                }
                            }
                        }

                        // Métrica Principal: Tortilla Disponible en Tienda (Caso 1: inicia en 0.0 kg)
                        Column(
                            horizontalAlignment = Alignment.CenterHorizontally,
                            verticalArrangement = Arrangement.spacedBy(2.dp)
                        ) {
                            Text(
                                text = "TORTILLA DISPONIBLE EN TIENDA",
                                fontSize = 11.sp,
                                fontWeight = FontWeight.Bold,
                                color = Color.White.copy(alpha = 0.90f),
                                letterSpacing = 0.5.sp
                            )
                            Text(
                                text = String.format(Locale.US, "%.1f kg", uiState.disponibleEnTienda),
                                fontSize = 42.sp,
                                fontWeight = FontWeight.Black,
                                color = Color.White
                            )
                        }

                        // Barra de Progreso de Venta
                        Column(
                            modifier = Modifier.fillMaxWidth(),
                            verticalArrangement = Arrangement.spacedBy(6.dp)
                        ) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween
                            ) {
                                Text(
                                    text = "Progreso de Venta",
                                    fontSize = 11.sp,
                                    fontWeight = FontWeight.Medium,
                                    color = Color.White.copy(alpha = 0.90f)
                                )
                                Text(
                                    text = String.format(Locale.US, "%.1f%% Vendido", uiState.porcentajeVenta),
                                    fontSize = 11.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = Color.White
                                )
                            }

                            // Barra horizontal blanca con track semitransparente
                            Box(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .height(8.dp)
                                    .clip(CircleShape)
                                    .background(Color.White.copy(alpha = 0.30f))
                            ) {
                                Box(
                                    modifier = Modifier
                                        .fillMaxWidth(fraction = (uiState.porcentajeVenta / 100.0).toFloat().coerceIn(0f, 1f))
                                        .fillMaxHeight()
                                        .clip(CircleShape)
                                        .background(Color.White)
                                )
                            }
                        }

                        // Sub-tarjetas Gemelas en la base: Producido Neto y Total Vendido
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(10.dp)
                        ) {
                            Surface(
                                modifier = Modifier.weight(1f),
                                shape = RoundedCornerShape(16.dp),
                                color = Color.White,
                                shadowElevation = 1.dp
                            ) {
                                Column(
                                    modifier = Modifier.padding(vertical = 12.dp, horizontal = 14.dp),
                                    horizontalAlignment = Alignment.CenterHorizontally,
                                    verticalArrangement = Arrangement.spacedBy(3.dp)
                                ) {
                                    Text(
                                        text = "PRODUCIDO NETO",
                                        fontSize = 9.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = TextSecondary,
                                        letterSpacing = 0.4.sp
                                    )
                                    Text(
                                        text = String.format(Locale.US, "%.1f kg", uiState.producidoNeto),
                                        fontSize = 16.sp,
                                        fontWeight = FontWeight.ExtraBold,
                                        color = TextPrimary
                                    )
                                }
                            }

                            Surface(
                                modifier = Modifier.weight(1f),
                                shape = RoundedCornerShape(16.dp),
                                color = Color.White,
                                shadowElevation = 1.dp
                            ) {
                                Column(
                                    modifier = Modifier.padding(vertical = 12.dp, horizontal = 14.dp),
                                    horizontalAlignment = Alignment.CenterHorizontally,
                                    verticalArrangement = Arrangement.spacedBy(3.dp)
                                ) {
                                    Text(
                                        text = "TOTAL VENDIDO",
                                        fontSize = 9.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = TextSecondary,
                                        letterSpacing = 0.4.sp
                                    )
                                    Text(
                                        text = String.format(Locale.US, "%.1f kg", uiState.totalVendidoKg),
                                        fontSize = 16.sp,
                                        fontWeight = FontWeight.ExtraBold,
                                        color = MaizPrimary
                                    )
                                }
                            }
                        }

                        // Indicador de merma del turno si existe
                        if (uiState.mermaTurnoKg > 0.0) {
                            Surface(
                                shape = CircleShape,
                                color = Color.White.copy(alpha = 0.22f)
                            ) {
                                Text(
                                    text = String.format(Locale.US, "Merma de turno registrada: -%.1f kg", uiState.mermaTurnoKg),
                                    fontSize = 11.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = Color.White,
                                    modifier = Modifier.padding(horizontal = 10.dp, vertical = 3.dp)
                                )
                            }
                        }
                    }
                }
            }

            // ── 2. SECCIÓN: REGISTRO DE NUEVA TANDA ───────────────────────────
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "Registro de Nueva Tanda",
                    fontSize = 16.sp,
                    fontWeight = FontWeight.Bold,
                    color = TextPrimary
                )

                Surface(
                    shape = RoundedCornerShape(12.dp),
                    color = Color(0xFFFFF3EB),
                    border = BorderStroke(1.dp, Color(0xFFFFD7BF))
                ) {
                    Text(
                        text = "Rendimiento: %.1f kg / bulto".format(uiState.rendimientoPorBulto),
                        fontSize = 10.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color(0xFFE05300),
                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                    )
                }
            }

            // Card Interactiva de Insumos (Bultos de Harina)
            Surface(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(20.dp),
                color = Color.White,
                border = BorderStroke(1.dp, BorderSubtle),
                shadowElevation = 2.dp
            ) {
                Column(
                    modifier = Modifier.padding(18.dp),
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    // Encabezado del insumo
                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.spacedBy(3.dp)
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(6.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Default.Inventory2,
                                contentDescription = null,
                                tint = MaizPrimary,
                                modifier = Modifier.size(18.dp)
                            )
                            Text(
                                text = "Bultos de Harina (%.0f kg c/u)".format(uiState.pesoBultoHarinaKg),
                                fontSize = 15.sp,
                                fontWeight = FontWeight.Bold,
                                color = TextPrimary
                            )
                        }

                        Text(
                            text = "Rinde ~%.1f kg de tortilla cocida por bulto".format(uiState.rendimientoPorBulto),
                            fontSize = 12.sp,
                            color = TextSecondary
                        )
                    }

                    // Stepper central de alta usabilidad táctil
                    Surface(
                        shape = RoundedCornerShape(26.dp),
                        color = Color(0xFFF7F5F2),
                        border = BorderStroke(1.dp, BorderSubtle.copy(alpha = 0.5f))
                    ) {
                        Row(
                            modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(16.dp)
                        ) {
                            // Botón Menos
                            FilledIconButton(
                                onClick = { onEvent(ProduccionUiEvent.OnDecrementarBultos) },
                                enabled = uiState.bultosHarinaInput > 0,
                                modifier = Modifier
                                    .size(44.dp)
                                    .testTag("produccion_stepper_decrement"),
                                shape = CircleShape,
                                colors = IconButtonDefaults.filledIconButtonColors(
                                    containerColor = Color.White,
                                    contentColor = TextPrimary,
                                    disabledContainerColor = Color.White.copy(alpha = 0.5f),
                                    disabledContentColor = Color.LightGray
                                )
                            ) {
                                Icon(Icons.Default.Remove, contentDescription = "Reducir bultos")
                            }

                            // Contador central
                            Row(
                                verticalAlignment = Alignment.Bottom,
                                horizontalArrangement = Arrangement.spacedBy(4.dp)
                            ) {
                                Text(
                                    text = "${uiState.bultosHarinaInput}",
                                    fontSize = 26.sp,
                                    fontWeight = FontWeight.Black,
                                    color = TextPrimary
                                )
                                Text(
                                    text = "bultos",
                                    fontSize = 13.sp,
                                    fontWeight = FontWeight.Medium,
                                    color = TextSecondary,
                                    modifier = Modifier.padding(bottom = 3.dp)
                                )
                            }

                            // Botón Más
                            FilledIconButton(
                                onClick = { onEvent(ProduccionUiEvent.OnIncrementarBultos) },
                                modifier = Modifier
                                    .size(44.dp)
                                    .testTag("produccion_stepper_increment"),
                                shape = CircleShape,
                                colors = IconButtonDefaults.filledIconButtonColors(
                                    containerColor = MaizPrimary,
                                    contentColor = Color.White
                                )
                            ) {
                                Icon(Icons.Default.Add, contentDescription = "Aumentar bultos")
                            }
                        }
                    }

                    // Equivalencia en masa cruda y tortilla
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        Text(
                            text = "Equivale a ",
                            fontSize = 12.sp,
                            color = TextSecondary
                        )
                        Text(
                            text = String.format(Locale.US, "%.0f kg", uiState.masaCrudaCalculada),
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Bold,
                            color = TextPrimary
                        )
                        Text(
                            text = "masa cruda",
                            fontSize = 12.sp,
                            color = TextSecondary
                        )
                        Text(
                            text = "➔",
                            fontSize = 13.sp,
                            fontWeight = FontWeight.Bold,
                            color = MaizPrimary
                        )
                        Text(
                            text = String.format(Locale.US, "%.1f kg", uiState.tortillaCalculada),
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Bold,
                            color = MaizPrimary
                        )
                        Text(
                            text = "tortilla",
                            fontSize = 12.sp,
                            color = TextSecondary
                        )
                    }
                }
            }

            // ── 3. BOTONES DE ACCIÓN ──────────────────────────────────────────
            Column(
                modifier = Modifier.fillMaxWidth(),
                verticalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                // CTA Principal Naranja
                Button(
                    onClick = { onEvent(ProduccionUiEvent.OnRegistrarTandaClick) },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(52.dp)
                        .testTag("produccion_btn_registrar_tanda"),
                    shape = RoundedCornerShape(26.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = MaizPrimary),
                    elevation = ButtonDefaults.buttonElevation(defaultElevation = 2.dp)
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Text(
                            text = "Registrar Tanda de Producción",
                            fontSize = 15.sp,
                            fontWeight = FontWeight.Bold,
                            color = Color.White
                        )
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.ArrowForward,
                            contentDescription = null,
                            tint = Color.White,
                            modifier = Modifier.size(18.dp)
                        )
                    }
                }

                // CTA Merma Directa en Producción (Caso 6)
                OutlinedButton(
                    onClick = { onEvent(ProduccionUiEvent.OnOpenRegistrarMermaDialog) },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(50.dp)
                        .testTag("produccion_btn_registrar_merma"),
                    shape = RoundedCornerShape(25.dp),
                    border = BorderStroke(1.dp, Color(0xFFEF4444)),
                    colors = ButtonDefaults.outlinedButtonColors(
                        containerColor = Color(0xFFFEF2F2),
                        contentColor = Color(0xFFDC2626)
                    )
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.DeleteOutline,
                            contentDescription = null,
                            tint = Color(0xFFDC2626),
                            modifier = Modifier.size(18.dp)
                        )
                        Text(
                            text = "Registrar Merma Operativa",
                            fontSize = 14.sp,
                            fontWeight = FontWeight.SemiBold,
                            color = Color(0xFFDC2626)
                        )
                    }
                }

                // CTA Secundario Outlined: Historial
                OutlinedButton(
                    onClick = onVerHistorialProduccion,
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(50.dp)
                        .testTag("produccion_btn_ver_historial"),
                    shape = RoundedCornerShape(25.dp),
                    border = BorderStroke(1.dp, BorderSubtle),
                    colors = ButtonDefaults.outlinedButtonColors(
                        containerColor = Color.White,
                        contentColor = Color(0xFF2C5282)
                    )
                ) {
                    Text(
                        text = "Ver Historial produccion",
                        fontSize = 14.sp,
                        fontWeight = FontWeight.SemiBold,
                        color = Color(0xFF3182CE)
                    )
                }
            }

            Spacer(modifier = Modifier.height(10.dp))
        }

        // ── 4. DIÁLOGO PARA REGISTRAR MERMA DIRECTA (Caso 6) ──────────────────
        if (uiState.showRegistrarMermaDialog) {
            AlertDialog(
                onDismissRequest = { onEvent(ProduccionUiEvent.OnDismissRegistrarMermaDialog) },
                title = {
                    Text(
                        text = "Registrar Merma de Turno",
                        fontWeight = FontWeight.Bold,
                        color = TextPrimary
                    )
                },
                text = {
                    Column(
                        modifier = Modifier.fillMaxWidth(),
                        verticalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        Text(
                            text = "Esta merma se descontará directamente del stock disponible de tortillas en tienda.",
                            fontSize = 13.sp,
                            color = TextSecondary
                        )

                        OutlinedTextField(
                            value = uiState.mermaKgInput,
                            onValueChange = { onEvent(ProduccionUiEvent.OnMermaKgInputChanged(it)) },
                            label = { Text("Kilogramos de Merma (ej. 2.5)") },
                            singleLine = true,
                            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                            modifier = Modifier.fillMaxWidth()
                        )

                        OutlinedTextField(
                            value = uiState.motivoMermaInput,
                            onValueChange = { onEvent(ProduccionUiEvent.OnMotivoMermaInputChanged(it)) },
                            label = { Text("Motivo / Causa") },
                            singleLine = true,
                            modifier = Modifier.fillMaxWidth()
                        )

                        if (uiState.errorMessage != null) {
                            Text(
                                text = uiState.errorMessage,
                                fontSize = 12.sp,
                                color = Color.Red,
                                fontWeight = FontWeight.Medium
                            )
                        }
                    }
                },
                confirmButton = {
                    val parsedMerma = uiState.mermaKgInput.replace(',', '.').toDoubleOrNull()
                    val esMermaInvalida = uiState.errorMessage != null || parsedMerma == null || parsedMerma <= 0.0 || parsedMerma > uiState.disponibleEnTienda
                    Button(
                        onClick = { onEvent(ProduccionUiEvent.OnConfirmarRegistroMerma) },
                        enabled = !esMermaInvalida,
                        colors = ButtonDefaults.buttonColors(
                            containerColor = Color(0xFFDC2626),
                            disabledContainerColor = Color(0xFFE5E7EB),
                            disabledContentColor = Color(0xFF9CA3AF)
                        )
                    ) {
                        Text("Descontar Merma", fontWeight = FontWeight.Bold)
                    }
                },
                dismissButton = {
                    TextButton(onClick = { onEvent(ProduccionUiEvent.OnDismissRegistrarMermaDialog) }) {
                        Text("Cancelar", color = TextSecondary)
                    }
                }
            )
        }
    }
}

@Preview(showBackground = true, showSystemUi = true)
@Composable
fun ProduccionScreenPreview() {
    TortilleriaDerekTheme {
        ProduccionScreenContent(
            uiState = ProduccionUiState(
                producidoNeto = 154.0,
                totalVendidoKg = 139.5,
                disponibleEnTienda = 14.5,
                porcentajeVenta = 90.5
            ),
            onEvent = {}
        )
    }
}
