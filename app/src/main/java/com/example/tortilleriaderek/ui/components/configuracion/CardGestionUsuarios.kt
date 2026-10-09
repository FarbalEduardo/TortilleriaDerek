package com.example.tortilleriaderek.ui.components.configuracion

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.People
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
import com.example.tortilleriaderek.ui.screens.UsuarioConfig
import com.example.tortilleriaderek.ui.theme.*

/**
 * Componente stateless para la gestión de usuarios del sistema (Modo de Diseño).
 * Visualiza cuentas, roles, insignia de cuenta principal admin1 protegida y botones CRUD.
 */
@Composable
fun CardGestionUsuarios(
    usuarios: List<UsuarioConfig>,
    onAgregarClick: () -> Unit,
    onEditarClick: (UsuarioConfig) -> Unit,
    onEliminarClick: (UsuarioConfig) -> Unit,
    modifier: Modifier = Modifier
) {
    Surface(
        modifier = modifier
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
                            imageVector = Icons.Default.People,
                            contentDescription = null,
                            tint = MaizPrimary,
                            modifier = Modifier.size(20.dp)
                        )
                    }
                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = stringResource(R.string.config_usuarios_titulo),
                            fontSize = 15.sp,
                            fontWeight = FontWeight.Bold,
                            color = TextPrimary
                        )
                        Text(
                            text = stringResource(R.string.config_usuarios_subtitulo),
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
                    modifier = Modifier.testTag("ajustes_usuarios_btn_agregar")
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
                            text = stringResource(R.string.config_usuarios_btn_agregar),
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
                        modifier = Modifier
                            .fillMaxWidth()
                            .testTag("usuario_item_${usr.id}"),
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

                                Column(
                                    modifier = Modifier.weight(1f),
                                    verticalArrangement = Arrangement.spacedBy(2.dp)
                                ) {
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
                                    onClick = { onEditarClick(usr) },
                                    modifier = Modifier
                                        .size(44.dp)
                                        .testTag("usuario_btn_editar_${usr.id}")
                                ) {
                                    Icon(
                                        imageVector = Icons.Default.Edit,
                                        contentDescription = stringResource(R.string.config_btn_editar_item, usr.username),
                                        tint = TextSecondary,
                                        modifier = Modifier.size(18.dp)
                                    )
                                }

                                if (!usr.username.equals("admin1", ignoreCase = true) && usr.id != "1") {
                                    IconButton(
                                        onClick = { onEliminarClick(usr) },
                                        modifier = Modifier
                                            .size(44.dp)
                                            .testTag("usuario_btn_eliminar_${usr.id}")
                                    ) {
                                        Icon(
                                            imageVector = Icons.Default.Delete,
                                            contentDescription = stringResource(R.string.config_btn_eliminar_item, usr.username),
                                            tint = Color(0xFFA39E99),
                                            modifier = Modifier.size(18.dp)
                                        )
                                    }
                                } else {
                                    Box(
                                        modifier = Modifier.size(44.dp),
                                        contentAlignment = Alignment.Center
                                    ) {
                                        Icon(
                                            imageVector = Icons.Default.Lock,
                                            contentDescription = "Cuenta Principal Protegida",
                                            tint = Color(0xFFD1D5DB),
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
}

@Preview(showBackground = true)
@Composable
fun CardGestionUsuariosPreview() {
    TortilleriaDerekTheme {
        CardGestionUsuarios(
            usuarios = listOf(
                UsuarioConfig("1", "admin1", "ADMIN"),
                UsuarioConfig("2", "cajero_turno", "EMPLEADO")
            ),
            onAgregarClick = {},
            onEditarClick = {},
            onEliminarClick = {}
        )
    }
}
