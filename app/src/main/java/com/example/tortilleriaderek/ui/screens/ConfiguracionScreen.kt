package com.example.tortilleriaderek.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import androidx.compose.runtime.*
import kotlinx.coroutines.launch
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.tortilleriaderek.presentation.configuracion.ConfiguracionViewModel
import com.example.tortilleriaderek.ui.components.TortilleriaNavBar
import com.example.tortilleriaderek.ui.components.configuracion.*
import com.example.tortilleriaderek.ui.theme.*

/**
 * Pantalla de Ajustes ("Configuración del Sistema") modularizada conforme a la Constitución (Artículo III).
 * Descompuesta en subcomponentes stateless en `ui/components/configuracion/`.
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
    val repartidoresEntity by viewModel.repartidores.collectAsStateWithLifecycle()
    val repartidores = remember(repartidoresEntity) {
        repartidoresEntity.map { RepartidorConfig(it.id, it.numeroBadge, it.nombre, it.detalleRuta) }
    }
    val usuariosEntity by viewModel.usuarios.collectAsStateWithLifecycle()
    val usuarios = remember(usuariosEntity) {
        usuariosEntity.map { UsuarioConfig(it.id, it.username, it.rol) }
    }

    // Estados para Estándares de Producción
    var showEditEstandaresDialog by remember { mutableStateOf(false) }
    var editPesoBultoInput by remember { mutableStateOf("") }
    var editKgMasaInput by remember { mutableStateOf("") }
    var editRendimientoInput by remember { mutableStateOf("") }
    var editMermaInput by remember { mutableStateOf("") }

    val productos = listOf(
        ProductoConfig("1", "1k", "Kilo Completo", "1,000 g peso estándar", configProduccion.precioKilo, 1000, false),
        ProductoConfig("2", "½", "Medio Paquete", "${configProduccion.pesoMedioPaqueteGramos} g peso estándar", configProduccion.precioMedioPaquete, configProduccion.pesoMedioPaqueteGramos, true),
        ProductoConfig("3", "Pq", "Paquete Mostrador", "${configProduccion.pesoPaqueteGramos} g peso estándar", configProduccion.precioPaquete, configProduccion.pesoPaqueteGramos, true),
        ProductoConfig("4", "My", "Paquete Mayoreo (Reparto)", "${configProduccion.pesoPaqueteGramos} g tarifa mayoreo", configProduccion.precioPaqueteRepartidor, configProduccion.pesoPaqueteGramos, true)
    )

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

    // Permisos y Autorización admin1
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
        ConfiguracionBodyContent(
            configProduccion = configProduccion,
            productos = productos,
            repartidores = repartidores,
            usuarios = usuarios,
            esAccesoDesdeLogin = esAccesoDesdeLogin,
            onBackToLogin = onBackToLogin,
            onEditarProducto = { prod ->
                productoAEditar = prod
                nuevoPrecioInput = prod.precio.toString()
                nuevoPesoInput = prod.pesoGramos.toString()
            },
            onEditarEstandares = {
                editPesoBultoInput = configProduccion.pesoBultoHarinaKg.toString()
                editKgMasaInput = configProduccion.kgMasaPorBulto.toString()
                editRendimientoInput = configProduccion.rendimientoTortillaPorBulto.toString()
                editMermaInput = configProduccion.mermaToleradaKgPorBulto.toString()
                showEditEstandaresDialog = true
            },
            onAgregarRepartidor = { showAddRepartidorDialog = true },
            onEliminarRepartidor = { rep ->
                val repId = rep.id
                ejecutarConPermisoAdmin {
                    viewModel.eliminarRepartidor(repId)
                }
            },
            onAgregarUsuario = {
                nuevoUsuarioUsername = ""
                nuevoUsuarioPassword = ""
                nuevoUsuarioRol = "EMPLEADO"
                showAddUsuarioDialog = true
            },
            onEditarUsuario = { usr ->
                usuarioAEditar = usr
                nuevoUsernameEdit = usr.username
                nuevoRolEdit = usr.rol
                nuevaPasswordEdit = ""
            },
            onEliminarUsuario = { usr ->
                usuarioAEliminar = usr
            },
            modifier = Modifier.padding(innerPadding)
        )
    }

    // ── DIÁLOGOS MODALES Y SEGURIDAD (Layer modular) ──────────────────────────
    ConfiguracionDialogsContainer(
        productoAEditar = productoAEditar,
        nuevoPrecioInput = nuevoPrecioInput,
        onPrecioInputChange = { nuevoPrecioInput = it },
        nuevoPesoInput = nuevoPesoInput,
        onPesoInputChange = { nuevoPesoInput = it },
        onGuardarPrecioProducto = { prod, precio, peso ->
            ejecutarConPermisoAdmin {
                viewModel.guardarPrecioYPesoProducto(prod.id, precio, peso)
                productoAEditar = null
            }
        },
        onDismissEditarProducto = { productoAEditar = null },

        showAddRepartidorDialog = showAddRepartidorDialog,
        nuevoRepartidorNombre = nuevoRepartidorNombre,
        onRepartidorNombreChange = { nuevoRepartidorNombre = it },
        nuevaMotoRuta = nuevaMotoRuta,
        onMotoRutaChange = { nuevaMotoRuta = it },
        onGuardarRepartidor = { nom, rut ->
            ejecutarConPermisoAdmin {
                viewModel.agregarRepartidor(nombre = nom, motoRuta = rut)
                nuevoRepartidorNombre = ""
                nuevaMotoRuta = ""
                showAddRepartidorDialog = false
            }
        },
        onDismissAgregarRepartidor = { showAddRepartidorDialog = false },

        showAddUsuarioDialog = showAddUsuarioDialog,
        nuevoUsuarioUsername = nuevoUsuarioUsername,
        onUsuarioUsernameChange = { nuevoUsuarioUsername = it },
        nuevoUsuarioRol = nuevoUsuarioRol,
        onUsuarioRolChange = { nuevoUsuarioRol = it },
        nuevoUsuarioPassword = nuevoUsuarioPassword,
        onUsuarioPasswordChange = { nuevoUsuarioPassword = it },
        nuevoUsuarioPasswordVisible = nuevoUsuarioPasswordVisible,
        onToggleNuevoUsuarioPasswordVisible = { nuevoUsuarioPasswordVisible = !nuevoUsuarioPasswordVisible },
        onGuardarNuevoUsuario = {
            val cleanName = nuevoUsuarioUsername.trim()
            val pass = nuevoUsuarioPassword
            val rol = nuevoUsuarioRol
            if (cleanName.length >= 3 && pass.trim().length >= 4) {
                ejecutarConPermisoAdmin {
                    viewModel.crearUsuario(cleanName, pass, rol) { success, error ->
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
        onDismissAgregarUsuario = { showAddUsuarioDialog = false },

        usuarioAEditar = usuarioAEditar,
        nuevoUsernameEdit = nuevoUsernameEdit,
        onUsernameEditChange = { nuevoUsernameEdit = it },
        nuevoRolEdit = nuevoRolEdit,
        onRolEditChange = { nuevoRolEdit = it },
        nuevaPasswordEdit = nuevaPasswordEdit,
        onPasswordEditChange = { nuevaPasswordEdit = it },
        nuevaPasswordEditVisible = nuevaPasswordEditVisible,
        onTogglePasswordEditVisible = { nuevaPasswordEditVisible = !nuevaPasswordEditVisible },
        onGuardarEditarUsuario = { usr ->
            val cleanName = nuevoUsernameEdit.trim()
            val rol = nuevoRolEdit
            val pass = nuevaPasswordEdit.ifBlank { null }
            if (cleanName.length >= 3) {
                ejecutarConPermisoAdmin {
                    viewModel.editarUsuario(usr.id, cleanName, rol, pass) { success, error ->
                        if (success) {
                            usuarioAEditar = null
                        } else {
                            errorAlertaMensaje = error
                        }
                    }
                }
            }
        },
        onDismissEditarUsuario = { usuarioAEditar = null },

        usuarioAEliminar = usuarioAEliminar,
        onConfirmarEliminarUsuario = { usr ->
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
        onDismissEliminarUsuario = { usuarioAEliminar = null },

        showAdminConfirmDialog = showAdminConfirmDialog,
        adminConfirmUsernameInput = adminConfirmUsernameInput,
        onAdminUsernameChange = { adminConfirmUsernameInput = it },
        adminConfirmPasswordInput = adminConfirmPasswordInput,
        onAdminPasswordChange = {
            adminConfirmPasswordInput = it
            adminConfirmError = null
        },
        adminConfirmPasswordVisible = adminConfirmPasswordVisible,
        onToggleAdminPasswordVisible = { adminConfirmPasswordVisible = !adminConfirmPasswordVisible },
        adminConfirmError = adminConfirmError,
        onAutorizarAdmin1 = {
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
        onDismissAdminConfirm = {
            showAdminConfirmDialog = false
            pendingAction = null
        },

        errorAlertaMensaje = errorAlertaMensaje,
        onDismissAvisoSeguridad = { errorAlertaMensaje = null },

        showEditEstandaresDialog = showEditEstandaresDialog,
        editPesoBultoInput = editPesoBultoInput,
        onEditPesoBultoChange = { editPesoBultoInput = it },
        editKgMasaInput = editKgMasaInput,
        onEditKgMasaChange = { editKgMasaInput = it },
        editRendimientoInput = editRendimientoInput,
        onEditRendimientoChange = { editRendimientoInput = it },
        editMermaInput = editMermaInput,
        onEditMermaChange = { editMermaInput = it },
        onGuardarEstandares = {
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
        onDismissEditEstandares = { showEditEstandaresDialog = false }
    )
}

@Preview(showBackground = true, showSystemUi = true)
@Composable
fun ConfiguracionScreenPreview() {
    TortilleriaDerekTheme {
        ConfiguracionScreen()
    }
}
