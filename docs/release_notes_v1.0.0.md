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
7. **Pruebas Automatizadas**: 87 pruebas unitarias con JUnit4, MockK y Coroutines Test Dispatcher garantizando cero regresiones.

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
* **Sembrado de Cuenta Master `admin1` (`admin123`)**:
  * El sistema inicializa en una instalación limpia únicamente con el usuario de administración predeterminado `admin1` (contraseña inicial `admin123`, rol `ADMIN`).
  * **Protección inviolable**: `admin1` tiene bloqueo permanente de eliminación, no puede ser renombrado ni degradado de rol administrativo.
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

### 🛵 3. Módulo de Despacho y Liquidación de Rutas (Repartidores)
* **Control de Salidas a Ruta**:
  * Asignación de carga inicial en kilogramos por motocicleta.
  * Descuento automático de la tortilla en tienda al autorizar la salida del repartidor.
  * Registro de folios y control de estatus de la ruta (`EN_RUTA` / `LIQUIDADO`).
* **Liquidación y Arqueo de Chofer**:
  * Registro de kilogramos devueltos al final del recorrido (tortilla fría o no entregada).
  * Reingreso automático de producto devuelto al inventario de mostrador.
  * Cálculo instantáneo del total cobrado y registro del pendiente de cobro (saldo fiado a clientes de ruta).

---

### 🏭 4. Módulo de Producción de Masa y Harina
* **Registro de Tandas de Producción**:
  * Captura de bultos de harina utilizados (50 kg c/u).
  * Cálculo dinámico y proyectado de masa cruda producida y kilogramos de tortilla estimados según los factores de conversión de la tortillería.
* **Control de Mermas**:
  * Registro de pérdidas operativas en kg con motivos predeterminados (masa quemada, tortilla defectuosa, prueba de máquina, etc.).
  * Impacto directo en el balance de inventario neto.

---

### 📜 5. Módulo de Historial de Ventas y Filtros Avanzados
* **Consulta Completa de Transacciones**:
  * Listado reactivo de todas las ventas del turno o por rango de fechas.
  * Tarjetas de venta optimizadas contra desbordes de texto (`TextOverflow.Ellipsis`, layout responsivo de badges e información de ticket).
* **Enfoque de Navegación Estricto**:
  * Ocultamiento automático de la barra de navegación inferior (`BottomNavigationView`) al abrir el historial de ventas para forzar la confirmación de retorno mediante la barra superior de retroceso.

---

### 📊 6. Módulo de Métricas y Analítica de Negocio
* **Selector Dinámico de Período con Alto Contraste**:
  * Selector de días (Hoy, 7 Días, Mes) corregido con fondo oscuro y badges visibles (`SurfaceWarm` / `BorderSubtle`) evitando que el fondo blanco oculte la información.
* **Indicadores Clave de Desempeño (KPIs)**:
  * Total de ingresos generados en el período.
  * Desglose porcentual Mostrador vs Reparto.
  * Ticket promedio por transacción.
  * Gráfica de barras de distribución de ventas por hora para detectar horas pico de demanda.
  * Rendimiento promedio de producción (tortillas obtenidas por bulto de harina).

---

### ⚙️ 7. Módulo de Configuración Dinámica y Ajustes Blindados
* **Acceso Seguro desde Login**:
  * Al ingresar a Configuración directamente desde el icono de engrane en la pantalla de inicio (Login), la sección de **Gestión de Usuarios se oculta por completo**, impidiendo que cualquier persona no autenticada visualice las cuentas del sistema.
* **Precios y Estándares Iniciales en Cero (Instalación Limpia)**:
  * Al abrir por primera vez la app, precios y parámetros de producción inician en `0.00`, permitiendo al administrador ingresar sus propios valores que se persisten en Room Database.
* **Elevación de Privilegios de Seguridad (Admin Confirmation)**:
  * Si la aplicación no está operando bajo la cuenta `admin1`, **cualquier intento de guardar cambios** en:
    * Precios y pesos en gramos de mostrador/reparto,
    * Estándares de producción y rendimiento por bulto,
    * Alta o baja de repartidores,
    * Creación, edición o eliminación de usuarios,
    **despliega un modal de confirmación de credenciales de Administrador**, exigiendo el usuario y contraseña de `admin1` validados criptográficamente en tiempo real.
* **Protección Estricta del Administrador Principal (`admin1`)**:
  * Cuenta maestra inalterable e imposible de eliminar.
  * Se prohíbe eliminar al último administrador restante del sistema.

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

Se implementó una suite completa de **87 pruebas unitarias automatizadas** que se ejecutan sobre la JVM sin requerir emulador, cubriendo:

* `AuthRepositoryImplTest`: Hashing criptográfico, validación de login correcto/incorrecto y resiliencia ante usuarios inexistentes.
* `LoginUseCaseTest` & `AbrirTurnoUseCaseTest`: Validación de reglas de negocio para inicio de sesión y apertura de turnos.
* `VentaViewModelTest`: Cálculo de subtotales, pesaje acumulado, flujo de ticket, emisión de venta, salida a ruta y liquidación de repartidores.
* `ProduccionViewModelTest`: Registro de tandas, cálculo dinámico de masa/tortilla según bultos y registro de mermas.
* `ConfiguracionViewModelTest`: Guardado reactivo de precios y pesos en Room, alta y baja de repartidores, creación de usuarios con hashing PBKDF2, validación de credenciales `admin1`, rechazo de borrado de `admin1`, bloqueo de renombramiento/democión de `admin1` y bloqueo de borrado del último administrador.
* `HistorialViewModelTest` & `MetricasViewModelTest`: Agregación de ventas por turno, filtros de fechas y cálculo de analítica.

---

## 📦 Artefactos del Release

* **Instalador Binario**: `app-debug.apk` (Compilación oficial v1.0.0 lista para instalar y probar en dispositivos Android 8.0 / API 26+).
* **Documentación y Especificaciones**: Especificaciones funcionales completas en el directorio `/docs/features/`.
* **Prototipos de Stitch**: 11 pantallas HTML/CSS interactivas en `/stitch_designs/`.

---
*Tortillería Derek POS — Desarrollado con dedicación y excelencia técnica por FarbalApps.*
