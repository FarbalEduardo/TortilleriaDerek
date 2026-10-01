package com.example.tortilleriaderek.ui.screens

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.Factory
import androidx.compose.material.icons.filled.LocalOffer
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.People
import androidx.compose.material.icons.filled.Security
import androidx.compose.material.icons.filled.TwoWheeler
import androidx.compose.material.icons.filled.Visibility
import androidx.compose.material.icons.filled.VisibilityOff
import androidx.compose.material3.*
import androidx.compose.runtime.*
import kotlinx.coroutines.launch
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.tortilleriaderek.presentation.configuracion.ConfiguracionViewModel
import com.example.tortilleriaderek.ui.components.TortilleriaNavBar
import com.example.tortilleriaderek.ui.theme.*

data class ProductoConfig(
    val id: String,
    val monograma: String,
    val nombre: String,
    val descripcionPeso: String,
    val precio: Double,
    val pesoGramos: Int = 1000,
    val esPesoEditable: Boolean = false
)

data class RepartidorConfig(
    val id: String,
    val numeroBadge: String,
    val nombre: String,
    val detalleRuta: String
)

data class UsuarioConfig(
    val id: String,
    val username: String,
    val rol: String // "ADMIN", "EMPLEADO"
)

/**
 * Pantalla de Ajustes ("Configuración del Sistema") 100% fiel a la Imagen 3.
 *
 * Administra el catálogo de precios y pesos, los estándares de producción
 * (rendimiento neto y merma tolerada), la gestión de choferes/repartidores con sus motos,
 * y la administración completa de usuarios y credenciales del sistema (CRUD).
 */
@Composable
fun ConfiguracionScreen(
    viewModel: ConfiguracionViewModel = hiltViewModel(),
    onNavigateToVenta: () -> Unit = {},
    onNavigateToProduccion: () -> Unit = {},
    onNavigateToMetricas: () -> Unit = {},
    onNavigateToConfiguracion: () -> Unit = {},
    onBackToLogin: (() -> Unit)? = null,
    mostrarBottomBar: Boolean = true,
    modifier: Modifier = Modifier
) {
    val configProduccion by viewModel.configuracion.collectAsStateWithLifecycle()
    var showEditEstandaresDialog by remember { mutableStateOf(false) }
    var editPesoBultoInput by remember { mutableStateOf("") }
    var editKgMasaInput by remember { mutableStateOf("") }
    var editRendimientoInput by remember { mutableStateOf("") }
    var editMermaInput by remember { mutableStateOf("") }

    val productos = listOf(
        ProductoConfig("1", "1k", "Kilo Completo", "1,000 g peso estándar", configProduccion.precioKilo, pesoGramos = 1000, esPesoEditable = false),
        ProductoConfig("2", "½", "Medio Paquete", "${configProduccion.pesoMedioPaqueteGramos} g peso estándar", configProduccion.precioMedioPaquete, pesoGramos = configProduccion.pesoMedioPaqueteGramos, esPesoEditable = true),
        ProductoConfig("3", "Pq", "Paquete Mostrador", "${configProduccion.pesoPaqueteGramos} g peso estándar", configProduccion.precioPaquete, pesoGramos = configProduccion.pesoPaqueteGramos, esPesoEditable = true),
        ProductoConfig("4", "My", "Paquete Mayoreo (Reparto)", "${configProduccion.pesoPaqueteGramos} g tarifa mayoreo", configProduccion.precioPaqueteRepartidor, pesoGramos = configProduccion.pesoPaqueteGramos, esPesoEditable = true)
    )

    val repartidores by viewModel.repartidores.collectAsStateWithLifecycle()
    val usuariosEntity by viewModel.usuarios.collectAsStateWithLifecycle()
    val usuarios = remember(usuariosEntity) {
        usuariosEntity.map { UsuarioConfig(it.id, it.username, it.rol) }
    }

    var productoAEditar by remember { mutableStateOf<ProductoConfig?>(null) }
    var nuevoPrecioInput by remember { mutableStateOf("") }
    var nuevoPesoInput by remember { mutableStateOf("") }

    var showAddRepartidorDialog by remember { mutableStateOf(false) }
    var nuevoRepartidorNombre by remember { mutableStateOf("") }
    var nuevaMotoRuta by remember { mutableStateOf("") }

    // Estados para Gestión de Usuarios
    var showAddUsuarioDialog by remember { mutableStateOf(false) }
    var nuevoUsuarioUsername by remember { mutableStateOf("") }
    var nuevoUsuarioPassword by remember { mutableStateOf("") }
    var nuevoUsuarioRol by remember { mutableStateOf("EMPLEADO") }
    var nuevoUsuarioPasswordVisible by remember { mutableStateOf(false) }

    var usuarioAEditar by remember { mutableStateOf<UsuarioConfig?>(null) }
    var nuevoUsernameEdit by remember { mutableStateOf("") }
    var nuevoRolEdit by remember { mutableStateOf("EMPLEADO") }
    var nuevaPasswordEdit by remember { mutableStateOf("") }
    var nuevaPasswordEditVisible by remember { mutableStateOf(false) }

    var usuarioAEliminar by remember { mutableStateOf<UsuarioConfig?>(null) }
    var errorAlertaMensaje by remember { mutableStateOf<String?>(null) }

    // Control de permisos: ¿Estamos en la cuenta admin1 o se accede desde el login?
    val esAdmin1Activo by viewModel.esAdmin1Activo.collectAsStateWithLifecycle()
    val esAccesoDesdeLogin = onBackToLogin != null
    val coroutineScope = rememberCoroutineScope()

    var pendingAction by remember { mutableStateOf<(() -> Unit)?>(null) }
    var showAdminConfirmDialog by remember { mutableStateOf(false) }
    var adminConfirmUsernameInput by remember { mutableStateOf("admin1") }
    var adminConfirmPasswordInput by remember { mutableStateOf("") }
    var adminConfirmPasswordVisible by remember { mutableStateOf(false) }
    var adminConfirmError by remember { mutableStateOf<String?>(null) }

    fun ejecutarConPermisoAdmin(accion: () -> Unit) {
        if (esAdmin1Activo) {
            accion()
        } else {
            pendingAction = accion
            adminConfirmUsernameInput = "admin1"
            adminConfirmPasswordInput = ""
            adminConfirmPasswordVisible = false
            adminConfirmError = null
            showAdminConfirmDialog = true
        }
    }

    Scaffold(
        modifier = modifier.fillMaxSize(),
        containerColor = SurfaceWarm,
        contentWindowInsets = WindowInsets.navigationBars,
        bottomBar = {
            if (mostrarBottomBar) {
                TortilleriaNavBar(
                    activeItem = "Ajustes",
                    onNavigateToVenta = onNavigateToVenta,
                    onNavigateToProduccion = onNavigateToProduccion,
                    onNavigateToMetricas = onNavigateToMetricas,
                    onNavigateToConfiguracion = onNavigateToConfiguracion
                )
            }
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
            // ── 1. CABECERA: TÍTULO, SUBTÍTULO Y BOTÓN DE RETROCESO (FLECHA) ────────────
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                if (onBackToLogin != null) {
                    Surface(
                        shape = CircleShape,
                        color = Color.White,
                        border = BorderStroke(1.dp, BorderSubtle),
                        modifier = Modifier.size(38.dp)
                    ) {
                        IconButton(
                            onClick = onBackToLogin,
                            modifier = Modifier
                                .fillMaxSize()
                                .testTag("configuracion_back_button")
                        ) {
                            Icon(
                                imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                                contentDescription = "Regresar al Login",
                                tint = TextPrimary,
                                modifier = Modifier.size(18.dp)
                            )
                        }
                    }
                }

                Column(
                    modifier = Modifier.weight(1f),
                    verticalArrangement = Arrangement.spacedBy(2.dp)
                ) {
                    Text(
                        text = "Configuración del Sistema",
                        fontSize = 22.sp,
                        fontWeight = FontWeight.Black,
                        color = TextPrimary
                    )
                    Text(
                        text = "Parámetros operativos y catálogo activo",
                        fontSize = 12.sp,
                        color = TextSecondary
                    )
                }
            }

            // ── 2. CARD: PRECIOS Y PESOS DE PRODUCTOS ────────────────────────
            Surface(
                modifier = Modifier
                    .fillMaxWidth()
                    .testTag("ajustes_precios_card"),
                shape = RoundedCornerShape(22.dp),
                color = Color.White,
                border = BorderStroke(1.dp, BorderSubtle),
                shadowElevation = 2.dp
            ) {
                Column(
                    modifier = Modifier.padding(16.dp),
                    verticalArrangement = Arrangement.spacedBy(14.dp)
                ) {
                    // Encabezado de la Card
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Row(
                            modifier = Modifier.weight(1f),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(10.dp)
                        ) {
                            Box(
                                modifier = Modifier
                                    .size(36.dp)
                                    .clip(RoundedCornerShape(10.dp))
                                    .background(Color(0xFFFFF3EB)),
                                contentAlignment = Alignment.Center
                            ) {
                                Icon(
                                    imageVector = Icons.Default.LocalOffer,
                                    contentDescription = null,
                                    tint = MaizPrimary,
                                    modifier = Modifier.size(18.dp)
                                )
                            }
                            Column(modifier = Modifier.weight(1f)) {
                                Text(
                                    text = "Precios y Pesos de Productos",
                                    fontSize = 14.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = TextPrimary
                                )
                                Text(
                                    text = "Mostrador y venta mayoreo",
                                    fontSize = 11.sp,
                                    color = TextSecondary
                                )
                            }
                        }

                        Spacer(modifier = Modifier.width(8.dp))

                        // Badge: 4 activos
                        Surface(
                            shape = RoundedCornerShape(10.dp),
                            color = Color(0xFFFFF5EE),
                            border = BorderStroke(1.dp, Color(0xFFFFDEC9))
                        ) {
                            Text(
                                text = "${productos.size} activos",
                                fontSize = 11.sp,
                                fontWeight = FontWeight.Bold,
                                color = Color(0xFFE05300),
                                modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                            )
                        }
                    }

                    // Lista de Productos
                    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                        productos.forEach { prod ->
                            Surface(
                                modifier = Modifier.fillMaxWidth(),
                                shape = RoundedCornerShape(14.dp),
                                color = if (prod.id == "4") Color(0xFFFFFDFB) else Color(0xFFFAFAF9),
                                border = BorderStroke(1.dp, if (prod.id == "4") Color(0xFFFFDEC9) else Color(0xFFEFECE6))
                            ) {
                                Row(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .padding(horizontal = 12.dp, vertical = 10.dp),
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.SpaceBetween
                                ) {
                                    Row(
                                        modifier = Modifier.weight(1f),
                                        verticalAlignment = Alignment.CenterVertically,
                                        horizontalArrangement = Arrangement.spacedBy(10.dp)
                                    ) {
                                        // Monograma
                                        Box(
                                            modifier = Modifier
                                                .size(34.dp)
                                                .clip(RoundedCornerShape(8.dp))
                                                .background(if (prod.id == "4") Color(0xFFFFF3EB) else Color.White),
                                            contentAlignment = Alignment.Center
                                        ) {
                                            if (prod.id == "4") {
                                                Icon(
                                                    imageVector = Icons.Default.TwoWheeler,
                                                    contentDescription = null,
                                                    tint = MaizPrimary,
                                                    modifier = Modifier.size(18.dp)
                                                )
                                            } else {
                                                Text(
                                                    text = prod.monograma,
                                                    fontSize = 12.sp,
                                                    fontWeight = FontWeight.Bold,
                                                    color = TextPrimary
                                                )
                                            }
                                        }

                                        // Nombre y Descripción
                                        Column(
                                            modifier = Modifier.weight(1f),
                                            verticalArrangement = Arrangement.spacedBy(1.dp)
                                        ) {
                                            Text(
                                                text = prod.nombre,
                                                fontSize = 13.sp,
                                                fontWeight = FontWeight.Bold,
                                                color = TextPrimary
                                            )
                                            Text(
                                                text = prod.descripcionPeso,
                                                fontSize = 11.sp,
                                                color = if (prod.id == "4") Color(0xFFC04B00) else TextSecondary
                                            )
                                        }
                                    }

                                    // Pastilla de Precio y Botón de Edición
                                    Row(
                                        verticalAlignment = Alignment.CenterVertically,
                                        horizontalArrangement = Arrangement.spacedBy(6.dp)
                                    ) {
                                        Surface(
                                            shape = RoundedCornerShape(8.dp),
                                            color = Color.White,
                                            border = BorderStroke(1.dp, if (prod.id == "4") Color(0xFFFFDEC9) else BorderSubtle)
                                        ) {
                                            Text(
                                                text = "$${String.format("%.2f", prod.precio)} MXN",
                                                fontSize = 12.sp,
                                                fontWeight = FontWeight.ExtraBold,
                                                color = if (prod.id == "4") Color(0xFFC04B00) else TextPrimary,
                                                modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                                            )
                                        }

                                        IconButton(
                                            onClick = {
                                                productoAEditar = prod
                                                nuevoPrecioInput = String.format("%.2f", prod.precio)
                                                nuevoPesoInput = "${prod.pesoGramos}"
                                            },
                                            modifier = Modifier.size(32.dp)
                                        ) {
                                            Icon(
                                                imageVector = Icons.Default.Edit,
                                                contentDescription = "Editar ${prod.nombre}",
                                                tint = TextSecondary,
                                                modifier = Modifier.size(16.dp)
                                            )
                                        }
                                    }
                                }
                            }
                        }
                    }
                }
            }

            // ── 3. CARD: ESTÁNDARES DE PRODUCCIÓN ─────────────────────────────
            Surface(
                modifier = Modifier
                    .fillMaxWidth()
                    .testTag("ajustes_estandares_card"),
                shape = RoundedCornerShape(22.dp),
                color = Color.White,
                border = BorderStroke(1.dp, BorderSubtle),
                shadowElevation = 2.dp
            ) {
                Column(
                    modifier = Modifier.padding(16.dp),
                    verticalArrangement = Arrangement.spacedBy(14.dp)
                ) {
                    // Encabezado con Botón Editar
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(10.dp)
                        ) {
                            Box(
                                modifier = Modifier
                                    .size(36.dp)
                                    .clip(RoundedCornerShape(10.dp))
                                    .background(Color(0xFFFFF3EB)),
                                contentAlignment = Alignment.Center
                            ) {
                                Icon(
                                    imageVector = Icons.Default.Factory,
                                    contentDescription = null,
                                    tint = MaizPrimary,
                                    modifier = Modifier.size(18.dp)
                                )
                            }
                            Column {
                                Text(
                                    text = "Estándares de Producción",
                                    fontSize = 15.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = TextPrimary
                                )
                                Text(
                                    text = "Bulto estándar (%.1f kg) • Masa %.1f kg".format(configProduccion.pesoBultoHarinaKg, configProduccion.kgMasaPorBulto),
                                    fontSize = 11.sp,
                                    color = TextSecondary
                                )
                            }
                        }

                        IconButton(
                            onClick = {
                                editPesoBultoInput = configProduccion.pesoBultoHarinaKg.toString()
                                editKgMasaInput = configProduccion.kgMasaPorBulto.toString()
                                editRendimientoInput = configProduccion.rendimientoTortillaPorBulto.toString()
                                editMermaInput = configProduccion.mermaToleradaKgPorBulto.toString()
                                showEditEstandaresDialog = true
                            },
                            modifier = Modifier.size(32.dp).testTag("ajustes_btn_editar_estandares")
                        ) {
                            Icon(
                                imageVector = Icons.Default.Edit,
                                contentDescription = "Editar Estándares de Producción",
                                tint = TextSecondary,
                                modifier = Modifier.size(18.dp)
                            )
                        }
                    }

                    // Sub-cards lado a lado (Rendimiento Neto y Merma Tolerada)
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        // Rendimiento Neto
                        Surface(
                            modifier = Modifier.weight(1f),
                            shape = RoundedCornerShape(16.dp),
                            color = Color(0xFFFFFBF7),
                            border = BorderStroke(1.dp, Color(0xFFFFE8D6))
                        ) {
                            Column(
                                modifier = Modifier.padding(14.dp),
                                verticalArrangement = Arrangement.spacedBy(3.dp)
                            ) {
                                Text(
                                    text = "RENDIMIENTO NETO",
                                    fontSize = 10.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = Color(0xFFC04B00),
                                    letterSpacing = 0.4.sp
                                )
                                Text(
                                    text = "%.1f kg".format(configProduccion.rendimientoTortillaPorBulto),
                                    fontSize = 20.sp,
                                    fontWeight = FontWeight.Black,
                                    color = TextPrimary
                                )
                                Text(
                                    text = "Tortilla cocida / bulto",
                                    fontSize = 11.sp,
                                    color = TextSecondary
                                )
                            }
                        }

                        // Merma Tolerada
                        Surface(
                            modifier = Modifier.weight(1f),
                            shape = RoundedCornerShape(16.dp),
                            color = Color(0xFFF2FBF6),
                            border = BorderStroke(1.dp, Color(0xFFD4F3E2))
                        ) {
                            Column(
                                modifier = Modifier.padding(14.dp),
                                verticalArrangement = Arrangement.spacedBy(3.dp)
                            ) {
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Text(
                                        text = "MERMA\nTOLERADA",
                                        fontSize = 9.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = Color(0xFF15803D),
                                        letterSpacing = 0.4.sp,
                                        lineHeight = 11.sp
                                    )
                                    Surface(
                                        shape = RoundedCornerShape(8.dp),
                                        color = Color(0xFFDCFCE7)
                                    ) {
                                        Text(
                                            text = "%.1f%%".format(configProduccion.porcentajeMermaTolerada),
                                            fontSize = 10.sp,
                                            fontWeight = FontWeight.Bold,
                                            color = Color(0xFF15803D),
                                            modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                                        )
                                    }
                                }
                                Text(
                                    text = "%.1f kg".format(configProduccion.mermaToleradaKgPorBulto),
                                    fontSize = 20.sp,
                                    fontWeight = FontWeight.Black,
                                    color = TextPrimary
                                )
                                Text(
                                    text = "Máximo permitido / bulto",
                                    fontSize = 11.sp,
                                    color = TextSecondary
                                )
                            }
                        }
                    }
                }
            }

            // ── 4. CARD: GESTIÓN DE REPARTIDORES ──────────────────────────────
            Surface(
                modifier = Modifier
                    .fillMaxWidth()
                    .testTag("ajustes_repartidores_card"),
                shape = RoundedCornerShape(22.dp),
                color = Color.White,
                border = BorderStroke(1.dp, BorderSubtle),
                shadowElevation = 2.dp
            ) {
                Column(
                    modifier = Modifier.padding(16.dp),
                    verticalArrangement = Arrangement.spacedBy(14.dp)
                ) {
                    // Encabezado con Botón "+ Agregar"
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(10.dp)
                        ) {
                            Box(
                                modifier = Modifier
                                    .size(36.dp)
                                    .clip(RoundedCornerShape(10.dp))
                                    .background(Color(0xFFFFF3EB)),
                                contentAlignment = Alignment.Center
                            ) {
                                Icon(
                                    imageVector = Icons.Default.TwoWheeler,
                                    contentDescription = null,
                                    tint = MaizPrimary,
                                    modifier = Modifier.size(20.dp)
                                )
                            }
                            Column {
                                Text(
                                    text = "Gestión de Repartidores",
                                    fontSize = 15.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = TextPrimary
                                )
                                Text(
                                    text = "Motos y rutas de mayoreo",
                                    fontSize = 11.sp,
                                    color = TextSecondary
                                )
                            }
                        }

                        // Botón "+ Agregar"
                        Surface(
                            onClick = { showAddRepartidorDialog = true },
                            shape = RoundedCornerShape(14.dp),
                            color = Color(0xFFFFF4EC)
                        ) {
                            Row(
                                modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp),
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(4.dp)
                            ) {
                                Icon(
                                    imageVector = Icons.Default.Add,
                                    contentDescription = null,
                                    tint = Color(0xFFE05300),
                                    modifier = Modifier.size(14.dp)
                                )
                                Text(
                                    text = "Agregar",
                                    fontSize = 12.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = Color(0xFFE05300)
                                )
                            }
                        }
                    }

                    // Lista de Repartidores
                    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                        if (repartidores.isEmpty()) {
                            Surface(
                                modifier = Modifier.fillMaxWidth(),
                                shape = RoundedCornerShape(12.dp),
                                color = Color(0xFFFAFAF9),
                                border = BorderStroke(1.dp, Color(0xFFEFECE6))
                            ) {
                                Text(
                                    text = "No hay repartidores registrados. Agrega uno con el botón '+ Agregar'.",
                                    fontSize = 12.sp,
                                    color = TextSecondary,
                                    modifier = Modifier.padding(14.dp)
                                )
                            }
                        } else {
                            repartidores.forEach { rep ->
                                Surface(
                                    modifier = Modifier.fillMaxWidth(),
                                    shape = RoundedCornerShape(14.dp),
                                    color = Color(0xFFFAFAF9),
                                    border = BorderStroke(1.dp, Color(0xFFEFECE6))
                                ) {
                                    Row(
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .padding(horizontal = 12.dp, vertical = 10.dp),
                                        verticalAlignment = Alignment.CenterVertically,
                                        horizontalArrangement = Arrangement.SpaceBetween
                                    ) {
                                        Row(
                                            verticalAlignment = Alignment.CenterVertically,
                                            horizontalArrangement = Arrangement.spacedBy(10.dp)
                                        ) {
                                            // Badge #01, #02
                                            Box(
                                                modifier = Modifier
                                                    .size(34.dp)
                                                    .clip(RoundedCornerShape(8.dp))
                                                    .background(Color(0xFFFFF3E6)),
                                                contentAlignment = Alignment.Center
                                            ) {
                                                Text(
                                                    text = rep.numeroBadge,
                                                    fontSize = 12.sp,
                                                    fontWeight = FontWeight.Bold,
                                                    color = Color(0xFFC04B00)
                                                )
                                            }

                                            // Nombre y Ruta
                                            Column(verticalArrangement = Arrangement.spacedBy(1.dp)) {
                                                Text(
                                                    text = rep.nombre,
                                                    fontSize = 13.sp,
                                                    fontWeight = FontWeight.Bold,
                                                    color = TextPrimary
                                                )
                                                Text(
                                                    text = rep.detalleRuta,
                                                    fontSize = 11.sp,
                                                    color = TextSecondary
                                                )
                                            }
                                        }

                                        // Acciones: Editar y Borrar
                                        Row(
                                            verticalAlignment = Alignment.CenterVertically,
                                            horizontalArrangement = Arrangement.spacedBy(4.dp)
                                        ) {
                                            IconButton(
                                                onClick = { /* Editar repartidor */ },
                                                modifier = Modifier.size(30.dp)
                                            ) {
                                                Icon(
                                                    imageVector = Icons.Default.Edit,
                                                    contentDescription = "Editar ${rep.nombre}",
                                                    tint = TextSecondary,
                                                    modifier = Modifier.size(15.dp)
                                                )
                                            }

                                            IconButton(
                                                onClick = {
                                                    val repId = rep.id
                                                    ejecutarConPermisoAdmin {
                                                        viewModel.eliminarRepartidor(repId)
                                                    }
                                                },
                                                modifier = Modifier.size(30.dp)
                                            ) {
                                                Icon(
                                                    imageVector = Icons.Default.Delete,
                                                    contentDescription = "Eliminar ${rep.nombre}",
                                                    tint = Color(0xFFA39E99),
                                                    modifier = Modifier.size(16.dp)
                                                )
                                            }
                                        }
                                    }
                                }
                            }
                        }
                    }
                }
            }

            // ── 5. CARD: GESTIÓN DE USUARIOS DEL SISTEMA (Solo visible si no se accede desde Login) ──────────────
            if (!esAccesoDesdeLogin) {
                Surface(
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("ajustes_usuarios_card"),
                    shape = RoundedCornerShape(22.dp),
                    color = Color.White,
                    border = BorderStroke(1.dp, BorderSubtle),
                    shadowElevation = 2.dp
                ) {
                    Column(
                        modifier = Modifier.padding(16.dp),
                        verticalArrangement = Arrangement.spacedBy(14.dp)
                    ) {
                        // Encabezado con Botón "+ Agregar"
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(10.dp)
                            ) {
                                Box(
                                    modifier = Modifier
                                        .size(36.dp)
                                        .clip(RoundedCornerShape(10.dp))
                                        .background(Color(0xFFFFF3EB)),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Icon(
                                        imageVector = Icons.Default.People,
                                        contentDescription = null,
                                        tint = MaizPrimary,
                                        modifier = Modifier.size(20.dp)
                                    )
                                }
                                Column {
                                    Text(
                                        text = "Gestión de Usuarios",
                                        fontSize = 15.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = TextPrimary
                                    )
                                    Text(
                                        text = "Cuentas y roles del sistema",
                                        fontSize = 11.sp,
                                        color = TextSecondary
                                    )
                                }
                            }

                            // Botón "+ Agregar"
                            Surface(
                                onClick = {
                                    nuevoUsuarioUsername = ""
                                    nuevoUsuarioPassword = ""
                                    nuevoUsuarioRol = "EMPLEADO"
                                    showAddUsuarioDialog = true
                                },
                                shape = RoundedCornerShape(14.dp),
                                color = Color(0xFFFFF4EC)
                            ) {
                                Row(
                                    modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp),
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.spacedBy(4.dp)
                                ) {
                                    Icon(
                                        imageVector = Icons.Default.Add,
                                        contentDescription = null,
                                        tint = Color(0xFFE05300),
                                        modifier = Modifier.size(14.dp)
                                    )
                                    Text(
                                        text = "Agregar",
                                        fontSize = 12.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = Color(0xFFE05300)
                                    )
                                }
                            }
                        }

                        // Lista de Usuarios
                        Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                            usuarios.forEach { usr ->
                                Surface(
                                    modifier = Modifier.fillMaxWidth(),
                                    shape = RoundedCornerShape(14.dp),
                                    color = Color(0xFFFAFAF9),
                                    border = BorderStroke(1.dp, Color(0xFFEFECE6))
                                ) {
                                    Row(
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .padding(horizontal = 12.dp, vertical = 10.dp),
                                        verticalAlignment = Alignment.CenterVertically,
                                        horizontalArrangement = Arrangement.SpaceBetween
                                    ) {
                                        Row(
                                            verticalAlignment = Alignment.CenterVertically,
                                            horizontalArrangement = Arrangement.spacedBy(10.dp)
                                        ) {
                                            // Avatar Inicial
                                            Box(
                                                modifier = Modifier
                                                    .size(34.dp)
                                                    .clip(CircleShape)
                                                    .background(if (usr.rol == "ADMIN") Color(0xFFFFEDE0) else Color(0xFFEDF2F7)),
                                                contentAlignment = Alignment.Center
                                            ) {
                                                Text(
                                                    text = usr.username.take(2).uppercase(),
                                                    fontSize = 12.sp,
                                                    fontWeight = FontWeight.Bold,
                                                    color = if (usr.rol == "ADMIN") Color(0xFFE05300) else Color(0xFF4A5568)
                                                )
                                            }

                                            Column(verticalArrangement = Arrangement.spacedBy(2.dp)) {
                                                Row(
                                                    verticalAlignment = Alignment.CenterVertically,
                                                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                                                ) {
                                                    Text(
                                                        text = usr.username,
                                                        fontSize = 13.sp,
                                                        fontWeight = FontWeight.Bold,
                                                        color = TextPrimary
                                                    )
                                                    if (usr.username.equals("admin1", ignoreCase = true) || usr.id == "1") {
                                                        Surface(
                                                            shape = RoundedCornerShape(4.dp),
                                                            color = Color(0xFFFEF3C7)
                                                        ) {
                                                            Text(
                                                                text = "PRINCIPAL",
                                                                fontSize = 9.sp,
                                                                fontWeight = FontWeight.Bold,
                                                                color = Color(0xFFB45309),
                                                                modifier = Modifier.padding(horizontal = 4.dp, vertical = 1.dp)
                                                            )
                                                        }
                                                    }
                                                }
                                                Surface(
                                                    shape = RoundedCornerShape(6.dp),
                                                    color = if (usr.rol == "ADMIN") Color(0xFFFFF3E6) else Color(0xFFEDF2F7)
                                                ) {
                                                    Text(
                                                        text = usr.rol,
                                                        fontSize = 10.sp,
                                                        fontWeight = FontWeight.ExtraBold,
                                                        color = if (usr.rol == "ADMIN") Color(0xFFC04B00) else Color(0xFF4A5568),
                                                        modifier = Modifier.padding(horizontal = 6.dp, vertical = 1.dp)
                                                    )
                                                }
                                            }
                                        }

                                        // Acciones: Editar y Borrar (admin1 protegido contra borrado)
                                        Row(
                                            verticalAlignment = Alignment.CenterVertically,
                                            horizontalArrangement = Arrangement.spacedBy(4.dp)
                                        ) {
                                            IconButton(
                                                onClick = {
                                                    usuarioAEditar = usr
                                                    nuevoUsernameEdit = usr.username
                                                    nuevoRolEdit = usr.rol
                                                    nuevaPasswordEdit = ""
                                                },
                                                modifier = Modifier.size(30.dp)
                                            ) {
                                                Icon(
                                                    imageVector = Icons.Default.Edit,
                                                    contentDescription = "Editar ${usr.username}",
                                                    tint = TextSecondary,
                                                    modifier = Modifier.size(15.dp)
                                                )
                                            }

                                            if (!usr.username.equals("admin1", ignoreCase = true) && usr.id != "1") {
                                                IconButton(
                                                    onClick = {
                                                        usuarioAEliminar = usr
                                                    },
                                                    modifier = Modifier.size(30.dp)
                                                ) {
                                                    Icon(
                                                        imageVector = Icons.Default.Delete,
                                                        contentDescription = "Eliminar ${usr.username}",
                                                        tint = Color(0xFFA39E99),
                                                        modifier = Modifier.size(16.dp)
                                                    )
                                                }
                                            } else {
                                                Box(
                                                    modifier = Modifier.size(30.dp),
                                                    contentAlignment = Alignment.Center
                                                ) {
                                                    Icon(
                                                        imageVector = Icons.Default.Lock,
                                                        contentDescription = "Cuenta Principal Protegida",
                                                        tint = Color(0xFFD1D5DB),
                                                        modifier = Modifier.size(15.dp)
                                                    )
                                                }
                                            }
                                        }
                                    }
                                }
                            }
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(10.dp))
        }
    }

    // ── DIÁLOGO EDITAR PRECIO PRODUCTO ────────────────────────────────────────
    productoAEditar?.let { prod ->
        AlertDialog(
            onDismissRequest = { productoAEditar = null },
            shape = RoundedCornerShape(20.dp),
            containerColor = Color.White,
            titleContentColor = Color(0xFF111827),
            textContentColor = Color(0xFF111827),
            title = {
                Text(
                    text = "Editar Precio",
                    fontWeight = FontWeight.Bold,
                    fontSize = 16.sp,
                    color = Color(0xFF111827)
                )
            },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    Text(
                        text = "${prod.nombre} (${prod.descripcionPeso})",
                        fontSize = 13.sp,
                        color = Color(0xFF374151)
                    )
                    OutlinedTextField(
                        value = nuevoPrecioInput,
                        onValueChange = { nuevoPrecioInput = it },
                        textStyle = LocalTextStyle.current.copy(
                            color = Color(0xFF111827),
                            fontWeight = FontWeight.SemiBold,
                            fontSize = 15.sp
                        ),
                        label = { Text("Precio Unitario", color = Color(0xFF374151)) },
                        prefix = { Text("$ ", fontWeight = FontWeight.Bold, color = Color(0xFF111827)) },
                        suffix = { Text("MXN", fontWeight = FontWeight.Medium, color = Color(0xFF374151)) },
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                        singleLine = true,
                        shape = RoundedCornerShape(12.dp),
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedTextColor = Color(0xFF111827),
                            unfocusedTextColor = Color(0xFF111827),
                            focusedContainerColor = Color.White,
                            unfocusedContainerColor = Color.White,
                            focusedBorderColor = MaizPrimary,
                            unfocusedBorderColor = Color(0xFFD1D5DB),
                            focusedLabelColor = MaizPrimary,
                            unfocusedLabelColor = Color(0xFF374151),
                            focusedPrefixColor = Color(0xFF111827),
                            unfocusedPrefixColor = Color(0xFF111827),
                            focusedSuffixColor = Color(0xFF374151),
                            unfocusedSuffixColor = Color(0xFF374151)
                        )
                    )
                    if (prod.esPesoEditable) {
                        OutlinedTextField(
                            value = nuevoPesoInput,
                            onValueChange = { nuevoPesoInput = it },
                            textStyle = LocalTextStyle.current.copy(
                                color = Color(0xFF111827),
                                fontWeight = FontWeight.SemiBold,
                                fontSize = 15.sp
                            ),
                            label = { Text("Tamaño / Peso del Paquete", color = Color(0xFF374151)) },
                            suffix = { Text("gramos", fontWeight = FontWeight.Bold, color = Color(0xFF111827)) },
                            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                            singleLine = true,
                            shape = RoundedCornerShape(12.dp),
                            colors = OutlinedTextFieldDefaults.colors(
                                focusedTextColor = Color(0xFF111827),
                                unfocusedTextColor = Color(0xFF111827),
                                focusedContainerColor = Color.White,
                                unfocusedContainerColor = Color.White,
                                focusedBorderColor = MaizPrimary,
                                unfocusedBorderColor = Color(0xFFD1D5DB),
                                focusedLabelColor = MaizPrimary,
                                unfocusedLabelColor = Color(0xFF374151),
                                focusedSuffixColor = Color(0xFF111827),
                                unfocusedSuffixColor = Color(0xFF111827)
                            )
                        )
                    }
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        val precioParsed = nuevoPrecioInput.replace(',', '.').toDoubleOrNull()
                        val pesoParsed = nuevoPesoInput.toIntOrNull()
                        if (precioParsed != null && precioParsed >= 0) {
                            ejecutarConPermisoAdmin {
                                viewModel.guardarPrecioYPesoProducto(prod.id, precioParsed, pesoParsed)
                                productoAEditar = null
                            }
                        } else {
                            productoAEditar = null
                        }
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = MaizPrimary),
                    shape = RoundedCornerShape(12.dp)
                ) {
                    Text("Guardar", fontWeight = FontWeight.Bold, color = Color.White)
                }
            },
            dismissButton = {
                TextButton(onClick = { productoAEditar = null }) {
                    Text("Cancelar", color = Color(0xFF4B5563))
                }
            }
        )
    }


    // ── DIÁLOGO AGREGAR REPARTIDOR ────────────────────────────────────────────
    if (showAddRepartidorDialog) {
        AlertDialog(
            onDismissRequest = { showAddRepartidorDialog = false },
            shape = RoundedCornerShape(20.dp),
            containerColor = Color.White,
            titleContentColor = Color(0xFF111827),
            textContentColor = Color(0xFF111827),
            title = {
                Text(
                    text = "Nuevo Repartidor",
                    fontWeight = FontWeight.Bold,
                    fontSize = 16.sp,
                    color = Color(0xFF111827)
                )
            },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                    OutlinedTextField(
                        value = nuevoRepartidorNombre,
                        onValueChange = { nuevoRepartidorNombre = it },
                        textStyle = LocalTextStyle.current.copy(
                            color = Color(0xFF111827),
                            fontWeight = FontWeight.Medium,
                            fontSize = 14.sp
                        ),
                        label = { Text("Nombre Completo", color = Color(0xFF374151)) },
                        singleLine = true,
                        shape = RoundedCornerShape(12.dp),
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedTextColor = Color(0xFF111827),
                            unfocusedTextColor = Color(0xFF111827),
                            focusedContainerColor = Color.White,
                            unfocusedContainerColor = Color.White,
                            focusedBorderColor = MaizPrimary,
                            unfocusedBorderColor = Color(0xFFD1D5DB),
                            focusedLabelColor = MaizPrimary,
                            unfocusedLabelColor = Color(0xFF374151)
                        )
                    )
                    OutlinedTextField(
                        value = nuevaMotoRuta,
                        onValueChange = { nuevaMotoRuta = it },
                        textStyle = LocalTextStyle.current.copy(
                            color = Color(0xFF111827),
                            fontWeight = FontWeight.Medium,
                            fontSize = 14.sp
                        ),
                        label = { Text("Moto y Ruta (ej. Moto Honda • San Juan)", color = Color(0xFF374151)) },
                        singleLine = true,
                        shape = RoundedCornerShape(12.dp),
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedTextColor = Color(0xFF111827),
                            unfocusedTextColor = Color(0xFF111827),
                            focusedContainerColor = Color.White,
                            unfocusedContainerColor = Color.White,
                            focusedBorderColor = MaizPrimary,
                            unfocusedBorderColor = Color(0xFFD1D5DB),
                            focusedLabelColor = MaizPrimary,
                            unfocusedLabelColor = Color(0xFF374151)
                        )
                    )
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        val nom = nuevoRepartidorNombre.trim()
                        val rut = nuevaMotoRuta.trim()
                        if (nom.isNotBlank()) {
                            ejecutarConPermisoAdmin {
                                viewModel.agregarRepartidor(
                                    nombre = nom,
                                    motoRuta = rut
                                )
                                nuevoRepartidorNombre = ""
                                nuevaMotoRuta = ""
                                showAddRepartidorDialog = false
                            }
                        }
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = MaizPrimary),
                    shape = RoundedCornerShape(12.dp)
                ) {
                    Text("Agregar", fontWeight = FontWeight.Bold, color = Color.White)
                }
            },
            dismissButton = {
                TextButton(onClick = { showAddRepartidorDialog = false }) {
                    Text("Cancelar", color = Color(0xFF4B5563))
                }
            }
        )
    }

    // ── DIÁLOGO AGREGAR USUARIO (CRUD) ────────────────────────────────────────
    if (showAddUsuarioDialog) {
        AlertDialog(
            onDismissRequest = { showAddUsuarioDialog = false },
            shape = RoundedCornerShape(20.dp),
            containerColor = Color.White,
            titleContentColor = Color(0xFF111827),
            textContentColor = Color(0xFF111827),
            title = {
                Text(
                    text = "Nuevo Usuario",
                    fontWeight = FontWeight.Bold,
                    fontSize = 16.sp,
                    color = Color(0xFF111827)
                )
            },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                    OutlinedTextField(
                        value = nuevoUsuarioUsername,
                        onValueChange = { nuevoUsuarioUsername = it },
                        textStyle = LocalTextStyle.current.copy(
                            color = Color(0xFF111827),
                            fontWeight = FontWeight.Medium,
                            fontSize = 14.sp
                        ),
                        label = { Text("Nombre de Usuario", color = Color(0xFF374151)) },
                        singleLine = true,
                        shape = RoundedCornerShape(12.dp),
                        modifier = Modifier.fillMaxWidth(),
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedTextColor = Color(0xFF111827),
                            unfocusedTextColor = Color(0xFF111827),
                            focusedContainerColor = Color.White,
                            unfocusedContainerColor = Color.White,
                            focusedBorderColor = MaizPrimary,
                            unfocusedBorderColor = Color(0xFFD1D5DB),
                            focusedLabelColor = MaizPrimary,
                            unfocusedLabelColor = Color(0xFF374151)
                        )
                    )

                    // Selector de Rol
                    Text(
                        text = "Rol de Acceso",
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color(0xFF374151)
                    )
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Surface(
                            onClick = { nuevoUsuarioRol = "EMPLEADO" },
                            shape = RoundedCornerShape(10.dp),
                            color = if (nuevoUsuarioRol == "EMPLEADO") Color(0xFFFFF3EB) else Color(0xFFFAFAFA),
                            border = BorderStroke(1.dp, if (nuevoUsuarioRol == "EMPLEADO") MaizPrimary else BorderSubtle),
                            modifier = Modifier.weight(1f)
                        ) {
                            Text(
                                text = "Empleado",
                                fontSize = 12.sp,
                                fontWeight = FontWeight.Bold,
                                color = if (nuevoUsuarioRol == "EMPLEADO") Color(0xFFC04B00) else Color(0xFF4B5563),
                                modifier = Modifier.padding(vertical = 10.dp, horizontal = 12.dp)
                            )
                        }

                        Surface(
                            onClick = { nuevoUsuarioRol = "ADMIN" },
                            shape = RoundedCornerShape(10.dp),
                            color = if (nuevoUsuarioRol == "ADMIN") Color(0xFFFFF3EB) else Color(0xFFFAFAFA),
                            border = BorderStroke(1.dp, if (nuevoUsuarioRol == "ADMIN") MaizPrimary else BorderSubtle),
                            modifier = Modifier.weight(1f)
                        ) {
                            Text(
                                text = "Administrador",
                                fontSize = 12.sp,
                                fontWeight = FontWeight.Bold,
                                color = if (nuevoUsuarioRol == "ADMIN") Color(0xFFC04B00) else Color(0xFF4B5563),
                                modifier = Modifier.padding(vertical = 10.dp, horizontal = 12.dp)
                            )
                        }
                    }

                    OutlinedTextField(
                        value = nuevoUsuarioPassword,
                        onValueChange = { nuevoUsuarioPassword = it },
                        textStyle = LocalTextStyle.current.copy(
                            color = Color(0xFF111827),
                            fontWeight = FontWeight.Medium,
                            fontSize = 14.sp
                        ),
                        label = { Text("Contraseña o PIN", color = Color(0xFF374151)) },
                        visualTransformation = if (nuevoUsuarioPasswordVisible) VisualTransformation.None else PasswordVisualTransformation(),
                        trailingIcon = {
                            IconButton(onClick = { nuevoUsuarioPasswordVisible = !nuevoUsuarioPasswordVisible }) {
                                Icon(
                                    imageVector = if (nuevoUsuarioPasswordVisible) Icons.Default.VisibilityOff else Icons.Default.Visibility,
                                    contentDescription = null,
                                    tint = Color(0xFF4B5563)
                                )
                            }
                        },
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Password),
                        singleLine = true,
                        shape = RoundedCornerShape(12.dp),
                        modifier = Modifier.fillMaxWidth(),
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedTextColor = Color(0xFF111827),
                            unfocusedTextColor = Color(0xFF111827),
                            focusedContainerColor = Color.White,
                            unfocusedContainerColor = Color.White,
                            focusedBorderColor = MaizPrimary,
                            unfocusedBorderColor = Color(0xFFD1D5DB),
                            focusedLabelColor = MaizPrimary,
                            unfocusedLabelColor = Color(0xFF374151)
                        )
                    )
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        val cleanName = nuevoUsuarioUsername.trim()
                        val pass = nuevoUsuarioPassword
                        val rol = nuevoUsuarioRol
                        if (cleanName.length >= 3 && pass.trim().length >= 4) {
                            ejecutarConPermisoAdmin {
                                viewModel.crearUsuario(
                                    username = cleanName,
                                    passwordRaw = pass,
                                    rol = rol
                                ) { success, error ->
                                    if (success) {
                                        showAddUsuarioDialog = false
                                    } else {
                                        errorAlertaMensaje = error
                                    }
                                }
                            }
                        } else {
                            errorAlertaMensaje = "El usuario debe tener al menos 3 caracteres y la contraseña al menos 4."
                        }
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = MaizPrimary),
                    shape = RoundedCornerShape(12.dp)
                ) {
                    Text("Crear Usuario", fontWeight = FontWeight.Bold, color = Color.White)
                }
            },
            dismissButton = {
                TextButton(onClick = { showAddUsuarioDialog = false }) {
                    Text("Cancelar", color = Color(0xFF4B5563))
                }
            }
        )
    }

    // ── DIÁLOGO EDITAR USUARIO (CRUD) ─────────────────────────────────────────
    usuarioAEditar?.let { usr ->
        AlertDialog(
            onDismissRequest = { usuarioAEditar = null },
            shape = RoundedCornerShape(20.dp),
            containerColor = Color.White,
            titleContentColor = Color(0xFF111827),
            textContentColor = Color(0xFF111827),
            title = {
                Text(
                    text = "Editar Usuario",
                    fontWeight = FontWeight.Bold,
                    fontSize = 16.sp,
                    color = Color(0xFF111827)
                )
            },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                    val esAdmin1 = usr.username.equals("admin1", ignoreCase = true) || usr.id == "1"
                    OutlinedTextField(
                        value = nuevoUsernameEdit,
                        onValueChange = { if (!esAdmin1) nuevoUsernameEdit = it },
                        enabled = !esAdmin1,
                        textStyle = LocalTextStyle.current.copy(
                            color = Color(0xFF111827),
                            fontWeight = FontWeight.Medium,
                            fontSize = 14.sp
                        ),
                        label = { Text(if (esAdmin1) "Nombre de Usuario (admin1 Permanente)" else "Nombre de Usuario", color = Color(0xFF374151)) },
                        singleLine = true,
                        shape = RoundedCornerShape(12.dp),
                        modifier = Modifier.fillMaxWidth(),
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedTextColor = Color(0xFF111827),
                            unfocusedTextColor = Color(0xFF111827),
                            focusedContainerColor = Color.White,
                            unfocusedContainerColor = Color.White,
                            focusedBorderColor = MaizPrimary,
                            unfocusedBorderColor = Color(0xFFD1D5DB),
                            focusedLabelColor = MaizPrimary,
                            unfocusedLabelColor = Color(0xFF374151)
                        )
                    )

                    Text(
                        text = "Rol de Acceso",
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color(0xFF374151)
                    )
                    if (esAdmin1) {
                        Surface(
                            shape = RoundedCornerShape(10.dp),
                            color = Color(0xFFFFF3EB),
                            border = BorderStroke(1.dp, MaizPrimary),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Text(
                                text = "Administrador Principal (Rol Permanente)",
                                fontSize = 12.sp,
                                fontWeight = FontWeight.Bold,
                                color = Color(0xFFC04B00),
                                modifier = Modifier.padding(vertical = 10.dp, horizontal = 12.dp)
                            )
                        }
                    } else {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            Surface(
                                onClick = { nuevoRolEdit = "EMPLEADO" },
                                shape = RoundedCornerShape(10.dp),
                                color = if (nuevoRolEdit == "EMPLEADO") Color(0xFFFFF3EB) else Color(0xFFFAFAFA),
                                border = BorderStroke(1.dp, if (nuevoRolEdit == "EMPLEADO") MaizPrimary else BorderSubtle),
                                modifier = Modifier.weight(1f)
                            ) {
                                Text(
                                    text = "Empleado",
                                    fontSize = 12.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = if (nuevoRolEdit == "EMPLEADO") Color(0xFFC04B00) else Color(0xFF4B5563),
                                    modifier = Modifier.padding(vertical = 10.dp, horizontal = 12.dp)
                                )
                            }

                            Surface(
                                onClick = { nuevoRolEdit = "ADMIN" },
                                shape = RoundedCornerShape(10.dp),
                                color = if (nuevoRolEdit == "ADMIN") Color(0xFFFFF3EB) else Color(0xFFFAFAFA),
                                border = BorderStroke(1.dp, if (nuevoRolEdit == "ADMIN") MaizPrimary else BorderSubtle),
                                modifier = Modifier.weight(1f)
                            ) {
                                Text(
                                    text = "Administrador",
                                    fontSize = 12.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = if (nuevoRolEdit == "ADMIN") Color(0xFFC04B00) else Color(0xFF4B5563),
                                    modifier = Modifier.padding(vertical = 10.dp, horizontal = 12.dp)
                                )
                            }
                        }
                    }

                    OutlinedTextField(
                        value = nuevaPasswordEdit,
                        onValueChange = { nuevaPasswordEdit = it },
                        textStyle = LocalTextStyle.current.copy(
                            color = Color(0xFF111827),
                            fontWeight = FontWeight.Medium,
                            fontSize = 14.sp
                        ),
                        label = { Text("Nueva Contraseña / PIN (Opcional)", color = Color(0xFF374151)) },
                        visualTransformation = if (nuevaPasswordEditVisible) VisualTransformation.None else PasswordVisualTransformation(),
                        trailingIcon = {
                            IconButton(onClick = { nuevaPasswordEditVisible = !nuevaPasswordEditVisible }) {
                                Icon(
                                    imageVector = if (nuevaPasswordEditVisible) Icons.Default.VisibilityOff else Icons.Default.Visibility,
                                    contentDescription = null,
                                    tint = Color(0xFF4B5563)
                                )
                            }
                        },
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Password),
                        singleLine = true,
                        shape = RoundedCornerShape(12.dp),
                        modifier = Modifier.fillMaxWidth(),
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedTextColor = Color(0xFF111827),
                            unfocusedTextColor = Color(0xFF111827),
                            focusedContainerColor = Color.White,
                            unfocusedContainerColor = Color.White,
                            focusedBorderColor = MaizPrimary,
                            unfocusedBorderColor = Color(0xFFD1D5DB),
                            focusedLabelColor = MaizPrimary,
                            unfocusedLabelColor = Color(0xFF374151)
                        )
                    )
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        val cleanName = nuevoUsernameEdit.trim()
                        val rol = nuevoRolEdit
                        val pass = nuevaPasswordEdit.ifBlank { null }
                        if (cleanName.length >= 3) {
                            ejecutarConPermisoAdmin {
                                viewModel.editarUsuario(
                                    id = usr.id,
                                    nuevoUsername = cleanName,
                                    nuevoRol = rol,
                                    nuevaPasswordRaw = pass
                                ) { success, error ->
                                    if (success) {
                                        usuarioAEditar = null
                                    } else {
                                        errorAlertaMensaje = error
                                    }
                                }
                            }
                        }
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = MaizPrimary),
                    shape = RoundedCornerShape(12.dp)
                ) {
                    Text("Guardar Cambios", fontWeight = FontWeight.Bold, color = Color.White)
                }
            },
            dismissButton = {
                TextButton(onClick = { usuarioAEditar = null }) {
                    Text("Cancelar", color = Color(0xFF4B5563))
                }
            }
        )
    }

    // ── DIÁLOGO CONFIRMAR ELIMINACIÓN USUARIO ─────────────────────────────────
    usuarioAEliminar?.let { usr ->
        AlertDialog(
            onDismissRequest = { usuarioAEliminar = null },
            shape = RoundedCornerShape(20.dp),
            containerColor = Color.White,
            titleContentColor = Color(0xFF111827),
            textContentColor = Color(0xFF111827),
            title = {
                Text(
                    text = "Eliminar Usuario",
                    fontWeight = FontWeight.Bold,
                    fontSize = 16.sp,
                    color = Color(0xFF111827)
                )
            },
            text = {
                Text(
                    text = "¿Estás seguro de que deseas eliminar la cuenta '${usr.username}' (${usr.rol})? Esta acción revocará su acceso inmediatamente.",
                    fontSize = 13.sp,
                    color = Color(0xFF374151)
                )
            },
            confirmButton = {
                Button(
                    onClick = {
                        val usrId = usr.id
                        ejecutarConPermisoAdmin {
                            viewModel.eliminarUsuario(usrId) { success, error ->
                                if (!success) {
                                    errorAlertaMensaje = error
                                }
                                usuarioAEliminar = null
                            }
                        }
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = ErrorRed),
                    shape = RoundedCornerShape(12.dp)
                ) {
                    Text("Eliminar", fontWeight = FontWeight.Bold, color = Color.White)
                }
            },
            dismissButton = {
                TextButton(onClick = { usuarioAEliminar = null }) {
                    Text("Cancelar", color = Color(0xFF4B5563))
                }
            }
        )
    }

    // ── DIÁLOGO CONFIRMACIÓN / AUTORIZACIÓN ADMIN1 ────────────────────────────
    if (showAdminConfirmDialog) {
        AlertDialog(
            onDismissRequest = {
                showAdminConfirmDialog = false
                pendingAction = null
            },
            shape = RoundedCornerShape(20.dp),
            containerColor = Color.White,
            titleContentColor = Color(0xFF111827),
            textContentColor = Color(0xFF111827),
            title = {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    Box(
                        modifier = Modifier
                            .size(38.dp)
                            .clip(RoundedCornerShape(10.dp))
                            .background(Color(0xFFFFF3EB)),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = Icons.Default.Security,
                            contentDescription = null,
                            tint = MaizPrimary,
                            modifier = Modifier.size(22.dp)
                        )
                    }
                    Column {
                        Text(
                            text = "Autorización Requerida",
                            fontWeight = FontWeight.Bold,
                            fontSize = 16.sp,
                            color = Color(0xFF111827)
                        )
                        Text(
                            text = "Cuenta Principal admin1",
                            fontSize = 11.sp,
                            color = Color(0xFF6B7280)
                        )
                    }
                }
            },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                    Text(
                        text = "Para modificar la configuración del sistema, ingrese la confirmación de la cuenta de administración principal.",
                        fontSize = 13.sp,
                        color = Color(0xFF374151),
                        lineHeight = 18.sp
                    )

                    OutlinedTextField(
                        value = adminConfirmUsernameInput,
                        onValueChange = { adminConfirmUsernameInput = it },
                        textStyle = LocalTextStyle.current.copy(
                            color = Color(0xFF111827),
                            fontWeight = FontWeight.Medium,
                            fontSize = 14.sp
                        ),
                        label = { Text("Usuario Administrador", color = Color(0xFF374151)) },
                        singleLine = true,
                        shape = RoundedCornerShape(12.dp),
                        modifier = Modifier.fillMaxWidth(),
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedTextColor = Color(0xFF111827),
                            unfocusedTextColor = Color(0xFF111827),
                            focusedContainerColor = Color.White,
                            unfocusedContainerColor = Color.White,
                            focusedBorderColor = MaizPrimary,
                            unfocusedBorderColor = Color(0xFFD1D5DB),
                            focusedLabelColor = MaizPrimary,
                            unfocusedLabelColor = Color(0xFF374151)
                        )
                    )

                    OutlinedTextField(
                        value = adminConfirmPasswordInput,
                        onValueChange = {
                            adminConfirmPasswordInput = it
                            adminConfirmError = null
                        },
                        textStyle = LocalTextStyle.current.copy(
                            color = Color(0xFF111827),
                            fontWeight = FontWeight.Medium,
                            fontSize = 14.sp
                        ),
                        label = { Text("Contraseña de admin1", color = Color(0xFF374151)) },
                        visualTransformation = if (adminConfirmPasswordVisible) VisualTransformation.None else PasswordVisualTransformation(),
                        trailingIcon = {
                            IconButton(onClick = { adminConfirmPasswordVisible = !adminConfirmPasswordVisible }) {
                                Icon(
                                    imageVector = if (adminConfirmPasswordVisible) Icons.Default.VisibilityOff else Icons.Default.Visibility,
                                    contentDescription = null,
                                    tint = Color(0xFF4B5563)
                                )
                            }
                        },
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Password),
                        singleLine = true,
                        shape = RoundedCornerShape(12.dp),
                        modifier = Modifier.fillMaxWidth(),
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedTextColor = Color(0xFF111827),
                            unfocusedTextColor = Color(0xFF111827),
                            focusedContainerColor = Color.White,
                            unfocusedContainerColor = Color.White,
                            focusedBorderColor = MaizPrimary,
                            unfocusedBorderColor = Color(0xFFD1D5DB),
                            focusedLabelColor = MaizPrimary,
                            unfocusedLabelColor = Color(0xFF374151)
                        )
                    )

                    adminConfirmError?.let { err ->
                        Surface(
                            shape = RoundedCornerShape(8.dp),
                            color = Color(0xFFFEE2E2),
                            border = BorderStroke(1.dp, Color(0xFFEF4444)),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Text(
                                text = err,
                                color = Color(0xFFB91C1C),
                                fontSize = 12.sp,
                                fontWeight = FontWeight.SemiBold,
                                modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp)
                            )
                        }
                    }
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        coroutineScope.launch {
                            val esValido = viewModel.verificarCredencialesAdmin1(
                                adminConfirmUsernameInput,
                                adminConfirmPasswordInput
                            )
                            if (esValido) {
                                showAdminConfirmDialog = false
                                val accion = pendingAction
                                pendingAction = null
                                accion?.invoke()
                            } else {
                                adminConfirmError = "Credenciales incorrectas. Verifique el usuario y contraseña de admin1."
                            }
                        }
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = MaizPrimary),
                    shape = RoundedCornerShape(12.dp)
                ) {
                    Text("Autorizar", fontWeight = FontWeight.Bold, color = Color.White)
                }
            },
            dismissButton = {
                TextButton(
                    onClick = {
                        showAdminConfirmDialog = false
                        pendingAction = null
                    }
                ) {
                    Text("Cancelar", color = Color(0xFF4B5563))
                }
            }
        )
    }

    // ── ALERTA DE ERROR / VALIDACIÓN DE SEGURIDAD ────────────────────────────
    errorAlertaMensaje?.let { msg ->
        AlertDialog(
            onDismissRequest = { errorAlertaMensaje = null },
            shape = RoundedCornerShape(20.dp),
            containerColor = Color.White,
            titleContentColor = Color(0xFFC04B00),
            textContentColor = Color(0xFF111827),
            title = {
                Text(
                    text = "Aviso de Seguridad",
                    fontWeight = FontWeight.Bold,
                    color = Color(0xFFC04B00),
                    fontSize = 16.sp
                )
            },
            text = {
                Text(text = msg, fontSize = 13.sp, color = Color(0xFF374151))
            },
            confirmButton = {
                Button(
                    onClick = { errorAlertaMensaje = null },
                    colors = ButtonDefaults.buttonColors(containerColor = MaizPrimary),
                    shape = RoundedCornerShape(12.dp)
                ) {
                    Text("Entendido", fontWeight = FontWeight.Bold, color = Color.White)
                }
            }
        )
    }

    // ── DIÁLOGO EDITAR ESTÁNDARES DE PRODUCCIÓN (Casos 3, 4 y 5) ────────────
    if (showEditEstandaresDialog) {
        AlertDialog(
            onDismissRequest = { showEditEstandaresDialog = false },
            title = {
                Text("Estándares de Producción", fontWeight = FontWeight.Bold, color = TextPrimary)
            },
            text = {
                Column(
                    modifier = Modifier.fillMaxWidth(),
                    verticalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    OutlinedTextField(
                        value = editPesoBultoInput,
                        onValueChange = { editPesoBultoInput = it },
                        label = { Text("Peso Bulto Harina (kg)") },
                        singleLine = true,
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                        modifier = Modifier.fillMaxWidth()
                    )
                    OutlinedTextField(
                        value = editKgMasaInput,
                        onValueChange = { editKgMasaInput = it },
                        label = { Text("Total Masa por Bulto (kg)") },
                        singleLine = true,
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                        modifier = Modifier.fillMaxWidth()
                    )
                    OutlinedTextField(
                        value = editRendimientoInput,
                        onValueChange = { editRendimientoInput = it },
                        label = { Text("Rendimiento Tortilla por Bulto (kg)") },
                        singleLine = true,
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                        modifier = Modifier.fillMaxWidth()
                    )
                    OutlinedTextField(
                        value = editMermaInput,
                        onValueChange = { editMermaInput = it },
                        label = { Text("Merma Tolerada por Bulto (kg)") },
                        singleLine = true,
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                        modifier = Modifier.fillMaxWidth()
                    )
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        val pBulto = editPesoBultoInput.replace(',', '.').toDoubleOrNull() ?: configProduccion.pesoBultoHarinaKg
                        val kMasa = editKgMasaInput.replace(',', '.').toDoubleOrNull() ?: configProduccion.kgMasaPorBulto
                        val rend = editRendimientoInput.replace(',', '.').toDoubleOrNull() ?: configProduccion.rendimientoTortillaPorBulto
                        val merm = editMermaInput.replace(',', '.').toDoubleOrNull() ?: configProduccion.mermaToleradaKgPorBulto
                        val pctMerma = if (kMasa > 0) ((merm / kMasa) * 100.0) else 3.8

                        ejecutarConPermisoAdmin {
                            viewModel.guardarEstandaresProduccion(
                                pesoBulto = pBulto,
                                kgMasa = kMasa,
                                rendimiento = rend,
                                merma = merm,
                                porcentajeMerma = pctMerma
                            )
                            showEditEstandaresDialog = false
                        }
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = MaizPrimary)
                ) {
                    Text("Guardar", color = Color.White, fontWeight = FontWeight.Bold)
                }
            },
            dismissButton = {
                TextButton(onClick = { showEditEstandaresDialog = false }) {
                    Text("Cancelar", color = TextSecondary)
                }
            }
        )
    }
}

@Preview(showBackground = true, showSystemUi = true)
@Composable
fun ConfiguracionScreenPreview() {
    TortilleriaDerekTheme {
        ConfiguracionScreen()
    }
}
