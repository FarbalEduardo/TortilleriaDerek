package com.example.tortilleriaderek.ui.components.configuracion

import androidx.compose.runtime.Composable
import com.example.tortilleriaderek.ui.screens.ProductoConfig
import com.example.tortilleriaderek.ui.screens.UsuarioConfig

/**
 * Contenedor orquestador de diálogos modales para la pantalla de Configuración.
 * Centraliza la presentación de diálogos para mantener ConfiguracionScreen concisa (<300 líneas).
 */
@Composable
fun ConfiguracionDialogsContainer(
    // Diálogo Editar Producto
    productoAEditar: ProductoConfig?,
    nuevoPrecioInput: String,
    onPrecioInputChange: (String) -> Unit,
    nuevoPesoInput: String,
    onPesoInputChange: (String) -> Unit,
    onGuardarPrecioProducto: (ProductoConfig, Double, Int?) -> Unit,
    onDismissEditarProducto: () -> Unit,

    // Diálogo Agregar Repartidor
    showAddRepartidorDialog: Boolean,
    nuevoRepartidorNombre: String,
    onRepartidorNombreChange: (String) -> Unit,
    nuevaMotoRuta: String,
    onMotoRutaChange: (String) -> Unit,
    onGuardarRepartidor: (String, String) -> Unit,
    onDismissAgregarRepartidor: () -> Unit,

    // Diálogo Agregar Usuario
    showAddUsuarioDialog: Boolean,
    nuevoUsuarioUsername: String,
    onUsuarioUsernameChange: (String) -> Unit,
    nuevoUsuarioRol: String,
    onUsuarioRolChange: (String) -> Unit,
    nuevoUsuarioPassword: String,
    onUsuarioPasswordChange: (String) -> Unit,
    nuevoUsuarioPasswordVisible: Boolean,
    onToggleNuevoUsuarioPasswordVisible: () -> Unit,
    onGuardarNuevoUsuario: () -> Unit,
    onDismissAgregarUsuario: () -> Unit,

    // Diálogo Editar Usuario
    usuarioAEditar: UsuarioConfig?,
    nuevoUsernameEdit: String,
    onUsernameEditChange: (String) -> Unit,
    nuevoRolEdit: String,
    onRolEditChange: (String) -> Unit,
    nuevaPasswordEdit: String,
    onPasswordEditChange: (String) -> Unit,
    nuevaPasswordEditVisible: Boolean,
    onTogglePasswordEditVisible: () -> Unit,
    onGuardarEditarUsuario: (UsuarioConfig) -> Unit,
    onDismissEditarUsuario: () -> Unit,

    // Diálogo Eliminar Usuario
    usuarioAEliminar: UsuarioConfig?,
    onConfirmarEliminarUsuario: (UsuarioConfig) -> Unit,
    onDismissEliminarUsuario: () -> Unit,

    // Diálogo Confirmación admin1
    showAdminConfirmDialog: Boolean,
    adminConfirmUsernameInput: String,
    onAdminUsernameChange: (String) -> Unit,
    adminConfirmPasswordInput: String,
    onAdminPasswordChange: (String) -> Unit,
    adminConfirmPasswordVisible: Boolean,
    onToggleAdminPasswordVisible: () -> Unit,
    adminConfirmError: String?,
    onAutorizarAdmin1: () -> Unit,
    onDismissAdminConfirm: () -> Unit,

    // Diálogo Aviso Seguridad
    errorAlertaMensaje: String?,
    onDismissAvisoSeguridad: () -> Unit,

    // Diálogo Estándares Producción
    showEditEstandaresDialog: Boolean,
    editPesoBultoInput: String,
    onEditPesoBultoChange: (String) -> Unit,
    editKgMasaInput: String,
    onEditKgMasaChange: (String) -> Unit,
    editRendimientoInput: String,
    onEditRendimientoChange: (String) -> Unit,
    editMermaInput: String,
    onEditMermaChange: (String) -> Unit,
    onGuardarEstandares: () -> Unit,
    onDismissEditEstandares: () -> Unit
) {
    productoAEditar?.let { prod ->
        DialogEditarProducto(
            producto = prod,
            precioInput = nuevoPrecioInput,
            onPrecioInputChange = onPrecioInputChange,
            pesoInput = nuevoPesoInput,
            onPesoInputChange = onPesoInputChange,
            onConfirmar = {
                val precioParsed = nuevoPrecioInput.replace(',', '.').toDoubleOrNull()
                val pesoParsed = nuevoPesoInput.toIntOrNull()
                if (precioParsed != null && precioParsed >= 0) {
                    onGuardarPrecioProducto(prod, precioParsed, pesoParsed)
                } else {
                    onDismissEditarProducto()
                }
            },
            onDismiss = onDismissEditarProducto
        )
    }

    if (showAddRepartidorDialog) {
        DialogAgregarRepartidor(
            nombreInput = nuevoRepartidorNombre,
            onNombreChange = onRepartidorNombreChange,
            motoRutaInput = nuevaMotoRuta,
            onMotoRutaChange = onMotoRutaChange,
            onConfirmar = {
                val nom = nuevoRepartidorNombre.trim()
                val rut = nuevaMotoRuta.trim()
                if (nom.isNotBlank()) {
                    onGuardarRepartidor(nom, rut)
                }
            },
            onDismiss = onDismissAgregarRepartidor
        )
    }

    if (showAddUsuarioDialog) {
        DialogAgregarUsuario(
            usernameInput = nuevoUsuarioUsername,
            onUsernameChange = onUsuarioUsernameChange,
            rolInput = nuevoUsuarioRol,
            onRolChange = onUsuarioRolChange,
            passwordInput = nuevoUsuarioPassword,
            onPasswordChange = onUsuarioPasswordChange,
            passwordVisible = nuevoUsuarioPasswordVisible,
            onTogglePasswordVisible = onToggleNuevoUsuarioPasswordVisible,
            onConfirmar = onGuardarNuevoUsuario,
            onDismiss = onDismissAgregarUsuario
        )
    }

    usuarioAEditar?.let { usr ->
        DialogEditarUsuario(
            usuario = usr,
            usernameInput = nuevoUsernameEdit,
            onUsernameChange = onUsernameEditChange,
            rolInput = nuevoRolEdit,
            onRolChange = onRolEditChange,
            passwordInput = nuevaPasswordEdit,
            onPasswordChange = onPasswordEditChange,
            passwordVisible = nuevaPasswordEditVisible,
            onTogglePasswordVisible = onTogglePasswordEditVisible,
            onConfirmar = { onGuardarEditarUsuario(usr) },
            onDismiss = onDismissEditarUsuario
        )
    }

    usuarioAEliminar?.let { usr ->
        DialogEliminarUsuarioConfirmacion(
            usuario = usr,
            onConfirmar = { onConfirmarEliminarUsuario(usr) },
            onDismiss = onDismissEliminarUsuario
        )
    }

    if (showAdminConfirmDialog) {
        DialogConfirmacionAdmin1(
            usernameInput = adminConfirmUsernameInput,
            onUsernameChange = onAdminUsernameChange,
            passwordInput = adminConfirmPasswordInput,
            onPasswordChange = onAdminPasswordChange,
            passwordVisible = adminConfirmPasswordVisible,
            onTogglePasswordVisible = onToggleAdminPasswordVisible,
            errorMessage = adminConfirmError,
            onAutorizar = onAutorizarAdmin1,
            onDismiss = onDismissAdminConfirm
        )
    }

    errorAlertaMensaje?.let { msg ->
        DialogAvisoSeguridad(
            mensaje = msg,
            onDismiss = onDismissAvisoSeguridad
        )
    }

    if (showEditEstandaresDialog) {
        DialogEditarEstandaresProduccion(
            pesoBultoInput = editPesoBultoInput,
            onPesoBultoChange = onEditPesoBultoChange,
            kgMasaInput = editKgMasaInput,
            onKgMasaChange = onEditKgMasaChange,
            rendimientoInput = editRendimientoInput,
            onRendimientoChange = onEditRendimientoChange,
            mermaInput = editMermaInput,
            onMermaChange = onEditMermaChange,
            onGuardar = onGuardarEstandares,
            onDismiss = onDismissEditEstandares
        )
    }
}
