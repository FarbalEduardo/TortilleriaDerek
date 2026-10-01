# 🌽 Tortillería Derek - Sistema Integral POS & Control de Producción Pro (v1.0.0)

> **Powered by FarbalApps**  
> *Solución integral para gestión de tortillerías tradicionales: control de inventario de masa, producción por bultos de harina, arqueo de turnos, rutas de reparto y punto de venta mostrador con almacenamiento local offline en Room Database.*

---

## 🚀 Novedades y Características Principales

### 🔐 1. Pantalla de Inicio y Seguridad
* **Firma de Marca**: Inclusión de la leyenda de autoría y calidad corporativa **`powered by FarbalApps`** inmediatamente arriba de la versión del sistema (`Sistema de Control Seguro • v1.0`).
* **Seguridad Criptográfica PBKDF2**: Almacenamiento y validación de contraseñas mediante hashing PBKDF2 con Salt aleatorio de 256 bits (`CryptoManager`), garantizando que ninguna credencial quede expuesta en texto plano.
* **Instalación Inicial Limpia**: Únicamente se siembra la cuenta de administrador oficial `admin1`.

### 💵 2. Punto de Venta (POS Mostrador & Reparto)
* **Venta en Mostrador**: Registro ágil de ventas por Kilogramo, Medio Paquete y Paquete Completo con cálculo dinámico de subtotales y peso en gramos.
* **Flujo de Turnos y Apertura/Cierre**: Control estricto de caja con saldo inicial, arqueo final y validación de tickets emitidos.
* **Control de Repartidores (Mayoreo)**: Despacho de paquetes para motocicletas con control de carga inicial, cálculo de importe pendiente de cobro y diálogo de liquidación final por ruta.

### 🏭 3. Control de Producción y Mermas
* **Registro de Tandas**: Monitoreo de bultos de harina procesados con cálculo reactivo de masa cruda estimada y tortillas terminadas.
* **Gestión de Merma**: Registro directo de merma (tortilla fría/rotura) con porcentaje de tolerancia configurado para evitar fugas de producto.
* **Balance de Tienda en Tiempo Real**: Visualización inmediata del producto disponible frente a lo vendido y lo despachado a ruta.

### 📊 4. Métricas, Analítica e Historial
* **Historial de Ventas Estricto**: Vista detallada de transacciones organizadas por fecha, hora y tipo (Mostrador vs Reparto). Se eliminó la barra de navegación en esta pantalla para forzar un flujo seguro con botón de retroceso superior.
* **Selector de Fechas de Alto Contraste**: Corrección visual integral en el componente de selección de rangos (`DatePickerDialog` / `DateRangePicker`), con fondo blanco y contraste perfecto de números y leyendas.
* **Gráficos de Tendencias**: Métricas de rendimiento, ticket promedio e ingresos comparativos por período.

### ⚙️ 5. Configuración Dinámica y Administración de Usuarios
* **Valores Iniciales en Cero**: En instalaciones limpias, los precios de mostrador, paquetes de mayoreo y estándares de producción arrancan en `0.00`, permitiendo al usuario ingresar las tarifas exactas de su negocio y persistirlas en Room.
* **Catálogo de Repartidores Dinámico**: Comienza limpio y permite agregar choferes y rutas personalizadas sobre la marcha.
* **CRUD Completo de Usuarios**: Pantalla de administración para crear, editar contraseñas/roles y eliminar cuentas de empleados y administradores, con regla de negocio inviolable: **no se puede eliminar ni degradar al único Administrador del sistema**.

### 🎨 6. Identidad Visual & Icono Launcher v2.1
* **Monograma de Masa 'D'**: Ícono adaptativo oficial con acabado dorado cálido de comal tradicional, textura sutil de masa de maíz y soporte multi-densidad (hdpi, mdpi, xhdpi, xxhdpi, xxxhdpi).

---

## 🧪 Pruebas Unitarias y Calidad
* **85 Tests Unitarios Aprobados al 100%**: Cobertura exhaustiva en UseCases, Repositories, ViewModels (`LoginViewModel`, `VentaViewModel`, `ProduccionViewModel`, `ConfiguracionViewModel`, `MetricasViewModel`, `HistorialViewModel`) y persistencia Room.
* **Compilación y Embalaje Verificados**: Build exitoso en Gradle 8.x con Hilt, Jetpack Compose y Room DB v7.

---

## 📦 Artefactos Adjuntos
* `app-debug.apk`: Instalador APK listo para pruebas en dispositivos físicos o emuladores Android (API 26+).
