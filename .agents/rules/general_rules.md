# [SYSTEM INSTRUCTIONS: GLOBAL GUARDRAILS]
## Project: app tortilleria Derek

### 1. Stack Tecnológico y Arquitectura
*   **Tecnologías Base:** Kotlin moderno, 100% Jetpack Compose, Room Database, Hilt, y Navigation Compose. Queda estrictamente prohibido el uso de XML, Retrofit o Firebase.
*   **Clean Architecture:** Separación rigurosa en tres capas lógicas (`Data` -> `Domain` -> `Presentation`). El framework de Android no debe filtrarse a la capa de dominio.
*   **Patrón MVI:** La capa de presentación debe ser reactiva. Todo `ViewModel` debe exponer un único estado inmutable (`StateFlow<UiState>`) y recibir acciones del usuario mediante un evento global (`UiEvent`). Los efectos de un solo disparo (navegación, snackbars, toasts) se modelan como `UiEffect` y se consumen exclusivamente en la pantalla Stateful raíz.
*   **Integridad de Base de Datos (Room):** Prohibido el uso de `.fallbackToDestructiveMigration()` en release. Todo cambio de versión de esquema en Room requiere una `Migration(x, y)` explícita y testada.
*   **Modularidad de UI (Anti-God Composables):** Ningún archivo de pantalla (`*Screen.kt`) debe superar las 350-400 líneas; fraccionar en sub-componentes stateless en `ui/components/...`.

### 2. UI, UX y Modo de Diseño (Design Mode)
*   **Constitución Rectora:** Toda actividad de diseño y desarrollo debe alinearse con la Constitución Maestra [AGENTS.md](file:///d:/TortilleriaDerek/AGENTS.md).
*   **Modo de Diseño Obligatorio:** Antes de programar lógica de negocio, se debe activar el Modo de Diseño siguiendo [.agents/workflows/design_mode_workflow.md](file:///d:/TortilleriaDerek/.agents/workflows/design_mode_workflow.md) para maquetar componentes stateless aislados con `@Preview`.
*   **Fidelidad a Stitch:** Utilizar los tokens del sistema *Maíz & Masa POS* ([stitch_designs/DESIGN_SYSTEM.md](file:///d:/TortilleriaDerek/stitch_designs/DESIGN_SYSTEM.md)): Maíz Dorado (`#8D4B00`), Terracota (`#AC3400`), Verde Agave (`#2A674C`), Crema Claro (`#FFF8F5`).
*   **Ergonomía de Tortillería:** Touch target mínimo de 56dp (64dp en cobro), números tabulares (`tnum`) para báscula/pesaje en `Plus Jakarta Sans`, y alto contraste bajo luz solar y harina.
*   **Diseño Adaptable:** Uso estricto de Material Design 3. La interfaz debe adaptarse dinámicamente a celulares y tabletas con `WindowSizeClass`.
*   **Cero Hardcoding (i18n & a11y):** Todos los textos deben residir obligatoriamente en `res/values/strings.xml` y consumirse vía `stringResource()`. Todo elemento interactivo requiere `contentDescription` y `testTag`.
*   **Persistencia Offline y SAF:** La aplicación es 100% local. Toda exportación o importación de respaldos debe realizarse mediante Storage Access Framework (SAF) nativo, sin permisos obsoletos.
*   **Seguridad en Respaldo:** `backup_rules.xml` y `data_extraction_rules.xml` deben configurar explícitamente exclusiones e inclusiones de bases de datos locales sensibles.

### 3. Flujo de Trabajo IA y SDD
*   **Spec-Driven Development (SDD):** Es obligatorio crear y validar un archivo de especificación en la carpeta `docs/features/` antes de generar código funcional.
*   **Test-First:** Ningún código de producción debe escribirse hasta generar su prueba unitaria en estado fallido (rojo).
*   **Manejo de Contexto y Observabilidad:** Mantener `task.md` actualizado y asegurar captura robusta de excepciones con tipos funcionales de dominio (`Result<T>`).
*   **Control de Versiones:** Uso obligatorio de *Conventional Commits*.

### 4. Contratos de Integración Inter-Agente
*   **Inyección de Dependencias (Hilt):** Constructor injection obligatorio en `Repository`, `UseCase` y `ViewModel` (`@Inject constructor`).
*   **Navigation Compose:** El grafo de navegación es propiedad exclusiva de `mobile-developer`. `design-ui-expert` consume las rutas definidas.
*   **UiEffect:** Se recolecta mediante `LaunchedEffect(Unit) { viewModel.effect.collect { ... } }` en la pantalla Stateful raíz.
*   **Cero dependencias de red remota:** Prohibido agregar Retrofit, OkHttp para red externa o SDKs de Firebase.
*   **Convención de `testTag`:** `screen_component_element` en snake_case.

### 5. Seguridad y Protección de Datos
*   **Cifrado en Reposo:** Datos sensibles (PIN de caja, config de negocio) vía `EncryptedSharedPreferences`/`EncryptedFile` (Jetpack Security Crypto).
*   **Integridad de Backups:** Todo archivo exportado incluye `schemaVersion` y checksum SHA-256; validación obligatoria antes de importar.
*   **Control de Acceso Operativo:** Operaciones sensibles requieren PIN validado mediante `UseCase` puro con hash+salt y rate limiting local.
*   **Higiene de Logs y Builds:** Prohibido registrar montos o nombres en `Log.*` en variantes `release`; R8/ProGuard habilitado.
