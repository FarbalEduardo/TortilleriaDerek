package com.example.tortilleriaderek.ui.components.configuracion

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.unit.dp
import com.example.tortilleriaderek.data.local.entity.ConfiguracionProduccionEntity
import com.example.tortilleriaderek.ui.screens.ProductoConfig
import com.example.tortilleriaderek.ui.screens.RepartidorConfig
import com.example.tortilleriaderek.ui.screens.UsuarioConfig
import com.example.tortilleriaderek.ui.theme.SurfaceWarm

/**
 * Contenedor del contenido principal scrolleable de Configuración.
 * Desacopla las tarjetas de la lógica de ViewModels y diálogos para cumplir el Artículo III (<400 líneas).
 */
@Composable
fun ConfiguracionBodyContent(
    configProduccion: ConfiguracionProduccionEntity,
    productos: List<ProductoConfig>,
    repartidores: List<RepartidorConfig>,
    usuarios: List<UsuarioConfig>,
    esAccesoDesdeLogin: Boolean,
    onBackToLogin: (() -> Unit)?,
    onEditarProducto: (ProductoConfig) -> Unit,
    onEditarEstandares: () -> Unit,
    onAgregarRepartidor: () -> Unit,
    onEliminarRepartidor: (RepartidorConfig) -> Unit,
    onAgregarUsuario: () -> Unit,
    onEditarUsuario: (UsuarioConfig) -> Unit,
    onEliminarUsuario: (UsuarioConfig) -> Unit,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier
            .fillMaxSize()
            .background(SurfaceWarm)
            .verticalScroll(rememberScrollState())
            .padding(horizontal = 16.dp, vertical = 12.dp)
            .testTag("configuracion_body_content"),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        // ── 1. CABECERA: TÍTULO Y BOTÓN REGRESAR ────────────
        ConfiguracionHeader(onBackToLogin = onBackToLogin)

        // ── 2. CARD: PRECIOS Y PESOS DE PRODUCTOS ────────────────────────
        CardPreciosProductos(
            productos = productos,
            onEditarProducto = onEditarProducto
        )

        // ── 3. CARD: ESTÁNDARES DE PRODUCCIÓN ────────────────────────────
        CardEstandaresProduccion(
            pesoBultoHarinaKg = configProduccion.pesoBultoHarinaKg,
            kgMasaPorBulto = configProduccion.kgMasaPorBulto,
            rendimientoTortillaPorBulto = configProduccion.rendimientoTortillaPorBulto,
            mermaToleradaKgPorBulto = configProduccion.mermaToleradaKgPorBulto,
            porcentajeMermaTolerada = configProduccion.porcentajeMermaTolerada,
            onEditar = onEditarEstandares
        )

        // ── 4. CARD: GESTIÓN DE REPARTIDORES ──────────────────────────────
        CardGestionRepartidores(
            repartidores = repartidores,
            onAgregarClick = onAgregarRepartidor,
            onEditarClick = { /* Próxima iteración de edición de chofer */ },
            onEliminarClick = onEliminarRepartidor
        )

        // ── 5. CARD: GESTIÓN DE USUARIOS (Oculto desde Login) ────────────
        if (!esAccesoDesdeLogin) {
            CardGestionUsuarios(
                usuarios = usuarios,
                onAgregarClick = onAgregarUsuario,
                onEditarClick = onEditarUsuario,
                onEliminarClick = onEliminarUsuario
            )
        }

        Spacer(modifier = Modifier.height(10.dp))
    }
}
