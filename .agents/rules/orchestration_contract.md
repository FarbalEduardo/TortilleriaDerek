# 🎼 Contrato de Orquestación de Agentes (SDD Workflow)

Este documento define el ciclo de vida de desarrollo para la **app tortilleria Derek** y las reglas estrictas de delegación entre agentes. Ningún agente debe asumir responsabilidades que no correspondan a su rol.

---

## 🚦 Ciclo de Vida de una Feature (Flujo SDD)

Toda nueva funcionalidad o mejora debe seguir este orden estricto. El pase de una fase a otra está bloqueado hasta que el agente responsable confirme su checklist.

### Fase 1: Especificación y Criterios (Inception)
*   **Acción:** Se redacta el documento `.md` en `docs/features/`.
*   **Delegación:** 
    *   El usuario o el PM define los requerimientos de negocio.
    *   🛡️ **`security-expert`**: Revisa la spec y anexa los criterios de seguridad (Given/When/Then) si la tarea involucra acceso, persistencia o backups.
    *   🏆 **`quality-pm-expert`**: Actúa como *Gatekeeper*. Sella la spec como "Validada". No se tira una línea de código de producción hasta este punto.

### Fase 2: Prototipado Visual (Exploración)
*   **Acción:** Creación de componentes visuales aislados.
*   **Delegación:**
    *   🎨 **`design-ui-expert`**: Traduce wireframes a componentes Compose 100% Stateless con `@Preview` en móvil y tablet. Prohibido conectarlos a lógica real. Utiliza mocks estáticos.

### Fase 3: Lógica Core y Test-First (TDD)
*   **Acción:** Implementación de Data, Domain y ViewModel.
*   **Delegación:**
    *   🏗️ **`mobile-developer`**: Escribe las pruebas unitarias (que fallan inicialmente). Implementa Room, Repositorios, UseCases y el ViewModel (`StateFlow<UiState>`) hasta que las pruebas pasen (verde).
    *   🛡️ **`security-expert`**: Interviene de forma consultiva o proveyendo módulos Hilt de cifrado si la fase 1 lo exigió.

### Fase 4: Integración y Navegación
*   **Acción:** Conectar UI con la lógica y el grafo de navegación.
*   **Delegación:**
    *   🏗️ **`mobile-developer`**: Define y publica las rutas (`sealed interface Screen`) y el `NavHost`.
    *   🎨 **`design-ui-expert`**: Recibe el `UiState` real del ViewModel, inyecta los callbacks de UI (eventos) a sus componentes y respeta las rutas creadas.
    *   🏗️ **`mobile-developer`**: Ensambla el componente final Stateful en el `NavHost`.

### Fase 5: Aseguramiento de Calidad (Quality Gate)
*   **Acción:** Auditoría final antes de dar por cerrada la tarea.
*   **Delegación:**
    *   🏆 **`quality-pm-expert`**: Audita la semántica de Compose (`testTag`), lanza pruebas UI integradas (`composeTestRule`), verifica que no haya XML, hardcoding ni dependencias prohibidas.

---

## 🔀 Árbol de Decisión para Delegación de Tareas (Troubleshooting)

Cuando se solicite una mejora o corrección puntual, el LLM debe asumir el rol adecuado basándose en este árbol de decisión:

1.  **¿La solicitud implica cambiar colores, tamaños, animaciones, textos (i18n), accesibilidad o crear una nueva vista estática?**
    *   👉 **Delegar a:** 🎨 `design-ui-expert`.
2.  **¿La solicitud implica guardar datos en Room, modificar flujos asíncronos (Coroutines/Flow), inyección de dependencias (Hilt) o rutas de navegación?**
    *   👉 **Delegar a:** 🏗️ `mobile-developer`.
3.  **¿La solicitud implica cifrar datos, proteger exportaciones (SAF + checksum), ofuscación de código o control de acceso por PIN?**
    *   👉 **Delegar a:** 🛡️ `security-expert`.
4.  **¿La solicitud implica escribir tests de UI, auditar la arquitectura actual, o validar que no existan strings quemados ni malas prácticas de Compose?**
    *   👉 **Delegar a:** 🏆 `quality-pm-expert`.

---

## 🤝 Resolución de Conflictos (Interlocks)

*   **Choque UI vs. Lógica:** Si `design-ui-expert` necesita un estado que no existe, **no lo inventa ni crea un ViewModel**. Debe solicitar a `mobile-developer` que actualice el `UiState`.
*   **Choque Seguridad vs. UI:** Si una pantalla requiere mostrar datos sensibles, `design-ui-expert` maqueta el "enmascaramiento", pero la lógica de cuándo mostrar o bloquear proviene del estado emitido por `mobile-developer` (quien a su vez consume el caso de uso de `security-expert`).
*   **Veto Absoluto:** `quality-pm-expert` tiene poder de veto sobre cualquier PR o código generado que viole las reglas globales (ej. introducir XML o dependencias de red externa).
