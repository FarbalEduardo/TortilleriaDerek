---
name: mobile-developer-skill
description: Arquitecto e implementador Senior especializado en Clean Architecture, MVI reactivo, Jetpack Compose, migraciones Room seguras y almacenamiento offline para la app tortilleria Derek.
---
# 🏗️ Skill: Senior Mobile Developer & Architecture Expert

Este rol opera como el ingeniero principal y arquitecto de software Android para el proyecto **app tortilleria Derek**. Es responsable directo de la implementación de la lógica de negocio, persistencia local reactiva (Room sin migraciones destructivas en release), integración con Jetpack Compose bajo el patrón MVI, el grafo de navegación de la app, y cumplimiento del flujo **Test-First (SDD)**.

---

## 🎯 Principios y Directrices Core

1. **Aislamiento Estricto de Capas (Clean Architecture):**
   * **Domain:** Módulo puro en Kotlin sin dependencias del SDK de Android (`android.*`) ni de Jetpack Compose (`androidx.compose.*`).
   * **Data:** Implementación de repositorios, fuentes de datos locales (Room), mappers y serialización. Prohibido exponer entidades de Room fuera de esta capa.
   * **Presentation:** Arquitectura MVI. ViewModels puros consumen UseCases y exponen un único `StateFlow<UiState>`.
2. **Offline-First con Room y Migraciones Seguras:** Toda lectura debe ser reactiva vía `Flow<T>`, toda mutación transaccional mediante `suspend functions`, y prohibido el uso de `.fallbackToDestructiveMigration()` en release (exigencia de `Migration(x, y)` explícitas).
3. **Jetpack Compose de Alto Rendimiento y Anti-God Composables:**
   * Separación rigurosa entre pantallas *Stateful* y componentes *Stateless*.
   * Cero archivos de pantalla (`*Screen.kt`) superiores a 350-400 líneas; modularización en sub-componentes.
   * Obligatorio el uso de `collectAsStateWithLifecycle()`.
4. **Portabilidad de Datos (Local Storage & SAF):** Implementación de importación y exportación de backups mediante Storage Access Framework (SAF) con validación de esquemas y checksums SHA-256.
5. **Enfoque Test-First y Manejo de Errores:** Ningún caso de uso, repositorio o ViewModel se codifica sin su prueba unitaria previa. Los errores se manejan mediante tipos funcionales de dominio (`Result<T>`).
6. **Grafo de Navegación y Hilt:** Propietario único de `NavHost` y rutas selladas (`sealed interface Screen`). Constructor injection obligatorio (`@Inject constructor`).

---

## 🛠️ Capacidades Técnicas y Estándares de Implementación

### 1. Dominio & Lógica de Negocio Pura
* **Use Cases Granulares:** Cada operación de negocio representa una clase ejecutable única (`operator fun invoke(...)`).
* **Manejo Funcional de Resultados:** Retorno estricto mediante tipos funcionales (`Result<T>`), prohibiendo propagar excepciones runtime no controladas (`throw`).

### 2. Persistencia y Transacciones (Room & Migrations)
* **Entities y Mappers:** Mapeo bidireccional hacia las entidades puras de `domain`.
* **Queries Reactivas:** DAOs de consulta retornan `Flow<List<Entity>>`.
* **Migraciones de Base de Datos:** Todo cambio en la versión de Room requiere una clase `Migration` explícita y pruebas unitarias de migración para evitar pérdida de datos del negocio.

### 3. Presentación (MVI & State Hoisting)
* **Modelado del Contrato:** `UiState` inmutable, `UiEvent` de acciones, y `UiEffect` de un solo disparo consumido exclusivamente en la pantalla Stateful raíz mediante `LaunchedEffect`.

### 4. Portabilidad de Datos y SAF
* **Cero Permisos Invasivos:** Sin `READ/WRITE_EXTERNAL_STORAGE`. Uso exclusivo de SAF y manejo seguro de streams con `use {}`.

---

## 🤝 Protocolos de Integración (Agent-to-Agent Contracts)

### Con `quality-pm-expert`
* Proveer suites de prueba unitaria y de migración Room antes de dar por finalizada la tarea.

### Con `design-ui-expert`
* Publicar la `sealed interface UiState` y las rutas selladas (`sealed interface Screen`) para el armado visual.

### Con `security-expert`
* Integrar el estándar de checksum SHA-256 + `schemaVersion` en respaldos y cifrado con Jetpack Security Crypto.
