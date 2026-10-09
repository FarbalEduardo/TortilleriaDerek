---
name: design-expert-skill
description: Especialista en Modo de Diseño, Sistema Maíz & Masa POS (Stitch), Material Design 3, ergonomía táctil industrial y cero hardcoding.
---

# 🎨 Skill: Design Expert & Modo de Diseño (Maíz & Masa POS)

Este skill provee directrices y contratos para la ejecución del **Modo de Diseño** en Tortillería Derek, asegurando que toda interfaz cumpla con el estándar visual de Stitch, ergonomía de fábrica/mostrador y cero hardcoding.

---

## 🌽 1. Paleta de Color Oficial (Maíz & Masa POS)

```kotlin
package com.example.tortilleriaderek.ui.theme

import androidx.compose.ui.graphics.Color

// Paleta Primaria: Maíz Dorado (Acciones clave, cobro, botones activos)
val MaizPrimary = Color(0xFF8D4B00)
val MaizPrimaryDark = Color(0xFFD97706)
val MaizPrimaryLight = Color(0xFFFFDDB3)
val MaizContainer = Color(0xFFFFDCC1)

// Paleta Secundaria: Terracota (Mermas, alertas, báscula, botones de peligro)
val Terracota = Color(0xFFAC3400)
val TerracotaDark = Color(0xFFC2410C)
val TerracotaContainer = Color(0xFFFFDBD0)

// Paleta Terciaria: Verde Agave (Turnos abiertos, estatus OK, cobros liquidados)
val VerdeAgave = Color(0xFF2A674C)
val VerdeAgaveDark = Color(0xFF2D6A4F)
val VerdeAgaveContainer = Color(0xFFD8F3DC)

// Superficies y Neutros
val CremaFondo = Color(0xFFFFF8F5)
val BlancoSuperficie = Color(0xFFFFFFFF)
val ObsidianaTexto = Color(0xFF1F1B17)
val CafeTextoSecundario = Color(0xFF554336)
val BordeSutil = Color(0xFFE7E0D6)
```

---

## 📐 2. Estándares Ergonómicos para Mostrador de Tortillería

1. **Touch Target Mínimo:** 
   - Botones regulares: mínimo **56dp**.
   - Botón principal de cobro ("Cobrar Orden"): mínimo **64dp**.
   - Teclado numérico / Stepper (+ / -): mínimo **56dp x 56dp**.
2. **Tipografía Tabular (`tnum`):**
   - Todo peso de báscula en kilogramos (`1.500 kg`) y precios (`$36.00 MXN`) debe usar la tipografía `Plus Jakarta Sans` con feature `tnum` para evitar saltos y vibraciones en pantalla mientras el operario despacha.
3. **Alto Contraste Bajo Harina y Reflejos de Luz Solar:**
   - La pantalla debe mantener un contraste WCAG AAA (texto oscuro `#1F1B17` sobre fondo claro `#FFFFFF` o `#FFF8F5`).

---

## 🧩 3. Anatomía de Componentes Stateless en Modo de Diseño

Todo componente maquetado durante el Modo de Diseño debe seguir esta plantilla:

```kotlin
/**
 * Componente stateless diseñado por 🎨 design-ui-expert.
 * Cero dependencia de ViewModels. Recibe estado puro y emite eventos.
 */
@Composable
fun ProductCounterCard(
    nombre: String,
    precioUnitario: Double,
    cantidad: Double,
    subtotal: Double,
    onIncrementar: () -> Unit,
    onDecrementar: () -> Unit,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier
            .fillMaxWidth()
            .heightIn(min = 90.dp)
            .testTag("mostrador_product_card_${nombre.lowercase()}"),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
    ) {
        // Implementación con strings externalizados
    }
}
```

---

## 🛠️ 4. Protocolo de Inspección Stitch

Cuando se diseñe una nueva vista:
1. Navegar a `stitch_designs/` y abrir la carpeta correspondiente.
2. Leer `index.html` para entender la estructura flex/grid y los badges.
3. Revisar `screenshot.png` para validar proporciones visuales y jerarquía.
4. Mapear cada elemento visual a su homólogo Compose en Material 3:
   - Badges HTML -> `AssistChip` o `SuggestionChip`.
   - Cards HTML -> `Card` con `RoundedCornerShape(16.dp)` y elevación tonal.
   - Steppers -> `Row` con `FilledIconButton` de 56dp.

---

## 🔍 5. Script de Verificación de Modo de Diseño

Para verificar que no existan strings quemados ni pantallas monolíticas:
`powershell .agents/skills/design-expert-skill/scripts/check_design.ps1`
