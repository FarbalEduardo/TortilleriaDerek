# [SYSTEM INSTRUCTIONS: GLOBAL GUARDRAILS]
## Project: app tortilleria Derek

### 1. Stack Tecnológico y Arquitectura
*   **Tecnologías Base:** Kotlin moderno, 100% Jetpack Compose, Room Database, Hilt, y Navigation Compose. Queda estrictamente prohibido el uso de XML, Retrofit o Firebase.
*   **Clean Architecture:** Separación rigurosa en tres capas lógicas (`Data` -> `Domain` -> `Presentation`). El framework de Android no debe filtrarse a la capa de dominio.
*   **Patrón MVI:** La capa de presentación debe ser reactiva. Todo `ViewModel` debe exponer un único estado inmutable (`StateFlow<UiState>`) y recibir acciones del usuario mediante un evento global (`UiEvent`). Los efectos de un solo disparo (navegación, snackbars, toasts) se modelan como `UiEffect` y se consumen exclusivamente en la pantalla Stateful raíz.

### 2. UI, UX y Portabilidad de Datos
*   **Diseño Adaptable:** Uso estricto de Material Design 3 y tokens de marca. La interfaz debe adaptarse dinámicamente a pantallas de celulares y tablets utilizando `WindowSizeClass`.
*   **Cero Hardcoding:** Todos los textos deben residir en los archivos `strings.xml` correspondientes y los colores/dimensiones consumirse directamente del esquema del tema (`MaterialTheme.colorScheme`, `MaterialTheme.shapes`, tokens de `Dimens.kt`).
*   **Persistencia Offline y SAF:** La aplicación es 100% local. Toda exportación o importación de respaldos técnicos y reportes de ventas (`.csv`) debe realizarse utilizando el Storage Access Framework (SAF) nativo de Android, sin declarar permisos de almacenamiento obsoletos (`READ/WRITE_EXTERNAL_STORAGE`).

### 3. Flujo de Trabajo IA y SDD
*   **Spec-Driven Development (SDD):** Es obligatorio crear y validar un archivo de especificación en la carpeta `docs/features/` antes de que la IA genere cualquier código funcional. Las maquetas de Stitch y wireframes forman parte del anexo visual de esa spec, **no la sustituyen**: el agente de diseño puede prototipar `@Preview`s de exploración visual antes de que la spec quede validada, pero el código de producción integrado a navegación real permanece bloqueado hasta entonces.
*   **Test-First:** Ningún código de producción debe escribirse hasta que se haya generado su respectiva prueba unitaria en estado fallido (rojo), verificando que la suite cubra los criterios de aceptación especificados (Given/When/Then).
*   **Manejo de Contexto:** Los agentes siempre deben leer los lineamientos en `.agent/rules/` antes de iniciar tareas y mantener un registro de progreso actualizado en el archivo `task.md`.
*   **Control de Versiones:** Uso obligatorio de *Conventional Commits* y prohibición de alterar los flujos de GitHub Actions sin autorización explícita.

### 4. Contratos de Integración Inter-Agente
*   **Inyección de Dependencias (Hilt):** Constructor injection obligatorio en `Repository`, `UseCase` y `ViewModel` (`@Inject constructor`). Módulos Hilt (`@Module @InstallIn`) organizados por capa (`DataModule`, `DomainModule`, etc.). Prohibido usar `@EntryPoint` salvo justificación documentada en la spec de la feature.
*   **Navigation Compose:** El grafo de navegación (`NavHost`, rutas selladas y argumentos tipados vía `savedStateHandle`) es propiedad exclusiva de `mobile-developer`. `design-ui-expert` únicamente consume las rutas ya definidas para construir los componentes visuales de navegación (`NavigationBar`, `NavigationRail`, `PermanentNavigationDrawer`); no declara rutas nuevas ni strings de navegación sueltos.
*   **UiEffect:** Todo efecto de un solo disparo se recolecta mediante `LaunchedEffect(Unit) { viewModel.effect.collect { ... } }` en la pantalla Stateful raíz. Prohibido manejar `UiEffect` dentro de composables Stateless.
*   **Cero dependencias de red remota:** Prohibido agregar Retrofit, OkHttp para red externa, o cualquier SDK de Firebase al `build.gradle`. `mobile-developer` es responsable de auditar toda nueva dependencia antes de introducirla.
*   **Convención de `testTag`:** `screen_component_element` en snake_case (ej. `checkout_total_text`, `product_card_add_button`). Obligatorio en todo nodo interactivo o de verificación, coordinado entre `design-ui-expert` (quien lo aplica) y `quality-pm-expert` (quien lo audita y consume en `composeTestRule`).

### 5. Seguridad y Protección de Datos
*   **Cifrado en Reposo:** Todo dato sensible (PIN de caja, configuración de negocio) se almacena vía `EncryptedSharedPreferences`/`EncryptedFile` (Jetpack Security Crypto), con la clave en Android Keystore. Prohibido texto plano o claves hardcodeadas en código o en archivos versionados.
*   **Integridad de Backups:** Todo archivo exportado por SAF (CSV/JSON) incluye `schemaVersion` y checksum SHA-256; la importación valida ambos antes de deserializar y rechaza esquemas desconocidos sin intentar parsearlos parcialmente.
*   **Control de Acceso Operativo:** Operaciones sensibles (corte de caja, eliminar ventas, exportar historial) requieren PIN validado mediante un `UseCase` de dominio puro, con hash+salt y bloqueo temporal tras intentos fallidos.
*   **Higiene de Logs y Builds:** Prohibido registrar datos de negocio (montos, nombres) en `Log.*` dentro de variantes `release`; R8/ProGuard habilitado sin romper la (de)serialización de backups.
*   **Propiedad de la Regla:** `security-expert` diseña e implementa estos controles en `data`/`domain`; `mobile-developer` los integra en repositorios y módulos Hilt; `design-ui-expert` construye la UI que los expone (campo de PIN, mensajes de error); `quality-pm-expert` los audita y cubre con tests — ningún agente duplica el trabajo de otro.
