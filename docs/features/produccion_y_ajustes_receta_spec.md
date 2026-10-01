# Especificación Técnica: Módulo de Producción, Receta y Mermas

**Estado:** Aprobada y Validada 🏆  
**Fecha:** 20 de Septiembre, 2026  
**Iteración:** Producción Reactiva, Ajustes de Receta y Auditoría en Room  

---

## 1. Contexto y Objetivos del Negocio
El módulo de **Producción** de **Tortillería Derek** es el núcleo operativo responsable de controlar la transformación de insumos (bultos de harina y agua) en masa cruda y producto final terminado (tortilla cocida). Asimismo, debe monitorear el inventario de producto neto disponible en tiempo real frente a las ventas consolidadas (Mostrador y Repartidores) y deducciones directas por mermas operativas, garantizando persistencia estricta en Room para alimentar los tableros analíticos de Métricas.

---

## 2. Historias de Usuario (Casos de Uso)

### Caso 1 (US-PROD-01): Inicialización Limpia del Contador de Stock
**Como** operador de caja/producción al iniciar un nuevo turno o sesión,  
**Quiero** que el contador de tortillas disponibles en tienda comience en `0.0 kg`,  
**Para** no arrastrar saldos ficticios o datos de demostración anteriores, reflejando fielmente que la producción apenas va a iniciar en ese turno.

### Caso 2 (US-PROD-02): Registro de Tandas y Base de Receta
**Como** operador de producción,  
**Quiero** que el selector de bultos inicie en `0` al abrir turno para poder elegir la cantidad deseada desde cero, ver la equivalencia estimada en masa cruda y kilos de tortilla cocida según los estándares de la receta (Base: 20 kg harina + 20 kg agua = 40 kg masa cruda por bulto; ~38.5 kg tortilla cocida por bulto), y que al presionar registrar la tanda el contador de bultos vuelva automáticamente a `0`,  
**Para** registrar formalmente la tanda en Room, alimentar el inventario disponible de la tienda y quedar listo para la siguiente tanda sin acumulación errónea.

### Caso 3 (US-PROD-03): Parámetro de Merma Tolerada en Ajustes (Settings)
**Como** administrador del sistema,  
**Quiero** poder configurar y actualizar en la pantalla de Ajustes la merma estimada o tolerada por bulto (ej. 1.5 kg / bulto o 3.8%),  
**Para** establecer los márgenes de eficiencia operativa cuando varíe la calidad de los insumos o condiciones ambientales.

### Caso 4 (US-PROD-04): Configuración de Peso por Bulto en Ajustes
**Como** administrador del sistema,  
**Quiero** modificar en Ajustes el peso estándar de los bultos de harina (default 20.0 kg, configurable a 25.0 kg, 44.0 kg, 50.0 kg, etc.),  
**Para** adaptar el sistema a distintos proveedores o presentaciones de harina sin alterar el código fuente.

### Caso 5 (US-PROD-05): Configuración de Rendimiento y Masa por Bulto en Ajustes
**Como** administrador del sistema,  
**Quiero** ajustar en Ajustes el total de masa cruda generada por bulto (default 40.0 kg) y el rendimiento neto esperado de tortilla cocida por bulto (default 38.5 kg),  
**Para** calibrar con precisión matemática los pronósticos de producción según las pruebas de molienda y amasado.

### Caso 6 (US-PROD-06): Descuento en Tiempo Real de Ventas y Captura Directa de Merma
**Como** operador de mostrador y producción,  
**Quiero** que el stock disponible en tienda descuente automáticamente tanto las ventas de mostrador como las liquidaciones de repartidores, y además me permita ingresar directamente en la pantalla de Producción la merma operativa ocurrida en el turno (sin obligarme a ir a Ajustes),  
**Para** mantener actualizado al segundo el producto neto real en kilogramos listo para venta:
$$\text{disponibleEnTienda} = \max(0.0, \text{producidoNeto} - \text{totalVendidoKg} - \text{mermaTurnoKg})$$

### Caso 7 (US-PROD-07): Persistencia Integral en Room para Métricas
**Como** propietario o supervisor,  
**Quiero** que cada tanda registrada, cada ajuste de merma y cada parámetro de configuración se persista en Room vinculado al turno activo y con auditoría del usuario,  
**Para** consultar estadísticas históricas, gráficos de rendimiento, comparativas semanales y auditorías de mermas en la pantalla de Métricas.

### Caso 8 (US-VENTA-STOCK): Control Estricto de Inventario (Prohibición de Venta sin Stock)
**Como** sistema de punto de venta y control operativo,  
**Quiero** validar que ninguna venta en mostrador ni salida de repartidor a ruta pueda confirmarse si la cantidad de kilogramos requerida excede la tortilla disponible en tienda,  
**Para** evitar desabastos físicos, ventas en negativo y descuadres contables en la tienda.

### Caso 9 (US-PROD-09): Tope y Validación de Merma vs Stock Disponible (Caso Límite)
**Como** jefe de turno o cajero,  
**Quiero** que el diálogo de captura de merma operativa impida registrar una cantidad superior a la tortilla que efectivamente se encuentra disponible en tienda (`kgMerma > disponibleEnTienda`),  
**Para** evitar errores de dedo tipográficos que distorsionen los ratios de merma del turno y generen saldos artificiales. Si la merma ingresada supera el disponible, el sistema debe alertar con mensaje de advertencia y bloquear la confirmación.

---

## 3. Modelo Matemático y Fórmulas Operativas

1. **Masa Cruda Calculada por Tanda:**
   $$\text{MasaCruda} = \text{bultosHarina} \times \text{kgMasaPorBulto}$$
2. **Tortilla Cocida Estimada por Tanda:**
   $$\text{TortillaEstimada} = \text{bultosHarina} \times \text{rendimientoPorBulto}$$
3. **Producido Neto Acumulado del Turno:**
   $$\text{ProducidoNeto} = \sum_{i=1}^{M} \text{Tanda}_i.\text{kgTortillaEstimada}$$
4. **Ventas Totales en Kilogramos:**
   $$\text{TotalVendidoKg} = \text{VentasMostradorKg} + \text{VentasRepartidoresKg}$$
5. **Merma Operativa Total del Turno:**
   $$\text{MermaTotalKg} = \sum_{j=1}^{K} \text{Merma}_j.\text{kgMerma}$$
6. **Stock Disponible en Tienda:**
   $$\text{DisponibleTiendaKg} = \max(0.0, \text{ProducidoNeto} - \text{TotalVendidoKg} - \text{MermaTotalKg})$$
7. **Progreso / Porcentaje de Venta:**
   $$\text{PorcentajeVenta} = \begin{cases} 0.0 & \text{si } \text{ProducidoNeto} \le 0 \\ \min\left(100.0, \frac{\text{TotalVendidoKg}}{\text{ProducidoNeto}} \times 100\right) & \text{si } \text{ProducidoNeto} > 0 \end{cases}$$

---

## 4. Contrato de Orquestación MVI (`ProduccionContract.kt`)

### 4.1. `ProduccionUiState`
- `isLoading: Boolean`
- `turnoActivo: TurnoEntity?`
- `fechaTurnoTexto: String`
- `estadoTurnoTexto: String`
- `pesoBultoHarinaKg: Double`
- `kgMasaPorBulto: Double`
- `rendimientoPorBulto: Double`
- `mermaToleradaPorBulto: Double`
- `bultosHarinaInput: Int`
- `masaCrudaCalculada: Double`
- `tortillaCalculada: Double`
- `producidoNeto: Double`
- `totalVendidoKg: Double`
- `totalVendidoMostradorKg: Double`
- `totalVendidoRepartoKg: Double`
- `mermaTurnoKg: Double`
- `disponibleEnTienda: Double`
- `porcentajeVenta: Double`
- `showRegistrarMermaDialog: Boolean`
- `mermaKgInput: String`
- `motivoMermaInput: String`
- `errorMessage: String?`

### 4.2. `ProduccionUiEvent`
- `OnIncrementarBultos`
- `OnDecrementarBultos`
- `OnBultosChanged(val bultos: Int)`
- `OnRegistrarTandaClick`
- `OnOpenRegistrarMermaDialog`
- `OnDismissRegistrarMermaDialog`
- `OnMermaKgInputChanged(val valor: String)`
- `OnMotivoMermaInputChanged(val motivo: String)`
- `OnConfirmarRegistroMerma`
- `OnVerHistorialClick`

### 4.3. `ProduccionUiEffect`
- `data class ShowToast(val mensaje: String) : ProduccionUiEffect`
- `data class ShowError(val error: String) : ProduccionUiEffect`
- `object NavigateToHistorial : ProduccionUiEffect`

---

## 5. Diseño de Base de Datos Room

### 5.1. `TandaProduccionEntity` (Tabla `tandas_produccion`)
```kotlin
@Entity(tableName = "tandas_produccion")
data class TandaProduccionEntity(
    @PrimaryKey val id: String,
    val turnoId: String,
    val fecha: Long,
    val hora: String,
    val bultosHarina: Int,
    val pesoBultoKg: Double,
    val kgMasaCruda: Double,
    val kgTortillaEstimada: Double,
    val usuarioId: String,
    val usuarioNombre: String
)
```

### 5.2. `MermaProduccionEntity` (Tabla `mermas_produccion`)
```kotlin
@Entity(tableName = "mermas_produccion")
data class MermaProduccionEntity(
    @PrimaryKey val id: String,
    val turnoId: String,
    val fecha: Long,
    val hora: String,
    val kgMerma: Double,
    val motivo: String,
    val usuarioId: String,
    val usuarioNombre: String
)
```

### 5.3. `ConfiguracionProduccionEntity` (Tabla `configuracion_produccion`)
```kotlin
@Entity(tableName = "configuracion_produccion")
data class ConfiguracionProduccionEntity(
    @PrimaryKey val id: String = "DEFAULT",
    val pesoBultoHarinaKg: Double = 20.0,
    val kgMasaPorBulto: Double = 40.0,
    val rendimientoTortillaPorBulto: Double = 38.5,
    val mermaToleradaKgPorBulto: Double = 1.5,
    val porcentajeMermaTolerada: Double = 3.8,
    val fechaModificacion: Long = System.currentTimeMillis()
)
```

---

## 6. Criterios de Aceptación QA (Gherkin)

### Escenario 1: Inicialización con turno activo sin tandas (Caso 1)
- **Given** un turno abierto recientemente con `id = "turno-001"`,
- **When** se inicia o navega a la pantalla de Producción,
- **Then** `producidoNeto` es `0.0 kg`, `totalVendidoKg` es `0.0 kg` y `disponibleEnTienda` es `0.0 kg`.

### Escenario 2: Registro de tanda y cálculo de receta (Caso 2)
- **Given** los parámetros estándar de receta (20 kg harina, 40 kg masa, 38.5 kg rendimiento por bulto),
- **When** el usuario selecciona 3 bultos y presiona `Registrar Tanda de Producción`,
- **Then** se inserta un registro en `tandas_produccion` con `bultosHarina = 3`, `kgMasaCruda = 120.0 kg` y `kgTortillaEstimada = 115.5 kg`, y el stock disponible pasa inmediatamente a `115.5 kg`.

### Escenario 3: Actualización de receta en Ajustes reflejada en Producción (Casos 3, 4 y 5)
- **Given** la configuración guardada con `pesoBultoHarinaKg = 25.0 kg`, `kgMasaPorBulto = 50.0 kg` y `rendimientoPorBulto = 47.0 kg`,
- **When** el operador en Producción tiene 2 bultos en el stepper,
- **Then** la equivalencia muestra `100 kg masa cruda ➔ 94.0 kg tortilla` en lugar de los valores por defecto.

### Escenario 4: Registro directo de merma en Producción (Caso 6)
- **Given** un stock neto producido de 100.0 kg y 0 ventas,
- **When** el operador registra directamente en Producción 4.0 kg de merma con motivo "Comal frío",
- **Then** se almacena en Room `MermaProduccionEntity` y `disponibleEnTienda` se reduce a `96.0 kg`.

### Escenario 5: Deducción combinada de Ventas y Merma (Casos 6 y 7)
- **Given** 200.0 kg de tortilla producida, 50.0 kg vendidos en mostrador, 70.0 kg despachados por repartidores y 10.0 kg de merma registrada,
- **When** el ViewModel evalúa el estado del inventario,
- **Then** `totalVendidoKg` es `120.0 kg`, `mermaTurnoKg` es `10.0 kg`, `disponibleEnTienda` es `70.0 kg` y el porcentaje vendido es `60.0%`.
