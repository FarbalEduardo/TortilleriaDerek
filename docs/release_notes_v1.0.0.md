# 🌽 Tortillería Derek - Sistema Integral de Punto de Venta (POS), Control de Producción y Métricas
## Versión Oficial 1.0.0 Pro — Powered by FarbalApps

> **Autor & Desarrollo**: Eduardo Farbal (`FarbalApps`)  
> **Repositorio**: [GitHub FarbalEduardo/TortilleriaDerek](https://github.com/FarbalEduardo/TortilleriaDerek)  
> **Fecha de Lanzamiento**: Octubre 2026  
> **Plataforma**: Android Nativo (Kotlin + Jetpack Compose)  
> **Arquitectura**: Clean Architecture + MVVM/MVI + Offline-First con Room Database  

---

## 📋 Resumen Ejecutivo del Proyecto

**Tortillería Derek** es una solución tecnológica integral de grado comercial diseñada para optimizar, blindar y sistematizar la operación diaria de tortillerías tradicionales y cadenas de masa y tortilla. 

El sistema digitaliza por completo los flujos críticos del negocio: desde la apertura de caja y el pesaje de bultos de harina en producción, pasando por la venta al menudeo en mostrador, el despacho y liquidación de flotas de repartidores en motocicleta, hasta el control de mermas por temperatura o rotura, el arqueo ciego de cortes de caja y el análisis financiero reactivo.

---

## 🏗️ Arquitectura Técnica y Tecnologías

El desarrollo del proyecto se realizó bajo las mejores prácticas modernas de desarrollo en Android Nativo:

1. **Lenguaje y Paradigma**: Kotlin 100% con tipado estricto, Corrutinas y `StateFlow`/`SharedFlow` reactivos.
2. **Interfaz de Usuario (UI)**: Jetpack Compose con arquitectura declarativa moderna, Material Design 3 y sistema de tokens de diseño personalizado (`MaizPrimary`, `BrandOrange`, `SurfaceWarm`, `BorderSubtle`).
3. **Inyección de Dependencias**: Dagger Hilt con scoping para `SingletonComponent` y `ViewModelScoped`.
4. **Almacenamiento Local (Offline-First)**: Android Room Database (Versión 7) con DAOs especializados, transacciones atómicas y migraciones destructivas seguras para desarrollo.
5. **Seguridad Criptográfica**: Implementación nativa de `CryptoManager` con hashing **PBKDF2WithHmacSHA256** (10,000 iteraciones) y sales criptográficas aleatorias (Salt de 256 bits).
6. **Manejo de Estados**: Patrón MVI/MVVM desacoplado con UiState inmutable, UiEvent para interacciones del operador y UiEffect para notificaciones y alertas one-shot.
7. **Pruebas Automatizadas**: 85 pruebas unitarias con JUnit4, MockK y Coroutines Test Dispatcher garantizando cero regresiones.

---

## 📚 Crónica Detallada de Características Desarrolladas (Desde el Origen)

### 🔐 1. Módulo de Autenticación, Seguridad y Gestión de Turnos
* **Pantalla de Login Premium**:
  * Formulario estilizado con validación de entradas en tiempo real.
  * Inclusión de la firma corporativa **`powered by FarbalApps`** sobre la etiqueta de versión y cifrado de datos.
  * Selección de usuario y contraseña con alternancia de visibilidad (icono de ojo).
* **Criptografía de Nivel Bancario (`CryptoManager`)**:
  * Ninguna credencial se almacena en texto plano en la base de datos.
  * Cada usuario cuenta con un `salt` único generado mediante `SecureRandom`.
  * Verificación de credenciales con hashing seguro PBKDF2.
* **Sembrado de Usuario Administrador Oficial**:
  * El sistema inicializa en una instalación limpia únicamente con el usuario de administración predeterminado `admin1` (`admin123`, rol `ADMIN`).
* **Ciclo de Vida de Turnos**:
  * Apertura de turno con validación de saldo base en caja (`fondoInicial`).
  * Asociación estricta de todas las transacciones, ventas, tandas y mermas al `turnoId` activo.
  * Diálogo de Cierre de Turno con arqueo detallado: desglose de ventas en mostrador, ventas cobradas por repartidores, total esperado en caja, efectivo reportado por el cajero y cálculo automático de diferencia (sobrante/faltante).
  * Soporte para múltiples turnos por día con fechas y horas formateadas en zona horaria local.

---

### 💵 2. Módulo de Punto de Venta (POS Mostrador)
* **Catálogo de Venta Directa en Mostrador**:
  * **Kilogramo Completo (1,000 g)**.
  * **Medio Paquete** (peso dinámico configurable en gramos).
  * **Paquete Mostrador** (peso dinámico configurable en gramos).
* **Control Numérico Ágil (Stepper)**:
  * Botones de incremento/decremento rápido y teclado de selección rápida de cantidades.
  * Cálculo instantáneo de subtotal monetario ($) y pesaje total estimado (kg).
* **Validación de Inventario en Tiempo Real**:
  * Bloqueo inteligente de órdenes si la cantidad de kilogramos a vender excede la tortilla disponible en tienda (`disponibleEnTiendaKg`).
* **Emisión de Tickets**:
  * Generación de folios consecutivos automáticos con prefijo (`M-0001`, `M-0002`...).
  * Selección de método de pago (Efectivo, Tarjeta, Transferencia).
  * Resumen de confirmación previo al cobro con desglose de productos y total.

---

### 🛵 3. Módulo de Repartidores y Rutas de Mayoreo
* **Control de Flotilla de Motocicletas**:
  * Catálogo de repartidores con badges identificadores (`#01`, `#02`...), moto asignada y nombre de ruta (ej. *Moto 01 • Ruta Centro*).
  * En una instalación limpia, el catálogo arranca completamente vacío hasta que el usuario añade su personal.
* **Despacho y Salida a Ruta**:
  * Diálogo de salida con asignación de paquetes de tortilla para reparto.
  * Cálculo reactivo de la carga inicial en kilogramos (`cargaInicialKg`) y del importe total que el chofer tiene pendiente de cobrar (`pendienteCobro`).
* **Liquidación de Ruta**:
  * Diálogo integral de liquidación al regreso del repartidor: captura de paquetes devueltos no vendidos y efectivo entregado.
  * Cálculo de paquetes efectivamente comercializados.
  * Cada liquidación genera un ticket formal de venta de mayoreo en el sistema con folio único cronológico (`R-0001`, `R-0002`...).

---

### 🏭 4. Módulo de Control de Producción y Mermas
* **Planificación y Registro de Tandas**:
  * Registro por bultos de harina procesados (sacos estándar).
  * Cálculo reactivo de masa cruda estimada a producir y kilos netos de tortilla terminada esperada.
  * Registro de tanda con marca de tiempo, usuario responsable y actualización inmediata del stock en tienda.
* **Control Riguroso de Mermas**:
  * Diálogo de registro rápido de merma de producción.
  * Motivos configurables: tortilla fría, merma de arranque, desajuste de comal/cortadora, o rotura en empaque.
  * Cálculo del porcentaje de merma en relación con los bultos procesados frente al porcentaje de tolerancia configurado en el sistema.
* **Balance de Tienda en Vivo**:
  * Cuadro de mandos de producción que muestra: Tortilla producida neta, Total vendido en mostrador, Total vendido en reparto, Merma acumulada y Disponible real en anaquel.

---

### 📜 5. Módulo de Historial de Ventas
* **Cronología de Transacciones**:
  * Lista detallada de todas las ventas realizadas agrupadas por orden cronológico inverso.
  * Visualización clara del tipo de venta:
    * **Mostrador / Menudeo**: desglose de kilos y paquetes, importe cobrado, método de pago y cajero.
    * **Repartidor / Mayoreo**: folio de liquidación, chofer, ruta, paquetes entregados e importe cobrado.
* **Navegación Modal Estricta**:
  * Se retiró intencionalmente la barra de navegación inferior (`TortilleriaNavBar`) en esta pantalla. El operador debe usar obligatoriamente la flecha de retroceso superior, asegurando un flujo de supervisión controlado.
* **Diseño Tipográfico Adaptativo**:
  * Corrección de casos límite de wrapping y truncamiento de palabras como *"Mostrador"* y números de folio en pantallas pequeñas.

---

### 📈 6. Módulo de Métricas y Analítica Financiera
* **Selector de Rango de Fechas de Alto Contraste**:
  * Rediseño completo de la paleta de colores del selector de fechas (`DatePickerDialog` y `DateRangePicker`).
  * Fondo blanco puro (`#FFFFFF`) con textos en gris oscuro (`#1F2937`) y selección en tono Maíz/Naranja, resolviendo el problema de texto invisible o lavado sobre fondos claros.
* **Filtros de Período**:
  * Botones rápidos para consultar: *Hoy*, *Esta Semana*, *Este Mes* o *Rango Personalizado*.
* **Dashboard Financiero**:
  * Total de ingresos generados en el período.
  * Desglose porcentual Mostrador vs Reparto.
  * Ticket promedio por transacción.
  * Gráfica de barras de distribución de ventas por hora para detectar horas pico de demanda.
  * Rendimiento promedio de producción (tortillas obtenidas por bulto de harina).

---

### ⚙️ 7. Módulo de Configuración Dinámica y Ajustes
* **Precios y Estándares Iniciales en Cero (Clean Install)**:
  * Al abrir por primera vez la aplicación, los precios de los productos (Kilo, Medio Paquete, Paquete Mostrador, Paquete Mayoreo) y los parámetros de producción (peso de bulto, rendimiento, masa por bulto, merma tolerada) inician en `0.00`.
  * El usuario ingresa manualmente sus propios precios y fórmulas de rendimiento, las cuales se persisten inmediatamente en SQLite/Room.
* **Gestión de Pesos de Paquetes en Gramos**:
  * Capacidad de ajustar los gramos exactos que lleva cada paquete de mostrador o reparto (ej. 800g, 400g, etc.).
* **Administración Completa de Cuentas y Contraseñas (CRUD)**:
  * Interfaz para visualizar todos los usuarios registrados en Room Database.
  * Diálogo para **Agregar Nuevo Usuario**: nombre de usuario (mínimo 3 caracteres), contraseña (mínimo 4 caracteres) y asignación de rol (`ADMIN` o `EMPLEADO`). La contraseña se procesa a través de `CryptoManager` con Salt aleatorio y Hash PBKDF2.
  * Diálogo para **Editar Usuario**: cambio de nombre, cambio de rol y actualización opcional de contraseña.
  * Diálogo para **Eliminar Usuario**: confirmación de borrado con validación de seguridad de alto nivel: **el sistema bloquea cualquier intento de eliminar o degradar al último Administrador**, impidiendo que el negocio se quede sin acceso administrativo.

---

### 🎨 8. Identidad de Marca e Iconografía Oficial
* **Icono Launcher Oficial v2.1**:
  * Concepto de monograma letra **'D'** estilizada con curvas de tortilla tradicional doblada y comal artesanal.
  * Paleta de gradientes dorados maíz (`#D97706`, `#B45309`, `#78350F`) con fondo cálido de piedra de molino (`#FFFDF7`).
  * Generación de recursos adaptativos completos:
    * Vectores XML: `ic_launcher_foreground.xml`, `ic_launcher_background.xml`, `ic_launcher_monochrome.xml`.
    * Recursos `anydpi`: `ic_launcher.xml`, `ic_launcher_round.xml`.
    * Bitmaps WebP optimizados para todas las densidades de pantalla: `mdpi` (48x48), `hdpi` (72x72), `xhdpi` (96x96), `xxhdpi` (144x144) y `xxxhdpi` (192x192).

---

## 🗄️ Modelo de Datos (Room Database v7)

La base de datos relacional local se compone de 8 tablas optimizadas:

| Tabla / Entidad | Propósito | Campos Clave |
|---|---|---|
| `usuarios` | Cuentas del sistema con contraseñas seguras | `id`, `username`, `passwordHash`, `salt`, `rol` |
| `turnos` | Sesiones operativas y cortes de caja | `id`, `fechaApertura`, `fechaCierre`, `fondoInicial`, `efectivoCierre`, `estado`, `usuarioId` |
| `ventas` | Registro individual de transacciones | `id`, `turnoId`, `tipoVenta`, `folioTicket`, `montoTotal`, `kilosTotales`, `metodoPago`, `fechaHora`, `usuarioId` |
| `tandas_produccion` | Registro de masa y producción por bulto | `id`, `turnoId`, `bultosHarina`, `pesoBultoKg`, `kgMasaCruda`, `kgTortillaEstimada`, `fecha`, `hora`, `usuarioId` |
| `mermas_produccion` | Registro de pérdidas de producto | `id`, `turnoId`, `kgMerma`, `motivo`, `fecha`, `hora`, `usuarioId` |
| `repartidores` | Directorio de choferes y unidades | `id`, `numeroBadge`, `nombre`, `moto`, `detalleRuta`, `fechaCreacion` |
| `rutas_repartidores` | Despacho y liquidación por turno | `id`, `turnoId`, `moto`, `repartidorNombre`, `status`, `cargaInicialKg`, `pendienteCobro`, `totalCobrado` |
| `configuracion_produccion` | Tarifas y estándares de producción | `id`, `pesoBultoHarinaKg`, `kgMasaPorBulto`, `rendimientoTortillaPorBulto`, `mermaToleradaKgPorBulto`, `precioKilo`, `precioPaquete`, `precioMedioPaquete`, `precioPaqueteRepartidor`, `pesoPaqueteGramos`, `pesoMedioPaqueteGramos` |

---

## 🧪 Pruebas Unitarias y Aseguramiento de Calidad (QA)

Se implementó una suite completa de **85 pruebas unitarias automatizadas** que se ejecutan sobre la JVM sin requerir emulador, cubriendo:

* `AuthRepositoryImplTest`: Hashing criptográfico, validación de login correcto/incorrecto y resiliencia ante usuarios inexistentes.
* `LoginUseCaseTest` & `AbrirTurnoUseCaseTest`: Validación de reglas de negocio para inicio de sesión y apertura de turnos.
* `VentaViewModelTest`: Cálculo de subtotales, pesaje acumulado, flujo de ticket, emisión de venta, salida a ruta y liquidación de repartidores.
* `ProduccionViewModelTest`: Registro de tandas, cálculo dinámico de masa/tortilla según bultos y registro de mermas.
* `ConfiguracionViewModelTest`: Guardado reactivo de precios y pesos en Room, alta y baja de repartidores, creación de usuarios con hashing PBKDF2, validación de duplicados y bloqueo de borrado del último administrador.
* `HistorialViewModelTest` & `MetricasViewModelTest`: Agregación de ventas por turno, filtros de fechas y cálculo de analítica.

---

## 📦 Artefactos del Release

* **Instalador Binario**: `app-debug.apk` (Compilación oficial v1.0.0 lista para instalar y probar en dispositivos Android 8.0 / API 26+).
* **Documentación y Especificaciones**: Especificaciones funcionales completas en el directorio `/docs/features/`.
* **Prototipos de Stitch**: 11 pantallas HTML/CSS interactivas en `/stitch_designs/`.

---
*Tortillería Derek POS — Desarrollado con dedicación y excelencia técnica por FarbalApps.*
