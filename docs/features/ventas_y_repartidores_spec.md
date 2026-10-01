# Especificación: Pantalla de Ventas (Mostrador y Repartidores) e Historial

## 1. Objetivo
Gestionar el registro ágil de ventas en mostrador (por kilo y paquete), el ciclo operativo de rutas de repartidores (salida, liquidación y emisión de tickets `R-XXXX`), el cierre seguro del turno diario y la consulta del historial de recaudación con diseño fiel a las maquetas aprobadas.

---

## 2. Historias de Usuario (US)
*   **US1 (Fecha y Turno):** Como cajero, necesito ver la fecha actual del día en que se realiza el turno (actualizada cada día al abrir el turno) en la cabecera principal.
*   **US2 (Cierre de Turno e Inmutabilidad):** Como administrador/cajero, necesito cerrar el turno mediante un diálogo de confirmación, consolidando las ventas del día en Room. Una vez cerrado el turno, no se podrá agregar, modificar ni eliminar ninguna venta, y el sistema cerrará la sesión enviando a la pantalla de Login.
*   **US3 (Filtro de Ventas en Header):** Como usuario, necesito ver el total acumulado en el header naranja y disponer de un selector desplegable para alternar entre "Todos", "Mostrador" y "Repartidores".
*   **US4 (Conmutador Mostrador / Repartidores con Cabecera Fija tipo Pager):** Como usuario, al pulsar los chips de "Mostrador" o "Repartidores" (o al deslizar), la tarjeta superior de ventas (`HeroSalesCard`) y los botones de chip (`MostradorRepartidoresTabBar`) deben permanecer completamente fijos e inmóviles en la parte superior; únicamente debe realizarse la transición animada del contenido inferior correspondiente (artículos y registro de mostrador vs. control de repartidores), funcionando como un pager fluido sin redibujar ni mover la cabecera.
*   **US5 (Control de Tickets):** Como negocio, el sistema debe emitir tickets correlativos iniciando con prefijo `M-` para mostrador (ej. `M-0143`) y prefijo `R-` para repartidores (ej. `R-0038`).
*   **US6 (Venta Mostrador):** Como cajero, podré agregar o quitar cantidades de Kilogramo ($24.00), 1/2 kilogramo ($20.00) y Paquete ($12.00). El botón "Ingresar venta" calculará el total en tiempo real y abrirá el diálogo de confirmación rápida.
*   **US7 (Control de Repartidores):** Como administrador, necesito ver las rutas activas (que salieron y no han liquidado), ver el estatus de cada moto (En Ruta, Pendiente de Salida, Liquidado), dar salida registrando carga en kg y liquidar al regreso calculando merma/devolución y emitiendo el ticket `R-`.
*   **US8 (Historial de Ventas):** Como usuario, dispondré de un botón al final para ver el Historial de Ventas con el desglose gráfico de Efectivo y Tarjeta y la lista de transacciones.
*   **US9 (Trazabilidad y Auditoría de Usuario en Ventas):** Como negocio/auditor, al momento de guardar cualquier venta en la base de datos (tanto venta de mostrador como venta por liquidación de repartidor), el sistema debe persistir obligatoriamente el identificador (`usuarioId`) y nombre del usuario autenticado (`usuarioNombre`) que operó y autorizó la transacción, impidiendo valores hardcodeados genéricos no asociados al operador.
*   **US10 (Confirmación de Venta de Contado y Jerarquía Visual):** Como cajero, al abrir el diálogo de confirmación de venta, no debe mostrarse selector de método de pago dado que por modelo de negocio las ventas de mostrador se realizan estrictamente de contado/efectivo. La interfaz del diálogo debe presentar una clara jerarquía visual y cromática:
    - **Nivel Primario:** Tarjeta central destacada con el Monto Total a cobrar en tamaño grande (`MaizPrimary`), acompañada del CTA principal "Cobrar e Imprimir" en botón relleno de alto contraste.
*   **US11 (Trazabilidad y Persistencia Obligatoria de Datos de Reparto en Room):** Como negocio/auditor, al momento de liquidar y registrar una venta de tipo `REPARTIDOR` (ticket `R-XXXX`), la base de datos Room (`VentaEntity`) debe persistir obligatoriamente en columnas independientes:
    - `nombreRepartidor`: Nombre completo del chofer (ej. "Carlos Ruiz", "Miguel Gómez").
    - `motoAsignada`: Identificación/número de moto (ej. "Moto 01 - Italika 125").
    - `nombreRuta`: Nombre asignado de la ruta (ej. "Ruta San Juan", "Ruta Taquerías").
    Asimismo, la entidad `RutaRepartidorEntity` debe incluir `nombreRuta` como campo persistente de primer nivel.
*   **US12 (Visualización Enriquecida en Historial de Ventas):** Como administrador/cajero, en la pantalla de `HistorialScreen`, al filtrar por "Reparto" o ver transacciones globales, las tarjetas de tickets con folio `R-XXXX` deben mostrar explícitamente una cápsula identificativa con el nombre del repartidor, el número de moto y el nombre de la ruta, facilitando la auditoría inmediata sin abrir modales secundarios.
*   **US13 (Repartidores Limpios en `PENDIENTE_SALIDA` al Abrir Turno):** Como negocio, al abrir un nuevo turno en la aplicación, todas las motos registradas deben inicializarse en Room en estado `PENDIENTE_SALIDA` con `cargaInicialKg = 0.0`, `pendienteCobro = 0.0` y `horaSalida = null`, garantizando que ningún turno comience con producto cargado previamente ni motos en ruta.
*   **US14 (Diálogos Jerárquicos de Despacho y Liquidación de Repartidores):** Como cajero, requiero que los diálogos modales para despachar y liquidar a un repartidor sigan una rigurosa jerarquía visual:
    - **Salida:** Identidad clara (moto, repartidor, badge de ruta), chips de pesaje rápido (`50 kg`, `70 kg`, `90 kg`) más captura manual, y tarjeta de cálculo en tiempo real del monto esperado a liquidar (`$22.00/kg`).
    - **Liquidación:** Resumen visual de carga inicial, campo de devolución/merma con cálculo reactivo de kilos netos vendidos y saldo a cobrar, confirmación de efectivo recibido y botón destacado en `VerdeAgave` para emitir el ticket `R-XXXX`.
*   **US15 (Bloqueo Estricto de Cierre de Turno por Rutas Activas):** Como dueño/administrador del negocio, el sistema prohíbe de forma estricta e inquebrantable el cierre de turno si existe al menos una moto en estado `EN_RUTA` (rutas activas > 0). El diálogo de cierre debe alertar con una tarjeta de advertencia indicando las motos en la calle y deshabilitar por completo el botón de confirmar cierre hasta que todas las rutas hayan sido liquidadas.
*   **US16 (Inicio Limpio en Cero de Contadores Mostrador y Choferes sin Asignación):** Como cajero/administrador, al iniciar sesión o ingresar a la pantalla de venta, los contadores de los productos en Mostrador (Kilogramo, 1/2 kilogramo, Paquete) deben iniciar estrictamente en cero (`quantity = 0`), eliminando cualquier valor preestablecido o mock. De igual manera, ningún repartidor debe aparecer con montos o kilos pre-asignados; todas las rutas deben figurar en estado inicial limpio (`PENDIENTE_SALIDA`, `0.0 kg` y `$0.00 MXN`).
*   **US17 (Validación Estricta de Devolución en Liquidación de Repartidores - Caso Límite):** Como cajero o auditor, en el diálogo de liquidación de repartidor, el sistema prohíbe capturar una devolución o merma que exceda la carga asignada originalmente a la moto (`devolucionKg > cargaInicialKg` o `paquetesDevueltos > paquetesCargados`). Si esto ocurre por error tipográfico del cajero, el campo se destaca en rojo, se muestra una alerta visual descriptiva (*"La devolución no puede exceder los X kg cargados"*) y se bloquea el botón de confirmar liquidación.
*   **US18 (Cancelación y Reversión de Salida a Ruta - Caso Límite):** Como cajero, si se asignó y despachó por error una moto a ruta (`status == 'EN_RUTA'`), dispondré de una opción de "Cancelar Salida". Al confirmar, el repartidor vuelve de inmediato a `PENDIENTE_SALIDA`, su carga inicial se reinicia en `0.0 kg` y la tortilla se restituye íntegramente al disponible de la tienda (`disponibleEnTiendaKg`), evitando pérdidas de stock por asignaciones erróneas.

---

## 3. Criterios de Seguridad (by 🛡️ security-expert)
*   **Inmutabilidad de Ventas:** Si el turno actual está en estado `CERRADO`, la base de datos y la capa de ViewModel rechazan y bloquean cualquier operación de inserción, actualización o eliminación de registros de ventas (`VentaEntity`).
*   **Bloqueo de Cierre con Mercancía en la Calle:** Se impide cerrar turno si `rutasActivasCount > 0`, asegurando que no existan mermas sin justificar ni dinero pendiente de ingresar a caja.
*   **Trazabilidad de Cajero y Reparto:** Cada venta registrada en Room vincula el `usuarioId` y el `usuarioNombre` exactos de quien liquida en caja, junto con `nombreRepartidor`, `motoAsignada` y `nombreRuta` inmutables.
*   **Cierre Seguro de Sesión:** Al confirmar "Cerrar Turno", se limpia la pila de navegación (`popUpTo(0)`) y se redirige a la pantalla de autenticación.

---

## 4. Diseño Técnico y Arquitectura (by 🏗️ mobile-developer)

### 4.1. Entidades Room (Data)
*   `VentaEntity`: `id`, `turnoId`, `folioTicket`, `tipo` (MOSTRADOR/REPARTIDOR), `fecha`, `hora`, `detalleProductos`, `total`, `metodoPago`, `usuarioId`, `usuarioNombre`, `estado`, `nombreRepartidor: String?`, `motoAsignada: String?`, `nombreRuta: String?`.
*   `RutaRepartidorEntity`: `id`, `turnoId`, `moto`, `repartidorNombre`, `nombreRuta: String`, `status` (PENDIENTE_SALIDA/EN_RUTA/LIQUIDADO), `cargaInicialKg`, `pendienteCobro`, `devolucionKg`, `entregadoKg`, `cobrado`, `folioTicket`, `horaSalida`, `horaLiquidacion`.
*   `TurnoEntity`: `id`, `fechaApertura`, `estado`, `usuarioId`, `fechaCierre`, `totalVentas`.

### 4.2. DAOs
*   `VentaDao`: `insertVenta`, `deleteVenta`, `getVentasPorTurno`, `getUltimoFolioMostrador`, `getUltimoFolioRepartidor`.
*   `RutaRepartidorDao`: `insertRutas`, `updateRuta`, `getRutasPorTurno`, `getRutasActivasCount`.
*   `TurnoDao`: `getTurnoActivo`, `cerrarTurno`.

### 4.3. Contrato MVI (Presentation)
*   **UiState:** `VentaUiState` con totales reactivos, productos, steppers, folios, rutas de repartidores y banderas de diálogos.
*   **UiEffect:** `NavigateToLogin`, `NavigateToHistorial`, `ShowToast(mensaje)`.

---

## 5. Diseño de Interfaz (by 🎨 design-ui-expert)
*   **HeroSalesCard:** Gradiente naranja cálido (`#FF6B00` a `#FA6400`), badge de fecha translúcido, botón blanco de candado "Cerrar Turno", tipografía grande y selector tipo dropdown para ámbito de ventas.
*   **MostradorRepartidoresTabBar:** Conmutador tipo cápsula con animación suave de selección.
*   **MostradorScreen:** Fila de productos con steppers redondeados (botón `+` en naranja, botón `-` en gris sutil con estado deshabilitado en 0) y CTA inferior con icono de ticket y monto dinámico.
*   **RepartidoresScreen:** Tarjetas individuales de choferes con badges de estado suaves (naranja para En Ruta, pizarra para Pendiente, verde menta para Liquidado), indicación de moto y nombre de ruta visible.
*   **HistorialScreen:** Header con botón volver circular, tarjeta resumen con split de Efectivo y Tarjeta, chips de filtro y tarjetas individuales de transacciones con cápsula de Repartidor, Moto y Ruta en tickets `R-XXXX`.
*   **TortilleriaNavBar:** Barra inferior compartida con los 4 destinos (Venta, Producción, Métricas, Ajustes).

---

## 6. Criterios de Aceptación QA (by 🏆 quality-pm-expert)
- [ ] **Test (US4):** Al conmutar entre Mostrador y Repartidores, el `HeroSalesCard` y `MostradorRepartidoresTabBar` se mantienen fijos y estáticos; la transición animada solo afecta al contenido inferior.
- [ ] **Test:** Al presionar stepper `+`, el subtotal del producto y el botón "Ingresar venta" se actualizan de inmediato.
- [ ] **Test:** Al confirmar una venta mostrador, se genera folio `M-XXXX`, se persiste en Room y se resetean las cantidades a cero.
- [ ] **Test:** Al liquidar un repartidor, se calcula el importe cobrado, se genera folio `R-XXXX` y se refleja como venta de repartidor en Room y en el total del día.
- [ ] **Test (US9):** Al guardar una venta de mostrador o liquidar una venta de repartidor, se verifica en la tabla `ventas` de Room que `usuarioId` y `usuarioNombre` contengan la información real del usuario autenticado que operó el turno.
- [ ] **Test (US10):** El diálogo de confirmación de venta de mostrador no muestra selector de método de pago, exhibe de forma prominente el total a cobrar en `MaizPrimary` y la insignia "De Contado" en `VerdeAgave`, persistiendo siempre `metodoPago = "Efectivo"` en Room.
- [ ] **Test (US11 & US12):** Al liquidar una ruta de repartidor, se verifica en la tabla `ventas` de Room que `nombreRepartidor`, `motoAsignada` y `nombreRuta` queden almacenados con los valores exactos de la ruta liquidada. En `HistorialScreen`, al consultar la transacción con folio `R-XXXX`, se muestran visualmente estos 3 datos.
- [ ] **Test (US13):** Al abrir un nuevo turno en la app, se verifica que todas las rutas se inicializan en Room con `status = "PENDIENTE_SALIDA"`, `cargaInicialKg = 0.0` y el contador de la cabecera marca `0 rutas activas`.
- [ ] **Test (US14):** En el diálogo de salida, los chips `50 kg`, `70 kg` y `90 kg` actualizan el cálculo del monto esperado en vivo. En el diálogo de liquidación, la devolución calcula en tiempo real los kilos netos vendidos y el monto total a cobrar.
- [ ] **Test (US15):** Si existe al menos una ruta en estado `EN_RUTA`, al presionar "Cerrar Turno", el diálogo muestra la alerta con las motos activas y el botón de confirmación permanece bloqueado/deshabilitado. Únicamente al liquidar todas las rutas (`rutasActivasCount == 0`) se habilita el cierre.
- [ ] **Test (US16):** Al iniciar sesión o entrar a Venta, se verifica que todos los productos de Mostrador inician con `quantity == 0` y subtotal `$0.00`, y que todas las rutas de repartidores inician en `PENDIENTE_SALIDA` con `0.0 kg` y `$0.00 MXN`.
- [ ] **Test:** Al cerrar turno, se actualiza el turno a `CERRADO` en Room y el usuario es redirigido a Login sin poder retroceder.
- [ ] **Test:** La pantalla de Historial de Ventas muestra el desglose exacto y permite volver a la pantalla de Venta.

