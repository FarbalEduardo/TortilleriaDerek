# Spec-Driven Development (SDD): Plan de Mejora y Mitigación de Auditoría

## 1. Visión General de la Especificación
Este documento establece la especificación técnica y funcional para corregir los hallazgos críticos de la auditoría técnica senior en la aplicación **Tortilleria Derek**, abarcando:
1. Migraciones Room seguras sin pérdida de datos (`DAT-01`).
2. Configuración robusta de reglas de backup y extracción de datos (`SEC-01`).
3. Externalización de textos (`strings.xml`) y accesibilidad (`A11Y-01`).
4. Descomposición modular de componentes monolíticos UI (`ARC-01`).

---

## 2. Historias de Usuario (User Stories)

### US-01: Migraciones Room sin Destrucción de Datos
* **Como** administrador y operario del punto de venta,
* **Quiero** que al actualizar la versión de la aplicación, mi base de datos local (ventas, turnos, producción) se migre de forma segura sin perder información,
* **Para que** no se corrompa ni se borre el registro financiero e histórico del negocio.

### US-02: Protección de Datos Sensibles en Respaldos en Nube
* **Como** administrador del sistema,
* **Quiero** que las reglas del sistema operativo impidan el respaldo automático sin control de tablas financieras sensibles en servicios cloud genéricos,
* **Para que** los datos del negocio y operaciones de la tortillería no queden expuestos en copias de seguridad no cifradas.

### US-03: Cero Hardcoding y Localización de Textos
* **Como** usuario y operario,
* **Quiero** que todos los textos de la interfaz estén externalizados y adaptados para accesibilidad (TalkBack) e internacionalización,
* **Para que** la aplicación cumpla con los estándares técnicos y de usabilidad de Android.

---

## 3. Casos Límite (Edge Cases)

* **EC-01 (Migración Room):** ¿Qué ocurre si un usuario actualiza desde la versión 6 directamente a la versión 8?
  * *Resolución:* Definir una secuencia de migraciones encadenadas (`Migration(6, 7)` y `Migration(7, 8)`) o manejo robusto de alteración de tablas sin pérdida de filas existentes.
* **EC-02 (Backup en Nube):** ¿Qué ocurre si el usuario utiliza la función explícita de exportación por SAF provista por la app?
  * *Resolución:* Las reglas de exclusión de cloud backup automático (`cloud-backup`) no deben interferir con el flujo consciente de exportación voluntaria vía Storage Access Framework.
* **EC-03 (Textos dinámicos y concatenación):** ¿Cómo se manejan textos con montos numéricos o variables en `strings.xml`?
  * *Resolución:* Uso de format args en recursos de strings (ej. `getString(R.string.total_ventas, montoFormatted)`).

---

## 4. Criterios de Aceptación (Given / When / Then)

### Criterios para US-01 (Migraciones Room)
* **Given** una base de datos Room en versión 6 con registros de ventas existentes,
* **When** se ejecuta la actualización a la versión 7 con la clase `Migration(6, 7)` registrada,
* **Then** la base de datos se actualiza correctamente y el 100% de las ventas y turnos anteriores permanecen intactos sin invocar `fallbackToDestructiveMigration()`.

### Criterios para US-02 (Reglas de Backup)
* **Given** la configuración de `backup_rules.xml` y `data_extraction_rules.xml`,
* **When** el sistema operativo evalúa la inclusión de archivos de la base de datos de Room (`tortilleria_db*`),
* **Then** los archivos de base de datos local son excluidos explícitamente del respaldo automático de Google Cloud Backup.

### Criterios para US-03 (Cero Hardcoding)
* **Given** las pantallas principales de la aplicación,
* **When** un agente o auditor revisa el código fuente de los composables,
* **Then** no existen literales de texto sueltos (`Text("...")`), y todos los nodos interactivos cuentan con `contentDescription` o `testTag`.

---

## 5. Estrategia de Pruebas (Test Plan)

### Pruebas Unitarias y de Migración (Mobile Developer & QA)
1. **MigrationTest:** Utilizar `MigrationTestHelper` de Room para probar la transición de esquema de v6 a v7 validando integridad de tablas y datos.
2. **ViewModel & UseCase Tests:** Ampliar la suite actual para verificar manejo de resultados y errores.

### Pruebas Semánticas de Compose (Quality PM)
1. **Semantic Tests:** Verificar que todos los componentes clave respondan a `testTag` bajo la convención `screen_component_element` y que los textos se resuelvan desde recursos.

---

## 6. Plan de Tareas (Tasks Breakdown / `.agents/task.md`)

- [ ] **Fase 1: Especificación y SDD Gate**
  - [x] Redacción y validación de `docs/features/plan_mejora_audit_sdd.md`.
  - [ ] Revisión por parte de `security-expert` y sello de aprobación por `quality-pm-expert`.
- [ ] **Fase 2: Persistencia y Migraciones Room**
  - [ ] Implementar `Migration(6, 7)` en `TortilleriaDatabase` / `DataModule`.
  - [ ] Eliminar `.fallbackToDestructiveMigration()` de la configuración de producción.
  - [ ] Crear pruebas unitarias con `MigrationTestHelper`.
- [ ] **Fase 3: Seguridad en Respaldos**
  - [ ] Actualizar `backup_rules.xml` para excluir la base de datos local y SharedPreferences sensibles.
  - [ ] Actualizar `data_extraction_rules.xml` para control estricto de extracción cloud.
- [ ] **Fase 4: Refactorización i18n / Cero Hardcoding y Modularidad UI**
  - [ ] Migrar cadenas de texto literales a `res/values/strings.xml`.
  - [ ] Descomponer componentes largos en sub-composables stateless.
- [ ] **Fase 5: Quality Gate Final**
  - [ ] Ejecutar suite completa con `./gradlew test` y pruebas de integración.
