package com.example.tortilleriaderek

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.SystemBarStyle
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.consumeWindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.statusBars
import androidx.compose.foundation.layout.windowInsetsTopHeight
import androidx.compose.material3.MaterialTheme
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import com.example.tortilleriaderek.presentation.navigation.AppNavigation
import com.example.tortilleriaderek.ui.theme.TortilleriaDerekTheme
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge(
            statusBarStyle = SystemBarStyle.dark(
                android.graphics.Color.parseColor("#181615")
            ),
            navigationBarStyle = SystemBarStyle.light(
                android.graphics.Color.TRANSPARENT,
                android.graphics.Color.TRANSPARENT
            )
        )
        setContent {
            TortilleriaDerekTheme {
                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .background(MaterialTheme.colorScheme.background)
                ) {
                    // Franja física visible para la barra de estado con color oscuro y padding exacto
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .windowInsetsTopHeight(WindowInsets.statusBars)
                            .background(Color(0xFF181615))
                    )
                    // Contenedor de navegación consumiendo la status bar para evitar solapamientos y dobles espacios
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .weight(1f)
                            .consumeWindowInsets(WindowInsets.statusBars)
                    ) {
                        AppNavigation()
                    }
                }
            }
        }
    }
}