package com.example.tortilleriaderek.ui.components

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.RowScope
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.size
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Analytics
import androidx.compose.material.icons.filled.OutdoorGrill
import androidx.compose.material.icons.filled.PointOfSale
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarDefaults
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.NavigationBarItemDefaults
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.tortilleriaderek.ui.theme.BorderSubtle
import com.example.tortilleriaderek.ui.theme.MaizPrimary
import com.example.tortilleriaderek.ui.theme.MaizPrimaryLight
import com.example.tortilleriaderek.ui.theme.TextPrimary
import com.example.tortilleriaderek.ui.theme.TextSecondary
import com.example.tortilleriaderek.ui.theme.TortilleriaDerekTheme

/**
 * Barra de navegación inferior que se apega estrictamente a los lineamientos de Material Design 3 de Google.
 *
 * - Utiliza [NavigationBar] y [NavigationBarItem] estándar de Material 3.
 * - Respeta automáticamente los WindowInsets del sistema ([NavigationBarDefaults.windowInsets]),
 *   evitando que la barra de gestos o la botonera de 3 botones de Android se sobrepongan a los ítems.
 * - Extiende el fondo continuo (Edge-to-Edge) hasta el borde inferior de la pantalla.
 */
@Composable
fun TortilleriaNavBar(
    activeItem: String = "Venta",
    onNavigateToVenta: () -> Unit = {},
    onNavigateToProduccion: () -> Unit = {},
    onNavigateToMetricas: () -> Unit = {},
    onNavigateToConfiguracion: () -> Unit = {},
    windowInsets: WindowInsets = NavigationBarDefaults.windowInsets,
    modifier: Modifier = Modifier
) {
    Surface(
        modifier = modifier.fillMaxWidth(),
        color = Color.White,
        shadowElevation = 3.dp
    ) {
        Column(modifier = Modifier.fillMaxWidth()) {
            HorizontalDivider(
                color = BorderSubtle.copy(alpha = 0.6f),
                thickness = 1.dp
            )
            NavigationBar(
                modifier = Modifier.fillMaxWidth(),
                containerColor = Color.White,
                contentColor = TextPrimary,
                tonalElevation = 0.dp,
                windowInsets = windowInsets
            ) {
                NavBarItem(
                    icon = Icons.Default.PointOfSale,
                    label = "Venta",
                    isSelected = activeItem == "Venta",
                    onClick = onNavigateToVenta
                )
                NavBarItem(
                    icon = Icons.Default.OutdoorGrill,
                    label = "Producción",
                    isSelected = activeItem == "Producción",
                    onClick = onNavigateToProduccion
                )
                NavBarItem(
                    icon = Icons.Default.Analytics,
                    label = "Métricas",
                    isSelected = activeItem == "Métricas",
                    onClick = onNavigateToMetricas
                )
                NavBarItem(
                    icon = Icons.Default.Settings,
                    label = "Ajustes",
                    isSelected = activeItem == "Ajustes",
                    onClick = onNavigateToConfiguracion
                )
            }
        }
    }
}

@Composable
private fun RowScope.NavBarItem(
    icon: ImageVector,
    label: String,
    isSelected: Boolean,
    onClick: () -> Unit
) {
    NavigationBarItem(
        selected = isSelected,
        onClick = onClick,
        icon = {
            Icon(
                imageVector = icon,
                contentDescription = label,
                modifier = Modifier.size(22.dp)
            )
        },
        label = {
            Text(
                text = label,
                fontSize = 11.sp,
                fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium,
                maxLines = 1
            )
        },
        alwaysShowLabel = true,
        colors = NavigationBarItemDefaults.colors(
            selectedIconColor = MaizPrimary,
            selectedTextColor = MaizPrimary,
            indicatorColor = MaizPrimaryLight,
            unselectedIconColor = TextSecondary,
            unselectedTextColor = TextSecondary
        )
    )
}

@Preview(showBackground = true)
@Composable
private fun TortilleriaNavBarPreview() {
    TortilleriaDerekTheme {
        TortilleriaNavBar(activeItem = "Venta")
    }
}
