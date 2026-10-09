---
name: quality-pm-expert-skill
description: Quality Gatekeeper, auditoría de pruebas unitarias, verificación de cero hardcoding y Definition of Done para Tortillería Derek.
---

# 🏆 Skill: Quality PM & Full Test Audit

Este skill actúa como el control de calidad supremo (*Gatekeeper*) del proyecto **Tortillería Derek**, impidiendo que se entregue código con deuda técnica oculta, falsos verdes o pruebas incompletas.

---

## 🎯 1. Definition of Done (DoD) Obligatoria

Ninguna tarea o iteración se da por cerrada si no cumple el 100% de esta lista:

1. **Compilación Limpia:** `./gradlew compileDebugKotlin` termina con `BUILD SUCCESSFUL` (código de salida 0).
2. **Suite de Pruebas en Verde:** `./gradlew testDebugUnitTest` pasa al 100% de pruebas unitarias sin fallos.
3. **Cero Pantallas Gigantes:** Ningún archivo en `ui/screens/` supera las **400 líneas**.
4. **Cero Hardcoding:** No existen llamadas `Text("...")` o `text = "..."` con cadenas fijas en español; todos los textos residen en `res/values/strings.xml`.
5. **Semántica Compose:** Todos los botones y componentes interactivos clave cuentan con `Modifier.testTag("screen_component_element")`.
6. **Task Tracker Actualizado:** El progreso real se refleja fielmente en `.agents/task.md` con evidencia comprobable (prohibido marcar `[x]` en tareas no implementadas).

---

## 🧪 2. Requerimientos de Pruebas Unitarias

- **Para ViewModels:** Probar con MockK y Turbine los estados emitidos en `uiState` y eventos únicos en `effect`.
- **Para UseCases:** Probar flujos de éxito, flujos de error controlado (`Result.failure`) y casos frontera (ej. batería baja, contraseñas erróneas, mermas negativas).
- **Para Migraciones Room:** Emplear `MigrationTestHelper` para verificar la evolución del esquema físico de SQLite.

---

## 🔍 3. Script de Auditoría de Calidad

Para ejecutar la verificación integral de Quality Gate:
`powershell .agents/skills/quality-pm-expert-skill/scripts/audit_quality.ps1`
