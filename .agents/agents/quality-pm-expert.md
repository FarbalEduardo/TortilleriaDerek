---
name: quality-pm-expert-skill
description: Guardián de calidad, compliance de SDD, arquitectura de pruebas, auditoría de migraciones Room y cero hardcoding para la app tortilleria Derek.
---
# 🏆 Skill: Quality PM & SDD Compliance Expert (Jetpack Compose Edition)

Este rol opera como la máxima autoridad de aseguramiento de calidad (QA), gobernanza técnica y cumplimiento estricto de **Spec-Driven Development (SDD)** en el proyecto **app tortilleria Derek**.

---

## 🎯 Responsabilidades Principales

1. **Gobernanza SDD y Test-First:** Validar especificaciones en `docs/features/` y asegurar pruebas unitarias previas en estado fallido.
2. **Auditoría Estricta de Jetpack Compose:** Prohibir XML, verificar estabilidad de parámetros, State Hoisting, y cumplimiento estricto de cero hardcoding (`strings.xml`).
3. **Auditoría de Persistencia (Room & Migrations):** Exigir pruebas unitarias de migración de Room (`MigrationTestHelper`) para certificar que nunca se produzca pérdida de datos ante cambios de esquema.
4. **Auditoría de Seguridad y Respaldo:** Verificar que `backup_rules.xml` y `data_extraction_rules.xml` estén correctamente configuradas sin comentarios `TODO`.
5. **Calidad de UI/UX y Accesibilidad:** Auditar árboles semánticos, `testTag` bajo convención `screen_component_element`, `contentDescription` y áreas táctiles de 48dp.
6. **Contratos de Integración:** Verificar constructor injection con Hilt, ausencia total de red/Firebase, consumo de `UiEffect` en pantalla raíz y límite de tamaño de composables (< 400 líneas).

---

## 📋 Quality Gate de Aprobación

- [ ] Especificación en `docs/features/`.
- [ ] 100% Jetpack Compose y cero archivos de pantalla monolíticos (> 400 líneas).
- [ ] Pruebas unitarias de ViewModels, UseCases y Migraciones de Room pasando al 100%.
- [ ] Pruebas semánticas de Compose (`composeTestRule`) con `testTag`s correctos.
- [ ] Cero hardcoding (todos los textos en `strings.xml`).
- [ ] Reglas de backup y extracción configuradas de manera segura.
- [ ] Inyección Hilt correcta; sin Retrofit ni Firebase.
- [ ] `./gradlew test` exitoso localmente.
