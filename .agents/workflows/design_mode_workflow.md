# 🎨 Workflow: Modo de Diseño para Agentes (Design Mode)

**Propósito:** Guiar la concepción, prototipado visual y construcción de interfaces Jetpack Compose de alta gama para la aplicación **Tortillería Derek**, garantizando fidelidad total a los wireframes de Stitch, cero hardcoding, modularidad estricta y ergonomía táctil de nivel industrial.

---

## 🚦 Cuándo Activar el Modo de Diseño

El agente debe asumir o invocar el **Modo de Diseño** (`🎨 design-ui-expert`) en cualquiera de los siguientes escenarios:
1. **Nueva funcionalidad visual:** Maquetación de pantallas, tarjetas o diálogos descritos en `docs/features/` o solicitados por el usuario.
2. **Refactorización de pantallas monolíticas:** Fraccionamiento de archivos que superen o amenacen superar las 350-400 líneas (ej. descomponer `ConfiguracionScreen.kt` o `MetricasScreen.kt`).
3. **Consumo de diseños Stitch:** Traducción de interfaces HTML/CSS/Screenshots ubicadas en [stitch_designs/](file:///d:/TortilleriaDerek/stitch_designs/) a código Compose nativo.
4. **Optimización ergonómica:** Ajuste de botones, paneles numéricos, selectores de peso o alertas para operarios con manos húmedas o con harina.
5. **Adaptabilidad Tablet / Móvil:** Implementación de layouts responsivos basados en `WindowSizeClass`.

---

## 🛠️ Procedimiento Operativo Paso a Paso

### Paso 1: Extracción de Tokens y Análisis de Wireframe (Stitch -> Compose)
- Inspeccionar la carpeta correspondiente en [stitch_designs/](file:///d:/TortilleriaDerek/stitch_designs/) (ej. `02_punto_venta_mostrador`, `04_control_produccion`, etc.).
- Contrastar los colores y espaciados con los tokens oficiales de [DESIGN_SYSTEM.md](file:///d:/TortilleriaDerek/stitch_designs/DESIGN_SYSTEM.md):
  - **Maíz Dorado:** `Color(0xFF8D4B00)` / `Color(0xFFD97706)` (Acciones primarias, botones de cobro, badges activos).
  - **Terracota:** `Color(0xFFAC3400)` / `Color(0xFFC2410C)` (Alertas, mermas, indicadores numéricos de báscula).
  - **Verde Agave:** `Color(0xFF2A674C)` / `Color(0xFF2D6A4F)` (Confirmaciones, estados de éxito, turnos abiertos).
  - **Fondo Crema:** `Color(0xFFFFF8F5)` (Superficie base cálida).
  - **Obsidiana:** `Color(0xFF1F1B17)` (Texto de alto contraste).

### Paso 2: Cero Hardcoding — Registro Inmediato en `strings.xml`
- **Antes de escribir una sola línea de Compose**, todo texto visible (títulos, botones, labels de input, mensajes de ayuda, placeholders) debe agregarse a [app/src/main/res/values/strings.xml](file:///d:/TortilleriaDerek/app/src/main/res/values/strings.xml).
- Convención de nombres de string: `<modulo>_<componente>_<descripcion>` (ej. `mostrador_card_total`, `config_harina_bulto_label`).
- En Compose, consumir exclusivamente vía `stringResource(id = R.string.xxx)`.

### Paso 3: Descomposición en Subcomponentes Stateless (Anti-God Composables)
- Queda prohibido escribir componentes de más de 150-200 líneas.
- Toda pantalla debe dividirse en piezas modulares dentro de `ui/components/<feature>/`:
  - `HeroCard.kt`
  - `ActionGrid.kt`
  - `ItemRow.kt`
  - `ConfirmDialog.kt`
- **Regla de Pureza Stateless:** Los subcomponentes no deben inyectar ViewModels. Reciben estado plano (`data class` UI) y emiten callbacks (`onClick: () -> Unit`, `onValueChange: (Double) -> Unit`).

### Paso 4: Ergonomía de Tortillería y Accesibilidad
- **Touch Target:** Aplicar `Modifier.heightIn(min = 56.dp)` (o `64.dp` para botones de cobro rápido y teclados numéricos).
- **Cifras Tabulares (`tnum`):** Para cantidades en kilogramos, precios y montos que cambian en tiempo real, configurar la fuente con características OpenType tabulares para que los números mantengan un ancho fijo sin vibrar en pantalla:
  ```kotlin
  style = MaterialTheme.typography.headlineLarge.copy(
      fontFamily = FontFamily(Font(R.font.plus_jakarta_sans_bold)),
      fontFeatureSettings = "tnum"
  )
  ```
- **Semántica y Testabilidad:** Añadir `Modifier.testTag("${screen}_${component}_${element}")` y `contentDescription` descriptivo en todo elemento interactivo o ícono.

### Paso 5: Vistas Previas Multi-Dispositivo (`@Preview`)
Todo componente stateless debe incluir al menos dos `@Preview`:
```kotlin
@Preview(name = "Móvil Vertical", showBackground = true, widthDp = 390, heightDp = 844)
@Preview(name = "Tablet Mostrador", showBackground = true, widthDp = 1024, heightDp = 600)
@Composable
private fun MiComponentePreview() {
    TortilleriaDerekTheme {
        MiComponenteStateless(...)
    }
}
```

### Paso 6: Exportación del Contrato `UiState` a `mobile-developer`
Una vez aprobada la interfaz visualmente:
1. `design-ui-expert` redacta la interfaz o clase sellada del `UiState` requerida para alimentar los componentes.
2. Notifica a `mobile-developer` para que cree las propiedades en el ViewModel e implemente el flujo MVI correspondiente.

---

## 📋 Checklist del Design Gate (Criterios de Aceptación)

Antes de marcar una tarea de diseño como completada, verificar:
- [ ] ¿El archivo de pantalla resultante tiene menos de **350 líneas**?
- [ ] ¿Todos los textos usan `stringResource()` sin literales `Text("...")` quemados?
- [ ] ¿Los botones principales cumplen con la altura mínima de **56dp / 64dp**?
- [ ] ¿Los colores respetan la paleta oficial *Maíz & Masa* sin colores genéricos (`Color.Red`, `Color.Blue`)?
- [ ] ¿Todos los elementos interactivos tienen `testTag` y `contentDescription`?
- [ ] ¿El componente funciona en pantalla de celular y en modo horizontal/tablet?
- [ ] ¿El componente compila exitosamente con `./gradlew compileDebugKotlin`?
