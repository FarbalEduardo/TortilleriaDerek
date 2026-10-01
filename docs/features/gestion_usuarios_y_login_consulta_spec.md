# Especificación Técnica: Modo Solo Consulta, Navegación a Ajustes desde Login y Gestión de Usuarios en Configuración

**Estado:** Validada por 🏆 `quality-pm-expert` y 🛡️ `security-expert`  
**Fecha:** 17 de Septiembre, 2026  
**Iteración:** Seguridad, Modo Solo Consulta y CRUD de Usuarios  

---

## 1. Contexto y Objetivos del Negocio
La aplicación móvil **Tortillería Derek** requiere dotar a la pantalla de Login y Configuración de tres capacidades fundamentales:
1. **Modo Solo Consulta Estricto:** Permitir a usuarios supervisores o auditores acceder exclusivamente a la información analítica de la tortillería sin aperturar turnos de venta y **sin barra de navegación inferior (`Bottom Navigation View`)**, impidiendo la navegación a cajas, producción o ajustes operativos.
2. **Acceso Rápido a Configuración:** Permitir a administradores ingresar a la pantalla de Configuración desde el icono de ajustes en la cabecera de Login.
3. **Gestión Integral de Usuarios (CRUD):** Permitir a los encargados en la pantalla de Configuración dar de alta nuevos usuarios, editar datos y contraseñas/PIN, y dar de baja operadores, garantizando persistencia en Room y hashing criptográfico.

---

## 2. Historias de Usuario (US)

### US-LOG-01: Acceso en Modo Solo Consulta sin Bottom Navigation Bar
- **Como** supervisor o auditor del negocio,
- **Quiero** seleccionar "Modo Solo Consulta" e ingresar mis credenciales válidas en la pantalla de Login,
- **Para** visualizar de forma directa y exclusiva la pantalla de **Métricas**, **sin que aparezca la barra inferior de navegación (Bottom Navigation View)**, garantizando que no pueda desviar mi sesión hacia el registro de ventas, producción ni alteración de parámetros.
- **Detalle UX:** La pantalla de Métricas en este modo exhibirá un botón superior para "Cerrar Consulta" que regresa a la pantalla de Login.

### US-LOG-02: Acceso Directo a Configuración desde Login sin Bottom Navigation View
- **Como** administrador técnico del sistema,
- **Quiero** pulsar el botón de engranaje (ajustes) en la esquina superior derecha de la pantalla de Login,
- **Para** navegar directamente y de forma exclusiva a la pantalla de Configuración (`Screen.ConfiguracionDesdeLogin`) **sin que aparezca la barra inferior de navegación (Bottom Navigation View)**, permitiendo gestionar parámetros y usuarios con un botón para regresar a Login.

### US-CFG-01: Gestión de Usuarios (CRUD en Configuración)
- **Como** administrador de la tortillería,
- **Quiero** disponer de una sección "Gestión de Usuarios del Sistema" en la pantalla de Configuración,
- **Para** consultar la lista de operadores activos, agregar nuevos usuarios con rol y contraseña, editar sus credenciales o roles, y eliminar usuarios que ya no laboren en el negocio.

---

## 3. Criterios de Seguridad y Reglas de Negocio (🛡️ `security-expert`)

### US-SEC-04: Cifrado y Salting de Credenciales
- Toda contraseña o PIN ingresado al crear o editar un usuario debe cifrarse mediante `CryptoManager.generateSalt()` y `CryptoManager.hashPassword(passwordRaw, salt)`.
- Queda estrictamente prohibido persistir contraseñas en texto plano en la tabla `usuarios` de Room.

### US-SEC-05: Protección de Bloqueo Administrativo Total
- La base de datos y la lógica de negocio deben validar que siempre exista al menos un usuario con rol `ADMIN`.
- Si el usuario a eliminar es el único administrador del sistema, la acción es rechazada mostrando una alerta explicativa al usuario.

### US-SEC-06: Protección de Turno Activo
- Se prohíbe eliminar al usuario cuyo `usuarioId` coincida con el usuario del turno operativo actualmente en estado `ABIERTO`.

---

## 4. Criterios de Aceptación QA (🏆 `quality-pm-expert`)

### Scenario 1: Login en Modo Solo Consulta
- **Given** el usuario selecciona "Modo Solo Consulta" e ingresa credenciales válidas,
- **When** presiona "Iniciar Sesión",
- **Then** la aplicación navega a `Screen.MetricasSoloConsulta`, no se genera ningún turno en Room, la barra de navegación inferior (`TortilleriaNavBar`) **no se muestra** y un botón superior permite volver a Login.

### Scenario 2: Navegación de Login a Configuración
- **Given** la pantalla de Login,
- **When** el usuario hace clic en el icono de engranaje en la esquina superior derecha,
- **Then** la aplicación navega a la pantalla de Configuración (`Screen.ConfiguracionDesdeLogin`), la barra de navegación inferior (`Bottom Navigation View`) **no aparece** y se muestra un botón en la cabecera para regresar al Login.

### Scenario 3: Alta de Usuario en Configuración
- **Given** la sección "Gestión de Usuarios" en Configuración,
- **When** se pulsa "+ Nuevo Usuario" y se ingresa username "cajero3", rol "EMPLEADO" y PIN "1234",
- **Then** el nuevo usuario se persiste en Room con salt y hash, apareciendo en la lista de usuarios y permitiendo autenticarse en Login.

### Scenario 4: Intento de Borrado del Último Administrador
- **Given** una base de datos donde solo existe un usuario con rol "ADMIN",
- **When** el usuario intenta eliminarlo en Configuración,
- **Then** el sistema bloquea la eliminación informando que debe existir al menos un administrador en el sistema.
