# Especificación: Inicio de Sesión y Control de Turnos

## 1. Objetivo
Controlar el acceso seguro a la aplicación "Tortillería Derek", separando a los usuarios operativos de los consultivos, y gestionar el inicio de turnos de venta diarios para agrupar las transacciones de Room correctamente.

## 2. Historias de Usuario (US)
*   **US1 (Acceso):** Como usuario, necesito ingresar mi usuario y contraseña para acceder al sistema. El sistema debe venir precargado con 3 usuarios: `admin1`, `admin2` y `pruebas1`.
*   **US2 (Almacenamiento):** Como administrador, requiero que los usuarios y contraseñas se guarden en la base de datos local (Room).
*   **US3 (Seguridad de Credenciales - Exclusión de Login):** Por seguridad y control de acceso operativo, la pantalla de Login **no debe incluir** la opción de cambiar o restablecer contraseña. La actualización o cambio de contraseñas se gestiona exclusivamente dentro del sistema autenticado (en la sección de Ajustes / Configuración) por un usuario o administrador acreditado.
*   **US4 (Retroalimentación):** Como usuario, si me equivoco de credenciales, el sistema debe notificarme claramente que el usuario o contraseña son incorrectos.
*   **US5 (Modos de Ingreso):** Como usuario, al ingresar tendré dos opciones:
    *   *Abrir Nuevo Turno:* Crea un registro de turno diario para asociar las ventas de hoy y navega al Mostrador.
    *   *Solo Consulta:* No abre turno; navega directamente a la pantalla de Métricas/Historial.
*   **US6 (Ajustes):** Como administrador, necesito un icono de "Ajustes" en la pantalla de Login para configurar parámetros generales sin necesidad de abrir un turno.
*   **US7 (UI/UX):** La pantalla debe verse moderna, adaptativa (Móvil/Tablet) y alineada a Material Design 3.
*   **US8 (Persistencia y Enrutamiento Automático por Estado de Turno):** Como operador o cajero, al abrir o reiniciar la aplicación:
    *   Si **no se ha cerrado el turno** (existe una bandera/registro en Room con `estado = 'ABIERTO'`), el sistema debe cargar automáticamente la pantalla de venta **`Mostrador`**, evitando obligar al usuario a reautenticarse para continuar con las ventas en curso.
    *   Si el turno **ya fue cerrado** (`estado = 'CERRADO'`) o no existe turno previo, el sistema debe cargar obligatoriamente la pantalla de **`Login`** para iniciar sesión y abrir un nuevo turno de trabajo.

## 3. Criterios de Seguridad (by 🛡️ security-expert)
*   **Hash de Contraseñas:** Queda estrictamente prohibido guardar las contraseñas en texto plano en Room. Se debe usar una función de derivación de claves (ej. PBKDF2 o similar) para generar un Hash + Salt.
*   **Seed Inicial:** La creación de los usuarios por defecto (`admin1`, `admin2`, `pruebas1`) se hará mediante un `RoomDatabase.Callback` inicializando la base de datos con los hashes precalculados, nunca exponiendo las claves planas en el código de producción.
*   **Protección de Estado en Reposo:** El estado del turno (`ABIERTO`/`CERRADO`) reside exclusivamente en la base de datos local SQLite/Room con transacciones atómicas para prevenir condiciones de carrera al cerrar turno y reiniciar la aplicación.

## 4. Diseño Técnico y Arquitectura (by 🏗️ mobile-developer)

### 4.1. Entidades Room (Data)
*   `UsuarioEntity`: `id`, `username`, `passwordHash`, `salt`, `rol`.
*   `TurnoEntity`: `id`, `fechaApertura`, `estado` ("ABIERTO" / "CERRADO"), `usuarioId`, `fechaCierre`, `totalVentas`.
*   `TurnoDao`:
    *   `getTurnoActivo(): Flow<TurnoEntity?>`
    *   `getTurnoActivoSync(): TurnoEntity?`
    *   `cerrarTurno(turnoId, fechaCierre, totalVentas)`

### 4.2. Casos de Uso (Domain)
*   `LoginUseCase(username, password)` -> Retorna `Result<Usuario>`.
*   `AbrirTurnoUseCase(usuarioId)` -> Crea y retorna el turno del día persistiendo `estado = "ABIERTO"`.
*   `VerificarTurnoActivoUseCase()` -> Retorna `Boolean` (o `Flow<Boolean>`) consultando si `TurnoDao.getTurnoActivoSync() != null` y con `estado == "ABIERTO"`.
*   `CerrarTurnoUseCase(turnoId, totalVentas)` -> Actualiza en Room `estado = "CERRADO"`, `fechaCierre = now()`, invalidando el turno activo.
*   `CambiarPasswordUseCase(usuarioId, oldPass, newPass)`.

### 4.3. Lógica de Enrutamiento Inicial (Navigation Decision Router)
1. Al arrancar la aplicación (`MainActivity` / `AppNavigation`), se evalúa el estado del turno en Room mediante `VerificarTurnoActivoUseCase`:
   - Si existe turno con `estado == "ABIERTO"` -> Determina `startDestination = Screen.Mostrador`.
   - Si no existe turno o el último está en `estado == "CERRADO"` -> Determina `startDestination = Screen.Login`.
2. Mientras se consulta Room de forma asíncrona, se puede mostrar una pantalla de carga o Splash nativo para evitar parpadeos visuales (FOUC).
3. Al ejecutar la acción de "Cerrar Turno" en el Mostrador, una vez confirmada la actualización en Room, el efecto `NavigateToLogin` limpia el backstack (`popUpTo(0) { inclusive = true }`) para que el botón físico "Atrás" no permita reingresar sin credenciales.

### 4.4. Contrato MVI (Presentation)
*   **UiState:** `LoginUiState(isLoading, username, password, modoSeleccionado)`.
*   **UiEvent:** `OnUsernameChanged`, `OnPasswordChanged`, `OnAbrirTurnoClick`, `OnSoloConsultaClick`, `OnSettingsClick`.
*   **UiEffect:** `ShowError(message)`, `NavigateToMostrador`, `NavigateToMetricas`, `NavigateToSettings`.

## 5. Diseño de Interfaz (by 🎨 design-ui-expert)
*   **Móvil:** Formulario centrado tipo tarjeta, alineado a maqueta Stitch 01_login.
*   **Tablet:** Split screen; logo a la izquierda, formulario a la derecha.
*   **Componentes:** TextFields de Material 3 con `PasswordVisualTransformation` para la contraseña, textos en negro de alto contraste, espacio fijo reservado para avisos de error (previniendo desplazamiento de botones). Botones Primary ("Abrir Turno") y Secondary/Tonal ("Solo Consulta"). Icono de engranaje (Settings) en la esquina superior derecha.

## 6. Criterios de Aceptación y QA (by 🏆 quality-pm-expert)
- [ ] **Test (Enrutamiento Turno Activo):** Dado que en Room existe un registro en `turnos` con `estado = 'ABIERTO'`, cuando se inicia la app, entonces el destino inicial es automáticamente `Screen.Mostrador`.
- [ ] **Test (Enrutamiento Turno Cerrado):** Dado que en Room el último turno tiene `estado = 'CERRADO'` o la tabla está vacía, cuando se inicia la app, entonces el destino inicial es `Screen.Login`.
- [ ] **Test (Cierre de Turno y Bloqueo):** Dado un turno activo en `Mostrador`, cuando el usuario confirma "Cerrar Turno", entonces se actualiza `estado = 'CERRADO'` en Room y se navega a `Screen.Login` purgando el historial de navegación.
- [ ] **Test:** Dado un usuario no registrado, cuando intenta login, entonces el sistema emite `UiEffect.ShowError`.
- [ ] **Test:** Dado el usuario "admin1", cuando selecciona "Abrir Nuevo Turno", entonces se registra el turno en Room con estado "ABIERTO" y navega a `Mostrador`.
- [ ] **Test:** Cuando selecciona "Solo Consulta", entonces navega a `Metricas` y la tabla de Turnos no sufre inserciones.
- [ ] **UI:** Existen `@Preview`s para Móvil, Tablet, Tema Claro y Tema Oscuro.
- [ ] **Semántica:** Los botones e inputs tienen `testTag` (ej. `login_username_input`, `login_abrir_turno_button`).
