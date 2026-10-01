# Especificación Técnica: Rediseño Fiel de Pantallas (Producción, Métricas, Ajustes) y Conexión Sistémica

**Estado:** Validada por 🏆 `quality-pm-expert` y 🛡️ `security-expert`  
**Fecha:** 17 de Septiembre, 2026  
**Iteración:** Rediseño UI + Integración Sistémica Multi-Pantalla  

---

## 1. Contexto y Objetivos del Negocio
La aplicación móvil **Tortillería Derek** requiere que sus módulos de **Producción**, **Métricas** y **Ajustes (Configuración)** reflejen con un 100% de fidelidad visual los diseños aprobados, interconectándose de forma reactiva con la pantalla principal de **Ventas** y manteniendo una trazabilidad exhaustiva de auditoría de usuarios en cada operación.

---

## 2. Requerimientos Funcionales por Pantalla

### 2.1. Pantalla de Producción
- **Hero Card Naranja:**
  - Chips de cabecera: Fecha del día (`Hoy, Lun 7 Sept`) y estado de turno (`En Producción Activa`).
  - Métrica destacada: `TORTILLA DISPONIBLE EN TIENDA` en kilogramos (`14.5 kg`).
  - Barra de progreso de venta: Indicador visual y porcentaje (`Progreso de Venta 90.5% Vendido`).
  - Tarjetas inferiores gemelas: `PRODUCIDO NETO` (`154.0 kg`) y `TOTAL VENDIDO` (`139.5 kg`).
- **Sección Registro de Nueva Tanda:**
  - Título con badge de rendimiento (`Rendimiento: 38.5 kg / bulto`).
  - Tarjeta de insumos: Bultos de Harina (20 kg c/u) con selector Stepper interactivo (`[-] X bultos [+]`).
  - Indicador de equivalencia: `Equivale a X kg masa cruda -> Y kg tortilla`.
- **Botones de Acción:**
  - Botón principal naranja con flecha: `Registrar Tanda de Producción ->`.
  - Botón secundario outlined: `Ver Historial produccion`.
- **Navegación Inferior:** `TortilleriaNavBar` con tab activa "Producción".

### 2.2. Pantalla de Métricas ("Productividad y Ventas")
- **Cabecera y Selector de Período:**
  - Título en dos renglones: `Productividad y Ventas` con selector de fecha (`1 – 7 Sept 2026`).
  - Chips de períodos: `Semanal` (activo), `Quincenal`, `Mensual`, `Personalizado`.
  - Conmutador de métrica: `Producción Total (kg)` (activo/naranja) vs `Ventas Totales ($)`.
- **Card Comportamiento del Periodo:**
  - Valor total (`1,245.0 kg`) con badge de incremento verde (`+12.8% vs semana ant.`).
  - Menú de opciones (`⋮`).
  - Gráfica de curva bezier con gradiente inferior, marcadores diarios (`Lun` a `Dom`) y tooltip interactivo en día pico (`210 kg • Jueves`).
- **Card Distribución de Canales:**
  - Badge de total (`Total: $19,250 MXN`).
  - Barra segmentada proporcional: Mostrador (65%) vs Reparto Mayoreo (35%).
  - Desglose monetario y en kilos por canal:
    - Mostrador: `$12,400 MXN` / `810 kg entregados`.
    - Reparto Mayoreo: `$6,850 MXN` / `435 kg despachados`.
- **Cards Inferiores de Desempeño:**
  - `Promedio Diario`: `177.8 kg/día` con badge `Ritmo óptimo`.
  - `Merma Promedio`: `3.4% (Óptimo)` con subtítulo `Meta: menor a 5.0%`.
- **Navegación Inferior:** `TortilleriaNavBar` con tab activa "Métricas".

### 2.3. Pantalla de Ajustes ("Configuración del Sistema")
- **Cabecera:** `Configuración del Sistema` con subtítulo `Parámetros operativos y catálogo activo`.
- **Card 1: Precios y Pesos de Productos:**
  - Badge `3 activos`.
  - Ítems:
    - `[1k]` Kilo Completo / 1,000 g peso estándar / `$24.00 MXN` / Botón editar (lápiz).
    - `[Pq]` Paquete Mostrador / 1.33 kg aprox. (especial) / `$32.00 MXN` / Botón editar (lápiz).
    - `[½]` Medio Paquete / 650 g peso aproximado / `$16.00 MXN` / Botón editar (lápiz).
- **Card 2: Estándares de Producción:**
  - Sub-tarjeta Rendimiento Neto: `38.5 kg` / Tortilla cocida / bulto.
  - Sub-tarjeta Merma Tolerada: `1.5 kg` / Máximo permitido / bulto / badge `3.8%`.
- **Card 3: Gestión de Repartidores:**
  - Encabezado con botón `+ Agregar`.
  - Repartidor `#01 Carlos Ruiz` (Moto Italika 125 • Ruta San Juan) / Acciones editar y borrar.
### 2.4. Integración y Enlace con Historial de Ventas
- **Desde Producción:** El botón secundario `Ver Historial produccion` navega a la pantalla de `HistorialScreen`, permitiendo auditar los tickets de venta y liquidaciones de ruta que alimentan el cálculo de `TOTAL VENDIDO` (139.5 kg) y `TORTILLA DISPONIBLE EN TIENDA`.
- **Desde Métricas:** Los registros detallados del Historial constituyen la fuente transaccional atómica para la consolidación de la gráfica de tendencia semanal y el desglose de la tarjeta `Distribución de Canales` (Mostrador vs Reparto Mayoreo).
- **Desde Ajustes & Repartidores:** Los repartidores y rutas definidos en Ajustes (ej. `#01 Carlos Ruiz • Moto Italika 125 • Ruta San Juan`) quedan vinculados directamente a cada liquidación registrada en Room (`VentaEntity.nombreRepartidor`, `motoAsignada`, `nombreRuta`), reflejándose de forma enriquecida en las tarjetas de tickets `R-XXXX` del Historial.

---

## 3. Criterios de Seguridad y Auditoría (🛡️ `security-expert`)

### US-SEC-01: Trazabilidad del Operador en Producción
**Given** un usuario autenticado con sesión activa (`usuarioId: "u_cajero_1"`, `nombre: "Derek González"`),  
**When** registra una nueva tanda de 4 bultos de harina en la pantalla de Producción,  
**Then** el registro de `TandaProduccionEntity` almacena `usuarioId` y `usuarioNombre` junto con la marca de tiempo exacta para garantizar auditoría inmutable.

### US-SEC-02: Integridad de Precios y Ventas Históricas
**Given** un ticket de venta de mostrador registrado previamente a un precio de `$24.00/kg`,  
**When** el administrador actualiza el precio del Kilo Completo a `$26.00/kg` en la pantalla de Ajustes,  
**Then** las ventas históricas de turnos cerrados o tickets pasados conservan su precio original pactado y solo las ventas subsecuentes calculan su total con `$26.00/kg`.

### US-SEC-03: Trazabilidad en Repartidores
**Given** la asignación de carga inicial a una moto y su posterior liquidación,  
**Then** la entidad registra `usuarioAsignoId` / `usuarioAsignoNombre` en la salida y `usuarioLiquidoId` / `usuarioLiquidoNombre` en el arqueo y cierre de la ruta, persistiendo además en Room `nombreRepartidor`, `motoAsignada` y `nombreRuta` para auditoría total en el Historial de ventas.

---

## 4. Criterios de Aceptación QA (🏆 `quality-pm-expert`)

### Scenario 1: Actualización Reactiva de Precios de Ajustes a Mostrador
- **Given** la pantalla de Mostrador con el Kilo Completo a `$24.00 MXN`,
- **When** el usuario navega a Ajustes vía `TortilleriaNavBar`, edita el precio a `$25.00 MXN` y regresa a Mostrador,
- **Then** el catálogo de productos en Mostrador refleja `$25.00 MXN` y al seleccionar 2 kg el total calculado es `$50.00 MXN`.

### Scenario 2: Cálculo en Tiempo Real de Stock en Tienda
- **Given** una producción registrada de 154.0 kg y ventas acumuladas de 139.5 kg (disponible: 14.5 kg),
- **When** el cajero confirma una venta de 2.0 kg en Mostrador,
- **Then** al conmutar a la pantalla de Producción el disponible muestra automáticamente `12.5 kg` y el total vendido se incrementa a `141.5 kg`.

### Scenario 3: Stepper de Insumos en Producción
- **Given** la pantalla de Producción con el valor base de 4 bultos,
- **When** el usuario presiona el botón `[+]`,
- **Then** el contador incrementa a 5 bultos, la masa cruda calculada cambia a 200 kg y la tortilla estimada a 192.5 kg (5 * 38.5 kg).

### Scenario 4: Navegación a Historial y Auditoría de Reparto
- **Given** la pantalla de Producción,
- **When** el usuario presiona `Ver Historial produccion`,
- **Then** la aplicación navega a `HistorialScreen` donde las transacciones con folio `R-XXXX` presentan la insignia del repartidor, moto y nombre de la ruta persistidos en Room.

