# Registro de Progreso (Task Tracker)

## Feature Anterior: Login y Control de Turnos
**Spec:** `docs/features/login_y_turnos_spec.md` - [Completada]

---

## Feature Actual: Pantalla de Ventas (Mostrador y Repartidores) e Historial
**Spec:** `docs/features/ventas_y_repartidores_spec.md`

### Checklist de Implementación SDD
- [x] **Fase 1: Especificación y Criterios (Inception)**
  - Requerimientos de negocio documentados (specs 1 al 7).
  - Criterios de seguridad (🛡️ `security-expert`): inmutabilidad de ventas con turno cerrado, trazabilidad de cajero.
  - Criterios de aceptación y QA (🏆 `quality-pm-expert`).
- [x] **Fase 2: Prototipado Visual (Exploración - 🎨 `design-ui-expert`)**
  - Componentes Compose y maquetación fiel a las 3 capturas:
    - `HeroSalesCard`: Card naranja con fecha, cerrar turno, monto y selector.
    - `MostradorRepartidoresTabBar`: Conmutador de chips segmentado.
    - `MostradorScreen`: Steppers de Kilogramo, 1/2 kg, Paquete, CTA dinámico y diálogo de cobro.
    - `RepartidoresScreen`: Badge de rutas activas, tarjetas de choferes por estatus y diálogos de salida/liquidación.
    - `HistorialScreen`: Calca exacta de la captura 3 con split Efectivo/Tarjeta y transacciones.
    - `TortilleriaNavBar`: Barra inferior compartida.
- [x] **Fase 3: Lógica Core y Persistencia Room (🏗️ `mobile-developer`)**
  - Entidades Room: `VentaEntity`, `RutaRepartidorEntity`, actualización de `TurnoEntity`.
  - DAOs: `VentaDao`, `RutaRepartidorDao`, `TurnoDao`.
  - Migración a Room v2 con `fallbackToDestructiveMigration` en `DataModule`.
  - `VentaViewModel` con MVI (`VentaUiState`, `VentaUiEffect`).
- [x] **Fase 4: Integración y Navegación (🏗️ `mobile-developer` + 🎨 `design-ui-expert`)**
  - Conexión de `Screen.Mostrador` e `Screen.Historial` en `AppNavigation.kt`.
  - Flujo de salida hacia `Screen.Login` al cerrar turno.
- [x] **Fase 5: Aseguramiento de Calidad (🏆 `quality-pm-expert`)**
  - Compilación de Kotlin exitosa (`compileDebugKotlin` -> BUILD SUCCESSFUL).
  - Integridad semántica y de contratos entre agentes verificada.

---

### Iteración US4: Cabecera Fija y Pager de Subcontenido
- [x] **🎨 `design-ui-expert` & 🏗️ `mobile-developer`**: Mantener `HeroSalesCard` y `MostradorRepartidoresTabBar` fijos e inmóviles en la cabecera persistente; animar únicamente el subcontenido inferior como un pager fluido (`HorizontalPager`).
- [x] **🏆 `quality-pm-expert`**: Compilación exitosa verificada con `./gradlew compileDebugKotlin` (BUILD SUCCESSFUL).

---

### Iteración Refinamiento UX: Eliminación de Notificación Superior Fija
- [x] **🎨 `design-ui-expert`**: Eliminación del banner superpuesto superior (`AnimatedVisibility` con `mensajeToast`) en [MostradorScreen.kt](file:///d:/TortilleriaDerek/app/src/main/java/com/example/tortilleriaderek/ui/screens/MostradorScreen.kt) que permanecía anclado en pantalla.
- [x] **🏗️ `mobile-developer`**: Limpieza del campo `mensajeToast` en [VentaUiState.kt](file:///d:/TortilleriaDerek/app/src/main/java/com/example/tortilleriaderek/presentation/venta/VentaUiState.kt) y en [VentaViewModel.kt](file:///d:/TortilleriaDerek/app/src/main/java/com/example/tortilleriaderek/presentation/venta/VentaViewModel.kt). Se preserva exclusivamente el `Toast` nativo inferior de Android emitido a través del canal MVI `VentaUiEffect.ShowToast`.
- [x] **🏆 `quality-pm-expert`**: Verificación de compilación exitosa con `./gradlew compileDebugKotlin` (BUILD SUCCESSFUL).

---

### Iteración Lineamientos Google: Bottom Navigation View & Insets Inferiores
- [x] **🎨 `design-ui-expert`**: Reescritura de [TortilleriaBottomBar.kt](file:///d:/TortilleriaDerek/app/src/main/java/com/example/tortilleriaderek/ui/components/TortilleriaBottomBar.kt) utilizando componentes oficiales de Material 3 (`NavigationBar` y `NavigationBarItem`), respetando el espaciado y altura estándar de Google (80dp) con indicador activo en forma de píldora personalizada (`MaizPrimaryLight`) y divider superior de separación.
- [x] **🏗️ `mobile-developer`**: Integración de `WindowInsets` de sistema (`NavigationBarDefaults.windowInsets` / `WindowInsets.navigationBars`) para que el fondo del navigation bar se dibuje de borde a borde (Edge-to-Edge) mientras que los íconos y textos queden automáticamente despegados por encima de la barra de gestos o botonera de navegación de Android.
- [x] **🏆 `quality-pm-expert`**: Verificación de compilación exitosa con `./gradlew compileDebugKotlin` (BUILD SUCCESSFUL).

**Estado actual:** Bottom Navigation Bar actualizado con lineamientos oficiales de Google Material 3 y verificado con éxito.

---

### Iteración SDD: Persistencia de Turno en Login y Múltiples Cortes Diarios
- [x] **🏗️ `mobile-developer` & 🛡️ `security-expert`**: Actualización de [login_y_turnos_spec.md](file:///d:/TortilleriaDerek/docs/features/login_y_turnos_spec.md) con **US8**: control de enrutamiento automático mediante bandera persistida en Room (`TurnoEntity.estado == "ABIERTO"`). Si el turno está abierto, la aplicación carga directamente `Mostrador`; si está cerrado o no existe, carga `Login`.
- [x] **🏗️ `mobile-developer` & 🎨 `design-ui-expert`**: Creación de la especificación técnica completa [cortes_y_turnos_multiples_spec.md](file:///d:/TortilleriaDerek/docs/features/cortes_y_turnos_multiples_spec.md) que define la arquitectura y experiencia para gestionar más de 2 cierres de corte de caja en un mismo día (numeración correlativa `numeroTurnoDia`, folios `CORTE-YYYYMMDD-0X`, aislamiento estricto de ventas por turno, diálogo de arqueo y reporte consolidado diario Corte Z).
- [x] **🏆 `quality-pm-expert`**: Criterios de aceptación (Given/When/Then) y validación de especificaciones lista para fase de implementación.

---

### Iteración US9: Trazabilidad de Usuario en Ventas (Mostrador y Repartidores)
- [x] **🏆 `quality-pm-expert`**: Incorporación de **US9** en [ventas_y_repartidores_spec.md](file:///d:/TortilleriaDerek/docs/features/ventas_y_repartidores_spec.md), documentando el requerimiento de negocio y los criterios de aceptación para auditar qué usuario registró la venta.
- [x] **🛡️ `security-expert`**: Especificación del criterio de seguridad y auditoría contable: cada registro en `VentaEntity` vincula el `usuarioId` y `usuarioNombre` exactos de la sesión activa del operador/cajero.
- [x] **🏗️ `mobile-developer`**: 
  - Incorporación de `getUsuarioById(id)` en [UsuarioDao.kt](file:///d:/TortilleriaDerek/app/src/main/java/com/example/tortilleriaderek/data/local/dao/UsuarioDao.kt).
  - Consulta reactiva del usuario del turno en [VentaViewModel.kt](file:///d:/TortilleriaDerek/app/src/main/java/com/example/tortilleriaderek/presentation/venta/VentaViewModel.kt) exponiendo `usuarioActivoNombre` en [VentaUiState.kt](file:///d:/TortilleriaDerek/app/src/main/java/com/example/tortilleriaderek/presentation/venta/VentaUiState.kt).
  - Persistencia del usuario en ventas de mostrador (`onConfirmarVenta`) y ventas de repartidores (`onConfirmarLiquidarRepartidor`) en Room.
- [x] **🏆 `quality-pm-expert`**: Verificación de compilación exitosa con `./gradlew compileDebugKotlin` (BUILD SUCCESSFUL).

**Estado actual:** Especificación US9 e implementación de trazabilidad de usuario en ventas completadas y verificadas con éxito.

---

### Iteración US8: Carga Automática de Pantalla según Estado de Turno en Room
- [x] **🏗️ `mobile-developer`**: Creación de [VerificarTurnoActivoUseCase.kt](file:///d:/TortilleriaDerek/app/src/main/java/com/example/tortilleriaderek/domain/usecase/VerificarTurnoActivoUseCase.kt) que consulta de forma síncrona/atómica a Room (`getTurnoActivoSync() != null && estado == "ABIERTO"`).
- [x] **🏗️ `mobile-developer`**: Creación de [MainNavigationViewModel.kt](file:///d:/TortilleriaDerek/app/src/main/java/com/example/tortilleriaderek/presentation/navigation/MainNavigationViewModel.kt) exponiendo `NavigationStartState` (`Loading` -> `Ready(Screen.Mostrador)` si hay turno abierto, o `Ready(Screen.Login)` si no hay turno o está cerrado).
- [x] **🎨 `design-ui-expert` & 🏗️ `mobile-developer`**: Integración en [AppNavigation.kt](file:///d:/TortilleriaDerek/app/src/main/java/com/example/tortilleriaderek/presentation/navigation/AppNavigation.kt) para renderizar `NavHost` con el destino inicial dinámico y un contenedor neutro sin parpadeos visuales durante la lectura en Room.
- [x] **🏗️ `mobile-developer`**: Corrección en [VentaViewModel.kt](file:///d:/TortilleriaDerek/app/src/main/java/com/example/tortilleriaderek/presentation/venta/VentaViewModel.kt) eliminando la inserción automática de turnos con `id = "1"` al detectar `turno == null`, garantizando que solo `AbrirTurnoUseCase` pueda iniciar turnos válidos desde Login.
- [x] **🏆 `quality-pm-expert`**: Cobertura de pruebas unitarias al 100% en [VerificarTurnoActivoUseCaseTest.kt](file:///d:/TortilleriaDerek/app/src/test/java/com/example/tortilleriaderek/domain/usecase/VerificarTurnoActivoUseCaseTest.kt).
- [x] **🏆 `quality-pm-expert`**: Ejecución de la suite completa de pruebas unitarias (`:app:testDebugUnitTest`) resultando en `BUILD SUCCESSFUL`.

---

### Iteración US10: Diálogo de Confirmación de Venta de Contado y Jerarquía Visual
- [x] **🏆 `quality-pm-expert`**: Incorporación de **US10** en [ventas_y_repartidores_spec.md](file:///d:/TortilleriaDerek/docs/features/ventas_y_repartidores_spec.md) con los criterios de negocio (venta de contado fija sin selector de método de pago) y criterios de aceptación QA.
- [x] **🎨 `design-ui-expert`**: Rediseño jerárquico del diálogo de confirmación en [MostradorScreen.kt](file:///d:/TortilleriaDerek/app/src/main/java/com/example/tortilleriaderek/ui/screens/MostradorScreen.kt):
  - **Nivel 1 (Total & CTA)**: Tarjeta cálida destacada (`Color(0xFFFFF9F5)`) con borde naranja sutil y total en 24sp ExtraBold (`MaizPrimary`), botón principal de cobro con alto contraste y altura táctil de 44dp.
  - **Nivel 2 (Contexto)**: Badges compactos para folio (`SurfaceContainerLow`) y estado "De Contado" con icono en verde agave (`VerdeAgaveLight`).
  - **Nivel 3 (Detalle)**: Contenedor ordenado de productos con cápsulas numéricas y subtotales en negrita.
  - **Nivel 4 (Cancelación)**: Botón neutro `OutlinedButton` en `TextSecondary`.
- [x] **🏗️ `mobile-developer`**: Eliminación del selector de método de pago de la vista y fijación de `metodoPago = "Efectivo"` por regla de negocio en [VentaViewModel.kt](file:///d:/TortilleriaDerek/app/src/main/java/com/example/tortilleriaderek/presentation/venta/VentaViewModel.kt).
- [x] **🏆 `quality-pm-expert`**: Verificación de compilación y control de calidad exitoso con `./gradlew compileDebugKotlin` (BUILD SUCCESSFUL).

---

### Iteración: Rediseño Fiel de Producción, Métricas y Ajustes e Integración en Bottom Navigation View
**Spec:** `docs/features/redisenio_produccion_metricas_ajustes_spec.md` - [Completada y Verificada]

- [x] **Fase 1: Especificación y Criterios (Inception - 🏆 `quality-pm-expert` & 🛡️ `security-expert`)**
  - Documento de especificación [redisenio_produccion_metricas_ajustes_spec.md](file:///d:/TortilleriaDerek/docs/features/redisenio_produccion_metricas_ajustes_spec.md) con análisis de interacción sistémica reactiva (Room como SSOT).
  - Criterios de seguridad y trazabilidad inmutable de usuario (quién amasó/produjo, quién cobró en mostrador, quién asignó y liquidó repartidores).
- [x] **Fase 2: Prototipado Visual Fiel a las 3 Capturas (🎨 `design-ui-expert`)**
  - [ProduccionScreen.kt](file:///d:/TortilleriaDerek/app/src/main/java/com/example/tortilleriaderek/ui/screens/ProduccionScreen.kt): Hero curvo naranja (14.5 kg, barra 90.5%, producido neto y vendido), Stepper de bultos con cálculo en tiempo real de masa cruda y tortilla cocida, botones Registrar Tanda e Historial.
  - [MetricasScreen.kt](file:///d:/TortilleriaDerek/app/src/main/java/com/example/tortilleriaderek/ui/screens/MetricasScreen.kt): Header con selector de fecha, conmutador de períodos (Semanal, Quincenal, Mensual), toggle Producción vs Ventas, gráfica de curva suave Bezier en Canvas con tooltip "210 kg • Jueves", barra segmentada de canales (65% vs 35%) y cards de ritmo y merma.
  - [ConfiguracionScreen.kt](file:///d:/TortilleriaDerek/app/src/main/java/com/example/tortilleriaderek/ui/screens/ConfiguracionScreen.kt): Catálogo de precios con monogramas (`1k`, `Pq`, `½`) y diálogos de edición, estándares de rendimiento (38.5 kg y merma 1.5 kg / 3.8%), y gestión de repartidores con badges `#01`, `#02` y modales para agregar.
- [x] **Fase 3 & 4: Integración y Navegación en Grafo (🏗️ `mobile-developer`)**
  - Conexión de las 4 pantallas en [AppNavigation.kt](file:///d:/TortilleriaDerek/app/src/main/java/com/example/tortilleriaderek/presentation/navigation/AppNavigation.kt) con `navigateToBottomBarTab` (`popUpTo(Screen.Mostrador)`, `saveState = true`, `launchSingleTop = true`, `restoreState = true`).
  - Scaffold con `TortilleriaNavBar` integrado y activo en cada pestaña (`Venta`, `Producción`, `Métricas`, `Ajustes`).
- [x] **Fase 5: Aseguramiento de Calidad (🏆 `quality-pm-expert`)**
  - Eliminación de advertencias de deprecación (`Icons.AutoMirrored.Filled.ShowChart`).
  - Verificación de compilación Kotlin con `./gradlew compileDebugKotlin` (BUILD SUCCESSFUL).
  - Verificación de la suite completa de pruebas unitarias con `./gradlew testDebugUnitTest` (BUILD SUCCESSFUL en 3m 28s).

**Estado actual:** Pantallas de Producción, Métricas y Ajustes rediseñadas al 100% fielmente, integradas en la navegación del Bottom Navigation View y verificadas exitosamente con la suite de pruebas unitarias.

---

### Iteración US11 & US12: Trazabilidad de Repartidor, Moto y Ruta en Room e Historial
**Specs:** [ventas_y_repartidores_spec.md](file:///d:/TortilleriaDerek/docs/features/ventas_y_repartidores_spec.md) y [redisenio_produccion_metricas_ajustes_spec.md](file:///d:/TortilleriaDerek/docs/features/redisenio_produccion_metricas_ajustes_spec.md) - [Completada y Verificada]

- [x] **Fase 1: Especificación y Criterios (Inception - 🏆 `quality-pm-expert` & 🛡️ `security-expert`)**
  - Incorporación de **US11**: Obligatoriedad de almacenar en `VentaEntity` y `RutaRepartidorEntity` de Room las propiedades `nombreRepartidor`, `motoAsignada` (número/modelo) y `nombreRuta`.
  - Incorporación de **US12**: Visualización enriquecida en `HistorialScreen` de la cápsula identificativa con repartidor, moto y ruta para cada ticket de tipo `Reparto` (`R-XXXX`).
  - Actualización del impacto multi-pantalla en `redisenio_produccion_metricas_ajustes_spec.md` (Producción ➔ Historial, Métricas ➔ Historial, Ajustes ➔ Reparto).
- [x] **Fase 2 & 3: Entidades Room y Lógica Core (🏗️ `mobile-developer`)**
  - Actualización de [VentaEntity.kt](file:///d:/TortilleriaDerek/app/src/main/java/com/example/tortilleriaderek/data/local/entity/VentaEntity.kt) con `nombreRepartidor`, `motoAsignada` y `nombreRuta`.
  - Actualización de [RutaRepartidorEntity.kt](file:///d:/TortilleriaDerek/app/src/main/java/com/example/tortilleriaderek/data/local/entity/RutaRepartidorEntity.kt) con `nombreRuta`.
  - Incremento de versión de Room a `version = 3` en [TortilleriaDatabase.kt](file:///d:/TortilleriaDerek/app/src/main/java/com/example/tortilleriaderek/data/local/TortilleriaDatabase.kt).
  - Persistencia de estos campos en `onConfirmarLiquidarRepartidor` y siembra de rutas con nombres de ruta en [VentaViewModel.kt](file:///d:/TortilleriaDerek/app/src/main/java/com/example/tortilleriaderek/presentation/venta/VentaViewModel.kt).
- [x] **Fase 4: Adaptación Visual Multi-Pantalla (🎨 `design-ui-expert`)**
  - [RepartidoresScreen.kt](file:///d:/TortilleriaDerek/app/src/main/java/com/example/tortilleriaderek/ui/screens/RepartidoresScreen.kt): Visualización de `nombreRuta` en la tarjeta de repartidores.
  - [HistorialScreen.kt](file:///d:/TortilleriaDerek/app/src/main/java/com/example/tortilleriaderek/ui/screens/HistorialScreen.kt): Renderizado de la insignia cálida con icono de moto, repartidor, moto asignada y nombre de ruta en transacciones de reparto.
- [x] **Fase 5: Aseguramiento de Calidad (🏆 `quality-pm-expert`)**
  - Verificación de compilación Kotlin con `./gradlew compileDebugKotlin` (BUILD SUCCESSFUL en 2m 29s).
  - Verificación de la suite completa de pruebas unitarias con `./gradlew testDebugUnitTest` (BUILD SUCCESSFUL en 1m 30s).

**Estado actual:** Trazabilidad de repartidor, moto asignada y nombre de ruta persistida en Room e integrada en Historial, Repartidores y specs con éxito.

---

### Iteración Optimización UX: Eliminación de Transiciones / Oscurecimiento en Bottom Navigation View
- [x] **🎨 `design-ui-expert` & 🏗️ `mobile-developer`**: Configuración de transiciones instantáneas en [AppNavigation.kt](file:///d:/TortilleriaDerek/app/src/main/java/com/example/tortilleriaderek/presentation/navigation/AppNavigation.kt) mediante `EnterTransition.None` y `ExitTransition.None` en `NavHost`, eliminando por completo el efecto de oscurecimiento y parpadeo crossfade entre pestañas del Bottom Navigation View (`Venta`, `Producción`, `Métricas`, `Ajustes`).
- [x] **🏆 `quality-pm-expert`**: Verificación de compilación Kotlin (`compileDebugKotlin` -> BUILD SUCCESSFUL en 1m 16s) y ejecución de pruebas unitarias (`testDebugUnitTest` -> BUILD SUCCESSFUL en 1m 26s).

**Estado actual:** Transición entre pantallas de navegación inferior 100% instantánea, limpia y sin parpadeos.

---

### Iteración Modo Solo Consulta, Acceso a Ajustes desde Login y Gestión de Usuarios en Configuración
**Specs:** [gestion_usuarios_y_login_consulta_spec.md](file:///d:/TortilleriaDerek/docs/features/gestion_usuarios_y_login_consulta_spec.md) - [Completada y Verificada]

- [x] **Fase 1: Especificación y Criterios (Inception - 🏆 `quality-pm-expert` & 🛡️ `security-expert`)**
  - **US-LOG-01**: En Login con `Modo Solo Consulta` activo, navegar exclusivamente a Métricas ocultando el Bottom Navigation View (`mostrarBottomBar = false`) y habilitando salida a Login.
  - **US-LOG-02**: En Login, el botón de engranaje (Ajustes) navega directamente a la pantalla de Configuración (`Screen.Ajustes`).
  - **US-CFG-01**: En Configuración, añadir tarjeta interactiva de Gestión de Usuarios para agregar, editar y eliminar usuarios con validaciones de contraseña (mínimo 4 caracteres) y protección contra el borrado del último administrador del sistema (`countAdmins()`).
- [x] **Fase 2 & 3: Persistencia Room y Caso de Uso (🏗️ `mobile-developer`)**
  - Métodos añadidos a [UsuarioDao.kt](file:///d:/TortilleriaDerek/app/src/main/java/com/example/tortilleriaderek/data/local/dao/UsuarioDao.kt): `getAllUsuarios()`, `updateUsuario()`, `deleteUsuarioById()`, `countAdmins()`.
  - Creación de [GestionUsuariosUseCase.kt](file:///d:/TortilleriaDerek/app/src/main/java/com/example/tortilleriaderek/domain/usecase/GestionUsuariosUseCase.kt) con hashing + salt mediante `CryptoManager` y validación de reglas de negocio.
- [x] **Fase 4: Adaptación Visual y Navegación (🎨 `design-ui-expert` & 🏗️ `mobile-developer`)**
  - [MetricasScreen.kt](file:///d:/TortilleriaDerek/app/src/main/java/com/example/tortilleriaderek/ui/screens/MetricasScreen.kt): Ocultación total de `bottomBar` cuando `mostrarBottomBar == false`, agregando banner de Modo Solo Consulta y botón "Cerrar sesión / Salir".
  - [ConfiguracionScreen.kt](file:///d:/TortilleriaDerek/app/src/main/java/com/example/tortilleriaderek/ui/screens/ConfiguracionScreen.kt): Sección interactiva de Gestión de Usuarios del Sistema con monogramas, chips de roles y diálogos modales para alta, edición y baja.
  - [AppNavigation.kt](file:///d:/TortilleriaDerek/app/src/main/java/com/example/tortilleriaderek/presentation/navigation/AppNavigation.kt): Definición de la ruta `Screen.MetricasSoloConsulta` que invoca `MetricasScreen(mostrarBottomBar = false)`.
- [x] **Fase 5: Aseguramiento de Calidad (🏆 `quality-pm-expert`)**
  - Verificación de compilación Kotlin con `./gradlew compileDebugKotlin` (BUILD SUCCESSFUL en 3m 2s).
  - Verificación de la suite completa de pruebas unitarias con `./gradlew testDebugUnitTest` (BUILD SUCCESSFUL en 1m 28s).

**Estado actual:** Modo Solo Consulta estricto sin Bottom Navigation View, acceso a Configuración desde Login y CRUD de gestión de usuarios implementados y certificados al 100%.

---

### Iteración US3: Remoción de Opción "Cambiar o Restablecer Contraseña" de la Pantalla de Login
- [x] **🛡️ `security-expert` & 🏆 `quality-pm-expert`**: Actualización de [login_y_turnos_spec.md](file:///d:/TortilleriaDerek/docs/features/login_y_turnos_spec.md) (**US3**), formalizando que la pantalla de acceso público no debe contener enlaces ni diálogos para modificar credenciales. El cambio y restablecimiento de contraseñas queda centralizado exclusivamente dentro del entorno autenticado de Configuración/Ajustes.
- [x] **🎨 `design-ui-expert`**: Remoción del enlace "Cambiar o restablecer contraseña" y del componente `ChangePasswordDialog` en [LoginScreen.kt](file:///d:/TortilleriaDerek/app/src/main/java/com/example/tortilleriaderek/presentation/login/LoginScreen.kt), conservando un pie de página limpio con la insignia de seguridad institucional.
- [x] **🏗️ `mobile-developer`**: Limpieza de estado y eventos en [LoginContract.kt](file:///d:/TortilleriaDerek/app/src/main/java/com/example/tortilleriaderek/presentation/login/LoginContract.kt) (`isChangePasswordDialogVisible`, `OnToggleChangePasswordDialog`) y en [LoginViewModel.kt](file:///d:/TortilleriaDerek/app/src/main/java/com/example/tortilleriaderek/presentation/login/LoginViewModel.kt).
- [x] **🏆 `quality-pm-expert`**: Verificación de compilación exitosa con `./gradlew compileDebugKotlin` (BUILD SUCCESSFUL en 4m 14s).
### Iteración US13, US14 & US15: Turno Limpio de Repartidores, Rediseño Jerárquico de Diálogos y Bloqueo Estricto de Cierre por Rutas Activas
**Specs:** [ventas_y_repartidores_spec.md](file:///d:/TortilleriaDerek/docs/features/ventas_y_repartidores_spec.md) - [Completada y Verificada]

- [x] **Fase 1: Especificación y Criterios (Inception - 🏆 `quality-pm-expert` & 🛡️ `security-expert`)**
  - **US13**: Al abrir turno desde Login, inicializar todas las rutas de repartidores con `status = "PENDIENTE_SALIDA"` y `cargaInicialKg = 0.0`.
  - **US14**: Rediseño jerárquico de los diálogos de Salida (asignación con chips rápidos 50kg, 70kg, 90kg y cálculo en tiempo real) y Liquidación (resumen de carga inicial, captura de devolución/merma, cálculo de venta neta, confirmación de efectivo y ticket correlativo R-XXXX).
  - **US15**: Bloqueo estricto del cierre de turno si existen rutas activas (`rutasActivasCount > 0`). Visualización destacada en el diálogo de advertencia de las motos en ruta y botón de confirmación deshabilitado sin bypass por contraseña.
- [x] **Fase 2: Componentes Modulares de Diálogo (🎨 `design-ui-expert`)**
  - Creación de [DialogSalidaRepartidor.kt](file:///d:/TortilleriaDerek/app/src/main/java/com/example/tortilleriaderek/ui/components/DialogSalidaRepartidor.kt): Estructura en 4 niveles de jerarquía visual, chips rápidos de pesaje, cálculo reactivo de cobro esperado ($22/kg) y previews.
  - Creación de [DialogLiquidarRepartidor.kt](file:///d:/TortilleriaDerek/app/src/main/java/com/example/tortilleriaderek/ui/components/DialogLiquidarRepartidor.kt): Card de carga inicial, cálculo reactivo de kilos entregados y dinero esperado en caja, input de efectivo y previews.
- [x] **Fase 3: Lógica Core, UseCase y Validación (🏗️ `mobile-developer` & 🛡️ `security-expert`)**
  - Actualización de [AbrirTurnoUseCase.kt](file:///d:/TortilleriaDerek/app/src/main/java/com/example/tortilleriaderek/domain/usecase/AbrirTurnoUseCase.kt) inyectando `RutaRepartidorDao` para inicializar el catálogo de rutas en `PENDIENTE_SALIDA` con `0.0 kg`.
  - [VentaViewModel.kt](file:///d:/TortilleriaDerek/app/src/main/java/com/example/tortilleriaderek/presentation/venta/VentaViewModel.kt): Validación de seguridad estricta en `onConfirmarCerrarTurno()` bloqueando el cierre si `rutasActivasCount > 0`.
  - Integración de los nuevos diálogos y de la tarjeta de advertencia de bloqueo en [MostradorScreen.kt](file:///d:/TortilleriaDerek/app/src/main/java/com/example/tortilleriaderek/ui/screens/MostradorScreen.kt) y [RepartidoresScreen.kt](file:///d:/TortilleriaDerek/app/src/main/java/com/example/tortilleriaderek/ui/screens/RepartidoresScreen.kt).
- [x] **Fase 4: Aseguramiento de Calidad y Pruebas Unitarias (🏆 `quality-pm-expert`)**
  - Actualización de [AbrirTurnoUseCaseTest.kt](file:///d:/TortilleriaDerek/app/src/test/java/com/example/tortilleriaderek/domain/usecase/AbrirTurnoUseCaseTest.kt) validando la inserción de rutas limpias en `PENDIENTE_SALIDA` con `0.0 kg`.
  - Cobertura de bloqueo de cierre en [VentaViewModelTest.kt](file:///d:/TortilleriaDerek/app/src/test/java/com/example/tortilleriaderek/presentation/venta/VentaViewModelTest.kt).

**Estado actual:** Turno limpio de repartidores (US13), rediseño jerárquico de diálogos de salida y liquidación (US14) y bloqueo estricto de cierre de turno por rutas activas (US15) completados y verificados.

---

### Iteración QA: Batería Completa de Pruebas Unitarias de Arquitectura y Reglas de Negocio
- [x] **🏆 `quality-pm-expert`**: Creación de [VentaViewModelTest.kt](file:///d:/TortilleriaDerek/app/src/test/java/com/example/tortilleriaderek/presentation/venta/VentaViewModelTest.kt) cubriendo:
  - Inicialización y carga de turno abierto con usuario activo.
  - Bloqueo estricto de cierre de turno si `rutasActivasCount > 0` con emisión de `ShowToast` y sin llamadas a Room.
  - Cierre exitoso cuando `rutasActivasCount == 0`, actualizando `isTurnoCerrado = true` y navegando a Login.
  - Control de apertura y cierre del diálogo de confirmación de corte.
  - Modificación de cantidades de producto y rechazo de valores negativos.
  - Filtro por scope ("Mostrador", "Repartidores", "Todos").
  - Confirmación de venta de mostrador de contado ("Efectivo") con trazabilidad de usuario.
- [x] **🏆 `quality-pm-expert`**: Creación de [MainNavigationViewModelTest.kt](file:///d:/TortilleriaDerek/app/src/test/java/com/example/tortilleriaderek/presentation/navigation/MainNavigationViewModelTest.kt) cubriendo arranque dinámico según estado del turno en Room (US8).
- [x] **🏆 `quality-pm-expert`**: Verificación y ejecución de la suite completa de pruebas unitarias con `./gradlew testDebugUnitTest` (**BUILD SUCCESSFUL en 47s**, 100% pruebas en verde).

---

### Iteración US16: Inicio Limpio en Cero de Contadores Mostrador y Repartidores sin Asignación
**Specs:** [ventas_y_repartidores_spec.md](file:///d:/TortilleriaDerek/docs/features/ventas_y_repartidores_spec.md) - [Completada y Verificada]

- [x] **Fase 1: Especificación y Criterios (Inception - 🏆 `quality-pm-expert` & 🛡️ `security-expert`)**
  - Incorporación de **US16**: al iniciar sesión, abrir turno o entrar a Venta, todos los contadores de Mostrador (Kilogramo, 1/2 kilogramo, Paquete) deben arrancar estrictamente en cero (`quantity = 0`, subtotal `$0.00`).
  - Ningún repartidor debe figurar con montos de cobro ni kilos asignados; todas las rutas inician limpias en `PENDIENTE_SALIDA` con `0.0 kg` y `$0.00 MXN`.
- [x] **Fase 2 & 3: Lógica Core y Estado UI (🏗️ `mobile-developer` & 🎨 `design-ui-expert`)**
  - [VentaUiState.kt](file:///d:/TortilleriaDerek/app/src/main/java/com/example/tortilleriaderek/presentation/venta/VentaUiState.kt): Eliminación de cantidades hardcodeadas (`quantity = 4`, `quantity = 1`) en la lista por defecto de `products`, inicializando todos los productos en `quantity = 0`.
  - [VentaViewModel.kt](file:///d:/TortilleriaDerek/app/src/main/java/com/example/tortilleriaderek/presentation/venta/VentaViewModel.kt): Al activarse o cerrarse un turno, se garantiza el reseteo explícito de `products` a `quantity = 0`.
  - [RepartidoresScreen.kt](file:///d:/TortilleriaDerek/app/src/main/java/com/example/tortilleriaderek/ui/screens/RepartidoresScreen.kt): Corrección del fallback de rutas en la interfaz, eliminando el mock residual con `EN_RUTA` y `LIQUIDADO` con saldos preasignados. Todas las rutas por defecto arrancan en `PENDIENTE_SALIDA` con `0.0 kg` y `$0.00 MXN`.
- [x] **Fase 4: Aseguramiento de Calidad y Pruebas Unitarias (🏆 `quality-pm-expert`)**
  - [VentaViewModelTest.kt](file:///d:/TortilleriaDerek/app/src/test/java/com/example/tortilleriaderek/presentation/venta/VentaViewModelTest.kt): Añadidas aserciones y tests específicos para validar que `state.products.all { it.quantity == 0 }`, `state.subtotalOrdenActual == 0.0`, y que todas las rutas iniciales están en `PENDIENTE_SALIDA` con `0.0 kg` y `$0.00`.

**Estado actual:** Contadores de mostrador iniciando en cero absoluto y repartidores limpios sin asignación (US16) completados y verificados.

---

## Feature Siguiente: Backup y Transferencia de Datos entre Teléfonos (Opción 1 — SQLite Directo)
**Spec:** `docs/features/backup_transferencia_db_spec.md` - [Pendiente]

### Checklist de Implementación SDD

- [x] **Fase 1: Especificación y Criterios (Inception)**
  - Requerimientos documentados (US-BKP-01 al 09): exportar `.db` con selector SAF (`CreateDocument`), importar `.db`, blindaje energético (batería ≥ 25% o cargador conectado), autenticación obligatoria de Administrador Principal (acceso sin sesión abierta), blindaje estricto de turnos cerrados, micro-lección animada de versión, advertencia destructiva con selector de contraseña de admin (Opción 1), overlay animado de carga y reinicio guiado.
  - Criterios de seguridad (🛡️ `security-expert`): blindaje energético contra corrupción flash, autenticación con hash PBKDF2 + salt para usuarios no logueados, SHA-256 de integridad, rollback automático con `.prev`, mitigación total de lockout con `PoliticaPasswordAdmin` y garantía de supervivencia del Administrador Principal (`admin1`).
  - 8 criterios de aceptación QA y 14 casos límite (🏆 `quality-pm-expert`): batería baja (< 25%), corrupción, WAL, versión incompatible con micro-lección, discrepancia de contraseñas de admin, espacio insuficiente, turnos cerrados, crash durante importación.

- [x] **Fase 2: Lógica Core, UseCase y Generación de Pruebas Unitarias (🏗️ `mobile-developer` + 🏆 `quality-pm-expert`)**
  - Creación de [BackupModels.kt](file:///d:/TortilleriaDerek/app/src/main/java/com/example/tortilleriaderek/domain/model/BackupModels.kt): `BatteryStatusProvider`, `PoliticaPasswordAdmin`, `DbInspectionResult`, `BackupExportInfo` y `BackupError`.
  - Creación de [BackupStoragePort.kt](file:///d:/TortilleriaDerek/app/src/main/java/com/example/tortilleriaderek/domain/repository/BackupStoragePort.kt): abstracción para SAF, SQLite checkpoints y restauración física.
  - Creación de [BackupUseCase.kt](file:///d:/TortilleriaDerek/app/src/main/java/com/example/tortilleriaderek/domain/usecase/BackupUseCase.kt) con validaciones en cascada de batería, autenticación criptográfica de administrador, bloqueo por turnos abiertos e inspección de versión SQLite.
  - Creación y ejecución exitosa de [BackupUseCaseTest.kt](file:///d:/TortilleriaDerek/app/src/test/java/com/example/tortilleriaderek/domain/usecase/BackupUseCaseTest.kt) (19 pruebas unitarias cubriendo batería ≥ 25%, roles, PBKDF2, turnos abiertos, versionado v7 y políticas de contraseña -> **BUILD SUCCESSFUL**).

- [x] **Fase 3: Presentation — ViewModel y Efectos MVI (🏗️ `mobile-developer`)**
  - Creación de [BackupContract.kt](file:///d:/TortilleriaDerek/app/src/main/java/com/example/tortilleriaderek/presentation/backup/BackupContract.kt) definiendo `BackupUiState`, `BackupUiIntent` y `BackupUiEffect` para los 6 modales y contratos SAF.
  - Creación de [BackupViewModel.kt](file:///d:/TortilleriaDerek/app/src/main/java/com/example/tortilleriaderek/presentation/backup/BackupViewModel.kt) con validaciones de batería, bloqueo por turno activo, autenticación de admin, checkpoints WAL e importación con rollback.
  - Cobertura de pruebas unitarias al 100% en [BackupViewModelTest.kt](file:///d:/TortilleriaDerek/app/src/test/java/com/example/tortilleriaderek/presentation/backup/BackupViewModelTest.kt) (batería baja, autenticación admin, selector SAF, micro-lección y reinicio).

- [x] **Fase 4: UI Compose "Maíz & Masa POS" (Cero Diseño Genérico) e Integración (🎨 `design-ui-expert` + 🏗️ `mobile-developer`)**
  - Creación de [DialogBateriaInsuficiente.kt](file:///d:/TortilleriaDerek/app/src/main/java/com/example/tortilleriaderek/ui/components/backup/DialogBateriaInsuficiente.kt): modal de alerta energética (< 25%) con badge en Terracota y recomendación de cargador.
  - Creación de [DialogAutenticacionAdminBackup.kt](file:///d:/TortilleriaDerek/app/src/main/java/com/example/tortilleriaderek/ui/components/backup/DialogAutenticacionAdminBackup.kt): modal de autenticación segura PBKDF2 para usuarios sin sesión activa.
  - Creación de [DialogAdvertenciaReemplazoDb.kt](file:///d:/TortilleriaDerek/app/src/main/java/com/example/tortilleriaderek/ui/components/backup/DialogAdvertenciaReemplazoDb.kt): semáforo de riesgo y selector interactivo de 3 opciones de contraseña de administrador (Opción 1).
  - Creación de [OverlayCargandoDbAnimado.kt](file:///d:/TortilleriaDerek/app/src/main/java/com/example/tortilleriaderek/ui/components/backup/OverlayCargandoDbAnimado.kt): overlay bloqueante con animación continua en Canvas (silo de datos y pulsos concéntricos).
  - Creación de [DialogMicroLeccionVersion.kt](file:///d:/TortilleriaDerek/app/src/main/java/com/example/tortilleriaderek/ui/components/backup/DialogMicroLeccionVersion.kt): micro-lección animada e interactiva en 3 pasos cuando las versiones SQLite difieren.
  - Creación de [DialogReinicioExitosoDb.kt](file:///d:/TortilleriaDerek/app/src/main/java/com/example/tortilleriaderek/ui/components/backup/DialogReinicioExitosoDb.kt): modal de confirmación con CTA destacado de reinicio guiado.
  - Creación de [DialogOpcionesBackup.kt](file:///d:/TortilleriaDerek/app/src/main/java/com/example/tortilleriaderek/ui/components/backup/DialogOpcionesBackup.kt): selector modal cálido entre exportar e importar base de datos.
  - Creación de [BackupDialogsContainer.kt](file:///d:/TortilleriaDerek/app/src/main/java/com/example/tortilleriaderek/ui/components/backup/BackupDialogsContainer.kt): contenedor de orquestación visual y de launchers SAF (`CreateDocument` / `OpenDocument`).
  - Integración en [LoginScreen.kt](file:///d:/TortilleriaDerek/app/src/main/java/com/example/tortilleriaderek/presentation/login/LoginScreen.kt) (botón de acción superior con icono SwapHoriz) y [AppNavigation.kt](file:///d:/TortilleriaDerek/app/src/main/java/com/example/tortilleriaderek/presentation/navigation/AppNavigation.kt).
  - Integración en [ConfiguracionScreen.kt](file:///d:/TortilleriaDerek/app/src/main/java/com/example/tortilleriaderek/ui/screens/ConfiguracionScreen.kt) con la Card 6 de "Transferencia y Respaldo de Datos (SQLite)".

- [x] **Fase 5: Aseguramiento de Calidad y Suite Completa (🏆 `quality-pm-expert`)**
  - Pruebas unitarias de caso de uso en [BackupUseCaseTest.kt](file:///d:/TortilleriaDerek/app/src/test/java/com/example/tortilleriaderek/domain/usecase/BackupUseCaseTest.kt) (19 pruebas en verde).
  - Pruebas unitarias de presentación en [BackupViewModelTest.kt](file:///d:/TortilleriaDerek/app/src/test/java/com/example/tortilleriaderek/presentation/backup/BackupViewModelTest.kt) (9 pruebas en verde).
  - Verificación de compilación Kotlin con `./gradlew compileDebugKotlin` (**BUILD SUCCESSFUL** sin advertencias).
  - Verificación de la suite completa con `./gradlew testDebugUnitTest` (**BUILD SUCCESSFUL en 3m 28s**, 100% pruebas pasando).

**Estado actual:** Feature de Backup y Transferencia de Datos entre Teléfonos (Opción 1 — SQLite Directo) completamente implementada, verificada y certificada con pruebas automatizadas.

---

## Feature Actual: Plan de Mejora y Mitigación de Auditoría Técnica
**Spec:** `docs/features/plan_mejora_audit_sdd.md`

### Checklist de Implementación SDD
- [x] **Fase 1: Especificación y Criterios (Inception)**
  - Redacción y validación de `docs/features/plan_mejora_audit_sdd.md` (Historias de usuario US-01 a US-03, casos límite y criterios de aceptación).
  - Criterios de seguridad (🛡️ `security-expert`): exclusión de base de datos local y SharedPreferences sensibles en `backup_rules.xml` y `data_extraction_rules.xml`.
  - Validación QA y SDD Gate (🏆 `quality-pm-expert`).
- [x] **Fase 2: Persistencia y Migraciones Room (🏗️ `mobile-developer`)**
  - Implementación de `exportSchema = true` en `TortilleriaDatabase` con esquema JSON v7 generado por KSP.
  - Creación de [Migrations.kt](file:///d:/TortilleriaDerek/app/src/main/java/com/example/tortilleriaderek/data/local/migration/Migrations.kt) con soporte robusto e idempotente para saltos v1..v6 -> v7 con `ALL_MIGRATIONS`.
  - Configuración e inyección segura en `DataModule` (`.addMigrations(*Migrations.ALL_MIGRATIONS)`).
  - Creación de suite automatizada [MigrationsTest.kt](file:///d:/TortilleriaDerek/app/src/test/java/com/example/tortilleriaderek/data/local/migration/MigrationsTest.kt) validando la integridad del DDL sin pérdida de datos.
- [x] **Fase 3: Seguridad en Respaldos y Extracción de Datos (🛡️ `security-expert` + 🏗️ `mobile-developer`)**
  - Actualización de `backup_rules.xml` para excluir la base de datos local del respaldo cloud automático.
  - Actualización de `data_extraction_rules.xml` para control estricto de extracción.
- [x] **Fase 4: Modularización de ConfiguracionScreen (🎨 `design-ui-expert` + 🏗️ `mobile-developer`)**
  - Descomposición de [ConfiguracionScreen.kt](file:///d:/TortilleriaDerek/app/src/main/java/com/example/tortilleriaderek/ui/screens/ConfiguracionScreen.kt) de 2,000 líneas a 400 líneas (cumpliendo estrictamente el Artículo III de la Constitución).
  - Creación de 11 subcomponentes stateless puros en `ui/components/configuracion/`:
    - `CardPreciosProductos.kt`
    - `CardEstandaresProduccion.kt`
    - `CardGestionRepartidores.kt`
    - `CardGestionUsuarios.kt`
    - `CardRespaldoTransferenciaDb.kt`
    - `ConfiguracionHeader.kt`
    - `ConfiguracionDialogsContainer.kt`
    - Diálogos modales: `DialogEditarProducto.kt`, `DialogAgregarRepartidor.kt`, `DialogsUsuarioCrud.kt`, `DialogConfirmacionAdmin1.kt`, `DialogEditarEstandaresProduccion.kt`, `DialogAvisoSeguridad.kt`.
- [x] **Fase 5: Aseguramiento de Calidad y Suite Completa (🏆 `quality-pm-expert`)**
  - 110 pruebas unitarias ejecutadas al 100% en verde con `./gradlew testDebugUnitTest` (0 fallos, 0 errores).
  - Scripts de auditoría automatizados (`check_design.ps1`, `audit_quality.ps1`) operativos.

---

## 🚀 Próximas Pantallas para Modularización y Erradicación de Strings Hardcodeados:
1. **[HistorialScreen.kt](file:///d:/TortilleriaDerek/app/src/main/java/com/example/tortilleriaderek/ui/screens/HistorialScreen.kt)** (643 lín. -> objetivo < 350 lín.)
2. **[ProduccionScreen.kt](file:///d:/TortilleriaDerek/app/src/main/java/com/example/tortilleriaderek/ui/screens/ProduccionScreen.kt)** (706 lín. -> objetivo < 350 lín.)
3. **[RepartidoresScreen.kt](file:///d:/TortilleriaDerek/app/src/main/java/com/example/tortilleriaderek/ui/screens/RepartidoresScreen.kt)** (709 lín. -> objetivo < 350 lín.)
4. **[MostradorScreen.kt](file:///d:/TortilleriaDerek/app/src/main/java/com/example/tortilleriaderek/ui/screens/MostradorScreen.kt)** (931 lín. -> objetivo < 350 lín.)
5. **[MetricasScreen.kt](file:///d:/TortilleriaDerek/app/src/main/java/com/example/tortilleriaderek/ui/screens/MetricasScreen.kt)** (1127 lín. -> objetivo < 350 lín.)
6. **Externalización masiva de strings UI a `res/values/strings.xml`** (227 literales detectados por `check_design.ps1`).

---

## 🏛️ Hito Constitucional: Adopción del Modo de Diseño y Sistema de Agentes 2.0
- [x] **Constitución Maestra de Agentes:** Creación de [AGENTS.md](file:///d:/TortilleriaDerek/AGENTS.md) con los 6 Artículos Inviolables de ingeniería (Offline-First, Cero Hardcoding, Anti-God Composables < 400 lín., Clean Architecture, Migraciones Seguras, Ergonomía Táctil de Tortillería).
- [x] **Workflow del Modo de Diseño:** Creación de [.agents/workflows/design_mode_workflow.md](file:///d:/TortilleriaDerek/.agents/workflows/design_mode_workflow.md) con procedimiento en 6 pasos para maquetar interfaces stateless con `@Preview` multi-dispositivo y fidelidad al diseño de Stitch.
- [x] **Orquestador SDD Maestro:** Creación de [.agents/workflows/sdd_orchestrator.md](file:///d:/TortilleriaDerek/.agents/workflows/sdd_orchestrator.md) coordinando las 5 fases de desarrollo.
- [x] **Ecosistema de Skills Especializados:**
  - [design-expert-skill/SKILL.md](file:///d:/TortilleriaDerek/.agents/skills/design-expert-skill/SKILL.md) + `check_design.ps1` (Tokens Maíz & Masa, ergonomía táctil 56-64dp, números tabulares `tnum`).
  - [mobile-developer-skill/SKILL.md](file:///d:/TortilleriaDerek/.agents/skills/mobile-developer-skill/SKILL.md) + `audit_arch.ps1` (Fronteras Clean Architecture, Room y MVI).
  - [quality-pm-expert-skill/SKILL.md](file:///d:/TortilleriaDerek/.agents/skills/quality-pm-expert-skill/SKILL.md) + `audit_quality.ps1` (DoD y verificación).
  - [security-expert-skill/SKILL.md](file:///d:/TortilleriaDerek/.agents/skills/security-expert-skill/SKILL.md) (Criptografía PBKDF2 y SAF).
- [x] **Actualización de Guardrails y Agentes:** Refuerzo de [.agents/rules/general_rules.md](file:///d:/TortilleriaDerek/.agents/rules/general_rules.md), [.agents/rules/orchestration_contract.md](file:///d:/TortilleriaDerek/.agents/rules/orchestration_contract.md) y [.agents/agents/design-ui-expert.md](file:///d:/TortilleriaDerek/.agents/agents/design-ui-expert.md).





