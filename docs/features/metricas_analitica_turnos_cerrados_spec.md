# Especificación: Métricas Analíticas y Productividad desde Turnos Cerrados en Room

## 1. Objetivo
Transformar la pantalla de **Métricas ("Productividad y Ventas")** en un motor analítico integral, reactivo y de alto rendimiento, alimentado exclusivamente desde las tablas de **Room Database** considerando únicamente los **turnos en estado CERRADO**, con capacidad de consolidar múltiples turnos ocurridos en un mismo día calendario y permitiendo alternar dinámicamente entre 4 métricas clave mediante un selector con desplazamiento horizontal.

---

## 2. Historias de Usuario (US)

### US1: Selector de 4 Métricas con Scroll Horizontal
* **Como** administrador o encargado de la tortillería,
* **Quiero** poder desplazarme horizontalmente entre cuatro métricas clave:
  1. 🏭 **Producción Total** (`kg`)
  2. 💰 **Venta Total** (`$ MXN`)
  3. 🏪 **Venta Mostrador** (`$ MXN`)
  4. 🛵 **Venta Repartidor** (`$ MXN`)
* **Para** analizar el desempeño operativo y financiero del negocio desde cualquier ángulo sin saturar visualmente la pantalla.

#### Criterios de Aceptación:
- **Dado** que el usuario ingresa a la pantalla de Métricas,
- **Cuando** visualiza la barra de selección de métricas,
- **Entonces** debe poder deslizar horizontalmente los chips interactivos de las 4 opciones (`Producción Total`, `Venta Total`, `Venta Mostrador`, `Venta Repartidor`).
- **Cuando** selecciona una métrica,
- **Entonces** la cifra principal, la gráfica de curva, los ejes y el tooltip se actualizan de inmediato en la unidad correspondiente (`kg` o `MXN`).

---

### US2: Filtro Estricto de Turnos Cerrados (Exclusión de Turnos Abiertos)
* **Como** auditor o dueño del negocio,
* **Quiero** que las métricas reflejen únicamente datos de turnos que hayan sido formalmente liquidados y cerrados (`estado == 'CERRADO'`),
* **Para** que los datos mostrados sean cifras oficiales y no se vean alterados por operaciones provisionales o turnos activos en curso.

#### Criterios de Aceptación:
- **Dado** que hay un turno actualmente `ABIERTO` con ventas y tandas registradas,
- **Cuando** el usuario consulta las métricas de la semana o mes,
- **Entonces** las ventas, tandas y mermas del turno abierto **NO se suman** en ninguna gráfica ni totalizador.
- **Cuando** el operador ejecuta el Cierre de Turno en Mostrador/Repartidores y el turno pasa a `CERRADO`,
- **Entonces** sus datos se consolidan reactivamente e impactan de inmediato los acumulados analíticos.

---

### US3: Consolidación Multiturno por Día Calendario
* **Como** administrador,
* **Quiero** que las métricas se agrupen por día calendario (`LocalDate`),
* **Para** que si en un mismo día se abrieron y cerraron 2 o más turnos (ej. Turno Matutino y Turno Vespertino), el sistema sume y consolide todos los turnos cerrados de esa misma fecha en un único punto del gráfico diario.

#### Criterios de Aceptación:
- **Dado** un día en el que se cerraron dos turnos (Turno 1: $1,200 venta; Turno 2: $1,800 venta),
- **Cuando** se visualiza el gráfico semanal o mensual,
- **Entonces** el punto correspondiente a esa fecha muestra el valor consolidado de **$3,000 MXN**.
- **Y** los días del período donde no hubo turnos cerrados se representan con valor **0.0** para mantener la continuidad estética y analítica de la curva.

---

### US4: Curva Bezier Dinámica con Detección Automática de Pico y Tooltip
* **Como** usuario visual de la app,
* **Quiero** ver una curva Bezier suave con área degradada en naranja (`MaizPrimary`) que dibuje los valores diarios del período,
* **Para** identificar de un vistazo las tendencias de producción y ventas, destacando automáticamente el día con mayor rendimiento (pico) con su tooltip interactivo.

#### Criterios de Aceptación:
- **Dado** un conjunto de puntos diarios calculados desde Room,
- **Cuando** se dibuja el Canvas de la gráfica,
- **Entonces** las coordenadas $(x, y)$ se escalan de forma proporcional al ancho de pantalla y al valor máximo del período.
- **Y** el algoritmo identifica el día con el valor máximo ($\max$) y ubica sobre él un halo circular y un tooltip flotante oscuro (ej. `"240.0 kg • Jueves"` o `"$3,450 MXN • Sábado"`).

---

### US5: Comparativa Porcentual contra el Período Anterior
* **Como** administrador,
* **Quiero** ver una insignia con el porcentaje de variación ($\pm \Delta\%$) respecto al período previo equivalente (ej. `+12.8% vs semana ant.`),
* **Para** evaluar si el negocio está creciendo o decreciendo en comparación con el ciclo anterior.

#### Criterios de Aceptación:
- **Dado** el período actual de duración $D$ días (desde $t_0$ a $t_1$),
- **Cuando** se calcula el total del período,
- **Entonces** el sistema consulta la ventana previa ($t_0 - D$ a $t_0$) y calcula:
  $$\Delta\% = \frac{\text{Total Actual} - \text{Total Anterior}}{\text{Total Anterior}} \times 100$$
- **Y** muestra la insignia en verde con flecha hacia arriba si $\Delta\% \ge 0$, o en terracota si es negativo.

---

### US6: Desglose de Distribución de Canales (Mostrador vs Reparto Mayoreo)
* **Como** encargado de logística,
* **Quiero** ver la barra bi-color de canales y los montos y kilos divididos entre Mostrador y Reparto Mayoreo,
* **Para** saber qué porcentaje de ingresos y kilos se venden directamente en ventanilla y cuánto desplazan los repartidores.

#### Criterios de Aceptación:
- **Dado** el total de ventas filtradas de turnos cerrados en el período,
- **Cuando** se calcula la distribución,
- **Entonces** se divide entre ventas de `tipo == 'MOSTRADOR'` y ventas de `tipo == 'REPARTIDOR'`.
- **Y** la barra segmentada ajusta dinámicamente sus pesos visuales (`weight`) con el porcentaje exacto de cada canal (ej. Mostrador 68% vs Reparto 32%).

---

### US7: Indicadores de Promedio Diario y Ratio de Merma
* **Como** jefe de producción,
* **Quiero** conocer el promedio de producción/venta por día y el porcentaje real de merma respecto al total de tortilla producida,
* **Para** comprobar si la merma se mantiene en rango óptimo ($\le 5.0\%$) conforme a los estándares de la receta.

#### Criterios de Aceptación:
- **Dado** el total de producción y mermas de turnos cerrados en el período,
- **Cuando** se calculan los indicadores inferiores,
- **Entonces** el promedio diario divide el total entre el número de días del período:
  $$\bar{X} = \frac{\text{Total Periodo}}{\text{Días}}$$
- **Y** el porcentaje de merma se calcula como:
  $$\% \text{Merma} = \frac{\sum \text{kgMerma}}{\sum \text{kgTortillaEstimada}} \times 100$$
- **Y** si $\% \text{Merma} \le 5.0\%$, se muestra la insignia de "Ritmo óptimo", destacando en verde agave.

---

### US8: Acotación de Rango Personalizado ante Fechas Futuras (Caso Límite)
* **Como** usuario analítico,
* **Quiero** que si selecciono un rango personalizado cuya fecha final se encuentre en el futuro (más allá de hoy), el sistema acote automáticamente la fecha final a la fecha de hoy (`LocalDate.now()`),
* **Para** evitar generar días futuros sin actividad con ceros artificiales que desciendan falsamente el promedio diario y alarguen innecesariamente el eje X de la gráfica.

#### Criterios de Aceptación:
- **Dado** que el usuario selecciona un rango personalizado con fecha de inicio en el pasado y fecha final en el futuro,
- **Cuando** se aplican las fechas al filtro analítico,
- **Entonces** `fechaFin` se ajusta a `LocalDate.now()`.
- **Y** el eje X y la curva finalizan exactamente en el día presente.

---

## 3. Reglas de Negocio y Seguridad

1. **Aislamiento de Cierres**: Las consultas en Room solo leen transacciones con `INNER JOIN turnos t ON ... WHERE t.estado = 'CERRADO'`.
2. **Zona Horaria y Día Calendario**: Todas las marcas de tiempo en milisegundos se transforman usando `ZoneId.systemDefault()` a `LocalDate` para evitar traslapes de fechas en medianoche.
3. **Resiliencia ante Períodos Vacíos**: Si un período no cuenta con turnos cerrados registrados (ej. inicio de operaciones de una sucursal), la pantalla no debe arrojar errores (`NaN` o división entre cero); debe mostrar `0.0 kg / $0.00 MXN`, una curva plana en cero y el estado "Sin turnos cerrados registrados".

---

## 4. Diseño Técnico y DAOs

### 4.1. Consultas SQL en Room
- **`VentaDao.kt`**:
  ```sql
  SELECT v.* FROM ventas v
  INNER JOIN turnos t ON v.turnoId = t.id
  WHERE t.estado = 'CERRADO' AND v.estado = 'ACTIVA' AND v.fecha BETWEEN :inicio AND :fin
  ORDER BY v.fecha ASC
  ```
- **`ProduccionDao.kt`**:
  ```sql
  SELECT p.* FROM tandas_produccion p
  INNER JOIN turnos t ON p.turnoId = t.id
  WHERE t.estado = 'CERRADO' AND p.fecha BETWEEN :inicio AND :fin
  ORDER BY p.fecha ASC
  ```
  ```sql
  SELECT m.* FROM mermas_produccion m
  INNER JOIN turnos t ON m.turnoId = t.id
  WHERE t.estado = 'CERRADO' AND m.fecha BETWEEN :inicio AND :fin
  ORDER BY m.fecha ASC
  ```
- **`TurnoDao.kt`**:
  ```sql
  SELECT * FROM turnos
  WHERE estado = 'CERRADO' AND fechaApertura BETWEEN :inicio AND :fin
  ORDER BY fechaApertura ASC
  ```

---

## 5. Pruebas Unitarias Requeridas

1. `MetricasViewModelTest`:
   - `turnos abiertos en curso se excluyen estrictamente de los calculos` (US2).
   - `multiples turnos cerrados en el mismo dia se consolidan en un unico valor diario` (US3).
   - `selector de 4 metricas alterna correctamente entre Produccion, Venta Total, Mostrador y Reparto` (US1).
   - `calculo de distribucion porcentual de canales Mostrador vs Repartidores` (US6).
   - `deteccion del dia pico y armado del tooltip` (US4).
   - `calculo de ratio de merma promedio optimo menor a 5 por ciento` (US7).
   - `calculo de variacion porcentual contra el periodo anterior` (US5).
