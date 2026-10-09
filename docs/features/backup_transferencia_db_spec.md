# Especificación Técnica: Backup y Transferencia de Datos — Exportar/Importar `.db` Directo

**Estado:** Validada por 🏆 `quality-pm-expert` y 🛡️ `security-expert`  
**Fecha:** 3 de Octubre, 2026  
**Iteración:** Opción 1 — Backup SQLite Directo con Autenticación de Administrador, Selector SAF de Destino, Micro-Lección Animada, Blindaje de Turnos Cerrados, Advertencia Destructiva, Animación de Carga y Reinicio Obligatorio  

---

## 1. Contexto y Objetivos del Negocio

La aplicación **Tortillería Derek** opera 100% **offline-first** con Room Database (SQLite) local.
No existe backend ni sincronización en la nube. El negocio necesita poder:

1. **Clonar la app completa** de un teléfono a otro (cambio de dispositivo, relevo de equipo de mostrador, teléfono de respaldo).
2. **Crear respaldos manuales** sin necesidad de iniciar sesión operativa (accesible desde pantalla de Login o Configuración).
3. **Restaurar un respaldo** anterior ante eventualidades, garantizando la inmutabilidad de la contabilidad, la preservación del acceso administrativo y una experiencia visual clara que informe al usuario en todo momento.

### Base de Datos en Producción

| Parámetro | Valor |
|---|---|
| Nombre de BD | `tortilleria_db` |
| Versión Room actual | `7` |
| Motor | SQLite (Room con WAL mode) |
| Tablas | 8 (`usuarios`, `configuracion_produccion`, `repartidores`, `turnos`, `ventas`, `tandas_produccion`, `mermas_produccion`, `rutas_repartidores`) |
| Tamaño estimado | 500 KB – 10 MB |
| Archivos en disco | `tortilleria_db`, `tortilleria_db-shm`, `tortilleria_db-wal` |

---

## 2. Historias de Usuario (US)

### US-BKP-01: Exportar Respaldo con Selección de Destino (SAF)
- **Como** administrador de la tortillería,
- **Quiero** pulsar "📤 Exportar Respaldo" desde Configuración o desde el menú de utilidades de la pantalla de Login,
- **Para** que el sistema abra el selector de almacenamiento nativo de Android (Storage Access Framework `CreateDocument`), permitiéndome **elegir exactamente en qué carpeta, unidad (memoria interna, tarjeta SD, USB OTG o carpeta compartida) y con qué nombre guardar el archivo `.db`**, con opción inmediata de compartirlo (WhatsApp, Bluetooth, etc.).

### US-BKP-02: Importar Respaldo Completo con Reemplazo Atómico
- **Como** administrador de la tortillería,
- **Quiero** pulsar "📥 Importar Respaldo" y seleccionar un archivo `.db` válido mediante el explorador nativo,
- **Para** reemplazar de forma atómica y segura la base de datos local con el contenido del respaldo, restaurando la totalidad del historial sin riesgo de estados intermedios.

### US-BKP-03: Autenticación Obligatoria de Administrador Principal (Acceso sin Login)
- **Como** sistema y responsable de seguridad contable,
- **Quiero** que tanto la exportación como la lectura/importación de la base de datos —especialmente cuando se ejecuten desde la pantalla pública de Login o sin una sesión activa— **soliciten obligatoriamente el usuario y contraseña del Administrador Principal (`rol == 'ADMIN'`)**,
- **Para** impedir que cualquier empleado, cajero u operario no autorizado descargue la base contable con hashes o sobrescriba la base de datos del negocio.

### US-BKP-04: Blindaje Estricto de Turnos Cerrados (Integridad Contable)
- **Como** auditor y dueño del negocio,
- **Quiero** que el sistema verifique que **todos los turnos operativos se encuentren en estado `CERRADO`** (`TurnoEntity.estado == "CERRADO"`) antes de autorizar cualquier exportación o importación,
- **Para** evitar exportar o sobrescribir ventas pendientes de arqueo, rutas de repartidores aún activas en calle o folios de corte huérfanos.

### US-BKP-05: Micro-Lección Animada ante Incompatibilidad de Versión
- **Como** usuario que intenta restaurar un respaldo de una versión distinta de la base de datos,
- **Quiero** que en lugar de un mensaje de error técnico incomprensible, el sistema despliegue una **micro-lección animada e interactiva paso a paso**,
- **Para** comprender visualmente por qué las versiones de los dos teléfonos no coinciden y aprender exactamente qué 3 pasos debo seguir para actualizar la aplicación y migrar mis datos con éxito.

### US-BKP-06: Advertencia Explícita de Reemplazo Irreversible y Selector de Contraseña de Admin
- **Como** administrador del negocio,
- **Quiero** recibir una notificación clara antes de confirmar la carga de una base de datos informando que **los datos operativos actuales (ventas, turnos, producción, repartidores y usuarios operadores) serán reemplazados por completo y no se podrán recuperar**,
- **Y Quiero** poder elegir mediante un selector qué contraseña de Administrador Principal tendrá la app tras la restauración:
  1. 🔘 **Mantener la contraseña actual de este teléfono (Recomendada):** Conserva la clave/PIN que acabo de ingresar para autorizar la importación.
  2. 🔘 **Usar la contraseña del Respaldo:** Adopta la contraseña que tenía el teléfono de origen al momento de exportar.
  3. 🔘 **Establecer una nueva contraseña ahora:** Permite capturar un nuevo PIN/clave de administrador en el mismo diálogo.
- **Para** tener certeza absoluta de los datos que se sustituyen y evitar cualquier riesgo de quedarme bloqueado sin acceso (*lockout*) por discrepancias de contraseñas entre ambos teléfonos.

### US-BKP-07: Animación Continua de Carga de Base de Datos
- **Como** usuario que espera mientras se procesa la importación,
- **Quiero** ver en pantalla una **animación continua e interactiva de transferencia de base de datos** con textos explicativos de progreso,
- **Para** tener absoluta certeza de que el proceso está activo y no intentar tocar la pantalla ni forzar el cierre de la app durante la escritura en disco.

### US-BKP-08: Notificación de Finalización Exitosa y Reinicio Obligatorio
- **Como** administrador,
- **Quiero** que al finalizar la carga de la base de datos se despliegue un modal confirmando el éxito de la operación y solicitando el **reinicio de la aplicación**,
- **Para** pulsar un botón "Reiniciar Aplicación Ahora" que reinicie limpiamente el proceso de Android y arranque la app fresca en la pantalla de Login con los nuevos datos cargados y la contraseña elegida.

### US-BKP-09: Blindaje Energético — Batería Mínima ≥ 25% o Cargador Conectado
- **Como** sistema y responsable de la integridad de los datos,
- **Quiero** comprobar el nivel de energía del dispositivo antes de iniciar cualquier operación de exportación o importación de base de datos,
- **Para** exigir que el teléfono cuente con **al menos un 25% de batería o esté conectado a la corriente eléctrica (cargador)**, bloqueando la operación en caso contrario para evitar que el teléfono se apague abruptamente a mitad de la escritura en disco generando archivos truncados o bases de datos corruptas.

---

## 3. Arquitectura y Flujos Técnicos

### 3.1 Flujo de Exportación con Autenticación, Batería y Selector SAF

```
[Pantalla de Login / Configuración]
        │
        ▼  (Usuario pulsa "Exportar Respaldo")
[Verificación de Blindaje Energético (US-BKP-09)]
        ├─ Consultar: BatteryManager.BATTERY_PROPERTY_CAPACITY
        ├─ Consultar: IntentFilter(ACTION_BATTERY_CHANGED) -> isCharging
        └─ Si bateria < 25% && !isCharging:
              └─▶ [DESPLEGAR DIÁLOGO BATERÍA INSUFICIENTE] (STOP)
                  "Se requiere al menos 25% de batería o conectar el cargador."
        │
        ▼
[Diálogo Autenticación Administrador Principal]
        │  Usuario ingresa username + password/PIN
        ├─ Validar hash PBKDF2 + salt en tabla usuarios
        ├─ Validar usuario.rol == "ADMIN"
        └─ Si inválido → Error "Acceso denegado: solo Administrador Principal" (STOP)
        │
        ▼
[Verificación de Turnos Cerrados]
        ├─ Consultar: TurnoDao.getTurnoActivoSync()
        └─ Si != null (turno ABIERTO) → Bloquear: "Turno activo en curso. Realice el corte primero." (STOP)
        │
        ▼
[Checkpoint WAL y Preparación de Archivo]
        ├─ Ejecutar PRAGMA wal_checkpoint(FULL)
        ├─ Generar nombre sugerido: "tortilleria_backup_YYYYMMDD_HHmmss.db"
        └─ Calcular checksum SHA-256 de verificación
        │
        ▼
[Lanzar SAF ActivityResultContracts.CreateDocument("application/x-sqlite3")]
        │  Usuario selecciona carpeta/unidad destino (Descargas, SD, USB, etc.)
        ▼
[BackupUseCase.copiarHaciaUri(safDestinationUri)]
        ├─ Escribir stream hacia destino seleccionado por el usuario
        ├─ Notificar éxito con huella SHA-256
        └─ Ofrecer botón "Compartir archivo..." (Intent.ACTION_SEND a WhatsApp/Bluetooth)
```

### 3.2 Flujo de Importación Completo: Batería, Advertencia, Selector, Animación y Reinicio

```
[Pantalla de Login / Configuración]
        │
        ▼  (Usuario pulsa "Importar Respaldo")
[Verificación de Blindaje Energético (US-BKP-09)]
        ├─ Consultar: bateria >= 25% || isCharging
        └─ Si falso ──▶ [DESPLEGAR DIÁLOGO BATERÍA INSUFICIENTE] (STOP)
        │
        ▼
[Diálogo Autenticación Administrador Principal Actual]
        │  Requiere credenciales del admin local
        ├─ Validar credenciales de admin local en Room activo
        ├─ Guardar temporalmente en memoria: adminLocalHash y adminLocalSalt validados
        └─ Si inválido → Bloquear operación (STOP)
        │
        ▼
[Lanzar SAF ActivityResultContracts.OpenDocument()]
        │  Usuario selecciona archivo .db
        ▼
[Lectura Previa e Inspección de Cabecera SQLite]
        ├─ 1. Abrir SQLite en modo lectura desacoplado
        ├─ 2. Ejecutar: PRAGMA user_version;
        ├─ 3. ¿user_version == 7 (VERSIÓN_ACTUAL)?
        │     │
        │     ├─ NO ──▶ [DESPLEGAR MICRO-LECCIÓN ANIMADA (US-BKP-05)]
        │     │          Explicación visual del desalineamiento de APKs
        │     │          Guía paso a paso: actualizar app, reexportar, reimportar (STOP)
        │     │
        │     └─ SÍ ──▶ Continúa validación
        │
        ▼
[Inspección de Turnos en Archivo Importado]
        ├─ Verificar que no haya turnos en estado 'ABIERTO' en el archivo entrante
        └─ Si existe turno abierto en el backup → Alerta especial de arqueo forzado
        │
        ▼
[Diálogo de Advertencia Destructiva y Selector de Contraseña (US-BKP-06)]
        │  ⚠️ Notifica: "Los historiales de ventas, turnos, producción y usuarios
        │  operativos serán reemplazados por completo y NO se podrán recuperar."
        │  🔑 Selector de Contraseña de Admin:
        │     (•) Mantener la contraseña actual de este teléfono (Recomendada)
        │     ( ) Usar la contraseña del respaldo (Teléfono de origen)
        │     ( ) Establecer una nueva contraseña ahora [input PIN/clave]
        ▼  Usuario presiona botón de confirmación roja: "Sí, Reemplazar Base de Datos"
[Overlay Bloqueante con Animación Continua de Carga (US-BKP-07)]
        │  Pantalla bloqueada + animación Lottie/Compose de base de datos pulsante
        │  Textos dinámicos: "Cargando y validando base de datos...", "Consolidando registros..."
        │
        ├─ 1. Crear copia temporal: tortilleria_db.prev en filesDir
        ├─ 2. Cerrar instancia activa de Room (db.close())
        ├─ 3. Sustituir tortilleria_db con el stream del archivo seleccionado
        ├─ 4. Borrar explícitamente tortilleria_db-shm y tortilleria_db-wal
        ├─ 5. Reabrir Room y ejecutar PRAGMA integrity_check
        ├─ 6. Aplicar Política de Contraseña de Admin seleccionada:
        │     ├─ Si "Mantener actual" ──▶ UPDATE usuarios SET passwordHash = adminLocalHash, salt = adminLocalSalt WHERE rol = 'ADMIN'
        │     ├─ Si "Nueva clave"     ──▶ Generar salt fresco, hash PBKDF2 y UPDATE usuarios WHERE rol = 'ADMIN'
        │     └─ Si "Del respaldo"    ──▶ Conservar intacto el hash/salt que viene en el archivo
        ├─ 7. Validar presencia de Administrador Principal (si falta, inyectar admin1 de rescate)
        │     ├─ Si falla integridad ──▶ RESTAURAR DESDE .prev AUTOMÁTICAMENTE
        │     └─ Si exitoso ──▶ Eliminar .prev y finalizar animación
        │
        ▼
[Diálogo de Éxito y Reinicio Obligatorio de la App (US-BKP-08)]
        │  ✅ "Base de datos cargada exitosamente."
        │  🔄 "Es necesario reiniciar la aplicación para aplicar todos los cambios."
        │  Botón CTA: [REINICIAR APLICACIÓN AHORA]
        ▼
[Reinicio Limpio del Proceso Android / Navegación a Login]
```

---

## 4. Especificación Detallada: Blindaje de Turnos Cerrados

### 4.1 Justificación Contable
En el modelo de negocio de Tortillería Derek, los turnos abiertos representan **operaciones volátiles**:
- Hay dinero físico en caja no arqueado.
- Existen repartidores con carga de tortilla pendiente de liquidación.
- Los folios de venta aún no están consolidados en un `folioCorte` (`CORTE-YYYYMMDD-0X`).

Si se permitiera exportar o importar con un turno abierto:
1. Al importar en otro teléfono, el turno anterior quedaría congelado sin usuario físico que lo cierre, corrompiendo la correlación del siguiente corte (`numeroTurnoDia`).
2. Las pantallas de **Métricas** excluyen deliberadamente los turnos abiertos (conforme a `metricas_analitica_turnos_cerrados_spec.md`). Por tanto, una base de datos restaurada con turno abierto provocaría inconsistencias analíticas invisibles.

### 4.2 Reglas Inmutables de Turnos para Backup

1. **Precondición de Exportación:**
   ```kotlin
   val turnoActivo = turnoDao.getTurnoActivoSync()
   if (turnoActivo != null && turnoActivo.estado == "ABIERTO") {
       throw BackupTurnoActivoException("No se puede exportar con un turno abierto.")
   }
   ```
2. **Precondición de Importación Local:**
   - La base de datos local receptora **no debe tener un turno abierto**. Si el operador tiene un turno abierto en el teléfono B, debe hacer corte de caja antes de recibir la nueva base de datos.
3. **Validación del Archivo Entrante:**
   - Si por fuerza mayor el archivo exportado (ej. de un teléfono dañado repentinamente) contiene un turno en estado `ABIERTO`, el importador detecta esta anomalía y solicita al Administrador autorizar un **"Cierre de Turno de Emergencia"** asignando automáticamente `fechaCierre = now`, `notasCierre = "Cierre forzado por migración entre teléfonos"` antes de finalizar la restauración.
4. **Preservación Histórica:**
   - Todos los turnos cerrados (`TurnoEntity.estado == "CERRADO"`) se importan con sus claves foráneas idénticas (`VentaEntity.turnoId`, `RutaRepartidorEntity.turnoId`), garantizando que la pantalla de **Historial** y el motor de **Métricas** continúen dibujando las curvas de producción y venta sin distorsión.

---

## 5. Diseño UX, Identidad Visual y Estilo "Maíz & Masa POS" (No Genérico)

### 5.1 Filosofía Estética de Tortillería Derek
El diseño de los flujos de backup y migración **no debe ser un diálogo genérico estándar de Android**. Debe adherirse con rigor a la identidad visual establecida en la aplicación:

| Token Visual | Valor Hex | Rol en los Componentes de Respaldo |
|---|---|---|
| `MaizPrimary` | `#FF6B00` | Botones de acción principal, bordes activos de selección, anillos de animación. |
| `MaizGradient` | `#FF7A00` ➔ `#FA6400` | Degradado energético para cabeceras y barra de progreso fluida de la carga. |
| `MaizPrimaryLight` | `#FFF0E5` | Fondos de halos de iconos de advertencia y cápsulas de estado. |
| `TerracotaSecondary` | `#C2410C` | Texto de advertencia sobre datos irrecuperables y badges de riesgo. |
| `TerracotaLight` | `#FFEDD5` | Fondo cálido de alerta sin llegar a rojo agresivo corporativo. |
| `VerdeAgave` | `#2D6A4F` | Confirmación de integridad SHA-256 y éxito de la restauración. |
| `VerdeAgaveLight` | `#D1FAE5` | Fondo de cápsula de éxito y checkmarks de la micro-lección. |
| `SurfaceWarm` | `#FDFBF7` | Fondo cálido general de las tarjetas, emulando textura de papel estraza/maíz limpio. |
| `SurfaceContainerLow` | `#F6ECE6` | Fondo de los ítems de selección y tarjetas de pasos. |
| `BorderSubtle` | `#E7E0D6` | Borde de tarjetas y separadores para dar profundidad suave. |
| `TextPrimary` | `#1F1B17` | Títulos de alto contraste con jerarquía contundente en negrita. |
| `TextSecondary` | `#736E69` | Descripciones y notas explicativas cálidas. |

---

### 5.2 Modal de Advertencia Destructiva y Selección de Contraseña (`DialogAdvertenciaReemplazoDb`)
- **Estructura y Jerarquía Visual:**
  - Contenedor con `RoundedCornerShape(24.dp)` sobre `SurfaceWarm` con borde `BorderStroke(1.dp, BorderSubtle)`.
  - **Cabecera destacada:** Icono circular de 52dp en `CircleShape` con fondo `TerracotaLight` y glifo de advertencia en `TerracotaSecondary`. Título en 20sp `FontWeight.ExtraBold` en `TextPrimary`.
  - **Bloque de Datos a Reemplazar:** Tarjeta contenida en `SurfaceContainerLow` con micro-chips con monogramas temáticos (`Ventas`, `Turnos`, `Producción`, `Repartidores`, `Usuarios`) con viñetas en color naranja sutil.
  - **Selector de Contraseña de Admin Interactivo:**
    - Tres tarjetas seleccionables en lugar de simples radio buttons planos.
    - Cada tarjeta tiene un borde de 1.5dp que se ilumina en `MaizPrimary` con fondo `MaizPrimaryLight` cuando está activa.
    - Contiene título en negrita (ej. *"Mantener contraseña actual de este teléfono"*) y subtítulo en 12sp (`TextSecondary`).
    - Si se selecciona *"Establecer nueva contraseña"*, se despliega con animación fluida `AnimatedVisibility` un campo `OutlinedTextField` estilizado con soporte de visualización/ocultamiento de PIN.
  - **Botonera Ergonómica:**
    - Botón primario de 48dp de altura con fondo `ErrorRed` (`#BA1A1A`) y texto blanco en `FontWeight.Bold`: *"Sí, Reemplazar Base de Datos"*.
    - Botón secundario `OutlinedButton` en color neutro: *"Cancelar"*.

```
┌─────────────────────────────────────────────────────────┐
│     [⚠️ Icono en TerracotaLight con halo circular]       │
│         REEMPLAZO DE BASE DE DATOS LOCAL                │
├─────────────────────────────────────────────────────────┤
│  Se sobreescribirán los datos de este dispositivo:      │
│  ┌───────────────────────────────────────────────────┐  │
│  │ 📦 Ventas anteriores   🏭 Producción y tandas     │  │
│  │ 🛵 Rutas repartidores  👥 Usuarios y cajeros      │  │
│  │ ⚠️ Esta acción es definitiva y no se recupera.    │  │
│  └───────────────────────────────────────────────────┘  │
│                                                         │
│  🔑 ¿Qué contraseña de Admin quedará activa?            │
│  ┌───────────────────────────────────────────────────┐  │
│  │ ◉ Mantener la de este teléfono [Recomendada]     │  │
│  │   La que acabas de escribir para autorizar        │  │
│  ├───────────────────────────────────────────────────┤  │
│  │ ○ Usar la del respaldo                            │  │
│  │   La que tenía configurada el teléfono de origen  │  │
│  ├───────────────────────────────────────────────────┤  │
│  │ ○ Definir una nueva contraseña en este momento     │  │
│  │   [ • • • •                                     ] │  │
│  └───────────────────────────────────────────────────┘  │
│                                                         │
│  ┌───────────────────────────────────────────────────┐  │
│  │   [CANCELAR]        [SÍ, REEMPLAZAR BASE DE DATOS]│  │
│  └───────────────────────────────────────────────────┘  │
└─────────────────────────────────────────────────────────┘
```

---

### 5.3 Overlay Bloqueante con Animación Continua de Carga (`OverlayCargandoDbAnimado`)
- **Evitar loaders genéricos circulares grises de sistema.**
- **Composición Visual Personalizada:**
  - Fondo a pantalla completa oscurecido con `Color(0xCC1F1B17)` (80% opacidad del `TextPrimary`) con desenfoque suave.
  - **Cilindro de Base de Datos Estilizado:** Ilustración vectorial en Compose que representa un silo o disco cilíndrico en tonos dorados maíz (`MaizPrimary` y `MaizPrimaryDark`).
  - **Ondas de Pulso Concéntricas:** Canvas con círculos en degradado animado mediante `rememberInfiniteTransition` que emiten pulsos hacia afuera con escala (0.8f ➔ 1.4f) y alfa (0.6f ➔ 0.0f).
  - **Flujo de Partículas de Datos:** Puntos luminosos en `MaizGradientStart` que viajan verticalmente hacia el centro del cilindro.
  - **Barra de Progreso Personalizada:** Barra delgada de 6dp con bordes redondeados completos, fondo en `SurfaceContainerHigh` y relleno con gradiente `Brush.horizontalGradient(listOf(MaizGradientStart, MaizGradientEnd))`.
  - **Textos Dinámicos de Estado:** Texto centrado en 16sp `FontWeight.Bold` en blanco cálido (`#FFFFFF`), alternado con transiciones suaves:
    1. *"Preparando respaldo temporal de seguridad..."*
    2. *"Escribiendo tablas de Tortillería Derek..."*
    3. *"Verificando integridad física y matemática (SHA-256)..."*
    4. *"Configurando credenciales del Administrador Principal..."*
    5. *"Consolidando históricos de ventas y turnos..."*
  - **Subtexto de Advertencia Suave:** *"Por favor no salgas de la aplicación ni apagues el teléfono."* con icono de candado sutil.

---

### 5.4 Modal de Éxito y Reinicio Obligatorio (`DialogReinicioExitosoDb`)
- **Cabecera Triunfal:** Icono de 56dp en `CircleShape` con fondo `VerdeAgaveLight` y checkmark en `VerdeAgave`.
- **Insignia de Integridad:** Badge píldora con fondo `SurfaceContainerLow` y texto monospace con el hash SHA-256 verificado (*"SHA: 8A4F-2C9B • OK"*).
- **Tarjeta de Credenciales:** Mensaje cálido confirmando la política de contraseña aplicada: *"Tu usuario admin1 está listo para iniciar sesión con la contraseña seleccionada."*
- **Botón de Acción Primario (CTA):** Botón ancho con gradiente `MaizGradient`, altura de 50dp, esquinas de `14.dp` y texto en 16sp `FontWeight.ExtraBold` blanco: *"Reiniciar Aplicación Ahora"*, con icono de recarga circular.

---

### 5.5 Modal de Micro-Lección Animada de Versiones (`DialogMicroLeccionVersion`)
- **Propósito:** Educación visual interactiva sin tecnicismos ni códigos de error.
- **Cabecera Didáctica:** Icono circular de 52dp con dos dispositivos enlazados por un rayo cálido en `MaizPrimary`.
- **Comparador Visual de Dispositivos (Compose Canvas):**
  - Dos tarjetas de teléfono estilizadas una frente a la otra con esquinas redondeadas:
    - **Teléfono A (Origen):** Badge en `TerracotaLight` con texto *"Versión anterior"*.
    - **Teléfono B (Este teléfono):** Badge en `VerdeAgaveLight` con texto *"Versión más reciente"*.
    - Entre ambos, una línea de puntos con una micro-animación de alerta que parpadea suavemente.
- **Lista de 3 Pasos con Cápsulas Numeradas:**
  - Cada paso en una tarjeta con fondo `SurfaceWarm` y borde sutil:
    - **Paso 1:** Monograma circular naranja con número `1` en blanco: *"Actualiza la App en el teléfono origen instalando el APK más reciente."*
    - **Paso 2:** Monograma `2`: *"Abre la app actualizada y genera un nuevo respaldo (los datos se adaptarán automáticamente)."*
    - **Paso 3:** Monograma `3`: *"Envía e importa el nuevo archivo en este dispositivo."*
- **Botón de Cierre:** Botón neutral con altura táctil ergonómica: *"Entendido, Volver"*.


---

## 6. Criterios de Seguridad (🛡️ `security-expert`)

### US-SEC-BKP-01: Autenticación Obligatoria de Administrador Principal
- Antes de invocar cualquier método de lectura o escritura de archivos `.db`, el sistema exige ingresar las credenciales de un usuario con `rol == 'ADMIN'`.
- La verificación se realiza consultando la tabla local `usuarios` y ejecutando `CryptoManager.hashPassword(pin, salt)`.
- Si se ingresan credenciales de un empleado (`rol == 'EMPLEADO'`) o contraseña incorrecta, la acción se aborta registrando intento no autorizado.

### US-SEC-BKP-02: Integridad Criptográfica del Archivo Exportado
- Al exportar, se calcula el hash **SHA-256** del archivo copiado.
- El diálogo de éxito muestra los primeros 8 caracteres en un badge destacado (ej. `SHA: 8A4F-2C9B`) para que el usuario pueda cotejar visualmente la integridad si lo desea.

### US-SEC-BKP-03: Contraseñas y Datos Sensibles
- Las contraseñas en el archivo `.db` residen exclusivamente en formato cifrado PBKDF2 con salt individual.
- **Queda estrictamente prohibido** exponer contraseñas en texto plano durante la exportación o importación.

### US-SEC-BKP-04: Rollback Automático y Sanidad Post-Importación
- Antes de modificar los archivos de base de datos, se genera `tortilleria_db.prev`.
- Tras la sustitución, se ejecuta:
  ```sql
  PRAGMA integrity_check;
  SELECT COUNT(*) FROM usuarios WHERE rol = 'ADMIN';
  ```
- Si `integrity_check` ≠ `'ok'` o la cantidad de administradores es igual a 0, el sistema revierte inmediatamente los archivos copiando `.prev` de vuelta a `tortilleria_db` y relanzando la base de datos previa sin corrupción.

### US-SEC-BKP-05: Prevención de Bloqueo Administrativo (Lockout Prevention)
- **Riesgo:** Si el Teléfono A tenía una contraseña distinta a la del Teléfono B, el usuario podría quedar bloqueado sin poder entrar al Teléfono B tras reiniciar.
- **Mitigación obligatoria:**
  1. Si el usuario elige *"Mantener la contraseña actual de este teléfono"*, el sistema utiliza el `hash` y `salt` capturados durante la autorización inicial para reescribir la fila de `admin1` en la base de datos importada.
  2. Si elige *"Establecer una nueva contraseña ahora"*, se genera un salt criptográfico nuevo (`CryptoManager.generateSalt()`) y se aplica `CryptoManager.hashPassword(nuevoPin, salt)` sobre el usuario `admin1`.
  3. Si elige *"Usar la del respaldo"*, se conservan las credenciales originales que venían del Teléfono A.
- Si por corrupción o defecto la base importada no contuviera ningún registro con `rol == 'ADMIN'`, se inserta defensivamente el usuario `admin1` con las credenciales seleccionadas antes de concluir la operación.

### US-SEC-BKP-06: Blindaje Energético contra Corrupción por Apagado Prematuro
- **Riesgo:** Si el dispositivo tiene batería baja (< 25%) y se apaga en pleno proceso de escritura flash I/O o cerrado de Room, el archivo `.db` puede quedar irrecuperablemente corrupto o truncado.
- **Mitigación obligatoria:**
  - El sistema consulta a través de `BatteryManager` el nivel de capacidad (`BATTERY_PROPERTY_CAPACITY`) y el estado de carga (`isCharging`).
  - Si el nivel es inferior a **25%** y el cargador **no** está conectado, el sistema **aborta y bloquea** la operación antes de tocar cualquier archivo, solicitando conectar el dispositivo a la corriente eléctrica.

---

## 7. Criterios de Aceptación QA (🏆 `quality-pm-expert`)

### Scenario 1: Exportación Exitosa con Selección de Destino (SAF)
- **Given** el usuario se encuentra en la pantalla de Login o en Configuración sin sesión abierta, no existen turnos en estado `ABIERTO`, y el teléfono tiene ≥ 25% de batería (o cargador conectado),
- **When** pulsa "📤 Exportar Respaldo", ingresa credenciales válidas del Administrador Principal (`admin1`),
- **Then** el sistema valida el rol de administrador, ejecuta el checkpoint WAL y **abre el selector nativo de almacenamiento (Storage Access Framework)** con el nombre sugerido `tortilleria_backup_YYYYMMDD_HHmmss.db`,
- **And** al confirmar la ubicación en su carpeta o tarjeta SD preferida, el archivo se guarda con tamaño > 0 bytes y se muestra un diálogo de éxito con opción de compartirlo de inmediato.

### Scenario 2: Importación Exitosa con Selector de Contraseña de Admin
- **Given** el archivo `.db` exportado del Teléfono A (con clave `claveOrigen`) se encuentra en el almacenamiento del Teléfono B (con clave `claveDestino`), no hay turnos abiertos en el Teléfono B, y la batería es ≥ 25% (o con cargador conectado),
- **When** el administrador pulsa "📥 Importar Respaldo", valida sus credenciales con `claveDestino` y selecciona el archivo,
- **Then** el sistema despliega el diálogo de **Advertencia Destructiva** con el **Selector de Contraseña de Admin**:
  - **Caso 2A (Mantener actual):** Selecciona *"Mantener la contraseña actual de este teléfono"*, pulsa confirmar -> Se ejecuta la animación de carga -> Al reiniciar, el administrador inicia sesión exitosamente con `claveDestino`.
  - **Caso 2B (Usar la del respaldo):** Selecciona *"Usar la contraseña del Respaldo"*, pulsa confirmar -> Al reiniciar, el administrador inicia sesión con `claveOrigen`.
  - **Caso 2C (Nueva clave):** Selecciona *"Establecer nueva contraseña"*, ingresa `nuevaClave99` -> Al reiniciar, inicia sesión con `nuevaClave99`.
- **And** en todos los casos, los historiales de ventas, turnos cerrados, producción y catálogos del Teléfono A quedan restaurados al 100%.

### Scenario 3: Rechazo por Versión Incompatible y Despliegue de Micro-Lección
- **Given** el usuario selecciona un archivo `.db` con `user_version` distinta a la actual (ej. v5 vs v7),
- **When** el sistema inspecciona la cabecera SQLite del archivo,
- **Then** la base de datos local **NO se modifica en lo absoluto**, y el sistema despliega el diálogo modal con la **micro-lección animada interactiva**, explicando de forma didáctica la diferencia de versiones y guiando al usuario en los 3 pasos para solucionarlo.

### Scenario 4: Bloqueo Estricto por Turno Activo
- **Given** existe un turno en estado `ABIERTO` en la aplicación local,
- **When** el administrador intenta exportar o importar un respaldo,
- **Then** el sistema bloquea inmediatamente la operación emitiendo la alerta: *"Operación Bloqueada: Existe un turno activo en curso. Por favor realice el corte de caja antes de transferir o respaldar datos."*, impidiendo la ejecución de SAF.

### Scenario 5: Intento de Acceso por Usuario No Administrador
- **Given** la pantalla de solicitud de credenciales para respaldo,
- **When** el usuario ingresa las credenciales de un operador con rol `EMPLEADO` o un PIN incorrecto,
- **Then** el sistema rechaza la operación con el mensaje *"Acceso no autorizado: Solo el Administrador Principal puede exportar o importar bases de datos."*, sin abrir selectores de archivos.

### Scenario 6: Archivo Corrupto Rechazado con Rollback Automático
- **Given** el usuario selecciona un archivo truncado o de otra aplicación que pasó la extensión `.db`,
- **When** el sistema intenta la verificación post-importación y `PRAGMA integrity_check` detecta errores,
- **Then** el sistema restaura de inmediato la base de datos previa desde `tortilleria_db.prev`, eliminando los archivos corruptos y mostrando un mensaje de error seguro.

### Scenario 7: Detección de Turno Abierto en Archivo Importado (Caso Límite)
- **Given** un archivo de respaldo proveniente de un teléfono que se apagó con un turno abierto,
- **When** se valida el archivo en el proceso de importación,
- **Then** el sistema informa que el respaldo contiene un turno pendiente de cierre y ofrece ejecutar un "Cierre de Emergencia de Migración" para preservar la integridad del corte antes de abrir la sesión.

### Scenario 8: Bloqueo por Batería Insuficiente (< 25% sin Cargador)
- **Given** el teléfono tiene 18% de batería y no está conectado al cargador,
- **When** el usuario pulsa "Exportar Respaldo" o "Importar Respaldo",
- **Then** el sistema bloquea la operación antes de solicitar credenciales o archivos, mostrando el diálogo ilustrado `DialogBateriaInsuficiente` con el mensaje: *"Nivel de batería insuficiente (18%). Conecta el teléfono al cargador o cárgalo a más del 25% para evitar que se apague durante el proceso."*

---

## 8. Casos Límite y Mitigaciones (Matriz de Robustez)

| # | Caso Límite | Estrategia de Mitigación |
|---|---|---|
| CL-01 | WAL con transacciones pendientes al exportar | Ejecución obligatoria de `PRAGMA wal_checkpoint(FULL)` previo a la copia |
| CL-02 | Versión de Room incompatible (menor o mayor) | Detección previa vía `PRAGMA user_version` + **Micro-lección animada explicativa** |
| CL-03 | Turno `ABIERTO` en el dispositivo local | Bloqueo estricto con verificación de `TurnoDao.getTurnoActivoSync() == null` |
| CL-04 | Usuario sin loguear intenta exportar/importar | Solicitud modal obligatoria de usuario + contraseña de Administrador Principal |
| CL-05 | Empleado operativo intenta hacer backup | Validación de `usuario.rol == "ADMIN"`; rechazo inmediato si no es admin |
| CL-06 | Usuario cancela el selector SAF de guardado | Cancelación limpia de la corrutina sin generar archivos residuales |
| CL-07 | Caída de energía o crash en mitad del reemplazo | Preservación de `tortilleria_db.prev` con restauración automática |
| CL-08 | Discrepancia de contraseñas de admin entre teléfonos | **Selector de Contraseña en diálogo previo (Opción 1): mantener actual, respaldo o nueva** |
| CL-09 | BD importada sin administradores (archivo ajeno) | Inserción defensiva del usuario `admin1` con la clave seleccionada |
| CL-10 | Turno huérfano abierto en archivo entrante | Protocolo de cierre forzado de emergencia de migración con nota auditora |
| CL-11 | Archivos `.db-shm` o `.db-wal` huérfanos | Borrado explícito de ambos archivos tras sustituir el `.db` principal |
| CL-12 | Falta de espacio de almacenamiento | Verificación previa de espacio disponible (`StatFs`) antes de escribir |
| CL-13 | Permiso SAF revocado | Manejo de excepciones `SecurityException` con notificación al usuario |
| CL-14 | Batería baja (< 25%) y desconectado del cargador | Verificación vía `BatteryManager`; bloqueo previo con diálogo orientativo |

---

## 9. Componentes Técnicos a Desarrollar

### 9.1 Capa Domain & UseCases

- `BackupUseCase.kt` (`domain/usecase/`):
  - `verificarBateriaSegura(): Result<Unit>`
  - `validarCredencialesAdmin(username, pin): Result<UsuarioEntity>`
  - `verificarTurnosCerrados(): Result<Unit>`
  - `exportarRespaldoHaciaUri(uri: Uri): Result<BackupExportInfo>`
  - `inspeccionarArchivoDb(uri: Uri): Result<DbInspectionResult>`
  - `importarRespaldoDesdeUri(uri: Uri, politicaPassword: PoliticaPasswordAdmin): Result<Unit>`
- `BackupModels.kt` (`domain/model/`):
  - `DbInspectionResult(version: Int, esCompatible: Boolean, tieneTurnoAbierto: Boolean)`
  - `BackupExportInfo(uri: Uri, sha256: String, tamanioBytes: Long)`
  - `PoliticaPasswordAdmin` (Sealed class: `MantenerActual(hash, salt)`, `UsarDelRespaldo`, `EstablecerNueva(pin)`)
  - `BackupError` (Sealed hierarchy con `BateriaBaja(porcentaje)`, `VersionIncompatible`, `TurnoActivoLocal`, `CredencialesInvalidas`, `NoEsAdmin`, etc.)

### 9.2 Capa Presentation & MVI

- `BackupViewModel.kt` (`presentation/backup/`):
  - Estado: `BackupUiState` (diálogo de batería baja, diálogo de credenciales, diálogo de micro-lección animada, diálogo de advertencia destructiva con selector de clave, overlay de animación de carga, diálogo de reinicio exitoso).
  - Efectos: `BackupUiEffect` (`AbrirSelectorGuardar(nombre)`, `AbrirSelectorLeer`, `CompartirArchivo(uri)`, `ReiniciarApp`).

### 9.3 Capa UI Compose

- `DialogBateriaInsuficiente.kt`: Modal ilustrado con icono de batería baja en `TerracotaSecondary`, porcentaje actual y sugerencia de conectar el cargador.
- `DialogAutenticacionAdminBackup.kt`: Diálogo de seguridad para captura de credenciales de admin con validación en tiempo real.
- `DialogAdvertenciaReemplazoDb.kt`: Modal con semáforo de riesgo, lista de datos a sobreescribir y selector de 3 opciones de contraseña de admin.
- `OverlayCargandoDbAnimado.kt`: Modal bloqueante a pantalla completa con micro-animaciones de pulsos de base de datos y textos de progreso.
- `DialogReinicioExitosoDb.kt`: Modal verde de éxito con confirmación de credenciales y botón CTA "Reiniciar Aplicación Ahora".
- `DialogMicroLeccionVersion.kt`: Modal ilustrado con micro-animaciones Compose y guía en 3 pasos para solucionar incompatibilidad de versión.
- Integración en `LoginScreen.kt` (menú de utilidades/engranaje) y `ConfiguracionScreen.kt`.

---

## 10. Estimación y Fases de Trabajo

| Fase | Tarea | Responsable | Tiempo |
|---|---|---|---|
| **Fase 1** | Modelos de dominio (`PoliticaPasswordAdmin`), UseCase con validación de batería y turnos | 🏗️ `mobile-developer` + 🛡️ `security-expert` | 1.0 h |
| **Fase 2** | Flujos SAF (CreateDocument / OpenDocument) y manejo WAL | 🏗️ `mobile-developer` | 0.75 h |
| **Fase 3** | UI: Diálogo Batería, Diálogo Admin, Advertencia con Selector, Overlay Animado de Carga, Reinicio y Micro-Lección | 🎨 `design-ui-expert` | 1.25 h |
| **Fase 4** | Integración en Login y Configuración | 🏗️ `mobile-developer` + 🎨 `design-ui-expert` | 0.5 h |
| **Fase 5** | Pruebas Unitarias de Casos Límite y Cobertura QA (batería, contraseñas, SAF, WAL) | 🏆 `quality-pm-expert` | 0.75 h |
| **Total** | | | **~4.25 horas** |



