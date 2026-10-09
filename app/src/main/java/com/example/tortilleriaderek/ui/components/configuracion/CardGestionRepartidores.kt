package com.example.tortilleriaderek.ui.components.configuracion

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.TwoWheeler
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.res.stringResource
import com.example.tortilleriaderek.R
import com.example.tortilleriaderek.ui.screens.RepartidorConfig
import com.example.tortilleriaderek.ui.theme.*

/**
 * Componente stateless para la gestión de repartidores y rutas (Modo de Diseño).
 * Ergonomía táctil: botones >= 56dp o targets amplios con badges de alto contraste.
 */
@Composable
fun CardGestionRepartidores(
    repartidores: List<RepartidorConfig>,
    onAgregarClick: () -> Unit,
    onEditarClick: (RepartidorConfig) -> Unit,
    onEliminarClick: (RepartidorConfig) -> Unit,
    modifier: Modifier = Modifier
) {
    Surface(
        modifier = modifier
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
                            imageVector = Icons.Default.TwoWheeler,
                            contentDescription = null,
                            tint = MaizPrimary,
                            modifier = Modifier.size(20.dp)
                        )
                    }
                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = stringResource(R.string.config_repartidores_titulo),
                            fontSize = 15.sp,
                            fontWeight = FontWeight.Bold,
                            color = TextPrimary
                        )
                        Text(
                            text = stringResource(R.string.config_repartidores_subtitulo),
                            fontSize = 11.sp,
                            color = TextSecondary
                        )
                    }
                }

                // Botón "+ Agregar"
                Surface(
                    onClick = onAgregarClick,
                    shape = RoundedCornerShape(14.dp),
                    color = Color(0xFFFFF4EC),
                    modifier = Modifier.testTag("ajustes_repartidores_btn_agregar")
                ) {
                    Row(
                        modifier = Modifier.padding(horizontal = 12.dp, vertical = 8.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(4.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.Add,
                            contentDescription = null,
                            tint = Color(0xFFE05300),
                            modifier = Modifier.size(16.dp)
                        )
                        Text(
                            text = stringResource(R.string.config_repartidores_btn_agregar),
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
                            text = stringResource(R.string.config_repartidores_empty),
                            fontSize = 12.sp,
                            color = TextSecondary,
                            modifier = Modifier.padding(14.dp)
                        )
                    }
                } else {
                    repartidores.forEach { rep ->
                        Surface(
                            modifier = Modifier
                                .fillMaxWidth()
                                .testTag("repartidor_item_${rep.id}"),
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
                                    modifier = Modifier.weight(1f),
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
                                    Column(
                                        modifier = Modifier.weight(1f),
                                        verticalArrangement = Arrangement.spacedBy(1.dp)
                                    ) {
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
                                        onClick = { onEditarClick(rep) },
                                        modifier = Modifier
                                            .size(44.dp)
                                            .testTag("repartidor_btn_editar_${rep.id}")
                                    ) {
                                        Icon(
                                            imageVector = Icons.Default.Edit,
                                            contentDescription = stringResource(R.string.config_btn_editar_item, rep.nombre),
                                            tint = TextSecondary,
                                            modifier = Modifier.size(18.dp)
                                        )
                                    }

                                    IconButton(
                                        onClick = { onEliminarClick(rep) },
                                        modifier = Modifier
                                            .size(44.dp)
                                            .testTag("repartidor_btn_eliminar_${rep.id}")
                                    ) {
                                        Icon(
                                            imageVector = Icons.Default.Delete,
                                            contentDescription = stringResource(R.string.config_btn_eliminar_item, rep.nombre),
                                            tint = Color(0xFFA39E99),
                                            modifier = Modifier.size(18.dp)
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

@Preview(showBackground = true)
@Composable
fun CardGestionRepartidoresPreview() {
    TortilleriaDerekTheme {
        CardGestionRepartidores(
            repartidores = listOf(
                RepartidorConfig("1", "#01", "Carlos Méndez", "Moto 1 - Ruta Norte"),
                RepartidorConfig("2", "#02", "Juan Pérez", "Moto 2 - Mercado Central")
            ),
            onAgregarClick = {},
            onEditarClick = {},
            onEliminarClick = {}
        )
    }
}
