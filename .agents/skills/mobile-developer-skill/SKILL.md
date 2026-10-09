---
name: mobile-developer-skill
description: Clean Architecture, Repository Pattern, Room Safe Migrations, MVI reactivo y Jetpack Compose Stateful en Tortillería Derek.
---

# 🏗️ Skill: Mobile Developer & Clean Architecture

Este skill define los lineamientos de desarrollo Android nativo y patrones de arquitectura limpia para el proyecto **Tortillería Derek**.

---

## 🏛️ 1. Fronteras de Clean Architecture

### Regla de Oro:
**Las dependencias solo apuntan hacia adentro:**  
`Presentation` ➔ `Domain` 🠔 `Data`

1. **Capa Domain (`domain/`):**
   - 100% Kotlin puro.
   - Aloja modelos inmutables de dominio (`domain/model/`), interfaces de repositorio (`domain/repository/`) y casos de uso (`domain/usecase/`).
   - **PROHIBIDO:** Importar clases de Room (`dao.*`, `@Entity`, `SupportSQLiteDatabase`) o Android Framework en `domain/`.

2. **Capa Data (`data/`):**
   - Aloja la base de datos de Room (`data/local/TortilleriaDatabase`), DAOs, `@Entity`, mappers a dominio (`toDomain()`, `toEntity()`) e implementaciones de repositorio (`data/repository/*RepositoryImpl`).
   - Todos los DAOs se inyectan en los repositorios de Data, **nunca en ViewModels**.

3. **Capa Presentation (`presentation/`):**
   - ViewModels anotados con `@HiltViewModel`.
   - Inyectan Casos de Uso o Repositorios de Dominio.
   - Exponen un único `StateFlow<UiState>` inmutable y un canal de efectos `Channel<UiEffect>`.
   - **PROHIBIDO:** Inyectar Room DAOs o exponer clases `@Entity` directamente a Compose.

---

## 💾 2. Protocolo de Persistencia y Migraciones Room

1. **Esquema Exportado:** `exportSchema = true` en `@Database` para permitir verificación con `MigrationTestHelper`.
2. **Cero Borrado Destructivo:** Prohibido el uso de `.fallbackToDestructiveMigration()` en código de producción.
3. **Migraciones Seguras:**
   - Toda migración `Migration(from, to)` debe contener sentencias SQL reales ejecutables (`ALTER TABLE`, `CREATE TABLE`).
   - Proveer migraciones directas desde versiones anteriores (ej. `Migration(1, 7)`, `Migration(2, 7)`) o cadena encadenada continua para evitar crashes por salto de versiones.

---

## 🔄 3. Patrón MVI en ViewModels

```kotlin
@HiltViewModel
class MiViewModel @Inject constructor(
    private val getDatosUseCase: GetDatosUseCase,
    private val guardarUseCase: GuardarUseCase
) : ViewModel() {

    private val _uiState = MutableStateFlow(MiUiState())
    val uiState: StateFlow<MiUiState> = _uiState.asStateFlow()

    private val _effect = Channel<MiUiEffect>(Channel.BUFFERED)
    val effect = _effect.receiveAsFlow()

    fun onIntent(intent: MiUiIntent) {
        when (intent) {
            is MiUiIntent.Cargar -> cargar()
            is MiUiIntent.Guardar -> guardar(intent.dato)
        }
    }
}
```

---

## 🔍 4. Script de Auditoría de Arquitectura

Para verificar que no existan DAOs inyectados en ViewModels ni en Domain:
`powershell .agents/skills/mobile-developer-skill/scripts/audit_arch.ps1`
